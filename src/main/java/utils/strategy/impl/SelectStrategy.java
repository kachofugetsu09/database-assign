package utils.strategy.impl;

import annotations.Where;
import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.List;

public class SelectStrategy extends AbstractSqlStrategy {
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        String sql = createSelectSql(method);
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
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
        sb.append(String.join(", ", columns))
                .append(" FROM ")
                .append(getTableName(method.getReturnType()))
                .append(" WHERE ");

        Where where = method.getAnnotation(Where.class);
        sb.append(where != null ? where.value().replace("#{id}", "?") : "1=1");
        return sb.toString();
    }
}
