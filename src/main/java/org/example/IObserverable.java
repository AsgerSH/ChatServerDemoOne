package org.example;

public interface IObserverable {
//    void addObserver(IObserver observer);
//    void removeObserver(IObserver observer);
    void broadcast(String message); // Send message to all clients
    void stopAllServers();
}
