// StudentCourse.java
package entity;

import annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(tableName = "student_course")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StudentCourse {
    private Integer id;
    private Integer studentId;
    private Integer courseId;
    private Double score;
    private String semester;

    @Override
    public String toString() {
        return "StudentCourse{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", courseId=" + courseId +
                ", score=" + score +
                ", semester='" + semester + '\'' +
                '}';
    }
}