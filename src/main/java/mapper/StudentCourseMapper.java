// StudentCourseMapper.java
package mapper;

import annotations.Param;
import annotations.SQL;
import annotations.Where;
import entity.StudentCourse;

import java.util.List;

public interface StudentCourseMapper {
    @Where("id = #{id}")
    StudentCourse selectById(@Param("id") int id);

    StudentCourse insertStudentCourse(@Param("studentId") int studentId, 
                                    @Param("courseId") int courseId, 
                                    @Param("score") int score, 
                                    @Param("semester") String semester);

    @Where("id = #{id}")
    StudentCourse deleteStudentCourseById(@Param("id") int id);

    @Where("id = #{id}")
    StudentCourse updateStudentCourse(@Param("id") int id, 
                                    @Param("studentId") int studentId, 
                                    @Param("courseId") int courseId, 
                                    @Param("score") int score, 
                                    @Param("semester") String semester);

    @SQL("SELECT id, student_id AS studentId, course_id AS courseId, score AS score, semester FROM student_course WHERE student_id = #{studentId}")
    List<StudentCourse> selectByStudentId(@Param("studentId") int studentId);

    @SQL("SELECT id, student_id AS studentId, course_id AS courseId, score AS score, semester FROM student_course WHERE course_id = #{courseId}")
    List<StudentCourse> selectByCourseId(@Param("courseId") int courseId);

    @SQL("SELECT id, student_id AS studentId, course_id AS courseId, score AS score, semester FROM student_course")
    List<StudentCourse> selectAllStudentCourses();
}