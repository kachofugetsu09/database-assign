package servlet;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import entity.StudentCourse;
import mapper.StudentCourseMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/student-courses/*")
public class StudentCourseServlet extends BaseServlet<StudentCourse, StudentCourseMapper> {

    public StudentCourseServlet() {
        super(StudentCourseMapper.class);
        this.gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .create();
    }

    @Override
    protected Class<StudentCourse> getEntityClass() {
        return StudentCourse.class;
    }

    @Override
    protected void handleGetAll(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        String studentIdStr = req.getParameter("studentId");
        String courseIdStr = req.getParameter("courseId");

        try {
            if (studentIdStr != null) {
                int studentId = Integer.parseInt(studentIdStr);
                List<StudentCourse> studentCourses = mapper.selectByStudentId(studentId);
                out.print(gson.toJson(studentCourses));
            } else if (courseIdStr != null) {
                int courseId = Integer.parseInt(courseIdStr);
                List<StudentCourse> studentCourses = mapper.selectByCourseId(courseId);
                out.print(gson.toJson(studentCourses));
            } else {
                // 如果没有提供参数，返回所有记录
                List<StudentCourse> studentCourses = mapper.selectAllStudentCourses();
                out.print(gson.toJson(studentCourses));
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "无效的ID格式");
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "查询失败：" + e.getMessage());
        }
    }

    @Override
    protected void handleGetById(int id, HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        try {
            StudentCourse studentCourse = mapper.selectById(id);
            if (studentCourse != null) {
                out.print(gson.toJson(studentCourse));
            } else {
                sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "未找到ID为" + id + "的学生课程记录");
            }
        } catch (Exception e) {
            sendErrorResponse(resp, out, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "查询失败：" + e.getMessage());
        }
    }

    @Override
    protected StudentCourse handleInsert(StudentCourse studentCourse, HttpServletRequest req) throws Exception {
        validateStudentCourse(studentCourse);
        return mapper.insertStudentCourse(
                studentCourse.getStudentId(),
                studentCourse.getCourseId(),
                studentCourse.getScore(),
                studentCourse.getSemester()
        );
    }

    @Override
    protected StudentCourse handleUpdate(int id, StudentCourse studentCourse, HttpServletRequest req) throws Exception {
        validateStudentCourse(studentCourse);
        return mapper.updateStudentCourse(
                id,
                studentCourse.getStudentId(),
                studentCourse.getCourseId(),
                studentCourse.getScore(),
                studentCourse.getSemester()
        );
    }

    @Override
    protected StudentCourse handleDelete(int id) throws Exception {
        return mapper.deleteStudentCourseById(id);
    }

    private void validateStudentCourse(StudentCourse studentCourse) throws IllegalArgumentException {
        if (studentCourse.getStudentId() == null || studentCourse.getStudentId() <= 0) {
            throw new IllegalArgumentException("无效的学生ID");
        }
        if (studentCourse.getCourseId() == null || studentCourse.getCourseId() <= 0) {
            throw new IllegalArgumentException("无效的课程ID");
        }
        if (studentCourse.getScore() == null || studentCourse.getScore() < 0 || studentCourse.getScore() > 100) {
            throw new IllegalArgumentException("分数必须在0-100之间");
        }
        if (studentCourse.getSemester() == null || studentCourse.getSemester().trim().isEmpty()) {
            throw new IllegalArgumentException("学期不能为空");
        }
        if (!studentCourse.getSemester().matches("\\d{4}-\\d{4}-[12]")) {
            throw new IllegalArgumentException("学期格式不正确，应为：YYYY-YYYY-1或YYYY-YYYY-2");
        }
    }
}
