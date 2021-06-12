package me.mrfunny.anonymousmessenger.server;

public class SuperSecretClass {
    private static final String serverSecret = "Yoursupersecretkey";

    public static String getServerSecret() {
        return serverSecret;
    }
}
