package dev.coralwombat.jdbcpool.pool;

import java.sql.Connection;

/**
 * An interface that contains every methot a {@link JdbcPool} shall contain.
 */
public interface JdbcPool {

	/**
	 * Initializes the {@link JdbcPool}. After calling this method the {@link JdbcPool#getConnection()} should not fail.
	 */
	void construct();

	/**
	 * Decostructs the {@link JdbcPool}. After this every connection should be closed.
	 */
	void deconstruct();

	/**
	 * Looks up a {@link Connection} from the pool.
	 * @return A useable {@link Connection} that later has to be released.
	 */
	Connection getConnection();

	/**
	 * Releases the connection by making it available in the pool for later usage.
	 * @param connection The {@link Connection} to release.
	 */
	void releaseConnection(Connection connection);

	/**
	 * Tests all the connection in the {@link JdbcPool} and removes the invalid ones from the pool.
	 */
	void testConnections();

}
