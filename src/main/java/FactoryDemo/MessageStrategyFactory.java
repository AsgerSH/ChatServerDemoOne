package FactoryDemo;

import java.util.HashMap;
import java.util.Map;

public class MessageStrategyFactory {
    private static Map<String, IMessageStrategy> strategies = new HashMap();
    static {
        strategies.put("#JOIN", new JoinStrategy());
        strategies.put("#MESSAGE", new MessageStrategy());
        strategies.put("#WHISPER", new WhisperStrategy());
        strategies.put("#GETLIST", new GetListStrategy());
        strategies.put("#GROUP", new GroupStrategy());
        strategies.put("#HELP", new HelpStrategy());
        strategies.put("#LEAVE", new LeaveStrategy());
        strategies.put("#SHUTDOWN", new ShutdownStrategy());
    }

    public static IMessageStrategy getStrategy(String strategy) {
        return strategies.getOrDefault(strategy, new IMessageStrategy() {
            @Override
            public void execute(String message, ClientHandler client){
                client.notify("Sorry no command with the name: " +  message);
            }
        });
    }

    public static String[] getAllStrategyNames(){
        return strategies.keySet().toArray(new String[0]);
    }
}
