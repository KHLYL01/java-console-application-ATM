package server;

import sql.User;
import sql.UserRepository;
import util.AESUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class ATMServer {

    public static final int PORT = 3000;
    public static KeyPair serverKeyPair;

    public static void main(String[] args) {

        initialServer();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Server running ion port 3000");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client host: " + clientSocket.getInetAddress());
                new ClientHandler(clientSocket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void initialServer() {

        //initial server key pair
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            serverKeyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        //initial users
        List<User> users = UserRepository.findAll();
        if (users.isEmpty()) {
            UserRepository.save(
                    User.builder()
                            .username("kk0026999")
                            .password(AESUtil.encrypt("12345678"))
                            .balance(AESUtil.encrypt("0.0"))
                            .build()
            );

            UserRepository.save(User.builder()
                    .username("kmw")
                    .password(AESUtil.encrypt("123456"))
                    .balance(AESUtil.encrypt("0.0"))
                    .build()
            );
        }
        // print all user information
        users.forEach(System.out::println);
        System.out.println();
    }

}
