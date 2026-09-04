package hbv.web.Servlets;

import hbv.web.database.DBUtil;
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
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        boolean userLoggedIn =
                session != null
                && session.getAttribute("userId") != null;

        boolean adminLoggedIn =
                session != null
                && session.getAttribute("adminEmail") != null;

        if (!userLoggedIn && !adminLoggedIn) {
            response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
            );

            response.getWriter().print(
                "Sie sind nicht eingeloggt."
            );
            return;
        }

        String centerValue =
                request.getParameter("Center_id");

        String dateValue =
                request.getParameter("date");

        if (
            centerValue == null
            || centerValue.isBlank()
            || dateValue == null
            || dateValue.isBlank()
        ) {
            response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().print(
                "Center_id und Datum werden benötigt."
            );
            return;
        }

        try {
            int centerId =
                    Integer.parseInt(centerValue);

            LocalDate date =
                    LocalDate.parse(dateValue);

            try (
                Connection connection =
                    DBUtil.getConnection()
            ) {
                /*
                 * Alte ungebuchte Zeitslots
                 * automatisch entfernen.
                 */
                DBUtil.deleteExpiredUnusedTimeslots(
                    connection
                );

                List<JSONObject> timeslots =
                    DBUtil.getTimeslotsForDate(
                        connection,
                        centerId,
                        date
                    );

                response.setContentType(
                    "application/json;charset=UTF-8"
                );

                response.getWriter().print(
                    new JSONArray(timeslots)
                        .toString()
                );
            }

        } catch (NumberFormatException e) {
            response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().print(
                "Ungültige Impfzentrum-ID."
            );

        } catch (DateTimeParseException e) {
            response.setStatus(
                HttpServletResponse.SC_BAD_REQUEST
            );

            response.getWriter().print(
                "Ungültiges Datum."
            );

        } catch (Exception e) {
            e.printStackTrace();

            response.setStatus(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().print(
                "Fehler beim Laden der Zeitslots."
            );
        }
    }
}
