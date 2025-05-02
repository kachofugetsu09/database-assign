import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import filter.CORSFilter;

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * 应用程序主入口类，类似Spring Boot的快速启动方式
 */
public class Application {
    private static final int PORT = 8080;
    private static final String CONTEXT_PATH = "";
    private static final String WEB_APP_LOCATION = "src/main/webapp";
    private static final String WEB_APP_MOUNT = "/WEB-INF/classes";
    private static final String WEB_APP_CLASSES = "target/classes";

    private Tomcat tomcat;
    private Context context;

    /**
     * 创建应用程序实例
     */
    public Application() {
        this(PORT);
    }

    /**
     * 创建应用程序实例，指定端口
     * @param port 服务器端口
     */
    public Application(int port) {
        try {
            // 设置系统默认编码为UTF-8
            System.setProperty("file.encoding", "UTF-8");
            
            // 创建Tomcat实例
            tomcat = new Tomcat();
            tomcat.setPort(port);
            
            // 设置主机，确保绑定到所有网络接口
            tomcat.getConnector().setProperty("address", "0.0.0.0");

            // 创建临时目录
            File tempDir = Files.createTempDirectory("tomcat-temp").toFile();
            tomcat.setBaseDir(tempDir.getAbsolutePath());

            // 创建Context
            context = tomcat.addWebapp(CONTEXT_PATH, new File(WEB_APP_LOCATION).getAbsolutePath());
            
            // 设置为应用程序类加载器
            context.setParentClassLoader(Application.class.getClassLoader());
            
            // 添加编译后的类文件
            WebResourceRoot resources = new StandardRoot(context);
            resources.addPreResources(new DirResourceSet(resources, WEB_APP_MOUNT,
                    new File(WEB_APP_CLASSES).getAbsolutePath(), "/"));
            context.setResources(resources);
        } catch (IOException e) {
            throw new RuntimeException("初始化应用程序失败", e);
        }
    }

    /**
     * 启动应用程序
     */
    public void run() {
        try {
            // 注册Servlet
            registerServlets();
            
            // 注册Filters
            registerFilters();
            
            // 启动Tomcat
            tomcat.start();
            
            System.out.println("应用程序已启动，访问 http://localhost:" + tomcat.getConnector().getPort());
            System.out.println("按Ctrl+C停止服务器");
            
            // 等待接收请求
            tomcat.getServer().await();
        } catch (LifecycleException | ServletException e) {
            throw new RuntimeException("启动应用程序失败", e);
        }
    }

    /**
     * 注册所有Servlet
     */
    private void registerServlets() throws ServletException {
        app.ApplicationRegistration.registerServlets(context);
    }

    /**
     * 注册所有过滤器
     */
    private void registerFilters() {
        // 注册CORS过滤器
        FilterDef filterDef = new FilterDef();
        filterDef.setFilterName("CORSFilter");
        filterDef.setFilterClass(CORSFilter.class.getName());
        context.addFilterDef(filterDef);
        
        FilterMap filterMap = new FilterMap();
        filterMap.setFilterName("CORSFilter");
        filterMap.addURLPattern("/*");
        filterMap.setDispatcher(DispatcherType.REQUEST.name());
        context.addFilterMap(filterMap);
    }

    /**
     * 应用程序主入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 创建并运行应用
        new Application().run();
    }
} 