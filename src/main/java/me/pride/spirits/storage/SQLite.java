package me.pride.spirits.storage;

import me.pride.spirits.Spirits;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLite extends Database {
	public SQLite() {
		File file = StorageCache.STORAGE;
		try {
			file.createNewFile();
		} catch (IOException e) { }
	}
	
	@Override
	public Connection getConnection() {
		Connection connection = null;
		
		try {
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e) {
				Spirits.instance.getLogger().info("JDBC not found.");
			}
			connection = DriverManager.getConnection("jdbc:sqlite:storage.db");
			Spirits.instance.getLogger().info("ProjectKorraSpirits storage connected.");
			
		} catch (SQLException e) {
			Spirits.instance.getLogger().info("ProjectKorraSpirits storage cannot connect.");
		}
		return connection;
	}
}
