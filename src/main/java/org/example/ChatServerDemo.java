package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServerDemo implements IObserverable {

    private ChatServerDemo(){}

    public static synchronized IObserverable getInstance() {
        if (server == null){
            server = new ChatServerDemo();
        }
        return server;
    }
    private static volatile IObserverable server = getInstance();
    private List<ClientHandler> clients = new ArrayList<>();


    public static void main(String[] args) {
        new ChatServerDemo().starServer(12345);

    }

    public void starServer(int port) {

        try {
            ServerSocket serverSocket = new ServerSocket(port);
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

    private static class ClientHandler implements Runnable, IObserver {

        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private IObserverable server;
        private String name = "Guest";

        public ClientHandler(Socket socket, IObserverable server) throws IOException {
            this.clientSocket = socket;
            this.server = server;
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        @Override
        public void notify(String msg) {
//            System.out.println(msg);
            out.println(msg);
        }

        @Override
        public void run() {
            String msg;
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
                   } else if (msg.startsWith("#PRIVATE")){

                   }
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
        }
    }
}
