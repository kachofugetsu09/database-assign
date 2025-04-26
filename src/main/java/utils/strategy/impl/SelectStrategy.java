package utils.strategy.impl;

import annotations.Param;
import annotations.Where;
import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SelectStrategy extends AbstractSqlStrategy {
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        String sql = createSelectSql(method);
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            System.out.println("SELECT SQL: " + preparedStatement);
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
        }
    }

    private String createSelectSql(Method method) {
        StringBuilder sb = new StringBuilder("SELECT ");
        List<String> columns = getColumns(method.getReturnType());

        // Map Java field names to database column names
        List<String> dbColumns = new ArrayList<>();
        for (String column : columns) {
            String dbColumn = camelCaseToSnakeCase(column);
            // Add aliasing to map back to Java field names
            dbColumns.add(dbColumn + " AS " + column);
        }

        sb.append(String.join(", ", dbColumns))
                .append(" FROM ")
                .append(getTableName(method.getReturnType()))
                .append(" WHERE ");

        Where where = method.getAnnotation(Where.class);
        String whereClause = where != null ? where.value() : "1=1";

        // Handle camelCase parameter placeholders in WHERE clause
        if (where != null) {
            for (Parameter param : method.getParameters()) {
                Param paramAnnotation = param.getAnnotation(Param.class);
                if (paramAnnotation != null) {
                    String paramName = paramAnnotation.value();
                    whereClause = whereClause.replace("#{" + paramName + "}", "?");
                }
            }
        }

        sb.append(whereClause);
        return sb.toString();
    }
}
