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

@WebServlet("/add-teacher")
public class AddTeacherServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        int age = Integer.parseInt(request.getParameter("age"));
        String subject = request.getParameter("subject");

        Teacher teacher = new Teacher();
        teacher.setName(name);
        teacher.setAge(age);
        teacher.setSubject(subject);

        TeacherManager manager = new TeacherManager();
        manager.addTeacher(teacher);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("{\"status\":\"success\"}");
        out.flush();
    }
}