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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet("/Zeitslot")
public class Zeitslot extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {



        String CenterIdString = request.getParameter("Center_id");
        System.out.println("unparsed Center_id in Zeitslot.java ist:" + CenterIdString);

        String dateString = request.getParameter("date");

        // Wenn das Datum nicht übergeben wird, fehlerhafte Anfrage zurückgeben
        if (dateString == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Missing request parameter(s)");
            return;
        }

        LocalDate date;
        int CenterId = 0;

        try {
            date = LocalDate.parse(dateString);
            System.out.println("Parsed date: " + date);
        } catch (DateTimeParseException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Malformed request parameter(s)");
            return;
        }

        if (CenterIdString == null) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().println("Nicht eingeloggt");
                return;
            }
        }else {

            try {
                CenterId = Integer.parseInt(CenterIdString);

                System.out.println("CenterId:" + CenterId);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Invalid Center_id format");
                return;
            }
        }
        // Abrufen der verfügbaren Zeitfenster aus der Datenbank
        List<JSONObject> timeslotsJson;
        try (Connection con = DBUtil.getConnection()) {
            timeslotsJson = DBUtil.getTimeslotsForDate(con, CenterId, date);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("Internal Error");
            return;
        }

        // Erstellen der JSON-Antwort
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(new JSONArray(timeslotsJson));
    }
}


