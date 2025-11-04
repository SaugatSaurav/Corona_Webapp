package hbv.example.utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.ByteArrayOutputStream;


public class PDFGenerator {

    public static ByteArrayOutputStream generatePdf(
     int buchungId,String vorname, String nachname,
                String center, String vaccine,
                String date,String time){
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Titel
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD);
            Paragraph title = new Paragraph("Buchungsbestätigung", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Anrede
            Font textFont = new Font(Font.FontFamily.HELVETICA, 12);
            document.add(new Paragraph("Sehr geehrte/r " + vorname + " " + nachname + ",", textFont));
            document.add(new Paragraph("Ihre Impfung wurde erfolgreich gebucht. "
                    + "Bitte lesen Sie die folgenden Details sorgfältig durch.", textFont));

            // Trennlinie
            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // Persönliche Daten
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
            document.add(new Paragraph("Persönliche Daten", sectionFont));

            document.add(new Paragraph("BuchungId: " +buchungId, textFont));
            document.add(new Paragraph("Name: " + vorname + " " + nachname, textFont));
            document.add(new Paragraph("Impfzentrum: " + center, textFont));
            document.add(new Paragraph("Impfstoff: " + vaccine, textFont));
            document.add(new Paragraph("Termin_Datum: " + date));
            document.add(new Paragraph("Termin_Zeit: " +time));

            // Trennlinie
            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // Wichtige Hinweise
            document.add(new Paragraph("Wichtige Hinweise", sectionFont));

            List list = new List(List.UNORDERED);
            list.setListSymbol("-");
            list.add(new ListItem("Bitte erscheinen Sie pünktlich zu Ihrem Termin", textFont));
            list.add(new ListItem("Bringen Sie Ihren Personalausweis oder Reisepass mit", textFont));
            list.add(new ListItem("Falls verfügbar, bringen Sie bitte Ihren Impfpass mit", textFont));
            list.add(new ListItem("Tragen Sie eine Maske und halten Sie den Sicherheitsabstand ein", textFont));
            document.add(list);

            // Trennlinie
            document.add(Chunk.NEWLINE);
            document.add(new LineSeparator());
            document.add(Chunk.NEWLINE);

            // Schlusstext
            document.add(new Paragraph("Vielen Dank für Ihre Buchung.\n"
                    + "Wir freuen uns, Sie bald im Impfzentrum begrüßen zu dürfen.", textFont));

            Font italicFont = new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC);
            document.add(new Paragraph("Mit freundlichen Grüßen,\nIhr Impfzentrum-Team", italicFont));

            document.close();


            return outputStream;
        } catch (DocumentException e) {
            throw new RuntimeException("Fehler beim Erstellen des PDFs", e);
        }
    }
}
