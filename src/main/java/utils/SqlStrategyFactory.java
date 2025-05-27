package utils;

import annotations.SQL;
import utils.strategy.*;
import utils.strategy.impl.*;

import java.lang.reflect.Method;

/**
 * SQL策略工厂类
 * 根据Mapper接口方法的特点，创建合适的SQL执行策略
 * 支持以下策略：
 * 1. 自定义SQL策略：当方法有@SQL注解时使用
 * 2. 查询策略：方法名以select开头
 * 3. 插入策略：方法名以insert开头
 * 4. 更新策略：方法名以update开头
 * 5. 删除策略：方法名以delete开头
 */
public class SqlStrategyFactory {
    /**
     * 获取适合的SQL执行策略
     * @param method Mapper接口的方法
     * @return SQL执行策略实例
     * @throws IllegalArgumentException 如果方法名不符合任何策略的命名规则
     */
    public static SqlExecutionStrategy getStrategy(Method method) {
        // 1. 检查是否有@SQL注解，有则使用自定义SQL策略
        if (method.isAnnotationPresent(SQL.class)) {
            return new CustomSqlStrategy();
        }
        
        // 2. 根据方法名选择对应的策略
        String methodName = method.getName().toLowerCase();
        if (methodName.startsWith("select")) {
            return new SelectStrategy();
        } else if (methodName.startsWith("insert")) {
            return new InsertStrategy();
        } else if (methodName.startsWith("update")) {
            return new UpdateStrategy();
        } else if (methodName.startsWith("delete")) {
            return new DeleteStrategy();
        }
        
        // 3. 如果没有匹配的策略，抛出异常
        throw new IllegalArgumentException("Unsupported method: " + methodName);
    }
}
