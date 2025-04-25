package servlet;

import com.google.gson.Gson;
import utils.MySqlSessionFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class BaseServlet<T, M> extends HttpServlet {
    protected M mapper;
    protected Gson gson = new Gson();
    protected final Class<M> mapperClass;

    public BaseServlet(Class<M> mapperClass) {
        this.mapperClass = mapperClass;
    }

    @Override
    public void init() throws ServletException {
        MySqlSessionFactory mySqlSessionFactory = new MySqlSessionFactory();
        mapper = mySqlSessionFactory.getMapper(mapperClass);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        setupResponse(resp);
        PrintWriter out = resp.getWriter();

        try {
            // 读取请求体
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = req.getReader().readLine()) != null) {
                sb.append(line);
            }
            String requestBody = sb.toString();

            // 调试信息
            System.out.println("接收到的请求体: " + requestBody);

            // 解析JSON
            T entity = gson.fromJson(requestBody, getEntityClass());
            T insertedEntity = handleInsert(entity, req);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(insertedEntity));
        } catch (Exception e) {
            e.printStackTrace(); // 详细错误打印到控制台
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误: " + e.getMessage());
        }
    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setupResponse(resp);
        PrintWriter out = resp.getWriter();
        
        try {
            String pathInfo = req.getPathInfo();
            int id = extractIdFromPath(pathInfo);
            
            T entity = gson.fromJson(req.getReader(), getEntityClass());
            T updatedEntity = handleUpdate(id, entity, req);
            
            if (updatedEntity != null) {
                out.print(gson.toJson(updatedEntity));
            } else {
                sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "未找到ID为" + id + "的记录");
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "无效的ID格式");
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "服务器内部错误: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
