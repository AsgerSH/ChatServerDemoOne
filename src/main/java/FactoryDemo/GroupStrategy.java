package FactoryDemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupStrategy implements IMessageStrategy {

    @Override
    public void execute(String msg, ClientHandler client) {
        String[] parts = msg.split(" ", 2);
        String[] names = parts[0].split(",");
        String message = parts.length > 1 ? parts[1] : "";

        String coloredMessage = client.colorText("You whisper to group: ", "35");
        String coloredMessage2 = client.colorText(client.filterMessage(message), "33");

        Arrays.stream(names).forEach((name) -> client.directMessage(name, client.filterMessage(message)));

        client.notify(coloredMessage + coloredMessage2);

//        for (String name : names) {
//            client.directMessage(name, message);
//        }
    }
}


//        String[] parts = message.split(" ", 2);
//
//        if (parts.length == 1) {
//            client.notify("400 Bad Request");
//            client.notify("Correct Request is: #GROUP <name>, <name>, <...>, <message>");
//            return;
//        }
//
//        int lastIndex = parts[1].lastIndexOf(",");
//
//        if (lastIndex == -1) {
//            client.notify("400 Bad Request");
//            client.notify("Correct Request is: #GROUP <name>, <...>, <message>");
//            return;
//        }
//
//        String targetNames = parts[1].substring(0, lastIndex);
//        String restMessage = parts[1].substring(lastIndex + 1).trim();
//
//        String[] targets = targetNames.split(",");
//
//        List<String> targetList = new ArrayList<>();
//        for (String target : targets) {
//            targetList.add(target.trim());
//        }
//
//        List<String> notFound = new ArrayList<>();
//        boolean atLeastOneSent = false;
//
//        // Loop through all the targets
//        for (String target : targetList) {
//            boolean found = false;
//
//            // Check if the target exists in the server's clients list
//            for (IObserver obs : client.getServer().getClients()) {
//                if (obs.getName().equals(target)) { // Match the target name
//                    obs.notify(client.getName() + " whispers privately: " + restMessage);
//                    found = true;
//                    atLeastOneSent = true;
//                }
//            }
//
//            if (!found) {
//                notFound.add(target);
//            }
//        }
//
//        // Notify about any targets that were not found
//        for (String target : notFound) {
//            client.notify("400 Bad Request");
//            client.notify("There is no client named " + target);
//        }
//
//        // Notify the sender about the success of the operation
//        if (atLeastOneSent) {
//            client.notify("You whisper to group: " + restMessage);
//        }
//    }
//}