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
                String paramName = param.value();
                sql = sql.replace("#{" + paramName + "}", "?");
            }
        }
        return sql;
    }

    private void setParameterValues(PreparedStatement ps, Method method, Object[] args) throws SQLException {
        Parameter[] parameters = method.getParameters();
        SQL sqlAnnotation = method.getAnnotation(SQL.class);
        String sql = sqlAnnotation.value();
        
        // 从SQL语句中提取参数名的顺序
        java.util.List<String> paramOrder = new java.util.ArrayList<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("#\\{(.*?)}");
        java.util.regex.Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            paramOrder.add(matcher.group(1));
        }
        
        // 创建参数名到参数值的映射
        java.util.Map<String, Object> paramMap = new java.util.HashMap<>();
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

    protected void setParameterValue(PreparedStatement ps, int index, Object value) throws SQLException {
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
