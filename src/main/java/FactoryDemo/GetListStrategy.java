package FactoryDemo;

public class GetListStrategy implements IMessageStrategy {
    public void execute(String message, ClientHandler client) {

        for (IObserver obs : client.getServer().getClients()){
            client.notify(obs.getName() + " is connected to the server.");
        }
    }
}
