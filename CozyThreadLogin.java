import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class CozyThreadLogin extends JFrame {
    private static final String VALID_USER = "ying";
    private static final String VALID_PASS = "9999";

    public CozyThreadLogin() {
        setTitle("เข้าสู่ระบบ");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // HEADER/FOOTER โค้งชมพู
        HeaderPanel header = new HeaderPanel();
        header.setPreferredSize(new Dimension(0, 190));
        root.add(header, BorderLayout.NORTH);

        RoundedBar footer = new RoundedBar(new Color(0xFAD1D1), 24);
        footer.setPreferredSize(new Dimension(0, 72));
        root.add(footer, BorderLayout.SOUTH);

        // ฟอร์มตรงกลาง
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        root.add(center, BorderLayout.CENTER);

        JTextField user = textField("Username");
        JPasswordField pass = passwordField("Password");
        JButton login = blueButton("Login");

        center.add(Box.createVerticalStrut(80));
        center.add(wrap(user));
        center.add(Box.createVerticalStrut(28));
        center.add(wrap(pass));
        center.add(Box.createVerticalStrut(46));
        center.add(login);

        login.addActionListener(e -> {
            String u = user.getText().trim();
            String p = new String(pass.getPassword());
            if (VALID_USER.equals(u) && VALID_PASS.equals(p)) {
                JOptionPane.showMessageDialog(this, "Login successful ✅", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Username or password is incorrect. ❌", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    // ---------- UI Components ----------

    private static JPanel wrap(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    private static JTextField textField(String placeholder) {
        JTextField tf = new JTextField();
        baseField(tf, placeholder, false);
        return tf;
    }

    private static JPasswordField passwordField(String placeholder) {
        JPasswordField pf = new JPasswordField();
        baseField(pf, placeholder, true);
        return pf;
    }

    private static void baseField(JTextField field, String placeholder, boolean isPassword) {
        field.setPreferredSize(new Dimension(720, 70));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        field.setBackground(Color.WHITE);
        field.setBorder(compoundBorder(new Color(0xC9C9C9)));
        field.setForeground(new Color(0x9E9E9E));
        field.setText(placeholder);

        if (isPassword) ((JPasswordField) field).setEchoChar((char) 0);

        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(new Color(0x222222));
                    if (isPassword) ((JPasswordField) field).setEchoChar('•');
                }
                field.setBorder(compoundBorder(new Color(0x168BFF))); // โฟกัส=เส้นน้ำเงิน
            }
            @Override public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(0x9E9E9E));
                    if (isPassword) ((JPasswordField) field).setEchoChar((char) 0);
                }
                field.setBorder(compoundBorder(new Color(0xC9C9C9)));
            }
        });
    }

    private static Border compoundBorder(Color line) {
        return new CompoundBorder(
            new LineBorder(line, 2, true),
            new EmptyBorder(0, 18, 0, 18)
        );
    }

    private static JButton blueButton(String text) {
    JButton b = new JButton(text) {
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int arc = 44;
            boolean isHover = getModel().isRollover(); // ใช้สถานะ hover ของปุ่ม

            // เงา
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(8, 10, getWidth() - 16, getHeight() - 16, arc, arc);

            // พื้นปุ่ม (ไล่เฉดเล็กน้อยให้ดูหรู)
            Color base = isHover ? new Color(0x2A9CFF) : new Color(0x168BFF);
            GradientPaint gp = new GradientPaint(0, 0, base.brighter(), 0, getHeight(), base);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            super.paintComponent(g);
            g2.dispose();
        }
        @Override public void update(Graphics g) { paint(g); }
    };

    b.setFont(new Font("Segoe UI", Font.BOLD, 30));
    b.setForeground(Color.WHITE);
    b.setOpaque(false);
    b.setContentAreaFilled(false);
    b.setBorderPainted(false);
    b.setFocusPainted(false);
    b.setRolloverEnabled(true);
    b.setPreferredSize(new Dimension(500, 80)); // ปรับให้ยาวและสูงขึ้น
    b.setAlignmentX(Component.CENTER_ALIGNMENT);

    // **ลบ MouseListener เดิมทิ้งได้เลย** ไม่จำเป็นอีกต่อไป
    return b;
}


    // แถบโค้งชมพู (Footer)
    private static class RoundedBar extends JPanel {
        private final Color bg; private final int radius;
        RoundedBar(Color bg, int radius) { this.bg = bg; this.radius = radius; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Header วาดพื้นชมพู + ข้อความตรงกลางพร้อมเงา (ไม่โดนครอป)
    private static class HeaderPanel extends JPanel {
        HeaderPanel() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // พื้นชมพูโค้ง
            g2.setColor(new Color(0xFAD1D1));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 48, 48);

            // วาดข้อความใหญ่ตรงกลาง
            String text = "CozyThread";
            Font font = new Font("Georgia", Font.BOLD, 120);
            g2.setFont(font);

            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(text);
            int textH = fm.getAscent();

            int x = (getWidth() - textW) / 2;
            int y = (getHeight() + textH) / 2 - 10; // ยกขึ้นนิดให้บาลานซ์

            // เงา
            g2.setColor(new Color(255, 153, 153));
            g2.drawString(text, x + 10, y + 10);
            // ตัวอักษรหลัก
            g2.setColor(new Color(0xF28BA1));
            g2.drawString(text, x, y);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CozyThreadLogin().setVisible(true));
    }
}
