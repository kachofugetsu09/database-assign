package com.example.teacherms.dao;

import com.example.teacherms.model.Teacher;
import com.example.teacherms.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TeacherManager {
    public void addTeacher(Teacher teacher) {
        String sql = "INSERT INTO teachers (name, age, subject) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacher.getName());
            pstmt.setInt(2, teacher.getAge());
            pstmt.setString(3, teacher.getSubject());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTeacher(Teacher teacher) {
        String sql = "UPDATE teachers SET name = ?, age = ?, subject = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacher.getName());
            pstmt.setInt(2, teacher.getAge());
            pstmt.setString(3, teacher.getSubject());
            pstmt.setInt(4, teacher.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteTeacher(int id) {
        String sql = "DELETE FROM teachers WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT * FROM teachers";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Teacher teacher = new Teacher();
                teacher.setId(rs.getInt("id"));
                teacher.setName(rs.getString("name"));
                teacher.setAge(rs.getInt("age"));
                teacher.setSubject(rs.getString("subject"));
                teachers.add(teacher);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return teachers;
    }
}