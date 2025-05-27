package app;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import servlet.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * 这个类负责以编程方式注册所有的Servlet和Filter
 * 在使用嵌入式Tomcat时，可以替代web.xml配置或@WebServlet注解
 */
public class ApplicationRegistration {
    
    public static void registerServlets(Context context) throws ServletException {
        // 注册所有Servlet
        registerServlet(context, "studentServlet", "/api/students/*", new StudentServlet());
        registerServlet(context, "teacherServlet", "/api/teachers/*", new TeacherServlet());
        registerServlet(context, "courseServlet", "/api/courses/*", new CourseServlet());
        registerServlet(context, "studentCourseServlet", "/api/student-courses/*", new StudentCourseServlet());
    }
    
    private static void registerServlet(Context context, String servletName, String urlPattern, HttpServlet servlet) {
        Tomcat.addServlet(context, servletName, servlet);
        context.addServletMappingDecoded(urlPattern, servletName);
    }
} 