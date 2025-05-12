package com.example.teacherms.servlet;

import com.example.teacherms.dao.TeacherManager;
import com.example.teacherms.model.Teacher;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/get-teachers")
public class GetTeachersServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        TeacherManager manager = new TeacherManager();
        List<Teacher> teachers = manager.getAllTeachers();

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("[");
        for (int i = 0; i < teachers.size(); i++) {
            Teacher teacher = teachers.get(i);
            out.print("{\"id\":\"" + teacher.getId() + "\",");
            out.print("\"name\":\"" + teacher.getName() + "\",");
            out.print("\"age\":\"" + teacher.getAge() + "\",");
            out.print("\"subject\":\"" + teacher.getSubject() + "\"}");
            if (i < teachers.size() - 1) {
                out.print(",");
            }
        }
        out.print("]");
        out.flush();
    }
}