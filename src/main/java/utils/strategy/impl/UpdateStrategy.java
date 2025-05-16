package utils.strategy.impl;

import annotations.Param;
import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.*;

public class UpdateStrategy extends AbstractSqlStrategy {
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        String sql = createUpdateSql(method);
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            // 获取方法参数和它们的索引
            Parameter[] parameters = method.getParameters();
            Map<String, Integer> paramNameToArgIndex = new HashMap<>();
            List<String> nonIdParams = new ArrayList<>();
            String idParamName = null;

            // 构建参数名到参数值索引的映射
            for (int i = 0; i < parameters.length; i++) {
                Param param = parameters[i].getAnnotation(Param.class);
                if (param != null) {
                    String paramName = param.value();
                    paramNameToArgIndex.put(paramName, i);

                    if (isIdParam(paramName)) {
                        idParamName = paramName;
                    } else {
                        nonIdParams.add(paramName);
                    }
                }
            }

            // 设置SET子句中的参数值
            int paramIndex = 1;
            for (String paramName : nonIdParams) {
                int argIndex = paramNameToArgIndex.get(paramName);
                Object value = args[argIndex];
                System.out.println("Setting parameter " + paramIndex + " (" + paramName + ") = " + value);
                setParameterValue(preparedStatement, paramIndex++, value);
            }

            // 设置WHERE子句中的ID参数
            if (idParamName != null) {
                int idArgIndex = paramNameToArgIndex.get(idParamName);
                Object idValue = args[idArgIndex];
                System.out.println("Setting ID parameter " + paramIndex + " (" + idParamName + ") = " + idValue);
                setParameterValue(preparedStatement, paramIndex, idValue);
            }

            System.out.println("UPDATE SQL: " + preparedStatement);
            System.out.println("Executing SQL: " + sql);

            // 打印参数值用于调试
            for (int i = 0; i < args.length; i++) {
                System.out.println("Parameter " + (i+1) + ": " + args[i]);
            }

            int rs = preparedStatement.executeUpdate();
            if (rs > 0) {
                return parseUpdateResult(args, method.getReturnType(), method);
            }
        }
        return null;
    }

    private String createUpdateSql(Method method) {
        StringBuilder sb = new StringBuilder("UPDATE ");
        String tableName = getTableName(method.getReturnType());
        sb.append(tableName).append(" SET ");

        Parameter[] parameters = method.getParameters();
        List<String> setClauses = new ArrayList<>();
        String idParamName = null;

        for (Parameter param : parameters) {
            if (param.isAnnotationPresent(Param.class)) {
                String paramName = param.getAnnotation(Param.class).value();
                if (isIdParam(paramName)) {
                    idParamName = paramName;
                } else {
                    String dbColumnName = camelCaseToSnakeCase(paramName);
                    setClauses.add(dbColumnName + " = ?");
                }
            }
        }

        sb.append(String.join(", ", setClauses));

        // 构建WHERE子句，使用ID参数
        if (idParamName != null) {
            String idColumnName = camelCaseToSnakeCase(idParamName);
            sb.append(" WHERE ").append(idColumnName).append(" = ?");
        }

        return sb.toString();
    }

    // 判断是否为ID参数
    private boolean isIdParam(String paramName) {
        return paramName.equals("id") || paramName.endsWith("Id");
    }

    // 查找ID参数名
    private String findIdParamName(Parameter[] parameters) {
        for (Parameter param : parameters) {
            Param annotation = param.getAnnotation(Param.class);
            if (annotation != null) {
                String paramName = annotation.value();
                if (isIdParam(paramName)) {
                    return paramName;
                }
            }
        }
        return null;
    }

    // 处理更新结果
    private Object parseUpdateResult(Object[] args, Class<?> returnType, Method method) throws Exception {
        Object instance = returnType.getDeclaredConstructor().newInstance();

        // 设置所有属性
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Param paramAnnotation = parameters[i].getAnnotation(Param.class);
            if (paramAnnotation != null && i < args.length) {
                String fieldName = paramAnnotation.value();
                String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                try {
                    Method setter = returnType.getMethod(setterName, parameters[i].getType());
                    setter.invoke(instance, args[i]);
                } catch (NoSuchMethodException e) {
                    System.out.println("Warning: Could not find setter: " + setterName + " for type " + parameters[i].getType().getName());
                }
            }
        }

        return instance;
    }
}
