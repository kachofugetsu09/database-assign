// TeacherMapper.java
package mapper;

import annotations.Param;
import annotations.SQL;
import annotations.Where;
import entity.Teacher;

import java.util.List;

public interface TeacherMapper {
    @SQL("SELECT teacher_id AS teacherId, name, gender, title FROM teacher WHERE teacher_id = #{teacherId}")
    Teacher selectById(@Param("teacherId") int teacherId);

    @SQL("INSERT INTO teacher (name, gender, title) VALUES (#{name}, #{gender}, #{title})")
    Boolean insertTeacher(@Param("name") String name, 
                         @Param("gender") String gender, 
                         @Param("title") String title);
                         
    @SQL("SELECT teacher_id AS teacherId, name, gender, title FROM teacher ORDER BY teacher_id DESC LIMIT 1")
    Teacher selectLastInsertedTeacher();

    @SQL("DELETE FROM teacher WHERE teacher_id = #{teacherId}")
    Boolean deleteTeacherById(@Param("teacherId") int teacherId);

    @SQL("UPDATE teacher SET name = #{name}, gender = #{gender}, title = #{title} WHERE teacher_id = #{teacherId}")
    Boolean updateTeacher(@Param("teacherId") int teacherId, 
                         @Param("name") String name, 
                         @Param("gender") String gender, 
                         @Param("title") String title);

    @SQL("SELECT teacher_id AS teacherId, name, gender, title FROM teacher")
    List<Teacher> selectAllTeachers();
}