package servlet;

import com.google.gson.Gson;
import entity.Course;
import mapper.CourseMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/api/courses/*")
public class CourseServlet extends BaseServlet<Course, CourseMapper> {

    public CourseServlet() {
        super(CourseMapper.class);
    }

    @Override
    protected Class<Course> getEntityClass() {
        return Course.class;
    }

    @Override
    protected void handleGetAll(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        String teacherIdStr = req.getParameter("teacherId");
        if (teacherIdStr != null) {
            int teacherId = Integer.parseInt(teacherIdStr);
            List<Course> courses = mapper.selectCoursesByTeacher(teacherId);
            out.print(gson.toJson(courses));
        } else {
            sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "请提供teacherId参数");
        }
    }

    @Override
    protected void handleGetById(int id, HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        Course course = mapper.selectById(id);

        if (course != null) {
            out.print(gson.toJson(course));
        } else {
            sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "未找到ID为" + id + "的课程");
        }
    }

    @Override
    protected Course handleInsert(Course course, HttpServletRequest req) throws Exception {
        return mapper.insertCourse(
                course.getCourseName(),
                course.getCredit(),
                course.getTeacherId()
        );
    }

    @Override
    protected Course handleUpdate(int id, Course course, HttpServletRequest req) throws Exception {
        return mapper.updateCourse(
                id,
                course.getCourseName(),
                course.getCredit(),
                course.getTeacherId()
        );
    }

    @Override
    protected Course handleDelete(int id) throws Exception {
        return mapper.deleteCourseById(id);
    }
}
