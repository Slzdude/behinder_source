import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OpSqliteDB {
    private static final String Class_Name = "org.sqlite.JDBC";
    private static final String DB_URL = "jdbc:sqlite:F:\\xxxdatabase.db";

    public static void main(String[] args) {
        Connection connection = null;
        try {
            connection = createConnection();
            func1(connection);
            System.out.println("Success!");
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println(e);
                    e.printStackTrace();
                }
            }
        } catch (SQLException e2) {
            System.err.println(e2.getMessage());
            e2.printStackTrace();
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e3) {
                    System.err.println(e3);
                    e3.printStackTrace();
                }
            }
        } catch (Exception e4) {
            e4.printStackTrace();
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e5) {
                    System.err.println(e5);
                    e5.printStackTrace();
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e6) {
                    System.err.println(e6);
                    e6.printStackTrace();
                }
            }
        }
    }

    public static Connection createConnection() throws SQLException, ClassNotFoundException {
        Class.forName(Class_Name);
        return DriverManager.getConnection(DB_URL);
    }

    public static void func1(Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        Statement statement1 = connection.createStatement();
        statement.setQueryTimeout(30);
        ResultSet rs = statement.executeQuery("select * from table_name1");
        while (rs.next()) {
            String col1 = rs.getString("col1_name");
            System.out.println("col1 = " + col1 + "  col2 = " + rs.getString("col2_name"));
            System.out.println(col1);
            statement1.executeUpdate("insert into table_name2(col2) values('3')");
            statement1.executeUpdate("update table_name2 set ×Ö¶ÎÃû1=55 where ×Ö¶ÎÃû2='66'");
        }
    }
}
