package FactoryDemo;

public class MessageStrategy implements IMessageStrategy {
    @Override
    public void execute(String message, ClientHandler client) {
        String coloredName = client.colorText(client.getName(), "34");
    client.getServer().broadcast(coloredName + ": " + client.filterMessage(message));
    }
}
