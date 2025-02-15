package FactoryDemo;

import org.example.ChatServerDemo;

public interface IMessageStrategy {
    void execute(String message, ClientHandler client);
}
