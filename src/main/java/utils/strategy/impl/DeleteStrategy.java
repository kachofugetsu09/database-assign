package utils.strategy.impl;

import annotations.Param;
import annotations.Where;
import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteStrategy extends AbstractSqlStrategy {
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        String sql = createDeleteSql(method);
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            System.out.println("DELETE SQL: " + preparedStatement);

            // 设置参数值
            for (int i = 0; i < args.length; i++) {
                setParameterValue(preparedStatement, i + 1, args[i]);
            }

            // 打印更详细的日志
            System.out.println("Executing SQL: " + sql);
            for (int i = 0; i < args.length; i++) {
                System.out.println("Parameter " + (i+1) + ": " + args[i]);
            }

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

        String whereClause = where != null ? where.value() : "1=1";

        // 创建参数映射表
        Map<String, String> paramMap = new HashMap<>();
        for (Parameter param : method.getParameters()) {
            Param paramAnnotation = param.getAnnotation(Param.class);
            if (paramAnnotation != null) {
                String paramName = paramAnnotation.value();
                String dbParamName = camelCaseToSnakeCase(paramName);
                paramMap.put(paramName, dbParamName);
            }
        }

        // 替换所有 #{paramName} 格式的占位符为 ?
        Pattern pattern = Pattern.compile("#\\{(\\w+)\\}");
        Matcher matcher = pattern.matcher(whereClause);
        StringBuilder processedWhereClause = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            processedWhereClause.append(whereClause.substring(lastEnd, matcher.start()));
            processedWhereClause.append("?");
            lastEnd = matcher.end();
        }

        if (lastEnd < whereClause.length()) {
            processedWhereClause.append(whereClause.substring(lastEnd));
        }

        sb.append(processedWhereClause);

        // 打印生成的SQL，便于调试
        System.out.println("Generated SQL: " + sb.toString());

        return sb.toString();
    }
}
