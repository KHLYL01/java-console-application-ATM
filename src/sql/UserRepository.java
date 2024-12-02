package sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {

    public static List<User> findAll() {
        List<User> users = new ArrayList<>();
        try (
                Connection connection = MySQlConfig.connectDB();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM `users`");
        ) {
            while (resultSet.next()) {
                users.add(
                        User.builder()
                                .username(resultSet.getString("username"))
                                .password(resultSet.getString("password"))
                                .balance(resultSet.getString("balance"))
                                .build()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }


    public static Optional<User> findByUsername(String username) {
        User user = null;
        try (
                Connection connection = MySQlConfig.connectDB();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * FROM `users` u WHERE u.username = '" + username + "'");
        ) {
            if (resultSet.next()) {
                user = User.builder()
                        .username(resultSet.getString("username"))
                        .password(resultSet.getString("password"))
                        .balance(resultSet.getString("balance"))
                        .build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(user);
    }

    public static void save(User user) {
        try (
                Connection connection = MySQlConfig.connectDB();
                Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate("INSERT INTO `users`(`username`,`password`,`balance`) " +
                    "VALUES ('" + user.getUsername() + "','" + user.getPassword() + "','" + user.getBalance() + "')");
            System.out.println("user created successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updateBalanceByUsername(String username, String balance) {
        try (
                Connection connection = MySQlConfig.connectDB();
                Statement statement = connection.createStatement();
        ) {

            statement.executeUpdate("UPDATE `users` SET `balance` = '" + balance +
                    "' WHERE username = '" + username + "'");
            System.out.println("balance updated successfully");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
