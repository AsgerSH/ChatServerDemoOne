package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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

    private void sendRandomMessages(){
        String[] messages = {"Hej!", "Ha' en god dag!", "Jeg kan lide gulerødder", "#Datamatikerlyfe", "Ja da", "Hvad er klokken?", "Hvornår har vi fri?", "Hent lige en monner til mig"};
        int randomDelay = random.nextInt(7000);
        while (true) {
            try {
                Thread.sleep((randomDelay) + 10000);
                synchronized (clients) {
                    if(!clients.isEmpty()){
                        ClientHandler randomClient = clients.get(random.nextInt(clients.size()));
                        String randomMessage = messages[random.nextInt(messages.length)];
                        randomClient.notify("Anonym bruger skriver:  " + randomMessage);
                    }
                }
        } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
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

        @Override
        public String toString(){
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
//                   System.out.println(msg);
                   if(msg.startsWith("#JOIN")){
                       this.name = msg.split(" ")[1];
                       server.broadcast("A new person joined the chat server. Welcome to: " + name);
                   }else if (msg.startsWith("#MESSAGE")){
                       server.broadcast(name + " says to everyone: " + msg.replace("#MESSAGE", ""));
                   } else if (msg.startsWith("#LEAVE")){
                       server.broadcast(name + " left the chat server");
                       break;
                   } else if (msg.startsWith("#USERS")){
                       for (ClientHandler ch : clients) {
                           out.println(ch.toString() + " is connected to the server");
                       }
                   } else if(msg.startsWith("#PRIVATE")) {
                       String[] parts = msg.split(" ", 3); // Deler op i #PRIVATE, modtager, besked
                       String targetClientName = parts[1];
                       String privateMsg = parts[2];
                       boolean found = false;

                       synchronized (clients){
                           for (ClientHandler ch : clients) {
                               if (ch.name.equalsIgnoreCase(targetClientName)){
                                   ch.notify(name + " hvisker '" + privateMsg + "' til dig");
                                   found = true;
                                   out.println(name + " hvisker '" + privateMsg + "' til " + targetClientName);
                                   break;
                               }
                           }
                       }
                       if (!found){
                           out.println("400 Bad Request");
                           out.println("Client with name '" + targetClientName + "' not found");
                           out.println("Syntaxen er: #PRIVATE <name> <message>");
                       }
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
           } finally {
               try {
                   clientSocket.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
               ((ChatServerDemo)server).removeClient(this);
           }
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
