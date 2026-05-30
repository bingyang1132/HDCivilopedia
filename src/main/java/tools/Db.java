package tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared SQLite connection holder.
 *
 * Opening a SQLite connection is expensive (opens the file, reads schema). The
 * original code created a brand new {@link DriverManager#getConnection} for
 * every text/icon lookup and every model load, which dominated runtime.
 *
 * This class keeps one long-lived {@link Connection} per database URL and hands
 * it back on demand. Callers still create their own {@link java.sql.Statement}
 * exactly as before (so ResultSet semantics are unchanged); only the underlying
 * connection is reused. Call {@link #shutdown()} once at the end of the run.
 *
 * NOTE: a SQLite {@link Connection} is not thread-safe. This holder is fine for
 * the current single-threaded pipeline. If model loading is parallelized later,
 * switch the cache to a {@link ThreadLocal} per database.
 */
public final class Db {

    private static final Map<String, Connection> CONNECTIONS = new ConcurrentHashMap<>();

    private Db() {
    }

    /** Returns a reused read connection for the given JDBC URL, opening it on first use. */
    public static synchronized Connection conn(String url) {
        Connection c = CONNECTIONS.get(url);
        try {
            if (c == null || c.isClosed()) {
                c = DriverManager.getConnection(url);
                CONNECTIONS.put(url, c);
            }
        } catch (Exception e) {
            throw new RuntimeException("failed to open database connection: " + url, e);
        }
        return c;
    }

    /** Closes all open connections. Call once when the whole run finishes. */
    public static synchronized void shutdown() {
        for (Connection c : CONNECTIONS.values()) {
            try {
                if (c != null && !c.isClosed()) {
                    c.close();
                }
            } catch (Exception e) {
                // ignore on shutdown
            }
        }
        CONNECTIONS.clear();
    }
}
