package utils.strategy.impl;

import annotations.Param;
import annotations.Where;
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
            
            Parameter[] parameters = method.getParameters();
            Map<String, Object> paramMap = new HashMap<>();
            for (int i = 0; i < parameters.length; i++) {
                Param param = parameters[i].getAnnotation(Param.class);
                if (param != null) {
                    paramMap.put(param.value(), args[i]);
                }
            }

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

            Object idValue = paramMap.get("id");
            if (idValue != null) {
                setParameterValue(preparedStatement, paramIndex, idValue);
            }

            System.out.println("Executing SQL: " + sql);
            int rs = preparedStatement.executeUpdate();
            if (rs > 0) {
                return parseInsertResult(args, method.getReturnType());
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
}
