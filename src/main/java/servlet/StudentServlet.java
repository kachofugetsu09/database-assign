package servlet;

import entity.Student;
import mapper.StudentMapper;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet("/api/students/*")
public class StudentServlet extends BaseServlet<Student, StudentMapper> {

    public StudentServlet() {
        super(StudentMapper.class);
    }

    @Override
    protected Class<Student> getEntityClass() {
        return Student.class;
    }

    @Override
    protected void handleGetAll(HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        // 获取年龄范围参数
        String minAgeStr = req.getParameter("minAge");
        String maxAgeStr = req.getParameter("maxAge");

        if (minAgeStr != null && maxAgeStr != null) {
            try {
                int minAge = Integer.parseInt(minAgeStr);
                int maxAge = Integer.parseInt(maxAgeStr);
                List<Student> students = mapper.selectStudentsByAgeRange(minAge, maxAge);
                out.print(gson.toJson(students));
            } catch (NumberFormatException e) {
                sendErrorResponse(resp, out, HttpServletResponse.SC_BAD_REQUEST, "年龄参数格式不正确");
            }
        } else {
            // 如果没有提供年龄参数，返回所有学生
            List<Student> students = mapper.selectStudentsByAgeRange(0, Integer.MAX_VALUE);
            for(Student student : students){
                System.out.println(student.toString());
            }
            out.print(gson.toJson(students));
        }
    }


    @Override
    protected void handleGetById(int id, HttpServletRequest req, HttpServletResponse resp, PrintWriter out) throws Exception {
        Student student = mapper.selectById(id);

        if (student != null) {
            out.print(gson.toJson(student));
        } else {
            sendErrorResponse(resp, out, HttpServletResponse.SC_NOT_FOUND, "未找到ID为" + id + "的学生");
        }
    }

    @Override
    protected Student handleInsert(Student student, HttpServletRequest req) throws Exception {
        try {
            // 打印日志以便诊断
            System.out.println("接收到的学生数据: " + student);

            // 确保enrollmentDate不为null
            if (student.getEnrollmentDate() == null) {
                throw new IllegalArgumentException("入学日期不能为空");
            }

            // 使用SimpleDateFormat解析日期字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = sdf.format(student.getEnrollmentDate());

            Student insertedStudent = mapper.insertStudent(
                    student.getName(),
                    student.getGender(),
                    student.getAge(),
                    dateStr
            );

            return insertedStudent;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected Student handleUpdate(int id, Student student, HttpServletRequest req) throws Exception {
        try {
            // 使用SimpleDateFormat解析日期字符串
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = sdf.format(student.getEnrollmentDate());

            return mapper.updateStudent(
                    id,
                    student.getName(),
                    student.getGender(),
                    student.getAge(),
                    dateStr
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    protected Student handleDelete(int id) throws Exception {
        return mapper.deleteStudentById(id);
    }
}
