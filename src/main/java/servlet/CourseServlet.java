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
import java.io.IOException;

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
            // 验证课程数据
            if (!validateCourse(course)) {
                throw new IllegalArgumentException("课程数据验证失败");
            }
            
            // 检查课程ID是否已存在
            Course existingCourse = mapper.getCourseById(course.getCourseId());
            if (existingCourse != null) {
                throw new IllegalArgumentException("课程ID已存在");
            }
            
            // 检查课程名称是否重复
            List<Course> allCourses = mapper.selectAllCourses();
            for (Course c : allCourses) {
                if (c.getCourseName().equals(course.getCourseName())) {
                    throw new IllegalArgumentException("课程名称已存在");
                }
            }
            
            // 插入课程
            boolean success = mapper.insertCourse(
                course.getCourseId(),
                course.getCourseName(),
                course.getCredit(),
                course.getTeacherId()
            );
            
            if (!success) {
                throw new Exception("课程添加失败");
            }
            
            return course;
        } catch (Exception e) {
            System.out.println("添加课程失败: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected Course handleUpdate(int id, Course course, HttpServletRequest req) throws Exception {
        try {
            System.out.println("更新操作解析后的课程数据: " + course);
            System.out.println("要更新的课程ID: " + id);

            // 检查课程是否存在
            Course existingCourse = mapper.selectById(id);
            if (existingCourse == null) {
                throw new IllegalArgumentException("课程ID " + id + " 不存在");
            }

            // 设置课程ID
            course.setCourseId(id);

            // 如果前端没有传递某些字段，使用现有课程的值
            if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
                course.setCourseName(existingCourse.getCourseName());
            }
            if (course.getCredit() == null) {
                course.setCredit(existingCourse.getCredit());
            }
            if (course.getTeacherId() == null) {
                course.setTeacherId(existingCourse.getTeacherId());
            }

            // 增强的数据验证
            validateCourse(course);

            // 检查课程名称是否与其他课程重复
            if (!course.getCourseName().equals(existingCourse.getCourseName())) {
                List<Course> allCourses = mapper.selectAllCourses();
                for (Course otherCourse : allCourses) {
                    if (!otherCourse.getCourseId().equals(id) && 
                        otherCourse.getCourseName().equals(course.getCourseName())) {
                        throw new IllegalArgumentException("课程名称已存在");
                    }
                }
            }

            // 打印更新前的数据
            System.out.println("更新前的课程数据: " + existingCourse);
            System.out.println("准备更新的课程数据: " + course);

            // 打印SQL参数
            System.out.println("SQL参数 - courseId: " + id);
            System.out.println("SQL参数 - courseName: " + course.getCourseName());
            System.out.println("SQL参数 - credit: " + course.getCredit());
            System.out.println("SQL参数 - teacherId: " + course.getTeacherId());

            // 确保teacherId不为null
            Integer teacherId = course.getTeacherId();
            if (teacherId == null) {
                teacherId = existingCourse.getTeacherId();
            }

            boolean success = mapper.updateCourse(
                    course.getCourseName(),
                    course.getCredit(),
                    teacherId,
                    id
            );

            if (!success) {
                throw new Exception("课程更新失败");
            }

            // 获取并返回更新后的课程信息
            Course updatedCourse = mapper.getCourseById(id);
            if (updatedCourse == null) {
                throw new Exception("无法获取更新后的课程信息");
            }

            System.out.println("更新后的课程数据: " + updatedCourse);
            return updatedCourse;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("数据验证失败: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("更新课程时发生错误: " + e.getMessage());
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
        
        boolean success = mapper.deleteCourseById(id);
        if (!success) {
            throw new Exception("课程删除失败");
        }
        
        return course;
    }

    // 课程数据验证
    private boolean validateCourse(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("课程数据不能为空");
        }
        if (course.getCourseId() == null || course.getCourseId() <= 0) {
            throw new IllegalArgumentException("课程ID必须大于0");
        }
        if (course.getCourseName() == null || course.getCourseName().trim().isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (course.getCredit() == null || course.getCredit() <= 0) {
            throw new IllegalArgumentException("学分必须大于0");
        }
        if (course.getTeacherId() == null || course.getTeacherId() <= 0) {
            throw new IllegalArgumentException("教师ID必须大于0");
        }
        
        return true;
    }
}
