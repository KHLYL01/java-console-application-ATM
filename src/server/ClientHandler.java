package server;

import sql.User;
import sql.UserRepository;
import util.AESUtil;
import util.RSAUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.PublicKey;
import java.util.Objects;

public class ClientHandler extends Thread {

    final Socket clientSocket;

    BufferedReader in;
    PrintWriter out;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();

        try {
            out.println("Welcome to The ATM Server");

            // convert server public key to string and sent it to client
            PublicKey serverPublicKey = ATMServer.serverKeyPair.getPublic();
            String stringPublicKey = RSAUtil.toStringPublicKey(serverPublicKey);
            out.println(stringPublicKey);

            // received client public key
            String stringClientPublicKey = in.readLine();
            // convert to PublicKey object
            PublicKey clientPublicKey = RSAUtil.toPublicKey(stringClientPublicKey);


            // authentication
            boolean exit = false;
            while (true) {
                if (handleAuthentication()) {
                    out.println("Authenticated successfully");
                    break;
                } else {
                    out.println("Authenticated Failure");
                    out.println("press any letter to try again or (press 0 to exit)");
                    String failureResult = in.readLine();
                    if (failureResult.equals("0")) {
                        exit = true;
                        break;
                    }
                }
            }

            // handle any message request if not exit from login
            if (!exit) {
                while (true) {
                    String message = in.readLine();
                    if (message.equals("Exit")) {
                        System.out.println("Client Exit");
                        break;
                    }
                    handleMessages(message, clientPublicKey);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void handleMessages(String message, PublicKey clientPublicKey) {
        String decryptedMessage = RSAUtil.decrypt(message, ATMServer.serverKeyPair.getPrivate());
        String selected = Objects.requireNonNull(decryptedMessage).split(" ")[0];
        System.out.println(Objects.requireNonNull(decryptedMessage).split(" ")[0] + " " + Objects.requireNonNull(decryptedMessage).split(" ")[1]);
        switch (selected) {
            case "Deposit" -> out.println(handleDeposit(decryptedMessage));
            case "Withdraw" -> out.println(handleWithdraw(decryptedMessage));
            case "Check_Balance" -> out.println(handleCheckBalance(decryptedMessage, clientPublicKey));
            default -> throw new IllegalStateException("Unexpected value: " + selected);
        }

    }

    private boolean handleAuthentication() {
        try {
            String encryptedMessage = in.readLine();

            //decrypt message and get username and  password
            String message = RSAUtil.decrypt(encryptedMessage, ATMServer.serverKeyPair.getPrivate());
            String[] auth = Objects.requireNonNull(message).split(" ");
            System.out.println(auth[0] + " " + auth[1] + " " + auth[2]);

            // encode password
            String encryptedPassword = AESUtil.encrypt(auth[2]);
            //check auth
            User user = UserRepository.findByUsername(auth[1]).get();
            if (user.getPassword().equals(encryptedPassword)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String handleDeposit(String message) {

        String username = message.split(" ")[1];
        double amount = Double.parseDouble(message.split(" ")[2]);

        // get user
        User user = UserRepository.findByUsername(username).get();

        // decrypt balance and convert to double
        double balance = Double.parseDouble(AESUtil.decrypt(user.getBalance()));

        // add amount to balance
        String newBalance = String.valueOf(balance + amount);

        //encrypt new balance and update user in database
        UserRepository.updateBalanceByUsername(username,AESUtil.encrypt(newBalance));

        // refresh user
        user = UserRepository.findByUsername(username).get();

        return "Deposit " + amount + " successfully, balance: " + user.getBalance();
    }

    private String handleWithdraw(String message) {
        String username = message.split(" ")[1];
        double amount = Double.parseDouble(message.split(" ")[2]);

        // get user
        User user = UserRepository.findByUsername(username).get();

        // decrypt balance and convert to double
        double balance = Double.parseDouble(AESUtil.decrypt(user.getBalance()));

        // verify sufficient balance
        if (balance < amount) {
            return "Withdraw " + amount + " invalid, your balance is not sufficient";
        }

        // deduct amount from balance
        String newBalance = String.valueOf(balance - amount);

        //encrypt new balance and update user in database
        UserRepository.updateBalanceByUsername(username,AESUtil.encrypt(newBalance));

        // refresh user
        user = UserRepository.findByUsername(username).get();

        return "Withdraw " + amount + " successfully, balance: " + user.getBalance();
    }

    private String handleCheckBalance(String message, PublicKey clientPublicKey) {
        String username = message.split(" ")[1];

        //get user
        User user = UserRepository.findByUsername(username).get();

        // decrypt balance
        String balance = AESUtil.decrypt(user.getBalance());

        // encrypt balance by client public key and return it
        return RSAUtil.encrypt(balance, clientPublicKey);
    }

}
