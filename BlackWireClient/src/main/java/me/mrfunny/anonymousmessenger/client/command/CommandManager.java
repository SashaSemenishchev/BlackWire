package me.mrfunny.anonymousmessenger.client.command;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final HashMap<CommandInfo, Runnable> commands = new HashMap<>();

    public CommandManager(){
        registerCommand(command("/help", "shows this message"), () -> {
            for(CommandInfo commandInfo : commands.keySet()){
                System.out.println(commandInfo.getCommand() + " - " + commandInfo.getDescription());
            }
        });
    }

    public void registerCommand(CommandInfo command, Runnable commandRunnable){
        commands.put(command, commandRunnable);
    }

    public CommandInfo command(String name, String description){
        return new CommandInfo(name, description);
    }

    public boolean parseInput(String input){
        if(input.startsWith("/")){
            for(CommandInfo commandInfo : commands.keySet()){
                if(commandInfo.getCommand().equals(input)){
                    commands.get(commandInfo).run();
                    return true;
                }
            }
            System.out.println("Unknown command. /help for list of commands");
            return true;
        }
        return false;
    }
}
