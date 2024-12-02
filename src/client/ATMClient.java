package client;

import util.RSAUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

public class ATMClient {

    public static final String HOST = "localhost";
    public static final int PORT = 3000;

    public static PublicKey serverPublicKey;
    public static KeyPair clientKeyPair;

    public static BufferedReader in, console;
    public static PrintWriter out;
    public static String username;

    public static void main(String[] args) {
        initialClient();

        try (Socket socket = new Socket(HOST, PORT)) {

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            console = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(), true);

            String serverMessage = in.readLine();
            System.out.println(serverMessage);

            // receive server public key from server
            String publickey = in.readLine();
            serverPublicKey = RSAUtil.toPublicKey(publickey);
            System.out.println("server public key: " + publickey);

            // send client public key to server
            out.println(RSAUtil.toStringPublicKey(clientKeyPair.getPublic()));

            //login
            login();

            //menu
            playATM();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void initialClient() {
        //initial client key pair
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            clientKeyPair = keyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void login() {
        try {

            while (true) {
                System.out.print("Enter username: ");

                username = console.readLine();

                System.out.print("Enter password: ");
                String password = console.readLine();

                // encrypt login information and send it to server
                String encryptedMessage = RSAUtil.encrypt("LOGIN " + username + " " + password, serverPublicKey);
                out.println(encryptedMessage);

                //read  auth result
                String authResult = in.readLine();
                System.out.println(authResult);

                if (authResult.equals("Authenticated successfully")) {
                    break;
                } else {
                    String failure = in.readLine();
                    System.out.println(failure);
                    String request = console.readLine();
                    out.println(request);
                    if (request.equals("0")) {
                        System.exit(0);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void playATM() {
        try {

            while (true) {
                // view menu
                viewMenu();
                // choose selected
                String selected = console.readLine();

                switch (selected) {
                    case "1" -> {
                        System.out.print("Enter amount: ");
                        String amount = console.readLine();
                        String encryptDepositRequest = RSAUtil.encrypt("Deposit " + username + " " + amount, serverPublicKey);
                        out.println(encryptDepositRequest);
                    }
                    case "2" -> {
                        System.out.print("Enter amount: ");
                        String amount = console.readLine();
                        String encryptWithdrawRequest = RSAUtil.encrypt("Withdraw " + username + " " + amount, serverPublicKey);
                        out.println(encryptWithdrawRequest);
                    }
                    case "3" -> {
                        String encryptCheckBalanceRequest = RSAUtil.encrypt("Check_Balance " + username, serverPublicKey);
                        out.println(encryptCheckBalanceRequest);

                        // received encryption Balance result
                        String encryptionBalance = in.readLine();

                        // decrypt by client private key
                        String balance  = RSAUtil.decrypt(encryptionBalance,clientKeyPair.getPrivate());

                        System.out.println("\n=============================\n" + "Balance: " +balance+ "\n=============================\n");
                        continue;
                    }
                    case "4" -> {
                        out.println("Exit");
                        System.exit(0);
                    }
                    default -> {
                        System.out.println("?????????? Invalid Input,try again ??????????");
                        continue;
                    }
                }

                // view selected result
                String result = in.readLine();
                System.out.println("\n=============================\n" + result + "\n=============================\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void viewMenu() {
        System.out.println("Choose from Menu:");
        System.out.println("1.Deposit");
        System.out.println("2.Withdraw");
        System.out.println("3.Check Balance");
        System.out.println("4.Exit");
    }
}
