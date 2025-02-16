package FactoryDemo;

public class LeaveStrategy implements IMessageStrategy {
    @Override
    public void execute(String message, ClientHandler client) {
        client.getServer().broadcast(client.getName() + " has left the server");
        client.removeClient(client);
    }
}
