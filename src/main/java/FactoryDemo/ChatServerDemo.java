package FactoryDemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class ChatServerDemo implements IObserverable{

    private List<IObserver> clients = new ArrayList<>();
    private static volatile ChatServerDemo instance;
    ServerSocket serverSocket;

    private ChatServerDemo(){}

    public static ChatServerDemo getInstance(){
        if(instance == null){
            instance = new ChatServerDemo();
        }
        return instance;
    }

    public static void main(String[] args) {
        new ChatServerDemo().startServer(12345);
    }

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            ExecutorService executor = Executors.newCachedThreadPool();
            while (true) {
                Socket client = serverSocket.accept();
                Runnable runnable = new ClientHandler(client, this);
//                new Thread(runnable).start();
                executor.submit(runnable);
                IObserver clientHandler = (IObserver) runnable;
                clients.add(clientHandler);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void addObserver(IObserver observer) {
        clients.add(observer);
    }

    @Override
    public void removeObserver(IObserver observer) {
        clients.remove(observer);
    }

    @Override
    public void broadcast(String msg){
        // TODO: Implement method
        for(IObserver observer : clients){
            observer.notify(msg);
        }
    }

    @Override
    public void stopAllServers() {

    }

    public List<IObserver> getClients() {
        return clients;
    }

    public void removeClient(FactoryDemo.ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

}
