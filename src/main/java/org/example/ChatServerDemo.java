package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ChatServerDemo implements IObserverable {
    private static volatile IObserverable server = getInstance();
    private final List<ClientHandler> clients = new ArrayList<>();
    private final Random random = new Random();
    private PrintWriter print;

    private ChatServerDemo(){}

    public static synchronized IObserverable getInstance() {
        if (server == null){
            server = new ChatServerDemo();
        }
        return server;
    }

    public List<ClientHandler> getClients() {
        return clients;
    }


    public static void main(String[] args) {
        new ChatServerDemo().startServer(12345);

    }

    public void startServer(int port) {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server started at port " + port);

            new Thread(this::sendRandomMessages).start();

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
                clients.add(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void broadcast(String msg) {
        for (ClientHandler ch : clients) {
            ch.notify(msg);
        }
    }

    public void removeClient(ClientHandler ch) {
        synchronized (clients) {
            clients.remove(ch);
        }
    }

    private void sendRandomMessages() {
        String[] messages = {"Hej!", "Ha' en god dag!", "Jeg kan lide bananer", "#Datamatikerlyfe", "Ja da", "Hvad er klokken?", "Hvad tid har vi fri?", "Hent lige en monner til mig"};
        int randomDelay = random.nextInt(5000) + 15000;

        while (true) {
            try {
                Thread.sleep((randomDelay));
                synchronized (clients) {
                    if (!clients.isEmpty()) {
                        ClientHandler randomClient = clients.get(random.nextInt(clients.size()));
                        String randomMessage = messages[random.nextInt(messages.length)];
                        broadcast(randomClient.getName() + " skriver random: " + randomMessage);
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        /* new Thread(() -> {
            try {
                Thread.sleep(randomDelay);
                synchronized (clients) {
                    if (!clients.isEmpty()) {
                        ClientHandler randomClient = clients.get(random.nextInt(clients.size()));
                        randomClient.notify(randomClient.getName() + " skriver random: " + randomMessage);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
        */
    }



    private static class ClientHandler implements Runnable, IObserver {

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private IObserverable server;
        private String name;

        public ClientHandler(Socket socket, IObserverable server) throws IOException {
            this.clientSocket = socket;
            this.server = server;
            this.name = "Guest";
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        public String getName(){
            return name;
        }

        @Override
        public void notify(String msg) {
//            System.out.println(msg);
            out.println(msg);
        }

        @Override
        public void run() {
            String msg;
            List<ClientHandler> clients = ((ChatServerDemo)server).getClients();
           try {
               while ((msg = in.readLine()) != null) {
                   if (msg.startsWith("#JOIN")) {
                       this.name = msg.split(" ")[1];
                       server.broadcast(name + " has joined the server.");
                   } else if (msg.startsWith("#MESSAGE")) {
                       server.broadcast(name + " says: " + msg.replace("#MESSAGE", ""));
                   } else if (msg.startsWith("#LEAVE")) {
                       server.broadcast(name + " left the chat server");
                       break;
                   } else if (msg.startsWith("#GETLIST")) {
                       for (ClientHandler ch : clients) {
                           out.println(ch.getName() + " is connected to the server");
                       }
                   } else if (msg.startsWith("#PRIVATESUBLIST")) {
                       String[] parts = msg.split(" ", 2);

                       if (parts.length < 2) {
                           out.println("400 Bad Request");
                           out.println("Correct request is: #PRIVATESUBLIST <name>, <name>, <...>, <message>");
                       }

                       int lastIndex = parts[1].lastIndexOf(",");
                       if (lastIndex == -1) {
                           out.println("400 Bad Request: Missing names or message");
                       }

                       String targetNames = parts[1].substring(0, lastIndex);
                       String targetMessage = parts[1].substring(lastIndex + 1).trim();

                       String[] targets = targetNames.split("\\s*, \\s*");

                       List<String> targetList = new ArrayList<>();
                       for (String target : targets) {
                           targetList.add(target.trim().toLowerCase());
                       }

                       for (ClientHandler ch : clients) {
                           boolean found = false;
                           if (targetList.contains(ch.getName().toLowerCase())) {
                               out.println("You whisper to group: " + targetMessage);
                               ch.notify(name + " whispers to group: " + targetMessage);
                               found = true;
                           }
                           if (!found) {
                               out.println("400 Bad Request: None of the specified users found");
                           }
                       }
                   } else if (msg.startsWith("#PRIVATE")) {
                       String[] parts = msg.split(" ", 3); // Deler op i #PRIVATE, modtager, besked

                       if (parts.length < 3) {
                           out.println("400 Bad Request: Missing names or message");
                           out.println("Correct Request is: #PRIVATE <name> <message>");
                       }

                       String targetClientName = parts[1];
                       String privateMsg = parts[2];
                       boolean found = false;

                       synchronized (clients) {
                           for (ClientHandler ch : clients) {
                               if (ch.name.equalsIgnoreCase(targetClientName)) {
                                   ch.notify(name + " whispers privately: " + privateMsg);
                                   found = true;
                                   out.println(name + " whispers privately '" + privateMsg + "' to " + targetClientName);
                                   break;
                               }
                           }
                       }
                       if (!found) {
                           out.println("400 Bad Request");
                           out.println("Client with name '" + targetClientName + "' not found");
                           out.println("Syntaxen er: #PRIVATE <name> <message>");
                       }
                   } else if (msg.startsWith("#HELP")) {
                       out.println("Available commands:");
                       out.println("#JOIN <name>");
                       out.println("#MESSAGE <message>");
                       out.println("#LEAVE");
                       out.println("#GETLIST");
                       out.println("#PRIVATE <name> <message>");
                       out.println("#PRIVATESUBLIST <name>, <name>, <name>, <...>, <message>");
                       out.println("#SHUTDOWN");
                   } else if (msg.startsWith("#SHUTDOWN")) {
                       out.println("Shutting down the chat server...");
                       synchronized (clients) {
                           for (ClientHandler ch : new ArrayList<>(clients)) {
                               ch.notify("The server is shutting down. Bye " + ch.name);
                               ch.closeConnection();
                               ((ChatServerDemo) server).removeClient(ch);
                           }
                       }
                       System.out.println("Server has been shut down");
                       System.exit(0);
                   } else {
                       server.broadcast(name + " says: " + msg);
                   }
               }
           } catch (IOException e) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
