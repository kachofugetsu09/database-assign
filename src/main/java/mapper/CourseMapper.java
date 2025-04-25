// CourseMapper.java
package mapper;

import annotations.Param;
import annotations.SQL;
import annotations.Where;
import entity.Course;

import java.util.List;

public interface CourseMapper {
    @Where("course_id = #{courseId}")
    Course selectById(@Param("courseId") int courseId);

    Course insertCourse(@Param("courseName") String courseName, 
                       @Param("credit") int credit, 
                       @Param("teacherId") Integer teacherId);

    @Where("course_id = #{courseId}")
    Course deleteCourseById(@Param("courseId") int courseId);

    @Where("course_id = #{courseId}")
    Course updateCourse(@Param("courseId") int courseId, 
                       @Param("courseName") String courseName, 
                       @Param("credit") int credit, 
                       @Param("teacherId") Integer teacherId);

    @SQL("SELECT * FROM course WHERE teacher_id = #{teacherId}")
    List<Course> selectCoursesByTeacher(@Param("teacherId") int teacherId);
}