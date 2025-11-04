package hbv.example.Servlets;


import hbv.example.database.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

@WebServlet("/appointment")
public class Getappointment extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws  IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("user_login.html");
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");

        List<JSONObject> appointments;

        try (Connection con = DBUtil.getConnection()) {
            appointments = DBUtil.getAppointments(con, userId);


            System.out.println("Anzahl der gefundenen Buchungen: " + appointments.size());

            JSONArray jsonArray = new JSONArray();
            for (JSONObject appointment : appointments) {
                jsonArray.put(appointment);
            }
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print(jsonArray.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Internal Error");
        }
    }
}



