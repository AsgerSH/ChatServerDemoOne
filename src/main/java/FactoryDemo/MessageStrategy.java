package FactoryDemo;

public class MessageStrategy implements IMessageStrategy {
    @Override
    public void execute(String message, ClientHandler client) {
    client.getServer().broadcast(client.getName() + ": " + client.filterMessage(message));
    }
}
