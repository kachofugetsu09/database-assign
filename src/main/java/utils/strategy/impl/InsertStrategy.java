package utils.strategy.impl;

import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InsertStrategy extends AbstractSqlStrategy {
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        String sql = createInsertSql(method);
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            System.out.println("INSERT SQL: " + preparedStatement);

            // 设置参数值
            for (int i = 0; i < args.length; i++) {
                setParameterValue(preparedStatement, i + 1, args[i]);
            }

            System.out.println("Executing SQL with parameters: " + sql);
            // 打印参数值以便调试
            for (int i = 0; i < args.length; i++) {
                System.out.println("Parameter " + (i+1) + ": " + args[i]);
            }

            int rs = preparedStatement.executeUpdate();
            if (rs > 0) {
                // 获取自动生成的主键
                ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return parseInsertResultWithGeneratedKey(args, method.getReturnType(), generatedKeys.getInt(1), method);
                }
                return parseInsertResult(args, method.getReturnType());
            }
        }
        return null;
    }

    private String createInsertSql(Method method) {
        // 获取方法参数的注解
        java.lang.reflect.Parameter[] parameters = method.getParameters();
        List<String> dbColumns = new ArrayList<>();

        for (java.lang.reflect.Parameter parameter : parameters) {
            annotations.Param paramAnnotation = parameter.getAnnotation(annotations.Param.class);
            if (paramAnnotation != null) {
                String columnName = camelCaseToSnakeCase(paramAnnotation.value());
                dbColumns.add(columnName);
            }
        }

        // 确保有列和值
        if (dbColumns.isEmpty()) {
            throw new IllegalArgumentException("No columns specified for insert");
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                getTableName(method.getReturnType()),
                String.join(", ", dbColumns),
                String.join(", ", Collections.nCopies(dbColumns.size(), "?")));
    }

    // 处理带有生成主键的插入结果
    private Object parseInsertResultWithGeneratedKey(Object[] args, Class<?> returnType, int generatedId, Method method) throws Exception {
        Object instance = returnType.getDeclaredConstructor().newInstance();

        // 设置生成的ID - 使用通用方法来处理不同的ID字段名
        String idFieldName = "studentId"; // 默认为studentId
        try {
            java.lang.reflect.Method setIdMethod = returnType.getMethod("set" + idFieldName.substring(0, 1).toUpperCase() + idFieldName.substring(1), int.class);
            setIdMethod.invoke(instance, generatedId);
        } catch (NoSuchMethodException e) {
            System.out.println("Warning: Could not find setter for ID field: " + idFieldName);
        }

        // 设置其他字段
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

    public Object parseInsertResult(Object[] args, Class<?> returnType) throws Exception {
        return parseInsertResultWithGeneratedKey(args, returnType, 0, null);
    }
}

