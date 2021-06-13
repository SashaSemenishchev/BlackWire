package me.mrfunny.anonymousmessenger.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbManager {
    private final Connection connection;

    public DbManager() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:db.db");
        PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS users (`id` INTEGER PRIMARY KEY, `username` TEXT NOT NULL UNIQUE, `password` TEXT NOT NULL, `publickey` TEXT NOT NULL);");
        statement.execute();
        statement.close();
        PreparedStatement statement1 = connection.prepareStatement("CREATE TABLE IF NOT EXISTS waitingMessages(`id` INTEGER PRIMARY KEY, `sender` TEXT NOT NULL, `receiver` TEXT NOT NULL, `message` TEXT NOT NULL );");
        statement1.execute();
        statement1.close();
    }

    public Connection getConnection() {
        return connection;
    }
}
