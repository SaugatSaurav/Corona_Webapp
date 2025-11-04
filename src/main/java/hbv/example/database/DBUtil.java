package hbv.example.database;


import hbv.example.passwordhash.hashingpassword;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



public class DBUtil {


    private static final String URL = "jdbc:mysql://localhost:3307/project";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "@Subedi1";
    private static final int INITIAL_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 100;
    private static final List<Connection> connectionPool = new ArrayList<>(MAX_POOL_SIZE);

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            try {
                Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                connectionPool.add(connection);
            } catch (SQLException e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    public static Connection getConnection() throws SQLException {
//        System.out.println("Connecting to database");
//        return DriverManager.getConnection(URL, USERNAME, PASSWORD);

        try {
            Connection connection = null;
            synchronized (connectionPool) {
                if (!connectionPool.isEmpty()) {
                    connection = connectionPool.remove(0);
                } else if (connectionPool.size() < MAX_POOL_SIZE) {
                    connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                }
            }

            if (connection == null) {
                throw new RuntimeException("Timeout beim Warten auf eine Datenbankverbindung");
            }

            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Fehler beim Abrufen der Datenbankverbindung", e);
        }





    }


//FÜR ADMIN ------------------------------------------------------------------

    public static boolean isValidAdmin(Connection conn, String email, String password) {
        String Query = "Select * from admin where email = ? and password = ?";
        boolean isvalid=false;

        try (PreparedStatement preparedStatement = conn.prepareStatement(Query)) {


            preparedStatement.setString(1, email);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    isvalid = true;

//                    admin = new Admin();
//                    admin.setId(resultSet.getInt("id"));
//                    admin.setEmail(resultSet.getString("email"));
//                    admin.setPassword(resultSet.getString("password"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isvalid;
    }

    //FÜR USER--------------------------------------------------------------

    public static boolean isValidUser(Connection conn, String email, String password) throws IOException {
        // String query = "SELECT * FROM user WHERE Email = ? AND Password = ?";

        String query = "SELECT  password, salt FROM user WHERE email = ?";

        boolean isValidUser= false;

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, email);

            //   preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String storedHash = resultSet.getString("password");
                    String storedSalt = resultSet.getString("salt");


                    byte[] hashedPassword = hashingpassword.hashPassword(password, hashingpassword.fromHex(storedSalt));

                    // Vergleiche den generierten Hash mit dem gespeicherten Hash


                    if (hashingpassword.toHex(hashedPassword).equals(storedHash)) {

                        isValidUser = true;
//                        user = new User();
//                        user.setId(resultSet.getInt("id"));
//                        user.setFirstName(resultSet.getString("firstname"));
//                        user.setLastName(resultSet.getString("lastname"));
//                        user.setAddress(resultSet.getString("address"));
//                        user.setEmail(resultSet.getString("email"));
//                        user.setPassword(storedHash);


                    }
                    System.out.println("Query is running");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isValidUser;
    }

    public static boolean isEmail(Connection conn, String email) throws Exception {


        String query = "SELECT * FROM user WHERE email = ?";


        try (
               // Connection connection = DBUtil.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(query);
        ) {
            preparedStatement.setString(1, email);


            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public static int addUser(Connection conn, String firstname, String lastname, String address, String email, String password) throws Exception {


        byte[] salt = hashingpassword.generateSalt();
        byte[] hashedPassword = hashingpassword.hashPassword(password, salt);


        // String query = "insert into user(firstname,lastname,address,email,password) values (?,?,?,?,?)";
        String query = "insert into user(firstname,lastname,address,email,password,salt) values (?,?,?,?,?,?)";
        try (

             PreparedStatement preparedStatement = conn.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, firstname);
            preparedStatement.setString(2, lastname);
            preparedStatement.setString(3, address);
            preparedStatement.setString(4, email);
            preparedStatement.setString(5, hashingpassword.toHex(hashedPassword)); // Hash als Hex-String speichern
            preparedStatement.setString(6, hashingpassword.toHex(salt)); // Sa

            int rowsAffected = preparedStatement.executeUpdate();

            // Wenn ein Benutzer erfolgreich eingefügt wurde, hole die generierte ID
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Gibt die generierte ID des neuen Benutzers zurück
                    }
                }


    }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        return -1;   // Wenn etwas schiefgeht, gib -1 zurück


    }


