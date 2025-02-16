package FactoryDemo;

public class BannedWordListStrategy implements IMessageStrategy {

    @Override
    public void execute(String message, ClientHandler client) {
        client.getBannedWords();
    }
}
