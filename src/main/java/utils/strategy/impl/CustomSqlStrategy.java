package utils.strategy.impl;

import annotations.Param;
import annotations.SQL;
import utils.strategy.AbstractSqlStrategy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义SQL执行策略实现类
 * 继承自AbstractSqlStrategy，实现具体的SQL执行逻辑
 * 支持动态SQL参数替换和结果集映射
 * 处理SELECT和UPDATE/INSERT/DELETE两种类型的SQL语句
 */
public class CustomSqlStrategy extends AbstractSqlStrategy {
    /**
     * 执行SQL方法
     * @param method 要执行的方法
     * @param args 方法参数
     * @return 执行结果
     * @throws Exception 执行过程中的异常
     */
    @Override
    public Object execute(Method method, Object[] args) throws Exception {
        // 获取方法上的SQL注解值
        SQL sqlAnnotation = method.getAnnotation(SQL.class);
        String sql = sqlAnnotation.value();
        
        try (Connection connection = getConnection()) {
            // 处理SQL参数，将命名参数替换为问号占位符
            sql = processSQLParameters(sql, method, args);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            // 设置参数值
            setParameterValues(preparedStatement, method, args);
            System.out.println("Executing SQL: " + sql);

            // 根据SQL类型执行不同的操作
            if (sql.trim().toUpperCase().startsWith("SELECT")) {
                ResultSet rs = preparedStatement.executeQuery();
                // 处理查询结果
                if (method.getReturnType().equals(List.class)) {
                    return parseResultList(rs, method);
                } else {
                    return rs.next() ? parseResult(rs, method.getReturnType()) : null;
                }
            } else {
                // 处理更新操作
                int affected = preparedStatement.executeUpdate();
                if (method.getReturnType().equals(Integer.class)) {
                    return affected;
                }
                return affected > 0;
            }
        }
    }

    /**
     * 处理SQL参数
     * @param sql 原始SQL语句
     * @param method 执行的方法
     * @param args 方法参数
     * @return 处理后的SQL语句
     */
    private String processSQLParameters(String sql, Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            // 查找带有@Param注解的参数
            Param param = parameters[i].getAnnotation(Param.class);
            if (param != null) {
                String paramName = param.value();
                // 将命名参数替换为问号占位符
                sql = sql.replace("#{" + paramName + "}", "?");
            }
        }
        return sql;
    }

    /**
     * 设置PreparedStatement的参数值
     * @param ps PreparedStatement对象
     * @param method 执行的方法
     * @param args 方法参数
     * @throws SQLException SQL异常
     */
    private void setParameterValues(PreparedStatement ps, Method method, Object[] args) throws SQLException {
        Parameter[] parameters = method.getParameters();
        SQL sqlAnnotation = method.getAnnotation(SQL.class);
        String sql = sqlAnnotation.value();
        
        // 从SQL语句中提取参数名的顺序
        List<String> paramOrder = new ArrayList<>();
        Pattern pattern = Pattern.compile("#\\{(.*?)}");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            paramOrder.add(matcher.group(1));
        }
        
        // 创建参数名到参数值的映射
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            Param param = parameters[i].getAnnotation(Param.class);
            if (param != null) {
                paramMap.put(param.value(), args[i]);
            }
        }
        
        // 按SQL中的参数顺序设置值
        for (int i = 0; i < paramOrder.size(); i++) {
            String paramName = paramOrder.get(i);
            Object value = paramMap.get(paramName);
            setParameterValue(ps, i + 1, value);
        }
    }

    /**
     * 设置单个参数值
     * @param ps PreparedStatement对象
     * @param index 参数索引
     * @param value 参数值
     * @throws SQLException SQL异常
     */
    protected void setParameterValue(PreparedStatement ps, int index, Object value) throws SQLException {
        // 根据参数类型设置对应的值
        if (value == null) {
            ps.setNull(index, Types.NULL);
        } else if (value instanceof String) {
            ps.setString(index, (String) value);
        } else if (value instanceof Integer) {
            ps.setInt(index, (Integer) value);
        } else if (value instanceof Long) {
            ps.setLong(index, (Long) value);
        } else if (value instanceof Double) {
            ps.setDouble(index, (Double) value);
        } else if (value instanceof Float) {
            ps.setFloat(index, (Float) value);
        } else if (value instanceof Boolean) {
            ps.setBoolean(index, (Boolean) value);
        } else if (value instanceof Date) {
            ps.setTimestamp(index, new Timestamp(((Date) value).getTime()));
        } else {
            ps.setObject(index, value);
        }
    }
}