    //FÜR VACCINATION CENTER---------------------------------------------------

    public static int insertVaccinationCenter(Connection connection, String name, String strasse, String stadt, String postal)
            throws SQLException {


        var pss = connection.prepareStatement(
                "INSERT INTO impfzentren(name, street, city, postalcode) VALUES(?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS);
        pss.setString(1, name);
        pss.setString(2, strasse);
        pss.setString(3, stadt);
        pss.setString(4, postal);
        pss.executeUpdate();
        try (var rs = pss.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return -1;
    }

//FÜR VACCINATION CENTER UND BUCHEN----------------------------------------------------

    public static List<JSONObject> Centerliste(Connection connection) throws SQLException {
        List<JSONObject> centers = new ArrayList<>();

        // SQL-Abfrage zum Abrufen der Daten
//        String query = "SELECT id, name, street, city, postalcode FROM impfzentren";
        String query = "SELECT * FROM impfzentren";

        try (PreparedStatement pss = connection.prepareStatement(query);
             ResultSet rs = pss.executeQuery()) {

            while (rs.next()) {
                // Erstellen eines JSON-Objekts für jedes Zentrum
                JSONObject center = new JSONObject();
                center.put("id", rs.getInt("id"));
                center.put("name", rs.getString("name"));
                center.put("strasse", rs.getString("street"));
                center.put("stadt", rs.getString("city"));
                center.put("postal", rs.getString("postalcode"));

                centers.add(center);
            }
        }

        return centers;
    }




//    public static List<Map<String, Object>> Centerliste(Connection connection) throws SQLException {
//        var pss = connection.prepareStatement("SELECT * FROM impfzentren");
//        var rs = pss.executeQuery();
//        List<Map<String, Object>> Vaccination_Centers = new ArrayList<>();
//        while (rs.next()) {
//            Map<String, Object> center = new HashMap<>();
//            center.put("id", rs.getInt("id"));
//            center.put("name", rs.getString("name"));
//            center.put("strasse", rs.getString("street"));
//            center.put("stadt", rs.getString("city"));
//            center.put("postal", rs.getString("postalcode"));
//
//            Vaccination_Centers.add(center);
//        }
//          //  var VaccinationCenters = new VaccinationCenters(
////                    rs.getInt("id"),
////                    rs.getString("name"),
////                    rs.getString("street"),
////                    rs.getString("city"),
////                    rs.getString("postalcode"));
////            Vaccination_Centers.add(VaccinationCenters);
//
//        return Vaccination_Centers;
//    }


    //FÜR VACCINE-----------------------------------------------------------------------

    public static int insertVaccines(Connection connection, String Name)
            throws SQLException {

        String Query = "Insert into Impfung(Name) values(?)";
        var pss = connection.prepareStatement(Query,
                Statement.RETURN_GENERATED_KEYS);
        pss.setString(1, Name);


        pss.executeUpdate();
        try (var rs = pss.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1); // Generierte ID zurückgeben
            }
        }

        return -1; // Falls keine ID generiert wurde
    }

