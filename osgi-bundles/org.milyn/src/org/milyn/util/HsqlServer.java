package org.milyn.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.Server;
import org.hsqldb.ServerConstants;
import org.hsqldb.jdbcDriver;
import org.milyn.io.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author
 */
public class HsqlServer {

    private static Log logger = LogFactory.getLog(HsqlServer.class);

    private Server hsqlServer;

    private String url;
    private String username = "sa";
    private String password = "";

    private Connection connection;

    public HsqlServer(final int port) throws Exception {
        final String databaseName = "milyn-hsql-" + port;

        url = "jdbc:hsqldb:hsql://localhost:" + port + "/" + databaseName;
        logger.info("Starting Hypersonic Database '" + url + "'.");
        new Thread() {
            @Override
            public void run() {
                Server server = new Server();
                Log targetLogger = LogFactory.getLog("org.hsqldb");
                server.setLogWriter(new PrintWriter(new StdoutToLog4jFilter(server.getLogWriter(), targetLogger)));
                server.setDatabasePath(0, "target/hsql/" + databaseName);
                server.setDatabaseName(0, databaseName);
                server.setNoSystemExit( true );
                server.setSilent( true );
                server.setPort(port);
                server.start();


                hsqlServer = server;
            }
        }.start();

        while(hsqlServer == null) {
            Thread.sleep(50);
        }

        DriverManager.registerDriver(new jdbcDriver());
        connection = DriverManager.getConnection(url, username, password);
    }

    public void stop() throws Exception {
        try {
            hsqlServer.signalCloseAllServerConnections();
            //connection.close();
        } finally {
            hsqlServer.stop();
            while( hsqlServer.getState() != ServerConstants.SERVER_STATE_SHUTDOWN) {
                Thread.sleep(100L);
            }
        }
    }

    public boolean execScript(InputStream script) throws SQLException {
        String scriptString;
        try {
            scriptString = StreamUtils.readStream(new InputStreamReader(script));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Statement statement = connection.createStatement();
        try {
            return statement.execute(scriptString);
        } finally {
            statement.close();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getState() {
        if (hsqlServer == null) {
            throw new IllegalStateException("hsqlServer was null. Perhaps there was an error upon startup?");
        }
        return hsqlServer.getState();
    }
}
