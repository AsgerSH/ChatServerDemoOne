package FactoryDemo;

public class LeaveStrategy implements IMessageStrategy {
    @Override
    public void execute(String message, ClientHandler client) {
        String coloredName = client.colorText(client.getName(), "34");
        client.getServer().broadcast(coloredName + " has left the server");
        client.removeClient(client);
    }
}
