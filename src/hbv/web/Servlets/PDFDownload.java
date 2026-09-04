package hbv.web.Servlets;


import hbv.web.database.DBUtil;
import hbv.web.utils.PDFGenerator;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import hbv.web.utils.QRCodeGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;


@WebServlet("/PDF")
public class PDFDownload extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect("user_login.html");
            return;
        }

        String appointmentid = request.getParameter("id");

        try {
            int appointmentId = Integer.parseInt(appointmentid);
            // Datenbankverbindung und Abfrage
            String[] bookingDetails = DBUtil.getBookingDetailsById(appointmentId);

            if (bookingDetails == null) {

                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Buchung nicht gefunden");
                return;
            }

            String vorname = bookingDetails[0];
            String nachname = bookingDetails[1];
            String centerName = bookingDetails[2];
            String vaccineName = bookingDetails[3];
            String appointmentDate = bookingDetails[4];


            try {
                String qrData ="https://informatik.hs-bremerhaven.de/docker-swe3-2023-08-java/"+ "qrcode?id="+ appointmentId;

            ByteArrayOutputStream qrCodeData =QRCodeGenerator.generateQRCode(qrData);

            ByteArrayOutputStream pdfData =PDFGenerator.generatePdf(
                appointmentId,
                vorname,
                nachname,
                centerName,
                vaccineName,
                appointmentDate.substring(0, 10),
                appointmentDate.substring(11),
                qrCodeData
        );
                
                
                
                
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=appointment_" + appointmentId + ".pdf");
                response.getOutputStream().write(pdfData.toByteArray());
            } catch (Exception e) {
                e.printStackTrace();  
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Fehler bei der PDF-Erstellung");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
