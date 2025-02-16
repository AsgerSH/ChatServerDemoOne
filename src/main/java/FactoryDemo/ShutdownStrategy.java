package FactoryDemo;

public class ShutdownStrategy implements IMessageStrategy {
    @Override
    public void execute(String message, ClientHandler client) {
        for (IObserver obs : client.getServer().getClients()){
            client.notify("Server is shutting down. Goodbye " + obs.getName());
            client.removeClient(obs);
            client.getServer().removeObserver(client);
            client.getServer().stopAllServers();
        }

    }
}
