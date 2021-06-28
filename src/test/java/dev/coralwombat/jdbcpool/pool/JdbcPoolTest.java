package dev.coralwombat.jdbcpool.pool;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import dev.coralwombat.jdbcpool.exception.JdbcPoolException;
import dev.coralwombat.jdbcpool.properties.JdbcProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JdbcPoolTest {

	private static Properties properties;

	private BasicJdbcPool bjp;

	@BeforeAll
	static void beforeAll() throws IOException {
		try (InputStream in = JdbcProperties.class.getClassLoader().getResourceAsStream("jdbcpool.properties")) {
			properties = new Properties();
			properties.load(in);
		}
	}

	@BeforeEach
	void beforeEach() {
		bjp = new BasicJdbcPool(properties.getProperty("jdbc.url"), properties.getProperty("jdbc.user"), properties.getProperty("jdbc.pass"));
		bjp.setInitialCapacity(3);
		bjp.setMinimumCapacity(1);
		bjp.setMaximumCapacity(5);
		bjp.construct();
	}

	@AfterEach
	void afterEach() {
		bjp.deconstruct();
	}

	@Test
	void testBasicJdbcPool() throws SQLException {
		Connection c = bjp.getConnection();

		try (Statement stmt = c.createStatement();
			 ResultSet rs = stmt.executeQuery("select 1 from dual")) {
			rs.next();
			assertNotNull(rs.getObject(1));
		}

		bjp.releaseConnection(c);
	}

	@Test
	void testGetAndReleaseConnection() {
		Connection c1 = bjp.getConnection();
		Connection c2 = bjp.getConnection();
		Connection c3 = bjp.getConnection();
		Connection c4 = bjp.getConnection();
		Connection c5 = bjp.getConnection();
		assertEquals(5, bjp.getSize());

		assertThrows(JdbcPoolException.class, () -> bjp.getConnection());
		assertEquals(5, bjp.getSize());
		bjp.releaseConnection(c5);
		assertDoesNotThrow(() -> bjp.getConnection());
		assertEquals(5, bjp.getSize());
	}

	@Test
	void testTestConnections() throws SQLException {
		Connection c1 = bjp.getConnection();
		c1.close();
		bjp.releaseConnection(c1);
		assertEquals(3, bjp.getSize());
		bjp.testConnections();
		assertEquals(2, bjp.getSize());

		Connection c2 = bjp.getConnection();
		c2.close();
		bjp.releaseConnection(c2);
		assertEquals(2, bjp.getSize());
		bjp.testConnections();
		assertEquals(1, bjp.getSize());

		Connection c3 = bjp.getConnection();
		c3.close();
		bjp.releaseConnection(c3);
		assertEquals(1, bjp.getSize());
		bjp.testConnections();
		assertEquals(1, bjp.getSize());
	}

	@Test
	void testConstructDeonstruct(){
		bjp.construct();
		assertEquals(3, bjp.getSize());

		bjp.deconstruct();
		assertEquals(0, bjp.getSize());
	}

}