package dev.coralwombat.jdbcpool.exception;

public class JdbcPoolException extends RuntimeException {

	public JdbcPoolException(String message) {
		super(message);
	}

	public JdbcPoolException(String message, Exception cause) {
		super(message, cause);
	}

}
