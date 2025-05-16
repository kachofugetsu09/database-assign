package servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import utils.MySqlSessionFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import static com.google.gson.FieldNamingPolicy.IDENTITY;
import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

public abstract class BaseServlet<T, M> extends HttpServlet {
    protected M mapper;
    protected Gson gson;
    protected final Class<M> mapperClass;

    public BaseServlet(Class<M> mapperClass) {
        this.mapperClass = mapperClass;
        this.gson = new GsonBuilder()
                .setFieldNamingPolicy(IDENTITY)
                .setDateFormat("yyyy-MM-dd")
                .serializeNulls()
                .create();
    }

    @Override
    public void init() throws ServletException {
        MySqlSessionFactory mySqlSessionFactory = new MySqlSessionFactory();
        mapper = mySqlSessionFactory.getMapper(mapperClass);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        setupResponse(resp);
        PrintWriter out = resp.getWriter();
        
        try {
            String pathInfo = req.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                handleGetAll(req, resp, out);
            } else {
                int id = extractIdFromPath(pathInfo);
                handleGetById(id, req, resp, out);
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "无效的ID或参数格式");
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        setupResponse(resp);
        PrintWriter out = resp.getWriter();

        try {
            // 使用工具方法读取请求体，确保只读取一次
            String requestBody = getRequestBody(req);

            // 调试信息
            System.out.println("接收到的请求体: " + requestBody);

            if (requestBody == null || requestBody.isEmpty()) {
                sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "请求体不能为空");
                return;
            }

            // 解析JSON并传递给子类处理
            T entity = gson.fromJson(requestBody, getEntityClass());
            System.out.println("接收到的实体: " + entity);
            
            // 打印实体类的所有字段值
            System.out.println("实体类字段值:");
            for (java.lang.reflect.Field field : entity.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    System.out.println(field.getName() + " = " + field.get(entity));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            
            T insertedEntity = handleInsert(entity, req);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(insertedEntity));
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "服务器内部错误: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        setupResponse(resp);
        PrintWriter out = resp.getWriter();

        try {
            String pathInfo = req.getPathInfo();
            int id = extractIdFromPath(pathInfo);

            // 读取请求体，确保只读取一次
            String requestBody = getRequestBody(req);
            T entity = gson.fromJson(requestBody, getEntityClass());

            T updatedEntity = handleUpdate(id, entity, req);

            if (updatedEntity != null) {
                out.print(gson.toJson(updatedEntity));
            } else {
                sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "未找到ID为" + id + "的记录");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "无效的ID格式");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误: " + e.getMessage());
        }
    }
    // 添加这个方法到BaseServlet中
    protected String getRequestBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }



    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        setupResponse(resp);
        PrintWriter out = resp.getWriter();
        
        try {
            String pathInfo = req.getPathInfo();
            int id = extractIdFromPath(pathInfo);
            
            T deletedEntity = handleDelete(id);
            
            if (deletedEntity != null) {
                out.print(gson.toJson(deletedEntity));
            } else {
                sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "未找到ID为" + id + "的记录");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "无效的ID格式");
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误: " + e.getMessage());
        }
    }
    
    protected int extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new NumberFormatException("路径中没有ID");
        }
        String[] parts = pathInfo.split("/");
        return Integer.parseInt(parts[1]);
    }
    
    protected void setupResponse(HttpServletResponse resp) {
        resp.setContentType("application/json;charset=UTF-8");
    }
    
    protected void sendErrorResponse(HttpServletResponse resp, PrintWriter out, int statusCode, String message) {
        resp.setStatus(statusCode);
        out.print(gson.toJson(message));
    }
    
    // Abstract methods to be implemented by subclasses
    protected abstract Class<T> getEntityClass();
    protected abstract void handleGetAll(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception;
    protected abstract void handleGetById(int id, HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception;
    protected abstract T handleInsert(T entity, HttpServletRequest req) throws Exception;
    protected abstract T handleUpdate(int id, T entity, HttpServletRequest req) throws Exception;
    protected abstract T handleDelete(int id) throws Exception;
}
