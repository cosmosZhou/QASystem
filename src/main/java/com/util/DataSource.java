package com.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import oracle.jdbc.pool.OracleDataSource;

// http://repo1.maven.org/maven2/com/zaxxer/HikariCP-java7/2.4.8/
public class DataSource implements AutoCloseable {

	abstract public class Invoker<TYPE> {
		protected abstract TYPE invoke() throws Exception;

		public DataSource getDataSource() throws Exception {
			return DataSource.this;
		}

		public TYPE execute() throws Exception {
			TYPE obj = null;
			open();
			try {
				obj = invoke();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				close();
			}
			return obj;
		}

	}

	abstract public class Executor {
		public Executor() throws Exception {
			open();
			try {
				invoke();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				close();
			}
		}

		protected abstract void invoke() throws Exception;

		public DataSource getDataSource() throws Exception {
			return DataSource.this;
		}

	}

	private HikariDataSource ds;
	Connection con = null;

	public DatabaseMetaData getDatabaseMetaData() throws SQLException {
		return con.getMetaData();
	}

	int cnt = 0;

	public synchronized DataSource open() throws Exception {
		++cnt;
		if (con != null) {
			log.info("Connection is already opened. cnt = " + cnt);
			// the Connection might be shut down automatically if it is not
			// used for a long time;
			if (con.isClosed()) {
				con = getConnection();
			}
			return this;
		}

		// Class.forName("com.mysql.jdbc.Driver");
		// con = DriverManager.getConnection(url, user, password);
		con = getConnection();
		log.info("Connection is opened.");
		return this;
	}

	public synchronized void close() throws Exception {
		--cnt;
		if (con == null || cnt < 0) {
			log.info("Connection is already closed. cnt = " + cnt);
			return;
		}

		if (cnt == 0) {
			con.close();
			con = null;
			// System.gc();
		}
		log.info("Connection is closed.");
	}

	public class Query implements Iterable<ResultSet>, Iterator<ResultSet> {

		public Query(String sql) throws SQLException {
			// log.info("Query : " + sql);
			prepareStatement(sql);
		}

		public void prepareStatement(String sql) throws SQLException {
			preparedStatement = con.prepareStatement(sql);
			result = preparedStatement.executeQuery();
		}

		ResultSet result;
		PreparedStatement preparedStatement;

