package utils.strategy.impl;

import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 插入策略实现类
 * 负责处理所有INSERT类型的SQL操作
 */
public class InsertStrategy extends AbstractSqlStrategy {
    /**
     * 执行插入操作
     * @param method 要执行的方法
     * @param args 方法参数
     * @return 插入结果，包含生成的主键（如果有）
     * @throws Exception 如果执行过程出错
     */
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        // 1. 创建INSERT SQL语句
        String sql = createInsertSql(method);
        try (Connection connection = getConnection()) {
            // 2. 准备SQL语句，启用自动生成主键
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            System.out.println("INSERT SQL: " + preparedStatement);

            // 3. 设置参数值
            for (int i = 0; i < args.length; i++) {
                setParameterValue(preparedStatement, i + 1, args[i]);
            }

            // 4. 执行插入操作
            System.out.println("Executing SQL with parameters: " + sql);
            // 5. 打印参数值用于调试
            for (int i = 0; i < args.length; i++) {
                System.out.println("Parameter " + (i+1) + ": " + args[i]);
            }

            // 6. 执行更新并获取影响的行数
            int rs = preparedStatement.executeUpdate();
            if (rs > 0) {
                // 7. 获取自动生成的主键
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return parseInsertResultWithGeneratedKey(args, method.getReturnType(), generatedKeys.getInt(1), method);
                }
                return parseInsertResult(args, method.getReturnType());
            }
        }
        return null;
    }

    /**
     * 创建INSERT SQL语句
     * @param method 要执行的方法
     * @return 生成的SQL语句
     */
    private String createInsertSql(Method method) {
        // 1. 获取方法参数的注解
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        List<String> dbColumns = new ArrayList<>();

        // 2. 收集所有列名
        for (java.lang.reflect.Parameter parameter : parameters) {
            annotations.Param paramAnnotation = parameter.getAnnotation(annotations.Param.class);
            if (paramAnnotation != null) {
                String columnName = camelCaseToSnakeCase(paramAnnotation.value());
                dbColumns.add(columnName);
            }
        }

        // 3. 确保有列和值
        if (dbColumns.isEmpty()) {
            throw new IllegalArgumentException("No columns specified for insert");
        }

        // 4. 构建完整的INSERT语句
        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                getTableName(method.getReturnType()),
                String.join(", ", dbColumns),
                String.join(", ", Collections.nCopies(dbColumns.size(), "?")));
    }

    /**
     * 处理带有生成主键的插入结果
     * @param args 方法参数
     * @param returnType 返回类型
     * @param generatedId 生成的主键值
     * @param method 执行的方法
     * @return 包含所有字段值的对象
     * @throws Exception 如果处理过程出错
     */
    private Object parseInsertResultWithGeneratedKey(Object[] args, Class<?> returnType, int generatedId, Method method) throws Exception {
        // 1. 创建返回类型的实例
        Object instance = returnType.getDeclaredConstructor().newInstance();

        // 2. 设置生成的ID
        String idFieldName = "studentId"; // 默认为studentId
        try {
            java.lang.reflect.Method setIdMethod = returnType.getMethod("set" + idFieldName.substring(0, 1).toUpperCase() + idFieldName.substring(1), int.class);
            setIdMethod.invoke(instance, generatedId);
        } catch (NoSuchMethodException e) {
            System.out.println("Warning: Could not find setter for ID field: " + idFieldName);
        }

        // 3. 设置其他字段
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            annotations.Param paramAnnotation = parameters[i].getAnnotation(annotations.Param.class);
            if (paramAnnotation != null && i < args.length) {
                String fieldName = paramAnnotation.value();
                String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                try {
                    java.lang.reflect.Method setter = returnType.getMethod(setterName, parameters[i].getType());
                    setter.invoke(instance, args[i]);
                } catch (NoSuchMethodException e) {
                    System.out.println("Warning: Could not find setter: " + setterName + " for type " + parameters[i].getType().getName());
                }
            }
        }

        return instance;
    }

    /**
     * 解析插入结果
     * @param args 方法参数
     * @param returnType 返回类型
     * @return 包含所有字段值的对象
     * @throws Exception 如果处理过程出错
     */
    public Object parseInsertResult(Object[] args, Class<?> returnType) throws Exception {
        return parseInsertResultWithGeneratedKey(args, returnType, 0, null);
    }
}

