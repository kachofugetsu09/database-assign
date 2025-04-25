package utils;

import utils.strategy.SqlExecutionStrategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class MySqlSessionFactory {
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL JDBC driver", e);
        }
    }

    public <T> T getMapper(Class<T> mapperClass) {
        return (T) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{mapperClass},
                new MapperInvocationHandler());
    }

    static class MapperInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            SqlExecutionStrategy strategy = SqlStrategyFactory.getStrategy(method);
            return strategy.execute(method, args);
        }
    }
}
