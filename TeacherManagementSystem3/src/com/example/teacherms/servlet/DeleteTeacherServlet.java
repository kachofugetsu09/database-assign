package com.example.teacherms.servlet;

import com.example.teacherms.dao.TeacherManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/delete-teacher")
public class DeleteTeacherServlet extends HttpServlet {
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));

        TeacherManager manager = new TeacherManager();
        manager.deleteTeacher(id);

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("{\"status\":\"success\"}");
        out.flush();
    }
}