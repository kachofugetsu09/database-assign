package servlet;

import com.google.gson.Gson;
import entity.User;
import mapper.UserMapper;
import utils.MySqlSessionFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {
    private UserMapper userMapper;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        MySqlSessionFactory mySqlSessionFactory = new MySqlSessionFactory();
        userMapper = mySqlSessionFactory.getMapper(UserMapper.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            String pathInfo = req.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // 获取年龄范围参数
                String minAgeStr = req.getParameter("minAge");
                String maxAgeStr = req.getParameter("maxAge");
                
                if (minAgeStr != null && maxAgeStr != null) {
                    int minAge = Integer.parseInt(minAgeStr);
                    int maxAge = Integer.parseInt(maxAgeStr);
                    List<User> users = userMapper.selectUserByAgeRange(minAge, maxAge);
                    out.print(gson.toJson(users));
                } else {
                    // 这里需要扩展，如果你想要获取所有用户
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson("请提供minAge和maxAge参数"));
                }
            } else {
                // 提取ID并查询单个用户
                int id = extractIdFromPath(pathInfo);
                User user = userMapper.selectById(id);
                
                if (user != null) {
                    out.print(gson.toJson(user));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print(gson.toJson("未找到ID为" + id + "的用户"));
                }
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("无效的ID或参数格式"));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("服务器内部错误: " + e.getMessage()));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            // 从请求体中获取用户数据
            User user = gson.fromJson(req.getReader(), User.class);
            
            // 插入用户
            User insertedUser = userMapper.insertUser(user.getId(), user.getName(), user.getAge());
            
            // 返回插入的用户
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(insertedUser));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("服务器内部错误: " + e.getMessage()));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            String pathInfo = req.getPathInfo();
            int id = extractIdFromPath(pathInfo);
            
            // 从请求体中获取更新数据
            User user = gson.fromJson(req.getReader(), User.class);
            
            // 更新用户
            User updatedUser = userMapper.updateUser(id, user.getName(), user.getAge());
            
            if (updatedUser != null) {
                out.print(gson.toJson(updatedUser));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson("未找到ID为" + id + "的用户"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("无效的ID格式"));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("服务器内部错误: " + e.getMessage()));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        
        try {
            String pathInfo = req.getPathInfo();
            int id = extractIdFromPath(pathInfo);
            
            // 删除用户
            User deletedUser = userMapper.deleteUserByID(id);
            
            if (deletedUser != null) {
                out.print(gson.toJson(deletedUser));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson("未找到ID为" + id + "的用户"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson("无效的ID格式"));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson("服务器内部错误: " + e.getMessage()));
        }
    }
    
    private int extractIdFromPath(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new NumberFormatException("路径中没有ID");
        }
        String[] parts = pathInfo.split("/");
        return Integer.parseInt(parts[1]); // /1 => [, 1]
    }
}