    public static List<JSONObject> Vaccinesliste(Connection connection) throws SQLException {
        List<JSONObject> vaccines = new ArrayList<>();

        // SQL-Abfrage zum Abrufen der Impfstoffe
        String query = "SELECT id, name FROM Impfung";
        try (PreparedStatement pss = connection.prepareStatement(query);
             ResultSet rs = pss.executeQuery()) {

            while (rs.next()) {
                // Erstellen eines JSON-Objekts für jeden Impfstoff
                JSONObject vaccine = new JSONObject();
                vaccine.put("id", rs.getInt("id"));
                vaccine.put("name", rs.getString("name"));

                vaccines.add(vaccine);
            }
        }

        return vaccines;
    }




//    public static List<Map<String, Object>> Vaccinesliste(Connection connection) throws SQLException {
//
//        List<Map<String, Object>> vaccines = new ArrayList<>();
//        var pss = connection.prepareStatement("SELECT * FROM Impfung");
//        var rs = pss.executeQuery();
//
//        while (rs.next()) {
//            Map<String, Object> vaccine = new HashMap<>();
//                  vaccine.put("id",rs.getInt("id"));
//                    vaccine.put("name",rs.getString("Name"));
//            vaccines.add(vaccine);
//        }
//        return vaccines;
//    }


//FÜR IMPFZENTREN_IMPFUNG-------------------------------------------------------------

