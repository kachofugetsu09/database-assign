package servlet;

import com.google.gson.Gson;
import entity.Course;
import mapper.CourseMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
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
        if (teacherIdStr != null && !teacherIdStr.isEmpty()) {
            try {
                int teacherId = Integer.parseInt(teacherIdStr);
                List<Course> courses = mapper.selectCoursesByTeacher(teacherId);
                out.print(gson.toJson(courses));
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "教师ID格式不正确");
            }
        } else {
            // 如果没有提供teacherId，返回所有课程
            List<Course> courses = mapper.selectAllCourses();
            out.print(gson.toJson(courses));
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
        try {
            // 从请求体中读取JSON数据
            BufferedReader reader = req.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String jsonData = jsonBuilder.toString();
            
            // 解析JSON数据
            Course requestCourse = gson.fromJson(jsonData, Course.class);
            System.out.println("解析后的课程数据: " + requestCourse);

            // 验证必要字段
            if (requestCourse.getCourseName() == null || requestCourse.getCourseName().isEmpty()) {
                throw new IllegalArgumentException("课程名称不能为空");
            }
            if (requestCourse.getCredit() == null) {
                throw new IllegalArgumentException("课程学分不能为空");
            }

            // 如果提供了courseId，检查是否已存在
            if (requestCourse.getCourseId() != null) {
                Course existingCourse = mapper.selectById(requestCourse.getCourseId());
                if (existingCourse != null) {
                    throw new IllegalArgumentException("课程ID " + requestCourse.getCourseId() + " 已存在");
                }
            }

            // 调用mapper插入数据
            return mapper.insertCourse(
                    requestCourse.getCourseId(),  // 可能是null，让数据库自动生成
                    requestCourse.getCourseName(),
                    requestCourse.getCredit(),
                    requestCourse.getTeacherId()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected Course handleUpdate(int id, Course course, HttpServletRequest req) throws Exception {
        try {
            // 从请求体中读取JSON数据
            BufferedReader reader = req.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String jsonData = jsonBuilder.toString();
            
            // 解析JSON数据
            Course requestCourse = gson.fromJson(jsonData, Course.class);
            System.out.println("更新操作解析后的课程数据: " + requestCourse);

            // 验证必要字段
            if (requestCourse.getCourseName() == null || requestCourse.getCourseName().isEmpty()) {
                throw new IllegalArgumentException("课程名称不能为空");
            }
            if (requestCourse.getCredit() == null) {
                throw new IllegalArgumentException("课程学分不能为空");
            }

            // 检查课程是否存在
            Course existingCourse = mapper.selectById(id);
            if (existingCourse == null) {
                throw new IllegalArgumentException("课程ID " + id + " 不存在");
            }

            return mapper.updateCourse(
                    id,
                    requestCourse.getCourseName(),
                    requestCourse.getCredit(),
                    requestCourse.getTeacherId()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected Course handleDelete(int id) throws Exception {
        return mapper.deleteCourseById(id);
    }
}
