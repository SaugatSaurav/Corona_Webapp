package hbv.example.Servlets;

import hbv.example.database.DBUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/VerfügbareVaccineslist")
public class VerfügbareVaccineslist extends HttpServlet {
   @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
       String CenterIdString = req.getParameter("Center_id");
       int CenterId;
       JSONArray vaccinesJson = new JSONArray();


       try {
           if (CenterIdString == null) {

                   res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                   res.getWriter().println("Missing request parameter");
                   return;
               }
            else {
               CenterId = Integer.parseInt(CenterIdString);
           }
       } catch (NumberFormatException e) {
           res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
           res.getWriter().println("Malformed request parameter");
           return;
       }
       try (Connection con = DBUtil.getConnection()) {
           vaccinesJson = DBUtil.getAvailableVaccines(con, CenterId);
       } catch (SQLException e) {
           res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
           res.getWriter().println("Interner Fehler");
           return;
       }





//       } catch (Exception e) {
//
//           res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//           res.getWriter().println("Interner Fehler");
//           return;
//       }
//       vaccinesJson = new JSONArray();
//       for (var v : vaccines) {
//           var vJson = new JSONObject();
//           vJson.put("id", v.getId());
//           vJson.put("name", v.getName());
//           vaccinesJson.put(vJson);
//       }
       res.setContentType("application/json");
       res.setCharacterEncoding("UTF-8");
       res.getWriter().println(vaccinesJson);

    }
}
