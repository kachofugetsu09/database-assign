package entity;

import annotations.Table;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;

@Table(tableName = "student")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Student {
    @SerializedName("studentId")
    private Integer studentId;

    private String name;
    private String gender;
    private Integer age;
    @SerializedName("enrollmentDate")
    private Date enrollmentDate;  // Gson 会自动处理

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = enrollmentDate != null ? sdf.format(enrollmentDate) : "null";

        return "Student{" +
                "studentId=" + studentId +
                ", name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", enrollmentDate=" + dateStr +
                '}';
    }
}