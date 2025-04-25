package utils.strategy;

import java.lang.reflect.Method;

public interface SqlExecutionStrategy {
    Object execute(Method method, Object[] args) throws Exception;
}
