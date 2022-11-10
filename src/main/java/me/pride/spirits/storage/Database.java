package me.pride.spirits.storage;

import java.sql.*;
import java.util.UUID;

public abstract class Database {
	private Connection connection;
	
	public abstract Connection getConnection();
	
	protected static final String
			INSERT = "INSERT INTO uuids(id) VALUES(?)",
			DELETE = "DELETE FROM uuids WHERE id=?",
			SELECT = "SELECT id FROM uuids WHERE id=?",
			SELECT_ALL = "SELECT id FROM uuids";
	
	public void init() {
		connection = getConnection();
		
		if (connection != null) {
			try {
				Statement statement = connection.createStatement();
				statement.execute("CREATE TABLE IF NOT EXISTS uuids(id)");
				statement.close();
			} catch (SQLException e) {
				// table exists
			}
		}
	}
	protected void insert(String... uuids) throws SQLException {
		PreparedStatement statement;
		if (uuids.length > 1) {
			statement = connection.prepareStatement(insertions(uuids));
		} else {
			statement = connection.prepareStatement(INSERT);
			for (String uuid : uuids) {
				statement.setString(1, uuid);
			}
		}
		statement.executeUpdate();
	}
	protected void delete(String... uuids) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(DELETE);
		for (String uuid : uuids) {
			statement.setString(1, uuid);
			statement.executeUpdate();
		}
	}
	protected void deleteAll() throws SQLException {
		connection.prepareStatement("DELETE FROM uuids").executeUpdate();
	}
	protected ResultSet set() throws SQLException {
		return connection.prepareStatement(SELECT_ALL).executeQuery();
	}
	private String insertions(String... uuids) {
		StringBuilder builder = new StringBuilder("INSERT INTO uuids(id) VALUES('");
		for (int i = 0; i < uuids.length; i++) {
			if (i == uuids.length - 1) {
				builder.append(uuids[i] + "');");
			} else {
				builder.append(uuids[i] + "'),('");
			}
		}
		return builder.toString();
	}
}
