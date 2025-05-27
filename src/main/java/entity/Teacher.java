package entity;

import annotations.Table;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(tableName = "teacher")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Teacher {
    @SerializedName("teacherId")
    private Integer teacherId;
    private String name;
    private String gender;
    private String title;

    @Override
    public String toString() {
        return "Teacher{" +
                "teacherId=" + teacherId +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}