package me.mrfunny.anonymousmessenger.client.commands;

public class CommandInfo {
    private final String command;
    private final String description;

    public CommandInfo(String command, String description) {
        this.command = command;
        this.description = description;
    }

    public String getCommand() {
        return command;
    }

    public String getDescription() {
        return description;
    }
}
