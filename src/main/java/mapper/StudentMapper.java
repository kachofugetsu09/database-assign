// StudentMapper.java
package mapper;

import annotations.Param;
import annotations.SQL;
import annotations.Where;
import entity.Student;

import java.util.List;

public interface StudentMapper {
    @Where("student_id = #{studentId}")
    Student selectById(@Param("studentId") int studentId);

    Student insertStudent(@Param("name") String name,
                         @Param("gender") String gender,
                         @Param("age") int age,
                         @Param("enrollmentDate") String enrollmentDate);

    @Where("student_id = #{studentId}")
    Student deleteStudentById(@Param("studentId") int studentId);

    @Where("student_id = #{studentId}")
    Student updateStudent(@Param("studentId") int studentId,
                         @Param("name") String name,
                         @Param("gender") String gender,
                         @Param("age") int age,
                         @Param("enrollmentDate") String enrollmentDate);

    @SQL("SELECT * FROM student WHERE age BETWEEN #{minAge} AND #{maxAge}")
    List<Student> selectStudentsByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
}