    public static void insertImpfzentrumfuerImpfung(Connection con, int impfzentrumId, int impfungId)
            throws SQLException {
        String sql = "INSERT INTO  impfzentren_impfung(Center_id, Vaccine_id) VALUES (?, ?)";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, impfzentrumId);
            pstmt.setInt(2, impfungId);
            pstmt.executeUpdate();
        }
    }


    //FÜR TIMESLOT------------------------------------------------------------------------


    public static boolean doesTimeslotExist(Connection con, LocalDateTime startTime, LocalDateTime endTime, int centerId) throws SQLException {
        String query = "SELECT COUNT(*) FROM zeitslot WHERE start_time = ? AND end_time = ? AND Center_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setObject(1, startTime);
            pstmt.setObject(2, endTime);
            pstmt.setInt(3, centerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }



    public static int insertTimeslot(Connection connection, LocalDateTime startTime, LocalDateTime endTime,
                                     int capacity, int CenterId) throws SQLException {

        // Hier wird die Existenz des Zeitslots überprüft, bevor wir ihn einfügen
        if (doesTimeslotExist(connection, startTime, endTime, CenterId)) {
            throw new SQLException("Zeitslot already exists");
        }

        try {
            // SQL-Abfrage zum Einfügen des Zeitslots
            var pss = connection.prepareStatement(
                    "INSERT INTO Zeitslot(start_time, end_time, capacity, Center_id) VALUES(?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            // Parameter setzen
            pss.setObject(1, startTime);
            pss.setObject(2, endTime);
            pss.setInt(3, capacity);
            pss.setInt(4, CenterId);

            // Abfrage ausführen
            pss.executeUpdate();

            // Rückgabe der generierten ID
            var rs = pss.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            // Wenn ein Fehler auftritt, werfen wir ihn einfach weiter
            throw e;
        }
        return -1; // Im Fall, dass keine ID generiert werden konnte
    }




//    public static int insertTimeslot(Connection connection, Zeitfenster timeslot)
//            throws SQLException {
//        try {
//            var pss = connection.prepareStatement(
//                    "INSERT INTO Zeitslot(start_time, end_time,capacity,Center_id) VALUES(?,?,?,?)",
//                    Statement.RETURN_GENERATED_KEYS);
//
//            pss.setObject(1, timeslot.getStartTime());
//            pss.setObject(2, timeslot.getEndTime());
//            pss.setInt(3, timeslot.getCapacity());
//            pss.setInt(4, timeslot.getCenter_id());
//
//            pss.executeUpdate();
//
//            var rs = pss.getGeneratedKeys();
//            if (rs.next()) {
//                return rs.getInt(1);
//            }
//        } catch (SQLException e) {
//
//            if (e.getErrorCode() == 1062) {
//                throw new SQLException("Zeitslot already exists");
//            }
//            throw e;
//        }
//        return -1;
//    }





//FÜR TIMESLOT UND BUCHUNG------------------------------------------------------------

public static List<JSONObject> getTimeslotsForDate(Connection connection, int Centerid, LocalDate date) throws SQLException {
    List<JSONObject> timeslots = new ArrayList<>();

    String query = "SELECT id, start_time, end_time, capacity FROM Zeitslot " +
            "WHERE Center_id = ? AND DATE(start_time) = ? AND capacity > 0 AND start_time > NOW() " +
            "AND id NOT IN (SELECT timeslot_id FROM Buchung WHERE timeslot_id IS NOT NULL) " +
            "ORDER BY start_time";

    try (PreparedStatement pss = connection.prepareStatement(query)) {
        pss.setInt(1, Centerid);
        pss.setObject(2, date);

        try (ResultSet rss = pss.executeQuery()) {
            while (rss.next()) {
                JSONObject timeslotJson = new JSONObject();
                timeslotJson.put("id", rss.getInt(1));
                timeslotJson.put("start_time", rss.getTimestamp(2).toLocalDateTime());
                timeslotJson.put("end_time", rss.getTimestamp(3).toLocalDateTime());
                timeslotJson.put("capacity", rss.getInt(4));
                timeslotJson.put("Center_id", Centerid);

                timeslots.add(timeslotJson);
            }
        }
    }

    return timeslots;
}



    //FÜR BUCHEN------------------------------------------------------------------------

//    public static List<Vaccine> getAvailableVaccines(Connection connection, int vaccinationCenterId)
//            throws SQLException {
//        var vList = new ArrayList<Vaccine>();
//        var pss = connection.prepareStatement(
//                "SELECT impfung.id, impfung.Name FROM impfzentren_impfung AS ii  JOIN impfung ON  ii.Vaccine_id=impfung.id WHERE ii.Center_id=?");
//        pss.setInt(1, vaccinationCenterId);
//        var rss = pss.executeQuery();
//        while (rss.next()) {
//            vList.add(new Vaccine(rss.getInt(1), rss.getString(2)));
//        }
//        return vList;
//    }

    public static JSONArray getAvailableVaccines(Connection connection, int vaccinationCenterId) throws SQLException {
        JSONArray vaccineList = new JSONArray();
        String query = "SELECT impfung.id, impfung.Name FROM impfzentren_impfung AS ii " +
                "JOIN impfung ON ii.Vaccine_id = impfung.id WHERE ii.Center_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, vaccinationCenterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject vaccineJson = new JSONObject();
                    vaccineJson.put("id", rs.getInt("id"));
                    vaccineJson.put("name", rs.getString("Name"));
                    vaccineList.put(vaccineJson);
                }
            }
        }
        return vaccineList;
    }




    public static int countDuplicateBookings(Connection con, String vorname, String nachname, String addresse) throws SQLException {
        String sql = "SELECT COUNT(*) FROM buchung WHERE vorname = ? AND nachname = ? AND addresse = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, vorname);
            pstmt.setString(2, nachname);
            pstmt.setString(3, addresse);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);  // Gibt die Anzahl der Buchungen zurück
            }
        }

        return 0;

    }


    public static int countUserBookings(Connection con, int userId) throws SQLException {
        int appointmentCount = 0;
        String query = "SELECT COUNT(*) AS count_appointment FROM buchung WHERE user_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, userId);
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt(1);  // Gibt die Anzahl der Buchungen zurück
//                }
//            }
//        }
//        return 0;
//
//    }
//}
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    appointmentCount = rs.getInt("count_appointment");
                }
            }
        }

        return appointmentCount;
    }

    public static boolean checkTimeslotExist(Connection con, int centerId, int timeslotId) throws SQLException {
        String query = "SELECT COUNT(*) FROM buchung WHERE  timeslot_id = ? AND  impfzentrum_id = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, timeslotId);
            pstmt.setInt(2, centerId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        }
    }



    //Count the Capacity
    public static int getCapacityFromAnyTimeslot(Connection con, int centerId) throws SQLException {
        String sql = "SELECT capacity FROM Zeitslot WHERE Center_id = ? AND  start_time > NOW() limit 1 ";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, centerId);

            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("capacity") : 0;
        }
    }

    // Count total Bookings
    public static int countBookingsForCenter(Connection con, int centerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Buchung WHERE impfzentrum_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, centerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public static LocalDateTime getTimeslot(Connection connection, int timeslotId) throws SQLException {
//        var pss = connection.prepareStatement(
//                "SELECT start_time, end_time, capacity,Center_id FROM Zeitslot WHERE id=?");
//        pss.setInt(1, timeslotId);
////        var rs = pss.executeQuery();
//        rs.next();
//        return new Zeitfenster(timeslotId, rs.getTimestamp(1).toLocalDateTime(),
//                rs.getTimestamp(2).toLocalDateTime(), rs.getInt(3), rs.getInt(4));
//    }
//, end_time, capacity,Center_id
        try (PreparedStatement ps = connection.prepareStatement("SELECT start_time FROM Zeitslot WHERE id=?")) {
            ps.setInt(1, timeslotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("start_time").toLocalDateTime();
                }
            }
        }
        System.out.println("Zeitslot nicht gefunden für ID: " + timeslotId);
        return null;  // Falls kein Ergebnis gefunden wird
    }



    public static int insertBuchung(Connection con,int userId, int centerId, int vaccineId, int timeslotId, String vorname, String nachname, String addresse, String date) {
        System.out.println("Date in DBUtil ist:" + date);

        String query = "INSERT INTO buchung(user_id, impfzentrum_id, impfstoff_id, timeslot_id, vorname, nachname, addresse, datum) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {


//            preparedStatement.setInt(1, buchung.getUserId());
//            preparedStatement.setInt(2, buchung.getCenterId());
//            preparedStatement.setInt(3, buchung.getVaccineId());
//            preparedStatement.setInt(4, buchung.getTimeslotId());
//            preparedStatement.setString(5, buchung.getVorname());
//            preparedStatement.setString(6, buchung.getNachname());
//            preparedStatement.setString(7, buchung.getAddresse());
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, centerId);
            preparedStatement.setInt(3, vaccineId);
            preparedStatement.setInt(4, timeslotId);
            preparedStatement.setString(5, vorname);
            preparedStatement.setString(6, nachname);
            preparedStatement.setString(7, addresse);
            preparedStatement.setString(8, date);
            preparedStatement.executeUpdate();
               System.out.println("Insert erfolgreich!");

            try (ResultSet rs = preparedStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Die generierte ID zurückgeben
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Speichern der Buchung: " + e.getMessage(), e);
        }

