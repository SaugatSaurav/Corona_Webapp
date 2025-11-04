package hbv.example.Servlets;

import java.io.IOException;
import java.sql.*;

import hbv.example.database.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;


@WebServlet("/Centers")
public class Centers extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws  IOException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.sendRedirect("user_login.html");
            return;
        }

        String name = request.getParameter("name");
        String strasse = request.getParameter("strasse");
        String stadt = request.getParameter("stadt");
        String postal = request.getParameter("postal");



        try (Connection connection = DBUtil.getConnection()) {
            int id =
                    DBUtil.insertVaccinationCenter(connection, name, strasse, stadt, postal);


            if (id != -1) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Error: Unable to insert center.");
            }

        }  catch (Exception e) {
            e.printStackTrace(); // Für Debugging
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error: " + e.getMessage());
        }
    }
}
