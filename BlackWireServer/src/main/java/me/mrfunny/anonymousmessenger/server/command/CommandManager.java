package me.mrfunny.anonymousmessenger.server.command;

import me.mrfunny.anonymousmessenger.server.MessageSocket;

import java.util.HashMap;
import java.util.function.Consumer;

public class CommandManager {
    private final HashMap<CommandInfo, Consumer<MessageSocket.Client>> commands = new HashMap<>();

    public void registerCommand(CommandInfo command, Consumer<MessageSocket.Client> commandRunnable){
        commands.put(command, commandRunnable);
    }

    public CommandInfo command(String name, String description){
        return new CommandInfo(name, description);
    }

    public boolean parseInput(String input, MessageSocket.Client client){
        if(input.startsWith("/")){
            for(CommandInfo commandInfo : commands.keySet()){
                if(commandInfo.getCommand().equals(input)){
                    commands.get(commandInfo).accept(client);
                    client.sendMessage("");
                    return true;
                }
            }
            client.sendMessage("Unknown command. /help for list of commands");
            return true;
        }
        return false;
    }

    public HashMap<CommandInfo, Consumer<MessageSocket.Client>> getCommands() {
        return commands;
    }
}
