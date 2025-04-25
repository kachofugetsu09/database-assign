package utils;

import annotations.SQL;
import utils.strategy.*;
import utils.strategy.impl.*;

import java.lang.reflect.Method;

public class SqlStrategyFactory {
    public static SqlExecutionStrategy getStrategy(Method method) {
        if (method.isAnnotationPresent(SQL.class)) {
            return new CustomSqlStrategy();
        }
        
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
        throw new IllegalArgumentException("Unsupported method: " + methodName);
    }
}
