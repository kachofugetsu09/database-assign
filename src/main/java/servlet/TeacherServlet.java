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
        List<Teacher> teachers = mapper.selectAllTeachers();
        out.print(gson.toJson(teachers));
    }

    @Override
    protected void handleGetById(int id, HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        Teacher teacher = mapper.selectById(id);

        if (teacher != null) {
            out.print(gson.toJson(teacher));
        } else {
            sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "未找到ID为" + id + "的教师");
        }
    }

    @Override
    protected Teacher handleInsert(Teacher teacher, HttpServletRequest req) throws Exception {
        return mapper.insertTeacher(
                teacher.getName(),
                teacher.getGender(),
                teacher.getTitle()
        );
    }

    @Override
    protected Teacher handleUpdate(int id, Teacher teacher, HttpServletRequest req) throws Exception {
        return mapper.updateTeacher(
                id,
                teacher.getName(),
                teacher.getGender(),
                teacher.getTitle()
        );
    }

    @Override
    protected Teacher handleDelete(int id) throws Exception {
        return mapper.deleteTeacherById(id);
    }
}
