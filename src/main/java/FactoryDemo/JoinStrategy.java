package FactoryDemo;

public class JoinStrategy implements IMessageStrategy {

    @Override
    public void execute(String message, ClientHandler client) {

        String[] parts = message.split(" ", 2);

        String name = parts[0];

        String lastName = parts.length > 1 ? parts[1] : "";

        client.setName(name);

        String coloredName = client.colorText(client.getName(), "34");

        client.getServer().broadcast("Welcome to: " + coloredName);
        client.notify("Please check #HELP for commands. 3 bad words = server kick.");
    }
}
