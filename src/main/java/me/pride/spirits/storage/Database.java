package me.pride.spirits.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class Database {
	private Connection connection;
	
	public abstract Connection getConnection();
	
	protected static final String
			INSERT_BOSS = "INSERT INTO uuids(id) VALUES(?)",
			DELETE_BOSS = "DELETE FROM uuids WHERE id=?",
			SELECT_BOSS = "SELECT id FROM uuids WHERE id=?",
			SELECT_ALL_BOSS = "SELECT id FROM uuids",

			INSERT_SPIRIT = "INSERT INTO spirit_uuids(spirit_id) VALUES(?)",
			DELETE_SPIRIT = "DELETE FROM spirit_uuids WHERE spirit_id=?",
			SELECT_SPIRIT = "SELECT spirit_id FROM spirit_uuids WHERE spirit_id=?",
			SELECT_ALL_SPIRIT = "SELECT spirit_id FROM spirit_uuids";
	
	public void init() {
		connection = getConnection();
		
		if (connection != null) {
			try {
				Statement statement = connection.createStatement();
				statement.execute("CREATE TABLE IF NOT EXISTS uuids(id)");
				statement.execute("CREATE TABLE IF NOT EXISTS spirit_uuids(spirit_id)");
				statement.close();
			} catch (SQLException e) {
				// table exists
			}
		}
	}
	protected void insertBoss(String... uuids) throws SQLException {
		PreparedStatement statement;
		if (uuids.length > 1) {
			statement = connection.prepareStatement(bossInsertions(uuids));
		} else {
			statement = connection.prepareStatement(INSERT_BOSS);
			for (String uuid : uuids) {
				statement.setString(1, uuid);
			}
		}
		statement.executeUpdate();
	}
	protected void insertSpirit(String... uuids) throws SQLException {
		PreparedStatement statement;
		if (uuids.length > 1) {
			statement = connection.prepareStatement(spiritInsertions(uuids));
		} else {
			statement = connection.prepareStatement(INSERT_SPIRIT);
			for (String uuid : uuids) {
				statement.setString(1, uuid);
			}
		}
		statement.executeUpdate();
	}
	protected void deleteBoss(String... uuids) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(DELETE_BOSS);
		for (String uuid : uuids) {
			statement.setString(1, uuid);
			statement.executeUpdate();
		}
	}
	protected void deleteSpirit(String... uuids) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(DELETE_SPIRIT);
		for (String uuid : uuids) {
			statement.setString(1, uuid);
			statement.executeUpdate();
		}
	}
	protected void deleteAllBosses() throws SQLException {
		connection.prepareStatement("DELETE FROM uuids").executeUpdate();
	}
	protected void deleteAllSpirits() throws SQLException {
		connection.prepareStatement("DELETE FROM spirit_uuids").executeUpdate();
	}
	protected ResultSet setBosses() throws SQLException {
		return connection.prepareStatement(SELECT_ALL_BOSS).executeQuery();
	}
	protected ResultSet setSpirits() throws SQLException {
		return connection.prepareStatement(SELECT_ALL_SPIRIT).executeQuery();
	}
	private String insertions(String s, String... uuids) {
		StringBuilder builder = new StringBuilder(s);
		for (int i = 0; i < uuids.length; i++) {
			if (i == uuids.length - 1) {
				builder.append(uuids[i] + "');");
			} else {
				builder.append(uuids[i] + "'),('");
			}
		}
		return builder.toString();
	}
	private String bossInsertions(String... uuids) {
		return insertions("INSERT INTO uuids(id) VALUES('", uuids);
	}
	private String spiritInsertions(String... uuids) {
		return insertions("INSERT INTO spirit_uuids(spirit_id) VALUES('", uuids);
	}
}
