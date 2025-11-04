package hbv.example.Servlets;

import hbv.example.database.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


@WebServlet("/Impfzentrum_impfung")
public class Impfzentrum_impfung extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect("user_login.html");
            return;
        }

            String Impfzentrum_id = request.getParameter("Impfzentrum_id");
            String Impfung_id = request.getParameter("Impfung_id");


            System.out.println("Impfzentrum_id" + Impfzentrum_id);
        System.out.println("Impfung_id" + Impfung_id);


        int impfzentrum_id = Integer.parseInt(request.getParameter("Impfzentrum_id"));
        int impfung_id = Integer.parseInt(request.getParameter("Impfung_id"));

        System.out.println("Nachdem prased Impfzentrum" + impfzentrum_id);
        System.out.println("Nachdem prased Impfung" + impfung_id);

        try(Connection conn= DBUtil.getConnection()){
            DBUtil.insertImpfzentrumfuerImpfung(conn,impfzentrum_id,impfung_id);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("Zuordnung erfolgreich gespeichert");

        }
        catch (SQLException e) {
            // Überprüfe, ob es sich um einen Duplikatfehler handelt
            if (e.getErrorCode() == 1062) { // Fehlercode für Duplikat
                response.setStatus(HttpServletResponse.SC_CONFLICT); // 409: Duplikat
                response.getWriter().println("Diese Zuordnung existiert bereits.");
            } else {
                // Andere SQL-Fehler behandeln
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Datenbankfehler: " + e.getMessage());
            }
        }


        catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Error: " + e.getMessage());
        }
    }

    }

