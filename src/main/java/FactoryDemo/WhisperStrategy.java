package FactoryDemo;

public class WhisperStrategy implements IMessageStrategy {

    @Override
    public void execute(String msg, ClientHandler client) {

        String[] parts = msg.split(" ", 2);
        String name = parts[0];
        String message = parts[1];
        client.directMessage(name, client.filterMessage(message));
        String coloredName = client.colorText(name, "34");
        String coloredText = client.colorText("You whisper to ", "35");
        String coloredText2 = client.colorText(": ", "35");

        String newMessage = client.filterMessage(message);
        String coloredNewMessage = client.colorText(newMessage, "33");

        client.notify(coloredText + coloredName + coloredText2 + coloredNewMessage);
    }
}


        /*String[] parts = message.split(" ", 2);

        if (parts.length < 2) {

            client.notify("Wrong request, try: #PRIVATE <target> <message>");
            return;
        }

        String target = parts[0];
        String restMessage = parts[1];

        boolean found = false;


       for (IObserver obs : client.getServer().getClients()){
            if (obs.getName().equalsIgnoreCase(target)) {
                obs.notify(client.getName() + " whispers privately: " + restMessage);
                client.notify("You whisper to " + target + ": " + restMessage);
                found = true;
                break;
            }
        }
        if (!found) {
            client.notify("There is no '" + target + "' on the server");
        }
        */


