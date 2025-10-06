import java.sql.*;

public class Database {
    private static final String URL = "jdbc:sqlite:cozythread.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC"); // โหลดไดรเวอร์
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    // สร้างตารางครั้งแรก
    private static void init() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS products (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL,
              description TEXT,
              category TEXT,
              care TEXT,
              price REAL,
              stock INTEGER,
              status TEXT,
              active INTEGER,  -- 1 = active, 0 = inactive
              size TEXT,
              color TEXT,
              image_path TEXT,
              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try (Connection c = get(); Statement st = c.createStatement()) {
            st.execute(sql);
        }
    }
}
