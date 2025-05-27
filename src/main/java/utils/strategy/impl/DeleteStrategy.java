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

/**
 * 删除策略实现类
 * 负责处理所有DELETE类型的SQL操作
 */
public class DeleteStrategy extends AbstractSqlStrategy {
    /**
     * 执行删除操作
     * @param method 要执行的方法
     * @param args 方法参数
     * @return 删除结果，包含删除的对象
     * @throws Exception 如果执行过程出错
     */
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        // 1. 创建DELETE SQL语句
        String sql = createDeleteSql(method);
        try (Connection connection = getConnection()) {
            // 2. 准备SQL语句
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            System.out.println("DELETE SQL: " + preparedStatement);

            // 3. 设置参数值
            for (int i = 0; i < args.length; i++) {
                setParameterValue(preparedStatement, i + 1, args[i]);
            }

            // 4. 打印详细的日志信息
            System.out.println("Executing SQL: " + sql);
            for (int i = 0; i < args.length; i++) {
                System.out.println("Parameter " + (i+1) + ": " + args[i]);
            }

            // 5. 执行删除操作
            int rs = preparedStatement.executeUpdate();
            if (rs > 0) {
                return parseInsertResult(args, method.getReturnType());
            }
        }
        return null;
    }

    /**
     * 创建DELETE SQL语句
     * @param method 要执行的方法
     * @return 生成的SQL语句
     */
    private String createDeleteSql(Method method) {
        // 1. 构建DELETE FROM子句
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        String tableName = getTableName(method.getReturnType());
        sb.append(tableName).append(" WHERE ");
        
        // 2. 获取WHERE注解
        Where where = method.getAnnotation(Where.class);
        String whereClause = where != null ? where.value() : "1=1";

        // 3. 创建参数映射表
        Map<String, String> paramMap = new HashMap<>();
        for (Parameter param : method.getParameters()) {
            Param paramAnnotation = param.getAnnotation(Param.class);
            if (paramAnnotation != null) {
                String paramName = paramAnnotation.value();
                String dbParamName = camelCaseToSnakeCase(paramName);
                paramMap.put(paramName, dbParamName);
            }
        }

        // 4. 处理WHERE子句中的参数占位符
        Pattern pattern = Pattern.compile("#\\{(\\w+)\\}");
        Matcher matcher = pattern.matcher(whereClause);
        StringBuilder processedWhereClause = new StringBuilder();
        int lastEnd = 0;

        // 5. 替换所有 #{paramName} 格式的占位符为 ?
        while (matcher.find()) {
            processedWhereClause.append(whereClause.substring(lastEnd, matcher.start()));
            processedWhereClause.append("?");
            lastEnd = matcher.end();
        }

        // 6. 添加剩余的WHERE子句内容
        if (lastEnd < whereClause.length()) {
            processedWhereClause.append(whereClause.substring(lastEnd));
        }

        // 7. 添加处理后的WHERE子句
        sb.append(processedWhereClause);

        // 8. 打印生成的SQL用于调试
        System.out.println("Generated SQL: " + sb.toString());

        return sb.toString();
    }
}
