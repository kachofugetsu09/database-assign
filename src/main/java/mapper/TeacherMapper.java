// TeacherMapper.java
package mapper;

import annotations.Param;
import annotations.SQL;
import annotations.Where;
import entity.Teacher;

import java.util.List;

public interface TeacherMapper {
    @Where("teacher_id = #{teacherId}")
    Teacher selectById(@Param("teacherId") int teacherId);

    Teacher insertTeacher(@Param("name") String name, 
                         @Param("gender") Character gender, 
                         @Param("title") String title);

    @Where("teacher_id = #{teacherId}")
    Teacher deleteTeacherById(@Param("teacherId") int teacherId);

    @Where("teacher_id = #{teacherId}")
    Teacher updateTeacher(@Param("teacherId") int teacherId, 
                         @Param("name") String name, 
                         @Param("gender") Character gender, 
                         @Param("title") String title);

    @SQL("SELECT * FROM teacher")
    List<Teacher> selectAllTeachers();
}