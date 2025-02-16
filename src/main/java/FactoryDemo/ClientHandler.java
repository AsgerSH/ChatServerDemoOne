package FactoryDemo;

import com.sun.security.jgss.GSSUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ClientHandler implements Runnable, IObserver {

    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final IObserverable server;
    private String name;
    private final List<String> BANNED_WORDS = Arrays.asList("fuck", "lort", "din mor", "luder");
    private int warningCount;
    private String colorCode;

    public ClientHandler(Socket socket, IObserverable server) throws IOException {
        this.clientSocket = socket;
        this.server = server;
        this.name = "Guest";

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void addBannedWord(String word) {
        if (BANNED_WORDS.contains(word.toLowerCase())) {
            notify("Word '" + word + "' is already banned.");
        }else{BANNED_WORDS.add(word.toLowerCase());}
    }

    public void removeBannedWord(String word) {
        if (BANNED_WORDS.contains(word.toLowerCase())) {
            BANNED_WORDS.remove(word.toLowerCase());
        } else {
            notify("Word '" + word + "' is not banned.");
        }
    }

    public void getBannedWords() {
        for (String word : BANNED_WORDS) {
            notify("Banned word: " + word);
        }
    }


    public String filterMessage(String message) {
        String messageToLowerCase;
        messageToLowerCase = message.toLowerCase();
        for (String word : BANNED_WORDS) {
            String wordLowerCase = word.toLowerCase();
            if (messageToLowerCase.contains(wordLowerCase)) {
                String replacement = "";
                for (int i = 0; i < word.length(); i++) {
                    replacement += "*";
                }
                message = message.replaceAll("(?i)\\b" + Pattern.quote(word) + "\\b", replacement);
                warningCount++;
                notify("You have received a warning. You have " + warningCount + " warnings.");

                if (warningCount == 3) {
                    notify("You have been banned from this server");
                    closeConnection();
                    server.broadcast(name + " has been banned from this server");
                }
            }
        }
        return message;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public void notify(String msg) {
        out.println(msg);
    }

    public void directMessage(String name, String message) {
        for (IObserver obs : getServer().getClients()) {
            if (obs.getName().contains(name)) {
                String coloredName = colorText(this.name, "34");
                obs.notify(coloredName + " whispers: " + message);
                break;
            }
        }
    }

    @Override
    public void run() {
        String msg;

        try {
            while ((msg = in.readLine()) != null) {
                String[] strs = msg.split(" ");
                String command = strs[0];
                String restMessage = strs.length > 1 ? msg.substring(command.length() + 1) : msg;
                MessageStrategyFactory.getStrategy(command).execute(restMessage, this);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            ((ChatServerDemo) server).removeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ChatServerDemo getServer() {
        return (ChatServerDemo) server;
    }

    public void removeClient(IObserver obs) {
        try {
            out.close();
            in.close();
            clientSocket.close();
            ((ChatServerDemo) obs).removeClient(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String colorText(String text, String colorCode) {
        return "\u001B[" + colorCode + "m" + text + "\u001B[0m";
    }

    public void changeNameColor(String newColorCode) {
        String coloredName = colorText(name, newColorCode);
        notify("Name color changed to: " + coloredName);
    }
}
