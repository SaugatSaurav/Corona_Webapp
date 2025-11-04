package hbv.example.Servlets;


import hbv.example.database.DBUtil;
import hbv.example.utils.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/registrierenServlet")
public class registrierenServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String firstname = request.getParameter("vname");
        String lastname = request.getParameter("nname");
        String address = request.getParameter("add");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String password2 = request.getParameter("password2");



     //   User user = new User(email, password, firstname, lastname, address);

        try (Connection conn = DBUtil.getConnection()) {



            if (DBUtil.isEmail(conn, email)) {
                response.sendRedirect("registrieren.html?error=2");
                return;
            }
            if (!password.equals(password2)) {
                response.sendRedirect("registrieren.html?error=4");
                return;
            }


           int  Useradd = DBUtil.addUser(conn, firstname, lastname, address, email, password);




            if (Useradd > 0) {
                HttpSession session = request.getSession();

                session.setAttribute("userId", Useradd);
                session.setAttribute("userFirstName", firstname);
                session.setAttribute("userLastName", lastname);
                session.setAttribute("userAddress", address);
                session.setAttribute("userEmail", email);



                //Von hier hinzugefügt
               EmailService.sendEmail(email, "Bestätigung der Registrierung",
                        "Hallo " + firstname +lastname+ "!\nIhr Konto wurde erfolgreich erstellt. Jetzt können Sie mit Ihre E-mail und Password einloggen");

                //bis hier

                response.sendRedirect("registrieren.html?error=3"); // Weiterleitung bei Erfolg
            } else {
                response.sendRedirect("registrieren.html?error=1"); // Weiterleitung bei Fehler
            }

        } catch (SQLException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);


        }
    }
}




