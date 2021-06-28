package dev.coralwombat.jdbcpool.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import dev.coralwombat.jdbcpool.exception.JdbcPoolException;
import lombok.NonNull;

/**
 * The abstract class that collects all {@link JdbcPool}s.
 */
public abstract class JdbcPoolManager {

	private JdbcPoolManager() {
	}

	private static final Map<String, JdbcPool> jdbcPools = new HashMap<>();

	private static final Map<String, String> poolKeyDictionary = new HashMap<>();

	/**
	 * Gets a connection from a {@link JdbcPool}.
	 * @param poolId - The id of the {@link JdbcPool}.
	 * @return A connection from the {@link JdbcPool} identified by the poolId.
	 */
	public static Connection getConnection(@NonNull String poolId) {
		return jdbcPools.get(poolId).getConnection();
	}

	/**
	 * Gets a connection from a {@link JdbcPool}.
	 * @return A connection from the default {@link JdbcPool}.
	 */
	public static Connection getConnection() {
		return jdbcPools.get("default").getConnection();
	}

	/**
	 * Releases a connection.
	 * @param connection The connection to release.
	 */
	public static void releaseConnection(Connection connection) {
		try {
			String key = poolKeyDictionary.get(getConnectionId(connection));
			jdbcPools.get(key).releaseConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Registrates a {@link JdbcPool}.
	 * @param jdbcPool The {@link JdbcPool} to register.
	 * @param key The key that the {@link JdbcPool} has to be registered to.
	 */
	public static void registerJdbcPool(@NonNull JdbcPool jdbcPool, @NonNull String key) {
		if (jdbcPools.containsKey(key)) {
			throw new JdbcPoolException("JdbcPool is already registered with key: " + key + ".");
		}

		try {
			jdbcPool.construct();
			var connection = jdbcPool.getConnection();
			poolKeyDictionary.put(getConnectionId(connection), key);
			jdbcPool.releaseConnection(connection);

			jdbcPools.put(key, jdbcPool);
		} catch (SQLException e) {
			throw new JdbcPoolException("Could not register JdbcPool.", e);
		}
	}

	/**
	 * Registrates a {@link JdbcPool} to the default key.
	 * @param jdbcPool The {@link JdbcPool} to register.
	 */
	public static void registerJdbcPool(@NonNull JdbcPool jdbcPool) {
		registerJdbcPool(jdbcPool, "default");
	}

	/**
	 * Tests all the connections in all {@link JdbcPool}s.
	 */
	public static void testConnections() {
		for (Map.Entry<String, JdbcPool> entry : jdbcPools.entrySet()) {
			entry.getValue().testConnections();
		}
	}

	private static String getConnectionId(Connection connection) throws SQLException {
		return connection.getMetaData().getURL() + ";" + connection.getMetaData().getUserName();
	}

}
