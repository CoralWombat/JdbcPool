package dev.coralwombat.jdbcpool.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A class to collect the properties of the JdbcPool
 */
public abstract class JdbcProperties {

	private JdbcProperties() {
	}

	private static final Properties properties = new Properties();

	static {
		try (InputStream in = JdbcProperties.class.getClassLoader().getResourceAsStream("jdbcpool.properties")) {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Looks up an {@link Integer} property. If not found returs the defaultValue.
	 * @param key The property that has to be looked up.
	 * @param defaultValue The value that has to be returned if the property was not found.
	 * @return The {@link Integer} value of the searched property. If not found: defaultValue.
	 */
	public static Integer getIntegerProperty(String key, Integer defaultValue) {
		return Integer.valueOf(properties.getProperty(key, defaultValue.toString()));
	}

}
