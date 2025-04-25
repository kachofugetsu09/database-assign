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
                continue;
            }
        }
        return result;
    }

    protected List<?> parseResultList(ResultSet rs, Method method) throws Exception {
        List<Object> results = new ArrayList<>();
        Type returnType = method.getGenericReturnType();
        Class<?> entityType = (Class<?>) ((ParameterizedType) returnType).getActualTypeArguments()[0];

        while (rs.next()) {
            results.add(parseResult(rs, entityType));
        }
        return results;
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
