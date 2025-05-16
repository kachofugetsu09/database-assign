package servlet;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    private final Gson gson;

    public CourseServlet() {
        super(CourseMapper.class);
        // 配置Gson使用驼峰命名
        this.gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .create();
    }

    @Override
    protected Class<Course> getEntityClass() {
        return Course.class;
    }

    @Override
    protected void handleGetAll(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        String teacherIdStr = req.getParameter("teacherId");
        List<Course> courses;
        
        if (teacherIdStr != null && !teacherIdStr.isEmpty()) {
            try {
                int teacherId = Integer.parseInt(teacherIdStr);
                courses = mapper.selectCoursesByTeacher(teacherId);
                System.out.println("按教师ID查询课程结果: " + courses);
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "教师ID格式不正确");
                return;
            }
        } else {
            courses = mapper.selectAllCourses();
            System.out.println("查询所有课程结果: " + courses);
        }
        
        // 验证每个课程对象的完整性
        if (courses != null) {
            for (Course course : courses) {
                System.out.println("课程详情 - ID: " + course.getCourseId() 
                    + ", 名称: " + course.getCourseName() 
                    + ", 学分: " + course.getCredit() 
                    + ", 教师ID: " + course.getTeacherId());
            }
        }
        
        String jsonResponse = gson.toJson(courses);
        System.out.println("发送到前端的JSON响应: " + jsonResponse);
        out.print(jsonResponse);
    }

    @Override
    protected void handleGetById(int id, HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        Course course = mapper.selectById(id);
        System.out.println("按ID查询课程结果 - ID: " + id + ", 课程: " + course);

        if (course != null) {
            String jsonResponse = gson.toJson(course);
            System.out.println("发送到前端的JSON响应: " + jsonResponse);
            out.print(jsonResponse);
        } else {
            sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "未找到ID为" + id + "的课程");
        }
    }

    @Override
    protected Course handleInsert(Course course, HttpServletRequest req) throws Exception {
        try {
            BufferedReader reader = req.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String jsonData = jsonBuilder.toString();
            
            Course requestCourse = gson.fromJson(jsonData, Course.class);
            System.out.println("解析后的课程数据: " + requestCourse);

            // 增强的数据验证
            validateCourse(requestCourse);

            // 确保使用数据库自增ID
            requestCourse.setCourseId(null);

            // 调用mapper插入数据
            Course insertedCourse = mapper.insertCourse(
                    null,  // 使用数据库自增ID
                    requestCourse.getCourseName(),
                    requestCourse.getCredit(),
                    requestCourse.getTeacherId()
            );

            if (insertedCourse == null) {
                throw new Exception("课程创建失败");
            }

            return insertedCourse;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("数据验证失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("创建课程时发生错误: " + e.getMessage());
        }
    }

    @Override
    protected Course handleUpdate(int id, Course course, HttpServletRequest req) throws Exception {
        try {
            BufferedReader reader = req.getReader();
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            String jsonData = jsonBuilder.toString();
            
            Course requestCourse = gson.fromJson(jsonData, Course.class);
            System.out.println("更新操作解析后的课程数据: " + requestCourse);

            // 增强的数据验证
            validateCourse(requestCourse);

            // 检查课程是否存在
            Course existingCourse = mapper.selectById(id);
            if (existingCourse == null) {
                throw new IllegalArgumentException("课程ID " + id + " 不存在");
            }

            Course updatedCourse = mapper.updateCourse(
                    id,
                    requestCourse.getCourseName(),
                    requestCourse.getCredit(),
                    requestCourse.getTeacherId()
            );

            if (updatedCourse == null) {
                throw new Exception("课程更新失败");
            }

            return updatedCourse;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("数据验证失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("更新课程时发生错误: " + e.getMessage());
        }
    }

    @Override
    protected Course handleDelete(int id) throws Exception {
        Course course = mapper.selectById(id);
        if (course == null) {
            throw new IllegalArgumentException("课程ID " + id + " 不存在");
        }
        return mapper.deleteCourseById(id);
    }

    // 课程数据验证
    private void validateCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("课程数据不能为空");
        }
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (course.getCredit() == null) {
            throw new IllegalArgumentException("课程学分不能为空");
        }
        if (course.getCredit() < 1 || course.getCredit() > 10) {
            throw new IllegalArgumentException("课程学分必须在1-10之间");
        }
    }
}
