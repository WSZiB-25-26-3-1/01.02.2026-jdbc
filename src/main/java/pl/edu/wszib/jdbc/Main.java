package pl.edu.wszib.jdbc;

import org.h2.tools.Server;
import pl.edu.wszib.jdbc.model.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        initDataBase();

        //Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection= DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1","sa","");
        //Connection connection= DriverManager.getConnection("jdbc:mysql://localhost:8080/baza1","","");

        String name = "Alice";
        String surname = "Johnson";
        String sql = "SELECT * FROM person WHERE name=? OR surname=?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, name);
        ps.setString(2, surname);
        ResultSet rs = ps.executeQuery();

        List<Person> persons = new ArrayList<>();
        while(rs.next()) {
            persons.add(new Person(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("password")));
        }

        System.out.println(persons);

        connection.close();

    }

    public static void initDataBase() throws SQLException {
        // Najpierw utwórz przykładowe dane w pamięci
        String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS person(id INT PRIMARY KEY, name VARCHAR(100), surname VARCHAR(100), password VARCHAR(100))");
                // poprawna składnia INSERT: podajemy listę kolumn lub używamy VALUES dla wszystkich kolumn
                st.execute("INSERT INTO person (id, name, surname, password) VALUES (1, 'Alice', 'Smith', 'password123')");
                st.execute("INSERT INTO person (id, name, surname, password) VALUES (2, 'Bob', 'Johnson', 'securePass!')");
                st.execute("INSERT INTO person (id, name, surname, password) VALUES (3, 'Charlie', 'Brown', 'charliePwd')");
            }
        }

        try {
            // Uruchom webową konsolę H2 (domyślnie http://localhost:8082)
            Server webServer = Server.createWebServer("-webPort", "8082", "-webAllowOthers");
            webServer.start();

            // Opcjonalnie uruchom serwer TCP, jeśli chcesz łączyć się z zewnątrz (np. z innych procesów)
            Server tcpServer = Server.createTcpServer("-tcpPort", "9092", "-tcpAllowOthers");
            tcpServer.start();

            System.out.println("Press ENTER to stop servers and exit...");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // Zamknij serwery
    //webServer.stop();
    //tcpServer.stop();
}
