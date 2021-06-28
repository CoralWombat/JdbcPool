package dev.coralwombat.jdbcpool.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import dev.coralwombat.jdbcpool.exception.JdbcPoolException;
import dev.coralwombat.jdbcpool.properties.JdbcProperties;
import dev.coralwombat.jdbcpool.properties.PropertyKeys;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * A {@link BasicJdbcPool} that can manage multiple connections at the same time.
 */
@RequiredArgsConstructor
public class BasicJdbcPool implements JdbcPool {

	/**
	 * The connection url.
	 */
	@NonNull
	@Getter
	private final String url;

	/**
	 * The authentication username for the connection.
	 */
	@NonNull
	@Getter
	private final String user;

	/**
	 * The authentication password for the connection.
	 */
	@NonNull
	@Getter
	private final String password;

	/**
	 * The number of connections that has to be opened when calling the {@link BasicJdbcPool#construct()}.
	 */
	@Setter
	@Getter
	private int initialCapacity;

	/**
	 * The minimum number of connections that has to be opened at all times.
	 */
	@Setter
	@Getter
	private int minimumCapacity;

	/**
	 * The maximum number of connections that can be opened at all times.
	 */
	@Setter
	@Getter
	private int maximumCapacity;

	/**
	 * The connections that are not in use.
	 */
	private final List<Connection> availableConnections = new LinkedList<>();

	/**
	 * The connections that are in use.
	 */
	private final List<Connection> usedConnections = new LinkedList<>();

	@Override
	public Connection getConnection() {
		if (availableConnections.isEmpty() && maximumCapacity <= getSize()) {
			throw new JdbcPoolException("Could not get connection from connection pool because it is full.");
		}

		try {
			Connection connection;
			if (availableConnections.isEmpty()) {
				connection = createConnection();
			} else {
				connection = availableConnections.remove(0);
				if (isInvalidConnection(connection)) {
					connection = createConnection();
				}
			}
			usedConnections.add(connection);
			return connection;
		} catch (SQLException e) {
			throw new JdbcPoolException("Could not get connection from connection pool.", e);
		}
	}

	@Override
	public void releaseConnection(Connection connection) {
		usedConnections.remove(connection);
		availableConnections.add(connection);
	}

	/**
	 * Creates a connection.
	 * @return The connection that was created.
	 * @throws SQLException If the connection cannot be opened.
	 */
	private Connection createConnection() throws SQLException {
		return DriverManager.getConnection(url, user, password);
	}

	/**
	 * Checks a connection.
	 * @param connection The connection that has to be tested.
	 * @return true if the {@link Connection} is invalid, false otherwise.
	 */
	private boolean isInvalidConnection(Connection connection) {
		try {
			return !connection.isValid(JdbcProperties.getIntegerProperty(PropertyKeys.JDBC_TEST_CONNECTION_TIMEOUT, 5000));
		} catch (SQLException e) {
			return true;
		}
	}

	@Override
	public void testConnections() {
		try {
			availableConnections.removeIf(this::isInvalidConnection);
			while (getSize() < minimumCapacity) {
				availableConnections.add(createConnection());
			}
		} catch (SQLException e) {
			throw new JdbcPoolException("Could not test connections.", e);
		}
	}

	@Override
	public void construct() {
		precheckConstruct();

		try {
			while (getSize() < initialCapacity) {
				availableConnections.add(createConnection());
			}
		} catch (SQLException e) {
			throw new JdbcPoolException("Could not initialize connection pool.", e);
		}
	}

	/**
	 * Validates the parameters of the {@link BasicJdbcPool}.
	 */
	private void precheckConstruct() {
		if (initialCapacity < 0) throw new JdbcPoolException("The initial capacity cannot be negative.");
		if (minimumCapacity < 0) throw new JdbcPoolException("The minimum capacity cannot be negative.");
		if (maximumCapacity <= 0) throw new JdbcPoolException("The maximum capacity must be positive.");
		if (initialCapacity < minimumCapacity) {
			throw new JdbcPoolException("The initial capacity cannot be less than the minimum capacity.");
		}
		if (maximumCapacity < initialCapacity) {
			throw new JdbcPoolException("The maximum capacity cannot be less than the initial capacity.");
		}
	}

	@Override
	public void deconstruct() {
		for (Connection connection : usedConnections) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		usedConnections.clear();
		for (Connection connection : availableConnections) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		availableConnections.clear();
	}

	/**
	 * @return The current size of the {@link BasicJdbcPool}. This contains both available and used connections.
	 */
	public int getSize() {
		return availableConnections.size() + usedConnections.size();
	}

}
