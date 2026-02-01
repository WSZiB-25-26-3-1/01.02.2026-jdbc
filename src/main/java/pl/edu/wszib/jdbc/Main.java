package pl.edu.wszib.jdbc;

import org.h2.tools.Server;
import pl.edu.wszib.jdbc.model.Person;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Main {
    public  static Connection connection = null;

    public static void main(String[] args) throws Exception {
        initDataBase();
        connect();

        Person newPerson = new Person(0, "Diana", "Prince", "wonderWoman");
        System.out.println("New person to save: " + newPerson);
        Person actual = saveUser(newPerson);
        System.out.println("Saved person: " + actual);

        deleteUser(2);

        List<Person> persons = getAllPersons();
        System.out.println(persons);

        Optional<Person> person = getPersonById(3);
        person.ifPresent(System.out::println);

        person.get().setName("Janusz");
        Person updated = updateUser(person.get());
        System.out.println("Updated person: " + updated);

        getPersonById(3).ifPresent(System.out::println);

        disconnect();
    }

    public static void connect() throws SQLException {
        //Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1","sa","");
        //Connection connection= DriverManager.getConnection("jdbc:mysql://localhost:8080/baza1","","");
    }

    public static void disconnect() throws SQLException {
        connection.close();
    }

    public static List<Person> getAllPersons() throws SQLException {
        String sql = "SELECT * FROM person";
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        List<Person> persons = new ArrayList<>();
        while(rs.next()) {
            persons.add(new Person(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("password")));
        }

        return persons;
    }

    public static Optional<Person> getPersonById(int id) throws SQLException {
        String sql = "SELECT * FROM person WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if(rs.next()) {
            return Optional.of(new Person(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("password")));
        }

        return Optional.empty();
    }

    public static Person saveUser(Person person) throws SQLException {
        String sql = "INSERT INTO person (name, surname, password) VALUES (?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, person.getName());
        ps.setString(2, person.getSurname());
        ps.setString(3, person.getPassword());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        keys.next();
        int newId = keys.getInt(1);

        person.setId(newId);
        return person;
    }

    public static Person updateUser(Person person) throws SQLException {
        String sql = "UPDATE person SET name = ?, surname = ?, password = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, person.getName());
        ps.setString(2, person.getSurname());
        ps.setString(3, person.getPassword());
        ps.setInt(4, person.getId());
        ps.executeUpdate();

        return person;
    }

    public static void deleteUser(int id) throws SQLException {
        String sql = "DELETE FROM person WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    public static void initDataBase() throws SQLException {
        // Najpierw utwórz przykładowe dane w pamięci
        String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(url, "sa", "")) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS person(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(100), surname VARCHAR(100), password VARCHAR(100))");
                // poprawna składnia INSERT: podajemy listę kolumn lub używamy VALUES dla wszystkich kolumn
                st.execute("INSERT INTO person (name, surname, password) VALUES ('Alice', 'Smith', 'password123')");
                st.execute("INSERT INTO person (name, surname, password) VALUES ('Bob', 'Johnson', 'securePass!')");
                st.execute("INSERT INTO person (name, surname, password) VALUES ('Charlie', 'Brown', 'charliePwd')");
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
