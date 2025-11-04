package hbv.example.Servlets;
import hbv.example.database.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/qrcode")
public class QRCodeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Die Buchungs-ID aus der URL extrahieren (z. B. /QRCode?buchungId=12345)
        String buchungIdString = request.getParameter("id");
        if (buchungIdString == null || buchungIdString.isEmpty()) {
            response.getWriter().println("Fehler: Buchungs-ID nicht gefunden.");
            return;
        }



        // Holen der Buchungsdetails aus der Datenbank
        try {
            int buchungId = Integer.parseInt(buchungIdString);


            String[] details= DBUtil.getBookingDetailsById(buchungId);

            if (details == null) {
//
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Buchung nicht gefunden");

            }
//
//            String vorname = bookingDetails[0];
//                String nachname = bookingDetails[1];
//                String centerName = bookingDetails[2];
//                String vaccineName = bookingDetails[3];
//                String appointmentDate = bookingDetails[4];
//
//                // Erstellen der Antwort-Seite
//                response.setContentType("text/html");
//                response.getWriter().println("<html><body>");
//                response.getWriter().println("<h1>Ihre Buchungsdetails</h1>");
//                response.getWriter().println("<p><b>Buchung ID:</b> " + buchungId + "</p>");
//                response.getWriter().println("<p><b>Name:</b> " + vorname + " " + nachname + "</p>");
//                response.getWriter().println("<p><b>Impfzentrum:</b> " + centerName + "</p>");
//                response.getWriter().println("<p><b>Impfstoff:</b> " + vaccineName + "</p>");
//                response.getWriter().println("<p><b>Datum:</b> " + appointmentDate.substring(0, 10) + "</p>");
//                response.getWriter().println("<p><b>Uhrzeit:</b> " + appointmentDate.substring(11) + "</p>");
//                response.getWriter().println("</body></html>");
//            } catch (NumberFormatException e) {
//            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ungültige Buchungs-ID");
//        } catch (Exception e) {
//            e.printStackTrace();
//            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }
//    }
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();

            out.println("<html><head><title>Buchungsdetails</title></head><body>");
            out.println("<h1>Impftermin Details</h1>");
            out.println("<p><b>ID:</b> " + buchungId + "</p>");
            out.println("<p><b>Name:</b> " + details[0] + " " + details[1] + "</p>");
            out.println("<p><b>Impfzentrum:</b> " + details[2] + "</p>");
            out.println("<p><b>Impfstoff:</b> " + details[3] + "</p>");
            out.println("<p><b>Datum:</b> " + details[4].substring(0,10) + "</p>");
            out.println("<p><b>Uhrzeit:</b> " + details[4].substring(11,16) + "</p>");
            out.println("</body></html>");

        } catch (NumberFormatException e) {
            response.sendError(400, "Ungültige Buchungs-ID");
        } catch (SQLException e) {
            response.sendError(500, "Datenbankfehler");
        } catch (Exception e) {
            response.sendError(500, "Serverfehler");
        }
    }





}