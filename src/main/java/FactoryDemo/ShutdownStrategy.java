package FactoryDemo;

public class ShutdownStrategy implements IMessageStrategy {
    @Override
    public void execute(String message, ClientHandler client) {
        for (IObserver obs : client.getServer().getClients()){
            String coloredName = client.colorText(obs.getName(), "34");
            client.notify("Server is shutting down. Goodbye " + coloredName);
            client.removeClient(obs);
            client.getServer().removeObserver(client);
            client.getServer().stopAllServers();
        }

    }
}
