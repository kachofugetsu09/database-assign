package entity;

import annotations.Table;
import com.google.gson.annotations.SerializedName;
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
    
    @SerializedName("studentId")
    private Integer studentId;
    
    @SerializedName("courseId")
    private Integer courseId;
    
    @SerializedName("score")
    private Integer score;
    
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