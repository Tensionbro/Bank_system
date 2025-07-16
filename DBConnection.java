/**
 *
 * @author Rahul Yadav
 */
import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/bank_system", "root", ""
            ); // Update user/pass if needed
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

