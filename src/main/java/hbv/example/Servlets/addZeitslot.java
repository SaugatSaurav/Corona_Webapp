package hbv.example.Servlets;

import  hbv.example.database.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;




    @WebServlet("/add-timeslot")
    public class addZeitslot extends HttpServlet {

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

            // Überprüfen, ob der Benutzer eingeloggt ist
            HttpSession session = req.getSession(false);
            if (session == null) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Nicht eingeloggt");
                return;
            }

            String CenterIdString = req.getParameter("Center_id");
            System.out.println("unparsed Center_id in addZeitslot.java ist: " + CenterIdString);

            if (CenterIdString == null) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Keine Center-ID gefunden");
                return;
            }

            int CenterId = Integer.parseInt(CenterIdString);
            System.out.println("parsed CenterId in addZeitslot.java ist: " + CenterId);

            int capacity;
            LocalDateTime startTime;
            LocalDateTime endTime;

            try {
                // Eingehende Parameter für Startzeit, Endzeit und Kapazität
                startTime = LocalDateTime.parse(req.getParameter("start_time"));
                endTime = LocalDateTime.parse(req.getParameter("end_time"));
                capacity = Integer.parseInt(req.getParameter("capacity"));

                // Überprüfen, ob die Endzeit nach der Startzeit liegt
                if (startTime.isAfter(endTime)) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                    resp.getWriter().println("Endzeit muss nach der Startzeit liegen!");
                    return;
                }

                // Daten in der Datenbank speichern
                try (Connection con = DBUtil.getConnection()) {
                    if (DBUtil.doesTimeslotExist(con, startTime, endTime, CenterId)) {
                        resp.sendError(HttpServletResponse.SC_CONFLICT); // 409: Konflikt
                        return;
                    }

                    // Zeitslot in der DB einfügen
                    DBUtil.insertTimeslot(con, startTime, endTime, capacity, CenterId);
                    resp.setStatus(HttpServletResponse.SC_CREATED); // 201: Erfolgreich
                } catch (SQLException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Datenbankfehler: " + e.getMessage());
                }
            } catch (DateTimeParseException e) {
                // Fehler beim Parsen der Zeit
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültiges Datumsformat für start_time oder end_time"); // 400: Ungültiges Format
            } catch (NumberFormatException e) {
                // Fehler beim Parsen der Kapazität
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Zahl für Kapazität"); // 400: Ungültige Zahl
            } catch (Exception e) {
                // Alle anderen Fehler
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Serverfehler: " + e.getMessage()); // 500: Allgemeiner Serverfehler
            }
        }
    }



