package dev.coralwombat.jdbcpool.pool;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import dev.coralwombat.jdbcpool.exception.JdbcPoolException;
import dev.coralwombat.jdbcpool.properties.JdbcProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdbcPoolManagerTest {

	private static Properties properties;

	@BeforeAll
	static void beforeAll() throws IOException {
		try (InputStream in = JdbcProperties.class.getClassLoader().getResourceAsStream("jdbcpool.properties")) {
			properties = new Properties();
			properties.load(in);
		}
	}

	@Test
	@Order(Integer.MIN_VALUE)
	void testRegisterJdbcPool() {
		BasicJdbcPool bjp1 = new BasicJdbcPool(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.user"), properties.getProperty("jdbc.pass"));
		bjp1.setInitialCapacity(3);
		bjp1.setMinimumCapacity(1);
		bjp1.setMaximumCapacity(5);
		assertDoesNotThrow(() -> JdbcPoolManager.registerJdbcPool(bjp1));

		BasicJdbcPool bjp2 = new BasicJdbcPool(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.user"), properties.getProperty("jdbc.pass"));
		bjp2.setInitialCapacity(3);
		bjp2.setMinimumCapacity(1);
		bjp2.setMaximumCapacity(5);
		assertThrows(JdbcPoolException.class, () -> JdbcPoolManager.registerJdbcPool(bjp2));
		assertDoesNotThrow(() -> JdbcPoolManager.registerJdbcPool(bjp2, "2"));
		assertThrows(JdbcPoolException.class, () -> JdbcPoolManager.registerJdbcPool(bjp2, "2"));
	}

	@Test
	void testGetConnection() {
		Connection connection = JdbcPoolManager.getConnection();
		assertNotNull(connection);
		JdbcPoolManager.releaseConnection(connection);

		Connection connection2 = JdbcPoolManager.getConnection("2");
		assertNotNull(connection2);
		JdbcPoolManager.releaseConnection(connection2);
	}

	@Test
	void testReleaseConnection() {
		Connection connection = JdbcPoolManager.getConnection();
		assertDoesNotThrow(() -> JdbcPoolManager.releaseConnection(connection));

		Connection connection2 = JdbcPoolManager.getConnection("2");
		assertDoesNotThrow(() -> JdbcPoolManager.releaseConnection(connection2));
	}

	@Test
	@Order(Integer.MAX_VALUE)
	void testTestConnections() {
		assertDoesNotThrow(JdbcPoolManager::testConnections);
	}
}