package utils;

import utils.strategy.SqlExecutionStrategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * MySQL会话工厂类
 * 负责创建Mapper接口的代理实例，实现动态SQL执行
 * 使用JDK动态代理技术，在运行时生成Mapper接口的实现类
 */
public class MySqlSessionFactory {
    /**
     * 静态初始化块
     * 加载MySQL JDBC驱动
     * 如果驱动加载失败，将抛出运行时异常
     */
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL JDBC driver", e);
        }
    }

    /**
     * 获取Mapper接口的代理实例
     * @param mapperClass Mapper接口的Class对象
     * @param <T> Mapper接口的类型
     * @return Mapper接口的代理实例
     */
    public <T> T getMapper(Class<T> mapperClass) {
        return (T) Proxy.newProxyInstance(
                this.getClass().getClassLoader(),
                new Class[]{mapperClass},
                new MapperInvocationHandler());
    }

    /**
     * Mapper接口的代理处理器
     * 负责拦截Mapper接口方法的调用，并根据方法名选择合适的SQL执行策略
     */
    static class MapperInvocationHandler implements InvocationHandler {
        /**
         * 处理代理对象的方法调用
         * @param proxy 代理对象
         * @param method 被调用的方法
         * @param args 方法参数
         * @return 方法执行结果
         * @throws Throwable 如果执行过程出错
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 1. 获取适合的SQL执行策略
            SqlExecutionStrategy strategy = SqlStrategyFactory.getStrategy(method);
            // 2. 执行SQL并返回结果
            return strategy.execute(method, args);
        }
    }
}
