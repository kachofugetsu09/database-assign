package utils.strategy.impl;

import annotations.Param;
import annotations.Where;
import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询策略实现类
 * 负责处理所有SELECT类型的SQL操作
 */
public class SelectStrategy extends AbstractSqlStrategy {
    /**
     * 执行查询操作
     * @param method 要执行的方法
     * @param args 方法参数
     * @return 查询结果，可能是单个对象或对象列表
     * @throws Exception 如果执行过程出错
     */
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        // 1. 创建SELECT SQL语句
        String sql = createSelectSql(method);
        try (Connection connection = getConnection()) {
            // 2. 准备SQL语句
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            System.out.println("SELECT SQL: " + preparedStatement);
            
            // 3. 设置参数值
            for (int i = 0; i < args.length; i++) {
                setParameterValue(preparedStatement, i + 1, args[i]);
            }
            
            // 4. 执行查询
            System.out.println("Executing SQL: " + sql);
            ResultSet rs = preparedStatement.executeQuery();

            // 5. 根据返回类型处理结果
            if (method.getReturnType().equals(List.class)) {
                return parseResultList(rs, method);
            } else {
                return rs.next() ? parseResult(rs, method.getReturnType()) : null;
            }
        }
    }

    /**
     * 创建SELECT SQL语句
     * @param method 要执行的方法
     * @return 生成的SQL语句
     */
    private String createSelectSql(Method method) {
        // 1. 构建SELECT子句
        StringBuilder sb = new StringBuilder("SELECT ");
        List<String> columns = getColumns(method.getReturnType());

        // 2. 将Java字段名映射为数据库列名
        List<String> dbColumns = new ArrayList<>();
        for (String column : columns) {
            String dbColumn = camelCaseToSnakeCase(column);
            // 3. 添加别名以映射回Java字段名
            dbColumns.add(dbColumn + " AS " + column);
        }

        // 4. 构建FROM子句
        sb.append(String.join(", ", dbColumns))
                .append(" FROM ")
                .append(getTableName(method.getReturnType()))
                .append(" WHERE ");

        // 5. 处理WHERE子句
        Where where = method.getAnnotation(Where.class);
        String whereClause = where != null ? where.value() : "1=1";

        // 6. 处理WHERE子句中的参数占位符
        if (where != null) {
            for (Parameter param : method.getParameters()) {
                Param paramAnnotation = param.getAnnotation(Param.class);
                if (paramAnnotation != null) {
                    String paramName = paramAnnotation.value();
                    whereClause = whereClause.replace("#{" + paramName + "}", "?");
                }
            }
        }

        // 7. 添加WHERE子句到SQL语句
        sb.append(whereClause);
        return sb.toString();
    }
}
