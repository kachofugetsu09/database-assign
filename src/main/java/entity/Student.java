package entity;

import annotations.Table;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Table(tableName = "student")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    // 确保JSON序列化时使用studentId字段名
    @SerializedName("studentId")
    private Integer studentId;

    private String name;
    private String gender;
    private Integer age;
    private Date enrollmentDate;

    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", age=" + age +
                ", enrollmentDate=" + enrollmentDate +
                '}';
    }
}
