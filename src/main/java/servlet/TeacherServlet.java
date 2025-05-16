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
            Teacher insertedTeacher = mapper.insertTeacher(
                    teacher.getName(),
                    teacher.getGender(),
                    teacher.getTitle()
            );
            return insertedTeacher;
        } catch (Exception e) {
            throw new Exception("创建教师失败：" + e.getMessage(), e);
        }
    }


    @Override
    protected Teacher handleUpdate(int id, Teacher teacher, HttpServletRequest req) throws Exception {
        try {
            Teacher updatedTeacher = mapper.updateTeacher(
                    id,
                    teacher.getName(),
                    teacher.getGender(),
                    teacher.getTitle()
            );
            return updatedTeacher;
        } catch (Exception e) {
            throw new Exception("更新教师失败：" + e.getMessage(), e);
        }
    }

    @Override
    protected Teacher handleDelete(int id) throws Exception {
        try {
            Teacher deletedTeacher = mapper.deleteTeacherById(id);
            if (deletedTeacher == null) {
                throw new Exception("未找到ID为" + id + "的教师");
            }
            return deletedTeacher;
        } catch (Exception e) {
            throw new Exception("删除教师失败：" + e.getMessage(), e);
        }
    }

    protected void sendErrorResponse(HttpServletResponse resp, PrintWriter out, int statusCode, String message) {
        resp.setStatus(statusCode);
        resp.setContentType("application/json;charset=UTF-8");
        out.print(gson.toJson(new ErrorResponse(statusCode, message)));
        out.flush();
    }

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
