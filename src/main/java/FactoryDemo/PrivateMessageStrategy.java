package FactoryDemo;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PrivateMessageStrategy implements IMessageStrategy {
    PrintWriter out;
    private boolean found = false;

    @Override
    public void execute(String message, ClientHandler client) {

        String[] parts = message.split(" ", 3);
        String command = parts[0];
        String target = parts[1];
        String restMessage = parts[2];




       for (IObserver ch :   client.getServer().getClients()){
           ClientHandler chClient = (ClientHandler) ch;
            if (chClient.getName().equalsIgnoreCase(target)) {
                ch.notify(client.getName() + " whispers privately: " + restMessage);
                out.println("You whisper to " + target + ": " + restMessage);
                found = true;
            }
        }
        if (!found) {
            out.println("There is no '" + target + "' on the server");
        }
    }
}
