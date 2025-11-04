package hbv.example.Servlets;


import hbv.example.database.DBUtil;
import hbv.example.utils.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;



import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;

//Extra
import hbv.example.utils.*;

@WebServlet("/Buchen")
public class Buchen extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("user_login.html");
            return;
        } else {
            String requestBody = getRequestBody(request.getReader());
            JSONObject requestJson = new JSONObject(requestBody);

//        String UseridString= (String) session.getAttribute("userId");
//        int userId = Integer.parseInt(UseridString);

            Integer userId = (Integer) session.getAttribute("userId");
            String firstName = (String) session.getAttribute("userFirstName");
            String lastName = (String) session.getAttribute("userLastName");
            String address = (String) session.getAttribute("userAddress");
            String email = (String) session.getAttribute("userEmail");


            System.out.println("Userid:" + userId);
            System.out.println("FirstName:" + firstName);
            System.out.println("LastName:" + lastName);
            System.out.println("Address:" + address);
            System.out.println("Email:" + email);

            String vorname = requestJson.has("vorname") && !requestJson.getString("vorname").isEmpty()
                    ? requestJson.getString("vorname")
                    : firstName;

            String nachname = requestJson.has("nachname") && !requestJson.getString("nachname").isEmpty()
                    ? requestJson.getString("nachname")
                    : lastName;

            String addresse = requestJson.has("addresse") && !requestJson.getString("addresse").isEmpty()
                    ? requestJson.getString("addresse")
                    : address;

            int centerId = requestJson.getInt("centerId");
            int timeslotId = requestJson.getInt("timeslotId");
            int vaccineId = requestJson.getInt("vaccineId");


            try (Connection con = DBUtil.getConnection()) {
                con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
                con.setAutoCommit(false);


                int duplicateCount = DBUtil.countDuplicateBookings(con, vorname, nachname, addresse);
                if (duplicateCount >= 1) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT); //409
                    response.setContentType("text/plain");
                    response.getWriter().println("personal_data_conflict");
                    con.rollback();
                    return;
                }


                int bookingCount = DBUtil.countUserBookings(con, userId);
                System.out.println("Booking count: " + bookingCount);


                if (bookingCount >= 4) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);  // 403 Forbidden
                    response.getWriter().println("Sie dürfen nicht mehr als 4 Mal den Termin buchen.");
                    con.rollback();
                    return;
                }


                if (DBUtil.checkTimeslotExist(con, centerId, timeslotId)) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.setContentType("text/plain"); // Vor response.getWriter()
                    response.getWriter().println("timeslot_already_booked");
                    con.rollback();
                    return;
                }


                int centerCapacity = DBUtil.getCapacityFromAnyTimeslot(con, centerId);
                System.out.println(centerCapacity);


                int bookedInCenter = DBUtil.countBookingsForCenter(con, centerId);
                System.out.println(bookedInCenter);

                if (bookedInCenter >= centerCapacity) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.getWriter().println("center_capacity_full");
                    con.rollback();
                    return;
                }


                LocalDateTime slotStartTime = DBUtil.getTimeslot(con, timeslotId);
                String appointmentDate = slotStartTime.toString();

                int buchungId = DBUtil.insertBuchung(con, userId, centerId, vaccineId, timeslotId, vorname, nachname, addresse, appointmentDate);
                System.out.println("Buchung ID: " + buchungId);
                con.commit();
                System.out.println("Transaktion erfolgreich commitet.");


                String centerName = DBUtil.getCenterNameById(centerId);
                if (centerName == null || centerName.isEmpty()) {
                    centerName = "Unbekanntes Impfzentrum";
                }

                String vaccineName = DBUtil.getVaccineNameById(vaccineId);
                if (vaccineName == null || vaccineName.isEmpty()) {
                    vaccineName = "Unbekanntes Vaccine";
                }


                //bis hier

//            session.setAttribute("vorname", vorname);
//            session.setAttribute("nachname", nachname);
//            session.setAttribute("addresse", addresse);
//            session.setAttribute("centerName", centerName);
//            session.setAttribute("vaccineName", vaccineName);
//            session.setAttribute("appointmentDate", appointmentDate);
//            session.setAttribute("email", email);
//            session.setAttribute("buchungId", buchungId);


                //  PDF für die Buchungsbestätigung generieren
                //  Erstellen des PDF mit Buchungsdetails
                ByteArrayOutputStream pdfData = PDFGenerator.generatePdf(buchungId, vorname, nachname, centerName, vaccineName, appointmentDate.substring(0, 10), appointmentDate.substring(11)
                );


////  Generierung des QR Codes mit Termin-URL
//            String qrData =
//                    "Buchung ID: " + buchungId + "\n" +
//                            "Name: " + vorname + " " + nachname + "\n" +
//                            "Adresse: " + addresse + "\n" +
//                            "Impfzentrum: " + centerName + "\n" +
//                            "Impfstoff: " + vaccineName + "\n" +
//                            "Datum: " + appointmentDate.substring(0, 10) + "\n" +
//                            "Uhrzeit: " + appointmentDate.substring(11);


                //   String qrData = "/qrcode?id=" + buchungId;  // Hier wird die Buchungs-ID in der URL verwendet


                // QR-Code mit vollständiger URL generieren
                // String baseUrl = "/qrcode"; // Anpassen!
                //String qrData = baseUrl + "qrcode?id=" + buchungId;


                String qrData = "/qrcode?id=" + buchungId;

                ByteArrayOutputStream qrCodeData = QRCodeGenerator.generateQRCode(qrData);

// E-Mail senden mit PDF und QR-Code
//            EmailService.sendEmailWithAttachments(
//                    email,
//                    "Impftermin Bestätigung",
//                    "Sehr geehrte/r " + vorname + " " + nachname + ",\n\n"
//                            + "anbei finden Sie Ihre Buchungsbestätigung.",
//                    pdfData.toByteArray(),
//                    qrCodeData.toByteArray()
//            );
//
//// QR-Code generieren (direkt in Buchen.java)
//            ByteArrayOutputStream qrCodeData = QRCodeGenerator.generateQRCode(qrData);

// E-Mail mit PDF und QR-Code senden
                EmailService.sendEmailWithAttachments(email, firstName, lastName, vorname, nachname, pdfData, qrCodeData);


                response.setStatus(HttpServletResponse.SC_OK);


            } catch (Exception e) {

                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Fehler bei der Buchung: " + e.getMessage());
            }
        }
    }


    private String getRequestBody(BufferedReader reader) throws IOException {
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        return requestBody.toString();
    }
}