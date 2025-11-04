package hbv.example.Servlets;

import hbv.example.database.DBUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;


@WebServlet("/DeleteAppointment")
public class DeleteAppointment extends HttpServlet {
    @Override
protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        JSONObject objSend = new JSONObject();
        JSONObject data = new JSONObject();

        int status = 200;
        String message = "";

        String id =request.getParameter("id");




        try(Connection conn=DBUtil.getConnection()){
            boolean Ergebnis=DBUtil.deleteappointment(conn,Integer.parseInt(id));
if(Ergebnis){

        status = 200;
        message = "Erfolgreich Storniert! " +
                "Laden Sie bitte die Seite neue";
    } else {
        status = 500;
        message = "Etwas schief gelaufen!";
    }

} catch (Exception e) {
                e.printStackTrace();
                status = 500;
            }

            objSend.put("status", status);
            objSend.put("data", data);
            objSend.put("message", message);


        System.out.println(objSend.toString());
            response.setContentType("text/json;charset=UTF-8");
            PrintWriter out = response.getWriter();
            try {
                out.print(objSend);
            } finally {
                out.close();
            }


        }

    }
