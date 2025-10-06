import java.sql.*;
import java.util.*;

public class ProductDao {

    public int insert(Product p) throws SQLException {
        String sql = """
            INSERT INTO products
            (name, description, category, care, price, stock, status, active, size, color, image_path)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (Connection c = Database.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.name);
            ps.setString(2, p.description);
            ps.setString(3, p.category);
            ps.setString(4, p.care);
            if (p.price == null) ps.setNull(5, Types.REAL); else ps.setDouble(5, p.price);
            if (p.stock == null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, p.stock);
            ps.setString(7, p.status);
            ps.setInt(8, p.active ? 1 : 0);
            ps.setString(9, p.size);
            ps.setString(10, p.color);
            ps.setString(11, p.imagePath);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    /** ดึงรายชื่อ category ที่มีอยู่ใน DB เพื่อนำไปเติม dropdown */
    public List<String> distinctCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM products WHERE category IS NOT NULL AND TRIM(category)<>'' ORDER BY category";
        List<String> out = new ArrayList<>();
        try (Connection c = Database.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(rs.getString(1));
        }
        return out;
    }
}
