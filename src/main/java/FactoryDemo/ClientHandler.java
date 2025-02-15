package FactoryDemo;

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
    private final List<ClientHandler> clients = new ArrayList<>();

    public ClientHandler(Socket socket, IObserverable server) throws IOException {
        this.clientSocket = socket;
        this.server = server;
        this.name = "Guest";

        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void addBannedWord(String word) {
        BANNED_WORDS.add(word.toLowerCase());
    }

    public void removeBannedWord(String word) {
        BANNED_WORDS.remove(word.toLowerCase());
    }

    public void getBannedWords() {
        for (String word : BANNED_WORDS) {
            notify(word);
        }
    }


    public String filterMessage(String message) {
        String messageToLowerCase = message.toLowerCase();
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

                if(warningCount == 3){
                    notify("You have been banned from this server");
                    closeConnection();
                    server.broadcast(name +" has been banned from this server");
                }
            }
        }
        return message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    @Override
    public void notify(String msg) {
//            System.out.println(msg);
        out.println(msg);
    }

    private List<ClientHandler> getClients() {
        return clients;
    }

    public void directMessage(String name, String msg){
        for(IObserver obs: clients){
            ClientHandler ch = (ClientHandler) obs;
            if(ch.name.equals(name)){
                ch.notify(this.name+": "+msg);
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
//                if (msg.startsWith("#JOIN")) {
//                    if(msg.split(" ").length == 1){
//                        notify("You haven't entered a name. Try again");
//                    } else {
//                        this.name = msg.split(" ")[1];
//                        server.broadcast(name + " has joined the server.");
//                    }
//                } else if (msg.startsWith("#ADDBADWORD")) {
//                    String word = msg.split(" ")[1];
//                    addBannedWord(word);
//                    notify("You have added: " + word + " to the banned word list.");
//                } else if (msg.startsWith("#REMOVEBANNEDWORD")) {
//                    String word = msg.split(" ")[1];
//                    removeBannedWord(word);
//                    notify("You have removed: " + word + " from the banned word list.");
//                } else if (msg.startsWith("#BANNEDLIST")) {
//                    notify("The list of banned words is as follows:");
//                    getBannedWords();
//                } else if (msg.startsWith("#MESSAGE")) {
//                    server.broadcast(name + " says: " + filterMessage(msg.replace("#MESSAGE", "")));
//                } else if (msg.startsWith("#LEAVE")) {
//                    server.broadcast(name + " left the chat server");
//                    break;
//                } else if (msg.startsWith("#GETLIST")) {
//                    for (ClientHandler ch : clients) {
//                        notify(getName() + " is connected to the server");
//                    }
//                } else if (msg.startsWith("#PRIVATESUBLIST")) {
//                    String[] parts = msg.split(" ", 2);
//
//                    if (parts.length < 2) {
//                        out.println("400 Bad Request");
//                        out.println("Correct request is: #PRIVATESUBLIST <name>, <name>, <...>, <message>");
//                    }
//
//                    int lastIndex = parts[1].lastIndexOf(",");
//                    if (lastIndex == -1) {
//                        out.println("400 Bad Request: Missing names or message");
//                    }
//
//                    String targetNames = parts[1].substring(0, lastIndex);
//                    String targetMessage = parts[1].substring(lastIndex + 1).trim();
//
//                    String[] targets = targetNames.split("\\s*, \\s*");
//
//                    List<String> targetList = new ArrayList<>();
//                    for (String target : targets) {
//                        targetList.add(target.trim().toLowerCase());
//                    }
//
//                    for (ClientHandler ch : clients) {
//                        boolean found = false;
//                        if (targetList.contains(getName().toLowerCase())) {
//                            notify("You whisper to group: " + targetMessage);
//                            ch.notify(name + " whispers to group: " + targetMessage);
//                            found = true;
//                        }
//                        if (!found) {
//                            notify("400 Bad Request: None of the specified users found");
//                        }
//                    }
//                } else if (msg.startsWith("#PRIVATE")) {
//                    String[] parts = msg.split(" ", 3); // Deler op i #PRIVATE, modtager, besked
//
//                    if (parts.length < 3) {
//                        out.println("400 Bad Request: Missing names or message");
//                        out.println("Correct Request is: #PRIVATE <name> <message>");
//                    }
//
//                    String targetClientName = parts[1];
//                    String privateMsg = parts[2];
//                    boolean found = false;
//
//                    synchronized (clients) {
//                        for (ClientHandler ch : clients) {
//                            if (ch.name.equalsIgnoreCase(targetClientName)) {
//                                ch.notify(name + " whispers privately: " + privateMsg);
//                                found = true;
//                                out.println(name + " whispers privately '" + privateMsg + "' to " + targetClientName);
//                                break;
//                            }
//                        }
//                    }
//                    if (!found) {
//                        out.println("400 Bad Request");
//                        out.println("Client with name '" + targetClientName + "' not found");
//                        out.println("Syntaxen er: #PRIVATE <name> <message>");
//                    }
//                } else if (msg.startsWith("#HELP")) {
//                    out.println("Available commands:");
//                    out.println("#JOIN <name>");
//                    out.println("#MESSAGE <message>");
//                    out.println("#LEAVE");
//                    out.println("#GETLIST");
//                    out.println("#PRIVATE <name> <message>");
//                    out.println("#PRIVATESUBLIST <name>, <name>, <name>, <...>, <message>");
//                    out.println("#SHUTDOWN");
//                } else if (msg.startsWith("#SHUTDOWN")) {
//                    server.broadcast("Shutting down the chat server...");
//                    synchronized (clients) {
//                        for (ClientHandler ch : new ArrayList<>(clients)) {
//                            ch.notify("The server is shutting down. Bye " + ch.name);
//                            ch.closeConnection();
//                        }
//                    }
//                    server.broadcast("Server has been shut down");
//                    server.stopAllServers();
//                    System.exit(0);
//                } else {
//                    server.broadcast(name + " says: " + msg);
//                }
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
           /*finally {
               try {
                   clientSocket.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
               ((ChatServerDemo)server).removeClient(this);
           }
            */
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

}
