// Course.java
package entity;

import annotations.Table;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(tableName = "course")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Course {
    @SerializedName("courseId")
    private Integer courseId;
    
    @SerializedName("courseName")
    private String courseName;
    
    @SerializedName("credit")
    private Integer credit;
    
    @SerializedName("teacherId")
    private Integer teacherId;

    @Override
    public String toString() {
        return "Course{" +
                "courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", credit=" + credit +
                ", teacherId=" + teacherId +
                '}';
    }
}