package xyz.olivermartin.multichat.common.database;

import java.io.File;
import java.sql.SQLException;

import org.apache.commons.dbcp2.BasicDataSource;

public class SQLiteDatabase extends GenericDatabase {

	private static final String URL_PREFIX = "jdbc:sqlite:";

	private BasicDataSource ds;

	public SQLiteDatabase(File path, String filename) throws SQLException {
		super(URL_PREFIX + path + File.separator + filename);
	}

	protected boolean setupDatabase(String url) throws SQLException {
		connect();
		return true;
	}

	@Override
	protected void disconnect() throws SQLException {
		if (ds != null) {
			ds.close();
		}
	}

	@Override
	protected boolean connect() throws SQLException {

		ds = new BasicDataSource();
		ds.setUrl(url);
		ds.setMinIdle(5);
		ds.setMaxIdle(10);
		ds.setMaxOpenPreparedStatements(100);
		ds.getConnection();
		return true;
	}

	@Override
	public SimpleConnection getConnection() throws SQLException {
		return new SimpleConnection(ds.getConnection());
	}

}
