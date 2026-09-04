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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@WebServlet("/add-timeslot")
public class addZeitslot extends HttpServlet {

    private static final int SLOT_DURATION = 15;

    private static final LocalTime OPENING_TIME =
            LocalTime.of(8, 0);

    private static final LocalTime CLOSING_TIME =
            LocalTime.of(18, 0);

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setCharacterEncoding("UTF-8");
        response.setContentType(
                "text/plain;charset=UTF-8"
        );

        HttpSession session =
                request.getSession(false);

        if (
            session == null
            || session.getAttribute("adminEmail") == null
        ) {
            sendError(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Sie sind nicht als Administrator eingeloggt."
            );
            return;
        }

        String centerValue =
                request.getParameter("Center_id");

        String startValue =
                request.getParameter("start_time");

        String endValue =
                request.getParameter("end_time");

        if (
            centerValue == null
            || centerValue.isBlank()
            || startValue == null
            || startValue.isBlank()
            || endValue == null
            || endValue.isBlank()
        ) {
            sendError(
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "Bitte übergeben Sie alle notwendigen Werte."
            );
            return;
        }

        try {
            int centerId =
                    Integer.parseInt(centerValue);

            LocalDateTime startTime =
                    LocalDateTime.parse(startValue);

            LocalDateTime endTime =
                    LocalDateTime.parse(endValue);

            if (centerId <= 0) {
                sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Ungültige Impfzentrum-ID."
                );
                return;
            }

            if (
                !startTime.toLocalDate()
                    .equals(endTime.toLocalDate())
            ) {
                sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Start- und Endzeit müssen "
                    + "am gleichen Tag liegen."
                );
                return;
            }

            if (!endTime.isAfter(startTime)) {
                sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Die Endzeit muss nach "
                    + "der Startzeit liegen."
                );
                return;
            }

            if (
                startTime.toLocalTime()
                    .isBefore(OPENING_TIME)
                || endTime.toLocalTime()
                    .isAfter(CLOSING_TIME)
            ) {
                sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Zeitslots sind nur zwischen "
                    + "08:00 und 18:00 Uhr erlaubt."
                );
                return;
            }

            if (startTime.isBefore(LocalDateTime.now())) {
                sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Die Startzeit liegt in der Vergangenheit."
                );
                return;
            }

            long duration =
                    Duration.between(
                        startTime,
                        endTime
                    ).toMinutes();

            boolean invalidInterval =
                    duration % SLOT_DURATION != 0
                    || startTime.getMinute()
                        % SLOT_DURATION != 0
                    || endTime.getMinute()
                        % SLOT_DURATION != 0
                    || startTime.getSecond() != 0
                    || endTime.getSecond() != 0;

            if (invalidInterval) {
                sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Die Zeiten müssen in "
                    + "15-Minuten-Schritten liegen."
                );
                return;
            }

            try (
                Connection connection =
                    DBUtil.getConnection()
            ) {
                connection.setTransactionIsolation(
                    Connection.TRANSACTION_SERIALIZABLE
                );

                connection.setAutoCommit(false);

                try {
                    /*
                     * Zuerst prüfen, ob einer der
                     * Zeitslots bereits existiert.
                     */
                    for (
                        LocalDateTime current = startTime;
                        current.isBefore(endTime);
                        current = current.plusMinutes(
                            SLOT_DURATION
                        )
                    ) {
                        LocalDateTime slotEnd =
                                current.plusMinutes(
                                    SLOT_DURATION
                                );

                        if (
                            DBUtil.doesTimeslotExist(
                                connection,
                                current,
                                slotEnd,
                                centerId
                            )
                        ) {
                            connection.rollback();

                            sendError(
                                response,
                                HttpServletResponse.SC_CONFLICT,
                                "Der Zeitslot "
                                + current.toLocalTime()
                                + " existiert bereits."
                            );
                            return;
                        }
                    }

                    int createdSlots = 0;

                    /*
                     * Den Zeitraum in einzelne
                     * 15-Minuten-Termine teilen.
                     */
                    for (
                        LocalDateTime current = startTime;
                        current.isBefore(endTime);
                        current = current.plusMinutes(
                            SLOT_DURATION
                        )
                    ) {
                        LocalDateTime slotEnd =
                                current.plusMinutes(
                                    SLOT_DURATION
                                );

                        DBUtil.insertTimeslot(
                            connection,
                            current,
                            slotEnd,
                            centerId
                        );

                        createdSlots++;
                    }

                    connection.commit();

                    response.setStatus(
                        HttpServletResponse.SC_CREATED
                    );

                    response.getWriter().print(
                        createdSlots
                        + " Zeitslots wurden erfolgreich "
                        + "hinzugefügt."
                    );

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }

        } catch (NumberFormatException e) {
            sendError(
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "Ungültige Impfzentrum-ID."
            );

        } catch (DateTimeParseException e) {
            sendError(
                response,
                HttpServletResponse.SC_BAD_REQUEST,
                "Ungültiges Datums- oder Zeitformat."
            );

        } catch (SQLException e) {
            e.printStackTrace();

            sendError(
                response,
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Datenbankfehler beim Speichern "
                + "der Zeitslots."
            );
        }
    }


    private void sendError(
            HttpServletResponse response,
            int status,
            String message
    ) throws IOException {

        response.setStatus(status);
        response.getWriter().print(message);
    }
}
