package utils.strategy.impl;

import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.Collections;
import java.util.List;

public class InsertStrategy extends AbstractSqlStrategy {
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        String sql = createInsertSql(method);
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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

    private String createInsertSql(Method method) {
        Class<?> returnType = method.getReturnType();
        List<String> columns = getColumns(returnType);

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                getTableName(returnType),
                String.join(", ", columns),
                String.join(", ", Collections.nCopies(columns.size(), "?")));
    }
}
