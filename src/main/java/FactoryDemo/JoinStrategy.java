package FactoryDemo;

public class JoinStrategy implements IMessageStrategy {

    @Override
    public void execute(String message, ClientHandler client) {
        client.setName(message);
        client.getServer().broadcast("Welcome to: " + client.getName());
    }
}
