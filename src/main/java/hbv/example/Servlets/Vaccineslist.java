package hbv.example.Servlets;


import hbv.example.database.DBUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.json.*;


import java.io.IOException;
import java.sql.Connection;
import java.util.List;


@WebServlet("/Vaccineslist")
public class Vaccineslist extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

List<JSONObject> allVaccines;
        try (Connection con = DBUtil.getConnection()) {
        // Abrufen der Liste der Impfstoffe als JSON-Objekte
        allVaccines = DBUtil.Vaccinesliste(con);
    } catch (Exception e) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        resp.getWriter().println("Internal Error");
        return;
    }

    // Wenn keine Impfstoffe gefunden wurden
        if (allVaccines.isEmpty()) {
        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        resp.getWriter().println("No vaccines found.");
        return;
    }

    // Konvertieren der Liste in ein JSON-Array
    JSONArray allVaccinesJson = new JSONArray(allVaccines);

    // Antwort als JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().println(allVaccinesJson);
}
}