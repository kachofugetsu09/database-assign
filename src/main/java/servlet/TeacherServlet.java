package servlet;

import entity.Teacher;
import mapper.TeacherMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/teachers/*")
public class TeacherServlet extends BaseServlet<Teacher, TeacherMapper> {

    public TeacherServlet() {
        super(TeacherMapper.class);
    }

    @Override
    protected Class<Teacher> getEntityClass() {
        return Teacher.class;
    }

    @Override
    protected void handleGetAll(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        // 获取所有教师
        try {
            List<Teacher> teachers = mapper.selectAllTeachers();
            out.print(gson.toJson(teachers));
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取教师列表失败：" + e.getMessage());
        }
    }

    @Override
    protected void handleGetById(int id, HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        try {
            Teacher teacher = mapper.selectById(id);
            if (teacher != null) {
                out.print(gson.toJson(teacher));
            } else {
                sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "未找到ID为" + id + "的教师");
            }
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "获取教师失败：" + e.getMessage());
        }
    }

    @Override
    protected Teacher handleInsert(Teacher teacher, HttpServletRequest req) throws Exception {
        try {
            // 打印实体类字段值
            System.out.println("添加教师: ");
            System.out.println("teacherId = " + teacher.getTeacherId());
            System.out.println("name = " + teacher.getName());
            System.out.println("gender = " + teacher.getGender());
            System.out.println("title = " + teacher.getTitle());
            
            Boolean success = mapper.insertTeacher(
                    teacher.getName(),
                    teacher.getGender(),
                    teacher.getTitle()
            );
            
            System.out.println("插入结果: " + success);
            
            if (success == null || !success) {
                throw new Exception("添加教师失败：操作未成功完成");
            }
            
            // 获取刚插入的教师记录
            Teacher insertedTeacher = mapper.selectLastInsertedTeacher();
            if (insertedTeacher == null) {
                throw new Exception("无法获取新添加的教师信息");
            }
            
            return insertedTeacher;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("添加教师失败: " + e.getMessage());
        }
    }

    @Override
    protected Teacher handleUpdate(int id, Teacher teacher, HttpServletRequest req) throws Exception {
        try {
            // 打印更新参数
            System.out.println("更新教师 - ID: " + id);
            System.out.println("name = " + teacher.getName());
            System.out.println("gender = " + teacher.getGender());
            System.out.println("title = " + teacher.getTitle());
            
            Boolean success = mapper.updateTeacher(
                    id,
                    teacher.getName(),
                    teacher.getGender(),
                    teacher.getTitle()
            );
            
            System.out.println("更新结果: " + success);
            
            if (success == null || !success) {
                throw new Exception("更新教师失败：操作未成功完成");
            }
            
            // 获取更新后的教师记录
            Teacher updatedTeacher = mapper.selectById(id);
            if (updatedTeacher == null) {
                throw new Exception("无法获取更新后的教师信息");
            }
            
            return updatedTeacher;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("更新教师失败: " + e.getMessage());
        }
    }

    @Override
    protected Teacher handleDelete(int id) throws Exception {
        try {
            // 先获取要删除的教师，用于返回
            Teacher teacherToDelete = mapper.selectById(id);
            if (teacherToDelete == null) {
                throw new Exception("未找到ID为" + id + "的教师");
            }
            
            System.out.println("删除教师 - ID: " + id);
            Boolean success = mapper.deleteTeacherById(id);
            System.out.println("删除结果: " + success);
            
            if (success == null || !success) {
                throw new Exception("删除教师失败：操作未成功完成");
            }
            
            return teacherToDelete;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("删除教师失败: " + e.getMessage());
        }
    }

    // 发送错误响应
    protected void sendErrorResponse(HttpServletResponse resp, PrintWriter out, int statusCode, String message) {
        resp.setStatus(statusCode);
        resp.setContentType("application/json;charset=UTF-8");
        out.print(gson.toJson(new ErrorResponse(statusCode, message)));
        out.flush();
    }

    // 错误响应类
    class ErrorResponse {
        private int code;
        private String message;

        public ErrorResponse(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
