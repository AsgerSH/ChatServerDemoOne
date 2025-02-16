package FactoryDemo;

public class GetListStrategy implements IMessageStrategy {
    public void execute(String message, ClientHandler client) {

        for (IObserver obs : client.getServer().getClients()){
            String coloredName = client.colorText(obs.getName(), "34");
            client.notify(coloredName + " is connected to the server.");
        }
    }
}
