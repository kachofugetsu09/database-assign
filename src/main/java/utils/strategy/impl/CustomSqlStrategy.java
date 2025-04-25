package utils.strategy.impl;

import annotations.Param;
import annotations.SQL;
import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.List;

public class CustomSqlStrategy extends AbstractSqlStrategy {
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        SQL sqlAnnotation = method.getAnnotation(SQL.class);
        String sql = sqlAnnotation.value();
        
        try (Connection connection = getConnection()) {
            sql = processSQLParameters(sql, method, args);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            setParameterValues(preparedStatement, method, args);
            System.out.println("Executing SQL: " + sql);

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
}
