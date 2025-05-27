package utils.strategy;

import annotations.Table;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * SQL执行策略的抽象基类
 * 提供了所有SQL策略共用的基础功能，包括数据库连接、参数处理、结果集解析等
 */
public abstract class AbstractSqlStrategy implements SqlExecutionStrategy {
    // 数据库连接配置
    protected static String jdbcUrl = "jdbc:mysql://localhost:3306/mybatis_db";
    protected static String username = "root";
    protected static String password = "12345678";

    /**
     * 获取数据库连接
     * @return 返回新创建的数据库连接对象
     * @throws SQLException 如果数据库连接失败
     */
    protected Connection getConnection() throws SQLException {
        // 1. 使用配置的URL、用户名和密码创建连接
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    /**
     * 将驼峰命名转换为下划线命名
     * @param camelCase 驼峰命名的字符串
     * @return 转换后的下划线命名字符串
     */
    protected String camelCaseToSnakeCase(String camelCase) {
        // 1. 检查输入是否为null
        if (camelCase == null) return null;
        // 2. 使用正则表达式匹配大小写字母之间的位置
        String regex = "([a-z])([A-Z])";
        String replacement = "$1_$2";
        // 3. 在匹配位置插入下划线并转换为小写
        return camelCase.replaceAll(regex, replacement).toLowerCase();
    }

    /**
     * 转换参数名称为数据库列名格式
     * @param paramName 参数名称
     * @return 转换后的数据库列名
     */
    protected String convertParamName(String paramName) {
        // 1. 调用camelCaseToSnakeCase方法进行转换
        return camelCaseToSnakeCase(paramName);
    }

    /**
     * 设置PreparedStatement的参数值
     * @param ps PreparedStatement对象
     * @param index 参数索引
     * @param value 参数值
     * @throws SQLException 如果设置参数失败
     */
    protected void setParameterValue(PreparedStatement ps, int index, Object value) throws SQLException {
        // 1. 处理null值的情况
        if (value == null) {
            ps.setNull(index, Types.NULL);
        }
        // 2. 根据参数类型选择适当的setter方法
        else if (value instanceof Integer) {
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

    /**
     * 解析查询结果集为对象
     * @param rs 结果集
     * @param returnType 返回类型
     * @return 解析后的对象
     * @throws Exception 如果解析过程出错
     */
    protected Object parseResult(ResultSet rs, Class<?> returnType) throws Exception {
        // 1. 创建返回类型的实例
        Object result = returnType.getDeclaredConstructor().newInstance();
        // 2. 获取结果集的元数据
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 3. 遍历所有列
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            // 4. 将列名转换为驼峰格式
            String propertyName = convertToCamelCase(columnName);

            try {
                // 5. 查找并调用对应的setter方法
                String setterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
               Method setter = returnType.getMethod(setterName, getColumnType(metaData, i));

                // 6. 设置属性值
                Object value = getColumnValue(rs, i, metaData.getColumnType(i));
                if (value != null) {
                    setter.invoke(result, value);
                }
            } catch (NoSuchMethodException e) {
                // 7. 如果找不到对应的setter方法，尝试使用Lombok生成的setter
                try {
                    String lombokSetterName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
                    Method lombokSetter = returnType.getMethod(lombokSetterName, getColumnType(metaData, i));
                    Object value = getColumnValue(rs, i, metaData.getColumnType(i));
                    if (value != null) {
                        lombokSetter.invoke(result, value);
                    }
                } catch (NoSuchMethodException ex) {
                    System.out.println("Warning: No setter found for property: " + propertyName);
                }
            }
        }
        return result;
    }

    /**
     * 将下划线格式转换为驼峰格式
     * @param columnName 下划线格式的列名
     * @return 转换后的驼峰格式字符串
     */
    private String convertToCamelCase(String columnName) {
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;

        // 1. 遍历字符串的每个字符
        for (int i = 0; i < columnName.length(); i++) {
            char currentChar = columnName.charAt(i);
            // 2. 遇到下划线时，将下一个字符转换为大写
            if (currentChar == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    result.append(Character.toUpperCase(currentChar));
                    nextUpper = false;
                } else {
                    // 3. 其他字符保持原样
                    result.append(Character.toLowerCase(currentChar));
                }
            }
        }
        return result.toString();
    }

    /**
     * 获取列对应的Java类型
     * @param metaData 结果集元数据
     * @param columnIndex 列索引
     * @return 对应的Java类型
     * @throws SQLException 如果获取列类型失败
     */
    private Class<?> getColumnType(ResultSetMetaData metaData, int columnIndex) throws SQLException {
        // 1. 获取SQL类型和列名
        int type = metaData.getColumnType(columnIndex);
        String columnName = metaData.getColumnName(columnIndex);
//        System.out.println("Column: " + columnName + ", SQL Type: " + type);
        
        // 2. 根据SQL类型映射到对应的Java类型
        switch (type) {
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.BIGINT:
            case Types.DECIMAL:
            case Types.NUMERIC:
                return Integer.class;
            case Types.VARCHAR:
            case Types.CHAR:
            case Types.LONGVARCHAR:
                return String.class;
            case Types.DATE:
                return java.util.Date.class;
            default:
                System.out.println("Unknown type for column " + columnName + ": " + type);
                return Object.class;
        }
    }

    /**
     * 从结果集中获取列值
     * @param rs 结果集
     * @param columnIndex 列索引
     * @param columnType 列类型
     * @return 列值
     * @throws SQLException 如果获取列值失败
     */
    private Object getColumnValue(ResultSet rs, int columnIndex, int columnType) throws SQLException {
        String columnName = rs.getMetaData().getColumnName(columnIndex);
        try {
            // 1. 根据列类型选择适当的getter方法
            switch (columnType) {
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                case Types.BIGINT:
                case Types.DECIMAL:
                case Types.NUMERIC:
                    int intValue = rs.getInt(columnIndex);
//                    System.out.println("Getting integer value for " + columnName + ": " + intValue);
                    return intValue;
                case Types.VARCHAR:
                case Types.CHAR:
                case Types.LONGVARCHAR:
                    return rs.getString(columnIndex);
                case Types.DATE:
                    return rs.getDate(columnIndex);
                default:
                    Object objValue = rs.getObject(columnIndex);
//                    System.out.println("Getting object value for " + columnName + ": " + objValue);
                    return objValue;
            }
        } catch (SQLException e) {
            System.out.println("Error getting value for column " + columnName + ": " + e.getMessage());
            throw e;
        }
    }

    /**
     * 解析结果集为对象列表
     * @param rs 结果集
     * @param method 方法对象
     * @return 对象列表
     * @throws Exception 如果解析过程出错
     */
    protected List<Object> parseResultList(ResultSet rs, Method method) throws Exception {
        // 1. 获取方法的返回类型和泛型类型
        List<Object> resultList = new ArrayList<>();
        Class<?> returnType = method.getReturnType();
        Class<?> genericType = getGenericType(method);

        // 2. 遍历结果集并创建对象
        while (rs.next()) {
            resultList.add(parseResult(rs, genericType));
        }
        return resultList;
    }

    /**
     * 获取List的泛型类型
     * @param method 方法对象
     * @return 泛型类型
     */
    private Class<?> getGenericType(Method method) {
        // 1. 获取方法的泛型返回类型
        java.lang.reflect.Type returnType = method.getGenericReturnType();
        // 2. 如果是ParameterizedType，获取实际的类型参数
        if (returnType instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.Type[] typeArguments = ((java.lang.reflect.ParameterizedType) returnType).getActualTypeArguments();
            if (typeArguments.length > 0) {
                return (Class<?>) typeArguments[0];
            }
        }
        return Object.class;
    }

    /**
     * 解析插入结果
     * @param args 参数数组
     * @param returnType 返回类型
     * @return 解析后的对象
     * @throws Exception 如果解析过程出错
     */
    protected Object parseInsertResult(Object[] args, Class<?> returnType) throws Exception {
        // 1. 创建返回类型的实例
        Constructor<?> constructor = returnType.getConstructor();
        Object result = constructor.newInstance();
        // 2. 获取所有字段
        Field[] fields = returnType.getDeclaredFields();

        // 3. 设置字段值
        for (int i = 0; i < Math.min(args.length, fields.length); i++) {
            Field field = fields[i];
            field.setAccessible(true);
            field.set(result, args[i]);
        }
        return result;
    }

    /**
     * 获取实体类对应的表名
     * @param clazz 实体类
     * @return 表名
     */
    protected String getTableName(Class<?> clazz) {
        // 1. 检查是否有@Table注解
        Table table = clazz.getAnnotation(Table.class);
        // 2. 如果有注解，使用注解中的表名，否则使用类名的小写形式
        return table != null ? table.tableName() : clazz.getSimpleName().toLowerCase();
    }

    /**
     * 获取实体类的所有字段名
     * @param clazz 实体类
     * @return 字段名列表
     */
    protected List<String> getColumns(Class<?> clazz) {
        // 1. 获取所有声明的字段并转换为列表
        return Arrays.stream(clazz.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toList());
    }
}
