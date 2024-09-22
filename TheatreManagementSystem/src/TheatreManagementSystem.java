import java.sql.*;
import java.util.Scanner;

public class TheatreManagementSystem {

    static final String DB_URL = "jdbc:mysql://localhost:3306/theatre_db";
    static final String USER = "root";
    static final String PASS = "yourpassword";

    // Method to add a movie to the database
    public static void addMovie(Connection conn, String title, String genre, String showTime, int availableSeats) throws SQLException {
        String query = "INSERT INTO movies (title, genre, show_time, available_seats) VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, title);
        pstmt.setString(2, genre);
        pstmt.setString(3, showTime);
        pstmt.setInt(4, availableSeats);
        pstmt.executeUpdate();

        // Initialize seats for the movie
        for (int i = 1; i <= availableSeats; i++) {
            String seatNumber = "S" + i;
            String seatQuery = "INSERT INTO seats (movie_id, seat_number, is_available) VALUES (?, ?, TRUE)";
            PreparedStatement seatStmt = conn.prepareStatement(seatQuery);
            seatStmt.setInt(1, getLastInsertedMovieId(conn));
            seatStmt.setString(2, seatNumber);
            seatStmt.executeUpdate();
        }

        System.out.println("Movie and seats added successfully!");
    }

    // Helper method to get the last inserted movie ID
    public static int getLastInsertedMovieId(Connection conn) throws SQLException {
        String query = "SELECT LAST_INSERT_ID() AS last_id";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        if (rs.next()) {
            return rs.getInt("last_id");
        }
        return -1; // Error case
    }

    // Method to display all movies
    public static void displayMovies(Connection conn) throws SQLException {
        String query = "SELECT * FROM movies";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        System.out.println("Movies available:");
        while (rs.next()) {
            System.out.println("ID: " + rs.getInt("movie_id") + ", Title: " + rs.getString("title") +
                    ", Genre: " + rs.getString("genre") + ", Show Time: " + rs.getTime("show_time") +
                    ", Available Seats: " + rs.getInt("available_seats"));
        }
    }

    // Method to display all available seats for a specific movie
    public static void displayAvailableSeats(Connection conn, int movieId) throws SQLException {
        String query = "SELECT seat_number FROM seats WHERE movie_id = ? AND is_available = TRUE";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, movieId);
        ResultSet rs = pstmt.executeQuery();

        System.out.println("Available seats:");
        while (rs.next()) {
            System.out.println("Seat: " + rs.getString("seat_number"));
        }
    }

    // Method to check if a seat is available
    public static boolean isSeatAvailable(Connection conn, int movieId, String seatNumber) throws SQLException {
        String query = "SELECT is_available FROM seats WHERE movie_id = ? AND seat_number = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, movieId);
        pstmt.setString(2, seatNumber);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getBoolean("is_available");
        }
        return false; // Seat not found or unavailable
    }

    // Method to book a ticket
    public static void bookTicket(Connection conn, int movieId, int customerId, String seatNumber) throws SQLException {
        if (isSeatAvailable(conn, movieId, seatNumber)) {
            String query = "INSERT INTO tickets (movie_id, customer_id, seat_number) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, movieId);
            pstmt.setInt(2, customerId);
            pstmt.setString(3, seatNumber);
            pstmt.executeUpdate();

            // Mark the seat as unavailable
            String updateSeatQuery = "UPDATE seats SET is_available = FALSE WHERE movie_id = ? AND seat_number = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSeatQuery);
            updateStmt.setInt(1, movieId);
            updateStmt.setString(2, seatNumber);
            updateStmt.executeUpdate();

            System.out.println("Ticket booked successfully!");
        } else {
            System.out.println("Seat is unavailable. Please choose a different seat.");
        }
    }

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                System.out.println("Theatre Management System");
                System.out.println("1. Add Movie");
                System.out.println("2. Display Movies");
                System.out.println("3. Display Available Seats");
                System.out.println("4. Book Ticket");
                System.out.println("5. Exit");
                System.out.print("Choose an option: ");
                int option = scanner.nextInt();

                switch (option) {
                    case 1:
                        System.out.print("Enter movie title: ");
                        String title = scanner.next();
                        System.out.print("Enter genre: ");
                        String genre = scanner.next();
                        System.out.print("Enter show time (HH:MM:SS): ");
                        String showTime = scanner.next();
                        System.out.print("Enter available seats: ");
                        int availableSeats = scanner.nextInt();
                        addMovie(conn, title, genre, showTime, availableSeats);
                        break;

                    case 2:
                        displayMovies(conn);
                        break;

                    case 3:
                        System.out.print("Enter movie ID: ");
                        int movieId = scanner.nextInt();
                        displayAvailableSeats(conn, movieId);
                        break;

                    case 4:
                        System.out.print("Enter movie ID: ");
                        int bookMovieId = scanner.nextInt();
                        System.out.print("Enter customer ID: ");
                        int customerId = scanner.nextInt();
                        System.out.print("Enter seat number: ");
                        String seatNumber = scanner.next();
                        bookTicket(conn, bookMovieId, customerId, seatNumber);
                        break;

                    case 5:
                        System.out.println("Exiting system...");
                        return;

                    default:
                        System.out.println("Invalid option. Try again.");
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