		public void close() throws SQLException {
			result.close();
			preparedStatement.close();
		}

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			try {
				if (result.next())
					return true;
				else {
					close();
					return false;
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public ResultSet next() {
			// TODO Auto-generated method stub
			return result;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub

		}

		@Override
		public Iterator<ResultSet> iterator() {
			// TODO Auto-generated method stub
			return this;
		}
	}

	public boolean execute(String sql) throws SQLException {
		log.info("sql: " + sql);
		boolean ret = false;
		PreparedStatement preparedStatement = con.prepareStatement(sql);
		try {
			ret = preparedStatement.execute();
		} catch (Exception e) {
			preparedStatement.close();
			e.printStackTrace();
			throw e;
		} finally {
			preparedStatement.close();
		}

		return ret;
	}

	public class BatchExecutive {
		public BatchExecutive() throws SQLException {
			statement = con.createStatement();
		}

		Statement statement;

		public void addBatch(String sql) throws SQLException {
			statement.addBatch(sql);
		}

		public void executeBatch() throws SQLException {
			try {
				statement.executeBatch();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				statement.close();
			}
		}
	}

	protected int[] execute(String sql[]) throws SQLException {
		return execute(sql, sql.length);
	}

	protected int[] execute(String sql[], int length) throws SQLException {
		Statement statement = con.createStatement();

		int[] res = null;
		try {
			for (int i = 0; i < length; ++i) {
				log.info("sql: " + sql[i]);
				if (sql[i] == null)
					throw new Exception("null sql occurred.");
				statement.addBatch(sql[i]);
			}
			res = statement.executeBatch();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			statement.close();
		}
		// con.commit();

		return res;
	}

	static public enum Driver {
		mysql, oracle
	}

	/**
	 * 初始化连接池
	 * 
	 * @param minimum
	 * @param Maximum
	 * @throws SQLException
	 */
	public DataSource(String url, String user, String password, Driver driver) {
		int minimum = 10;
		int maximum = 50;
		// 连接池配置
		HikariConfig config = new HikariConfig();
		switch (driver) {
		case mysql:
			config.setDriverClassName("com.mysql.jdbc.Driver");
			String value[] = { "user", "password", "true", "utf-8", "true", };
			String key[] = { "user", "password", "useUnicode", "characterEncoding", "autoReconnect", };

			value[0] = user;
			value[1] = password;
			for (int i = 0; i < value.length; ++i) {
				url += '&' + key[i] + '=' + value[i];
			}

			config.setJdbcUrl(url);
			log.info("url = " + url);
			config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
			config.setConnectionTestQuery("SELECT 1");

			break;
		case oracle:
			// config.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
			// config.setDataSourceClassName("oracle.jdbc.pool.OracleDataSource");
			config.setDriverClassName("oracle.jdbc.driver.OracleDriver");

			OracleDataSource dataSource = null;
			try {
				dataSource = new OracleDataSource();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			log.info("url = " + url);
			dataSource.setURL(url);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			config.setDataSource(dataSource);
			// config.setConnectionTestQuery("SELECT 1");
			break;
		}

		config.addDataSourceProperty("cachePrepStmts", true);
		config.addDataSourceProperty("prepStmtCacheSize", 500);

		config.setAutoCommit(true);
		// 池中最小空闲链接数量
		config.setMinimumIdle(minimum);
		// 池中最大链接数量
		config.setMaximumPoolSize(maximum);

		ds = new HikariDataSource(config);
	}

	public DataSource(String url, String user, String password, Driver driver, String serverTimezone) {
		int minimum = 10;
		int maximum = 50;
		// pool configuration
		HikariConfig config = new HikariConfig();
		switch (driver) {
		case mysql:
			config.setDriverClassName("com.mysql.jdbc.Driver");
			String value[] = { user, password, "true", "utf-8", "true", serverTimezone};
			String key[] = { "user", "password", "useUnicode", "characterEncoding", "autoReconnect", "serverTimezone"};

			for (int i = 0; i < value.length; ++i) {
				url += '&' + key[i] + '=' + value[i];
			}

			config.setJdbcUrl(url);
			log.info("url = " + url);
			config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
			config.setConnectionTestQuery("SELECT 1");

			break;
		case oracle:
			// config.setDriverClassName("org.hsqldb.jdbc.JDBCDriver");
			// config.setDataSourceClassName("oracle.jdbc.pool.OracleDataSource");
			config.setDriverClassName("oracle.jdbc.driver.OracleDriver");

			OracleDataSource dataSource = null;
			try {
				dataSource = new OracleDataSource();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			log.info("url = " + url);
			dataSource.setURL(url);
			dataSource.setUser(user);
			dataSource.setPassword(password);
			config.setDataSource(dataSource);
			// config.setConnectionTestQuery("SELECT 1");
			break;
		}

		config.addDataSourceProperty("cachePrepStmts", true);
		config.addDataSourceProperty("prepStmtCacheSize", 500);

		config.setAutoCommit(true);
		// 池中最小空闲链接数量
		config.setMinimumIdle(minimum);
		// 池中最大链接数量
		config.setMaximumPoolSize(maximum);

		ds = new HikariDataSource(config);
	}

	/**
	 * 销毁连接池
	 */
	@SuppressWarnings("deprecation")
	public void shutdown() {
		ds.shutdown();
	}

	/**
	 * 从连接池中获取链接
	 * 
	 * @return
	 */
	public Connection getConnection() {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			ds.resumePool();
			return null;
		}
	}

	public static void test(String[] args) throws SQLException {
		String url = "jdbc:mysql://121.40.196.48:3306/ucc?";
		String user = "root";
		String password = "client1!";

		DataSource ds = new DataSource(url, user, password, Driver.mysql);
		Connection conn = ds.getConnection();

		// ......
		// 最后关闭链接
		conn.close();
	}

	public static Logger log = Logger.getLogger(Utility.class);
}
