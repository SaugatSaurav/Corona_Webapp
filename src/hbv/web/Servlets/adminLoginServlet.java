package hbv.web.Servlets;


import hbv.web.database.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


@WebServlet("/adminLoginServlet")
public class adminLoginServlet  extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws  IOException {


        String email = request.getParameter("email");
        String password = request.getParameter("password");




        try(Connection conn= DBUtil.getConnection()){
            boolean isValidAdmin = DBUtil.isValidAdmin(conn, email, password);

            if (isValidAdmin) {


            HttpSession oldSession = request.getSession(false);

            if (oldSession != null) {
               oldSession.invalidate();
              }

           HttpSession session = request.getSession(true);
              session.setMaxInactiveInterval(30 * 60);

              session.setAttribute("adminEmail", email);

                response.sendRedirect("Admin_dashboard.html");

            } else {
                response.sendRedirect("admin_login.html?error=1");
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }



    }
}



