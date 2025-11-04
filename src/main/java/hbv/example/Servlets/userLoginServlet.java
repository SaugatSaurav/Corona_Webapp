package hbv.example.Servlets;



import hbv.example.database.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/userLoginServlet")
public class userLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");


        try (Connection conn = DBUtil.getConnection()) {
//
            if (DBUtil.isValidUser(conn, email, password)) {

                String query = "SELECT id, firstname, lastname, address, email FROM user WHERE email = ?";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {

                            HttpSession session = request.getSession();
                            session.setAttribute("userId", rs.getInt("id"));
                            session.setAttribute("userFirstName", rs.getString("firstname"));
                            session.setAttribute("userLastName", rs.getString("lastname"));
                            session.setAttribute("userAddress", rs.getString("address"));
                            session.setAttribute("userEmail", rs.getString("email"));


                            response.sendRedirect("buchen.html");
                            return;


//
                        }
                    }

                }

            }
            response.sendRedirect("user_login.html?error=1");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
