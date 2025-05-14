// CourseMapper.java
package mapper;

import entity.Course;
import annotations.Param;
import annotations.SQL;
import annotations.Where;
import java.util.List;

public interface CourseMapper {
    @SQL("SELECT * FROM course")
    List<Course> selectAllCourses();
    
    @Where("course_id = #{courseId}")
    Course selectById(@Param("courseId") Integer courseId);
    
    @SQL("SELECT * FROM course WHERE teacher_id = #{teacherId}")
    List<Course> selectCoursesByTeacher(@Param("teacherId") Integer teacherId);
    
    @SQL("INSERT INTO course (course_id, course_name, credit, teacher_id) VALUES (#{courseId}, #{courseName}, #{credit}, #{teacherId})")
    Course insertCourse(
        @Param("courseId") Integer courseId,
        @Param("courseName") String courseName,
        @Param("credit") Integer credit,
        @Param("teacherId") Integer teacherId
    );
    
    @Where("course_id = #{courseId}")
    @SQL("UPDATE course SET course_name = #{courseName}, credit = #{credit}, teacher_id = #{teacherId} WHERE course_id = #{courseId}")
    Course updateCourse(
        @Param("courseId") Integer courseId,
        @Param("courseName") String courseName,
        @Param("credit") Integer credit,
        @Param("teacherId") Integer teacherId
    );
    
    @Where("course_id = #{courseId}")
    @SQL("DELETE FROM course WHERE course_id = #{courseId}")
    Course deleteCourseById(@Param("courseId") Integer courseId);
}