return -1;
    }




    //Extra hinzugefügte für PDF
    public static String getCenterNameById(int centerId) {
        String centerName = "Unbekanntes Zentrum";

        String query = "SELECT name FROM impfzentren WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, centerId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    centerName = rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return centerName;
    }


    public static String getVaccineNameById(int vaccineId) {
        String vaccineName = "Unbekanntes Vaccine";

        String query = "SELECT Name FROM impfung WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, vaccineId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    vaccineName = rs.getString("name");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return vaccineName;
    }





//    public static List<Appointment> getAppointments(Connection con, int userId) throws SQLException {
//        List<Appointment> appointments = new ArrayList<>();
//
//        String query = "SELECT b.id, b.impfzentrum_id, b.impfstoff_id, b.timeslot_id, "
//                + "b.vorname, b.nachname, b.addresse, "
//                + "i.name AS impfstoff, z.name AS impfzentrum, b.datum "
//                + "FROM buchung b "
//                + "JOIN impfung i ON b.impfstoff_id = i.id "
//                + "JOIN impfzentren z ON b.impfzentrum_id = z.id "
//                + "WHERE b.user_id = ?";
//        try (PreparedStatement ps = con.prepareStatement(query)) {
//            ps.setInt(1, userId);
//
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) {
//                    Buchung buchung = new Buchung(
//                            rs.getInt("id"),
//                            userId,
//                            rs.getInt("impfzentrum_id"),
//                            rs.getInt("impfstoff_id"),
//                            rs.getInt("timeslot_id"),
//                            rs.getString("vorname"),
//                            rs.getString("nachname"),
//                            rs.getString("addresse")
//                    );
//
//                    LocalDateTime startTime = rs.getTimestamp("datum").toLocalDateTime();
//                    LocalDateTime endTime = startTime.plusMinutes(15);
//
//
//                    appointments.add(new Appointment(
//                            rs.getInt("id"),
//                            buchung,
//                            rs.getString("impfzentrum"),
//                            rs.getString("impfstoff"),
//                            startTime,
//                            endTime
//                    ));
//                }
//            } catch (SQLException e) {
//
//                e.printStackTrace();
//                throw e;
//            }
//        }
//        return appointments;
//    }



    //FÜR APPOINTMENT-----------------------------------------------------------------------


    public static List<JSONObject> getAppointments(Connection con, int userId) throws SQLException {
        List<JSONObject> appointments = new ArrayList<>();

        String query = "SELECT b.id, b.impfzentrum_id, b.impfstoff_id, b.timeslot_id, "
                + "b.vorname, b.nachname, b.addresse, "
                + "i.name AS impfstoff, z.name AS impfzentrum, b.datum "
                + "FROM buchung b "
                + "JOIN impfung i ON b.impfstoff_id = i.id "
                + "JOIN impfzentren z ON b.impfzentrum_id = z.id "
                + "WHERE b.user_id = ?";

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JSONObject json = new JSONObject();
                    json.put("id", rs.getInt("id"));
                    json.put("vorname", rs.getString("vorname"));
                    json.put("nachname", rs.getString("nachname"));
                    json.put("addresse", rs.getString("addresse"));
                    json.put("impfstoff", rs.getString("impfstoff"));
                    json.put("impfzentrum", rs.getString("impfzentrum"));
                    json.put("startTime", rs.getTimestamp("datum").toLocalDateTime().toString());
                    json.put("endTime", rs.getTimestamp("datum").toLocalDateTime().plusMinutes(15).toString()); // Annahme: 15 Minuten

                    appointments.add(json);
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw e;
            }
        }
        return appointments;
    }





    public static boolean deleteappointment(Connection conn, int id) throws SQLException {

        String Query = "DELETE FROM buchung WHERE id = ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(Query)) {
            preparedStatement.setInt(1, id);
            int rowsAffected = preparedStatement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static String[] getBookingDetailsById(int buchungId) throws SQLException {
        String[] bookingDetails = new String[5];
        String query = "SELECT b.vorname, b.nachname,  i.name, v.Name, b.datum\n" +
                "FROM buchung b\n" +
                "JOIN impfzentren i ON b.impfzentrum_id = i.id\n" +
                "JOIN impfung v ON b.impfstoff_id = v.id\n" +
                "WHERE b.id= ?\n";

        try (Connection con = DBUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, buchungId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                bookingDetails[0] = rs.getString("vorname");
                bookingDetails[1] = rs.getString("nachname");
                bookingDetails[2] = rs.getString("i.name");
                bookingDetails[3] = rs.getString("v.Name");
                bookingDetails[4] = rs.getString("datum");

                System.out.println("Vorname: " + rs.getString("vorname"));
                System.out.println("Nachname: " + rs.getString("nachname"));
                System.out.println("Impfzentrum: " + rs.getString("i.name"));
                System.out.println("Impfstoff: " + rs.getString("v.Name"));
                System.out.println("Datum: " + rs.getString("datum"));
            }
        }
        return bookingDetails;
    }



}
