package utils;

import annotations.Param;
import annotations.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[]{mapperClass}, new MapperInvocationHandler());
    }

    static class MapperInvocationHandler implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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

        private Object invokeUpdate(Object proxy, Method method, Object[] args) {
            String sql = createUpdateSql(method);
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Integer) {
                        preparedStatement.setInt(i + 1, (int) arg);
                    } else if (arg instanceof String) {
                        preparedStatement.setString(i + 1, (String) arg);
                    }
                }
                System.out.println("SQL: " + preparedStatement);
                int rs = preparedStatement.executeUpdate();
                if (rs > 0) {
                    return parseInsertResult(args, method.getReturnType());
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        //UPDATE user SET name = ?, age = ? WHERE id = ? AND status = ?
        private String createUpdateSql(Method method) {
            StringBuilder sb = new StringBuilder();
            sb.append("UPDATE ");
            String tableName = getSelectTableName(method.getReturnType());
            sb.append(tableName);
            sb.append(" SET ");

        }

        private Object invokeDelete(Object proxy, Method method, Object[] args) {
            String sql = createDeleteSql(method);
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Integer) {
                        preparedStatement.setInt(i + 1, (int) arg);
                    } else if (arg instanceof String) {
                        preparedStatement.setString(i + 1, (String) arg);
                    }
                }
                System.out.println("SQL: " + preparedStatement);
                int rs = preparedStatement.executeUpdate();
                if(rs>0){
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
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                for (int i = 0; i < args.length; i++) {
                    Object arg = args[i];
                    if (arg instanceof Integer) {
                        preparedStatement.setInt(i + 1, (int) arg);
                    } else if (arg instanceof String) {
                        preparedStatement.setString(i + 1, (String) arg);
                    }
                }
                System.out.println("SQL: " + preparedStatement);
                int rs = preparedStatement.executeUpdate();
                if(rs>0){
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
                    Object arg = args[i];
                    if (arg instanceof Integer) {
                        preparedStatement.setInt(i + 1, (int) arg);
                    } else if (arg instanceof String) {
                        preparedStatement.setString(i + 1, (String) arg);
                    }
                }
                System.out.println("SQL: " + preparedStatement);
                ResultSet rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    return parseResult(rs, method.getReturnType());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return null;
        }

        private Object parseInsertResult(Object[] args, Class<?> returnType) throws Exception {
            Constructor<?> constructor = returnType.getConstructor();
            Object result = constructor.newInstance();
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                Field field = returnType.getDeclaredFields()[i];
                field.setAccessible(true);
                field.set(result, arg);
            }
            return result;

        }

        private Object parseResult(ResultSet rs, Class<?> returnType) throws Exception {
            Constructor<?> constructor = returnType.getConstructor();
            Object result = constructor.newInstance();
            for (Field field : returnType.getDeclaredFields()) {
                Object column = null;
                String name = field.getName();
                if (field.getType() == String.class) {
                    column = rs.getString(name);
                } else if (field.getType() == Integer.class) {
                    column = rs.getInt(name);
                }
                field.setAccessible(true);
                field.set(result, column);
            }
            return result;
        }

        //        DELETE FROM user WHERE id = ?
        private String createDeleteSql(Method method) {
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM ");
            String tableName = getSelectTableName(method.getReturnType());
            sb.append(tableName);
            sb.append(" WHERE ");
            String where = getSelectWhere(method);
            sb.append(where);
            return sb.toString();

        }
        //String sql = "SELECT id,name,age FROM user WHERE id = ?";
        private String createSelectSql(Method method) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            List<String> selectCols = getSelectCols(method.getReturnType());
            // 修改这里，使用 String.join 来正确拼接列名
            sb.append(String.join(", ", selectCols));
            sb.append(" FROM ");
            String tableName = getSelectTableName(method.getReturnType());
            sb.append(tableName);
            sb.append(" WHERE ");
            String where = getSelectWhere(method);
            sb.append(where);
            return sb.toString();
        }

        //String sql =INSERT INTO user(id, name, age) VALUES(?, ?, ?)"
        private String createInsertSql(Method method) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ");
            String tableName = getInsertTableName(method.getReturnType());
            sb.append(tableName+"(");
            List<String> cols = getInsertCols(method.getReturnType());
            sb.append(String.join(", ", cols)+") VALUES(");
            sb.append(String.join(", ", Collections.nCopies(cols.size(), "?"))).append(")");
            System.out.println(sb.toString());
            return sb.toString();


        }


        private List<String> getInsertCols(Class<?> returnType) {
            Field[] declaredFields = returnType.getDeclaredFields();
            return Arrays.stream(declaredFields).map(Field::getName).collect(Collectors.toList());
        }

        private String getInsertTableName(Class<?> returnType) {
            Table annotation = returnType.getAnnotation(Table.class);
            if (annotation == null) {
                throw new RuntimeException();
            }
            return annotation.tableName();
        }

        private String getSelectWhere(Method method) {
            return Arrays.stream(method.getParameters()).map(
                    (parameter) -> {
                        Param param = parameter.getAnnotation(Param.class);
                        String value = param.value();
                        String condition = value + " = ?";
                        return condition;
                    }).collect(Collectors.joining(" AND "));
        }

        private String getSelectTableName(Class<?> returnType) {
            Table annotation = returnType.getAnnotation(Table.class);
            if (annotation == null) {
                throw new RuntimeException();
            }
            return annotation.tableName();
        }

        private List<String> getSelectCols(Class<?> returnType) {
            Field[] declaredFields = returnType.getDeclaredFields();
            return Arrays.stream(declaredFields).map(Field::getName).collect(Collectors.toList());
        }
    }
}


