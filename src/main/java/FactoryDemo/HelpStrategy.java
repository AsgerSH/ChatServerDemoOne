package FactoryDemo;


import java.util.List;

public class HelpStrategy implements IMessageStrategy {

    @Override
    public void execute(String message, ClientHandler client) {
    String[] strategyNames = MessageStrategyFactory.getAllStrategyNames();
    String helpMessage = "Available commands: " + String.join(", ", strategyNames);

    client.notify(helpMessage);
    }
}
