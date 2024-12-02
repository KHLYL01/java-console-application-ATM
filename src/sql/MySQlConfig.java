package sql;

import java.sql.*;

public class MySQlConfig {
    private final static String url = "jdbc:mysql://localhost:3306/";
    private final static String username = "root";
    private final static String password = "0942200578kh";
    private final static String databaseName = "atm";

    private static boolean checkDatabase = false;

    public static Connection connectDB() throws SQLException {

        Connection connection = DriverManager.getConnection(url, username, password);

        checkDatabaseIfNotExist(connection);

        connection.setCatalog(databaseName);

        return connection;
    }

    private static void checkDatabaseIfNotExist(Connection connection) throws SQLException {
        if(!checkDatabase){
            Statement statement = connection.createStatement();

            //create database if not exist
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS " + databaseName);

            System.out.println("Database '" + databaseName + "' created successfully or already exists.");

            // select created database
            connection.setCatalog(databaseName);

            statement = connection.createStatement();

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "username VARCHAR(255) NOT NULL, "
                    + "password VARCHAR(255) NOT NULL, "
                    + "balance VARCHAR(255) NOT NULL)");

            System.out.println("Table 'users' created successfully or already exists.");

            checkDatabase = true;
        }
    }


}
