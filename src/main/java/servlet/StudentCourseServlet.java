package servlet;

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
    }

    @Override
    protected Class<StudentCourse> getEntityClass() {
        return StudentCourse.class;
    }

    @Override
    protected void handleGetAll(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        String studentIdStr = req.getParameter("studentId");
        String courseIdStr = req.getParameter("courseId");
        if (studentIdStr != null) {
            int studentId = Integer.parseInt(studentIdStr);
            List<StudentCourse> studentCourses = mapper.selectByStudentId(studentId);
            out.print(gson.toJson(studentCourses));
        } else if (courseIdStr != null) {
            int courseId = Integer.parseInt(courseIdStr);
            List<StudentCourse> studentCourses = mapper.selectByCourseId(courseId);
            out.print(gson.toJson(studentCourses));
        } else {
            // 如果没有提供参数，返回所有选课记录
            List<StudentCourse> studentCourses = mapper.selectAll();
            out.print(gson.toJson(studentCourses));
        }
    }

    @Override
    protected void handleGetById(int id, HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        StudentCourse studentCourse = mapper.selectById(id);
        if (studentCourse != null) {
            out.print(gson.toJson(studentCourse));
        } else {
            sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "未找到ID为" + id + "的学生课程记录");
        }
    }

    @Override
    protected StudentCourse handleInsert(StudentCourse studentCourse, HttpServletRequest req) throws Exception {
        return mapper.insertStudentCourse(
                studentCourse.getStudentId(),
                studentCourse.getCourseId(),
                studentCourse.getScore(),
                studentCourse.getSemester()
        );
    }

    @Override
    protected StudentCourse handleUpdate(int id, StudentCourse studentCourse, HttpServletRequest req) throws Exception {
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
}
