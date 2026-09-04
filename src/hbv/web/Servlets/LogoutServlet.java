package hbv.web.Servlets;


import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    

    @Override
protected void doGet(
        HttpServletRequest req,
        HttpServletResponse resp
) throws IOException {

    HttpSession session = req.getSession(false);

    boolean wasAdmin =
            session != null
            && session.getAttribute("adminEmail") != null;

    if (session != null) {
        session.invalidate();
    }

    if (wasAdmin) {
        resp.sendRedirect("admin_login.html");
    } else {
        resp.sendRedirect("user_login.html");
    }
  }
}
  
 

