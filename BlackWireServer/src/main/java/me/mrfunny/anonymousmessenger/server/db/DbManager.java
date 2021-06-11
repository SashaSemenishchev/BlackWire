package me.mrfunny.anonymousmessenger.server.db;

public class DbManager {
    public DbManager() throws ClassNotFoundException{
        Class.forName("org.sql");
    }
}
