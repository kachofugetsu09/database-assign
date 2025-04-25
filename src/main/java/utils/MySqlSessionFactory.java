package utils;

import annotations.Param;
import annotations.SQL;
import annotations.Table;
import annotations.Where;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public class MySqlSessionFactory {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL JDBC driver", e);
        }
    }
    static String jdbcUrl = "jdbc:mysql://localhost:3306/mybatis_db";
    static String username = "root";
    static String password = "12345678";

    public <T> T getMapper(Class<T> mapperClass) {
        return (T) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{mapperClass},
                new MapperInvocationHandler());
    }

    static class MapperInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 检查是否有自定义SQL注解
            SQL sqlAnnotation = method.getAnnotation(SQL.class);
            if (sqlAnnotation != null) {
                return executeCustomSQL(sqlAnnotation.value(), method, args);
            }

            // 原有的基础CRUD处理
            if (method.getName().startsWith("select")) {
                return invokeSelect(proxy, method, args);
            }
            if (method.getName().startsWith("insert")) {
                return invokeInsert(proxy, method, args);
            }
            if (method.getName().startsWith("delete")) {
                return invokeDelete(proxy, method, args);
            }
            if (method.getName().startsWith("update")) {
                return invokeUpdate(proxy, method, args);
            }
            return null;
        }

        private Object executeCustomSQL(String sql, Method method, Object[] args) throws Exception {
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                // 处理SQL中的参数占位符
                sql = processSQLParameters(sql, method, args);

                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                // 设置参数值
                setParameterValues(preparedStatement, method, args);

                System.out.println("Executing SQL: " + sql);

                // 执行SQL并处理结果
                if (sql.trim().toUpperCase().startsWith("SELECT")) {
                    ResultSet rs = preparedStatement.executeQuery();
                    if (method.getReturnType().equals(List.class)) {
                        return parseResultList(rs, method);
                    } else {
                        return rs.next() ? parseResult(rs, method.getReturnType()) : null;
                    }
                } else {
                    int affected = preparedStatement.executeUpdate();
                    if (method.getReturnType().equals(Integer.class)) {
                        return affected;
                    }
                    return affected > 0;
                }
            }
        }

        private String processSQLParameters(String sql, Method method, Object[] args) {
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Param param = parameters[i].getAnnotation(Param.class);
                if (param != null) {
                    sql = sql.replace("#{" + param.value() + "}", "?");
                }
            }
            return sql;
        }

        private void setParameterValues(PreparedStatement ps, Method method, Object[] args) throws SQLException {
            Parameter[] parameters = method.getParameters();
            int parameterIndex = 1;

            for (int i = 0; i < parameters.length; i++) {
                Param param = parameters[i].getAnnotation(Param.class);
                if (param != null) {
                    setParameterValue(ps, parameterIndex++, args[i]);
                }
            }
        }

        private void setParameterValue(PreparedStatement ps, int index, Object value) throws SQLException {
            if (value == null) {
                ps.setNull(index, Types.NULL);
            } else if (value instanceof Integer) {
                ps.setInt(index, (Integer) value);
            } else if (value instanceof String) {
                ps.setString(index, (String) value);
            } else if (value instanceof Long) {
                ps.setLong(index, (Long) value);
            } else if (value instanceof Double) {
                ps.setDouble(index, (Double) value);
            } else if (value instanceof Date) {
                ps.setTimestamp(index, new Timestamp(((Date) value).getTime()));
            }
        }

        private List<?> parseResultList(ResultSet rs, Method method) throws Exception {
            List<Object> results = new ArrayList<>();
            Type returnType = method.getGenericReturnType();
            Class<?> entityType = (Class<?>) ((ParameterizedType) returnType).getActualTypeArguments()[0];

            while (rs.next()) {
                results.add(parseResult(rs, entityType));
            }
            return results;
        }

        private Object invokeUpdate(Object proxy, Method method, Object[] args) {
            String sql = createUpdateSql(method);
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                // 获取参数顺序
                Parameter[] parameters = method.getParameters();
                Map<String, Object> paramMap = new HashMap<>();
                for (int i = 0; i < parameters.length; i++) {
                    Param param = parameters[i].getAnnotation(Param.class);
                    if (param != null) {
                        paramMap.put(param.value(), args[i]);
                    }
                }

                // 设置更新字段的值
                int paramIndex = 1;
                for (Parameter param : parameters) {
                    if (param.isAnnotationPresent(Param.class)) {
                        String paramName = param.getAnnotation(Param.class).value();
                        if (!paramName.equals("id")) {
                            Object value = paramMap.get(paramName);
                            setParameterValue(preparedStatement, paramIndex++, value);
                        }
                    }
                }

                // 设置WHERE条件的值
                Object idValue = paramMap.get("id");
                if (idValue != null) {
                    setParameterValue(preparedStatement, paramIndex, idValue);
                }

                System.out.println("Executing SQL: " + sql);
                int rs = preparedStatement.executeUpdate();
                if (rs > 0) {
                    return parseInsertResult(args, method.getReturnType());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        private Object invokeDelete(Object proxy, Method method, Object[] args) {
            String sql = createDeleteSql(method);
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                for (int i = 0; i < args.length; i++) {
                    setParameterValue(preparedStatement, i + 1, args[i]);
                }
                System.out.println("Executing SQL: " + sql);
                int rs = preparedStatement.executeUpdate();
                if (rs > 0) {
                    return parseInsertResult(args, method.getReturnType());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        private Object invokeInsert(Object proxy, Method method, Object[] args) {
            String sql = createInsertSql(method);
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < args.length; i++) {
                    setParameterValue(preparedStatement, i + 1, args[i]);
                }
                System.out.println("Executing SQL: " + sql);
                int rs = preparedStatement.executeUpdate();
                if (rs > 0) {
                    return parseInsertResult(args, method.getReturnType());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        private Object invokeSelect(Object proxy, Method method, Object[] args) {
            String sql = createSelectSql(method);
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                for (int i = 0; i < args.length; i++) {
                    setParameterValue(preparedStatement, i + 1, args[i]);
                }
                System.out.println("Executing SQL: " + sql);
                ResultSet rs = preparedStatement.executeQuery();

                if (method.getReturnType().equals(List.class)) {
                    return parseResultList(rs, method);
                } else {
                    return rs.next() ? parseResult(rs, method.getReturnType()) : null;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private Object parseInsertResult(Object[] args, Class<?> returnType) throws Exception {
            Constructor<?> constructor = returnType.getConstructor();
            Object result = constructor.newInstance();
            Field[] fields = returnType.getDeclaredFields();

            for (int i = 0; i < Math.min(args.length, fields.length); i++) {
                Field field = fields[i];
                field.setAccessible(true);
                field.set(result, args[i]);
            }
            return result;
        }

        private Object parseResult(ResultSet rs, Class<?> returnType) throws Exception {
            Constructor<?> constructor = returnType.getConstructor();
            Object result = constructor.newInstance();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnLabel(i);
                try {
                    Field field = returnType.getDeclaredField(columnName);
                    field.setAccessible(true);
                    Object value = rs.getObject(i);
                    field.set(result, value);
                } catch (NoSuchFieldException e) {
                    // 字段不存在，跳过
                    continue;
                }
            }
            return result;
        }

        private String createDeleteSql(Method method) {
            StringBuilder sb = new StringBuilder("DELETE FROM ");
            String tableName = getTableName(method.getReturnType());
            sb.append(tableName).append(" WHERE ");
            Where where = method.getAnnotation(Where.class);
            sb.append(where != null ? where.value().replace("#{id}", "?") : "1=1");
            return sb.toString();
        }

        private String createSelectSql(Method method) {
            StringBuilder sb = new StringBuilder("SELECT ");
            List<String> columns = getColumns(method.getReturnType());
            sb.append(String.join(", ", columns))
                    .append(" FROM ")
                    .append(getTableName(method.getReturnType()))
                    .append(" WHERE ");

            Where where = method.getAnnotation(Where.class);
            sb.append(where != null ? where.value().replace("#{id}", "?") : "1=1");
            return sb.toString();
        }

        private String createInsertSql(Method method) {
            Class<?> returnType = method.getReturnType();
            List<String> columns = getColumns(returnType);

            return String.format("INSERT INTO %s (%s) VALUES (%s)",
                    getTableName(returnType),
                    String.join(", ", columns),
                    String.join(", ", Collections.nCopies(columns.size(), "?")));
        }

        private String createUpdateSql(Method method) {
            StringBuilder sb = new StringBuilder("UPDATE ");
            String tableName = getTableName(method.getReturnType());
            sb.append(tableName).append(" SET ");

            Parameter[] parameters = method.getParameters();
            List<String> setClauses = new ArrayList<>();

            for (Parameter param : parameters) {
                if (param.isAnnotationPresent(Param.class)) {
                    String paramName = param.getAnnotation(Param.class).value();
                    if (!paramName.equals("id")) {
                        setClauses.add(paramName + " = ?");
                    }
                }
            }

            sb.append(String.join(", ", setClauses));
            Where where = method.getAnnotation(Where.class);
            if (where != null) {
                sb.append(" WHERE ").append(where.value().replace("#{id}", "?"));
            }

            return sb.toString();
        }

        private String getTableName(Class<?> clazz) {
            Table table = clazz.getAnnotation(Table.class);
            return table != null ? table.tableName() : clazz.getSimpleName().toLowerCase();
        }

        private List<String> getColumns(Class<?> clazz) {
            return Arrays.stream(clazz.getDeclaredFields())
                    .map(Field::getName)
                    .collect(Collectors.toList());
        }
    }
}
