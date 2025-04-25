package utils.strategy.impl;

import annotations.Where;
import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.sql.*;

public class DeleteStrategy extends AbstractSqlStrategy {
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        String sql = createDeleteSql(method);
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            for (int i = 0; i < args.length; i++) {
                setParameterValue(preparedStatement, i + 1, args[i]);
            }
            System.out.println("Executing SQL: " + sql);
            int rs = preparedStatement.executeUpdate();
            if (rs > 0) {
                return parseInsertResult(args, method.getReturnType());
            }
        }
        return null;
    }

    private String createDeleteSql(Method method) {
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        String tableName = getTableName(method.getReturnType());
        sb.append(tableName).append(" WHERE ");
        Where where = method.getAnnotation(Where.class);
        sb.append(where != null ? where.value().replace("#{id}", "?") : "1=1");
        return sb.toString();
    }
}
