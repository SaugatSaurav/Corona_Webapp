package hbv.example.Servlets;

import hbv.example.database.DBUtil;
import java.io.IOException;
import java.sql.*;
import java.util.List;
import org.json.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/Centerliste")
public class Centerliste extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


        List<JSONObject> allCenters;
        try (Connection con = DBUtil.getConnection()) {
            allCenters = DBUtil.Centerliste(con); // Abruf der Impfzentren als Liste von JSON-Objekten
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Internal Error");
            return;
        }

        // Wenn keine Impfzentren gefunden wurden
        if (allCenters.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().println("No vaccination centers found.");
            return;
        }

        // Konvertieren der Liste in ein JSON-Array
        JSONArray allCentersJson = new JSONArray(allCenters);

        // Antwort als JSON senden
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println(allCentersJson);
    }
}