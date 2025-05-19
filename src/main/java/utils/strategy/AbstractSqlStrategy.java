package utils.strategy;

import annotations.Table;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

public abstract class AbstractSqlStrategy implements SqlExecutionStrategy {
    protected static String jdbcUrl = "jdbc:mysql://localhost:3306/mybatis_db";
    protected static String username = "root";
    protected static String password = "12345678";

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    protected String camelCaseToSnakeCase(String camelCase) {
        if (camelCase == null) return null;
        String regex = "([a-z])([A-Z])";
        String replacement = "$1_$2";
        return camelCase.replaceAll(regex, replacement).toLowerCase();
    }

    // Also modify or add this method to convert parameter names
    protected String convertParamName(String paramName) {
        return camelCaseToSnakeCase(paramName);

    }

    protected void setParameterValue(PreparedStatement ps, int index, Object value) throws SQLException {
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

    protected Object parseResult(ResultSet rs, Class<?> returnType) throws Exception {
        Object result = returnType.getDeclaredConstructor().newInstance();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            // 将下划线格式转换为驼峰格式
            String propertyName = convertToCamelCase(columnName);

            try {
                // 获取setter方法名
                String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
                // 获取setter方法
                java.lang.reflect.Method setter = returnType.getMethod(setterName, getColumnType(metaData, i));

                // 获取列值并设置到对象中
                Object value = getColumnValue(rs, i, metaData.getColumnType(i));
                setter.invoke(result, value);
            } catch (NoSuchMethodException e) {
                // 如果找不到对应的setter方法，跳过这个字段
                System.out.println("Warning: No setter found for property: " + propertyName);
            }
        }
        return result;
    }

    // 添加一个辅助方法来将下划线格式转换为驼峰格式
    private String convertToCamelCase(String columnName) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < columnName.length(); i++) {
            char currentChar = columnName.charAt(i);
            if (currentChar == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(currentChar));
                    nextUpper = false;
                } else {
                    result.append(Character.toLowerCase(currentChar));
                }
            }
        }
        return result.toString();
    }

    // 添加一个辅助方法来获取列的类型
    private Class<?> getColumnType(ResultSetMetaData metaData, int columnIndex) throws SQLException {
        int type = metaData.getColumnType(columnIndex);
        switch (type) {
            case Types.INTEGER:
                return Integer.class;
            case Types.VARCHAR:
            case Types.CHAR:
                return String.class;
            case Types.DATE:
                return java.util.Date.class;
            default:
                return Object.class;
        }
    }

    // 添加一个辅助方法来获取列的值
    private Object getColumnValue(ResultSet rs, int columnIndex, int columnType) throws SQLException {
        switch (columnType) {
            case Types.INTEGER:
                return rs.getInt(columnIndex);
            case Types.VARCHAR:
            case Types.CHAR:
                return rs.getString(columnIndex);
            case Types.DATE:
                return rs.getDate(columnIndex);

            default:
                return rs.getObject(columnIndex);
        }
    }

    protected List<Object> parseResultList(ResultSet rs, Method method) throws Exception {
        List<Object> resultList = new ArrayList<>();
        Class<?> returnType = method.getReturnType();
        Class<?> genericType = getGenericType(method);

        while (rs.next()) {
            resultList.add(parseResult(rs, genericType));
        }
        return resultList;
    }

    private Class<?> getGenericType(Method method) {
        // 获取List的泛型类型
        java.lang.reflect.Type returnType = method.getGenericReturnType();
        if (returnType instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.Type[] typeArguments = ((java.lang.reflect.ParameterizedType) returnType).getActualTypeArguments();
            if (typeArguments.length > 0) {
                return (Class<?>) typeArguments[0];
            }
        }
        return Object.class;
    }

    protected Object parseInsertResult(Object[] args, Class<?> returnType) throws Exception {
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

    protected String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        return table != null ? table.tableName() : clazz.getSimpleName().toLowerCase();
    }

    protected List<String> getColumns(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }
}
