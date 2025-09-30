import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** หน้าหลังเข้าสู่ระบบ: MENU + 6 ปุ่มวงรี */
public class MenuFrame extends JFrame {

    public MenuFrame() {
        setTitle("Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));
        setContentPane(root);

        // ---------- Header + profile icon (overlay) ----------
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new OverlayLayout(top));

        HeaderPanel header = new HeaderPanel("MENU");
        header.setPreferredSize(new Dimension(0, 150));
        header.setAlignmentX(0.5f);
        header.setAlignmentY(0.5f);

        JPanel profileLayer = new JPanel(new BorderLayout());
        profileLayer.setOpaque(false);
        profileLayer.setAlignmentX(0.5f);
        profileLayer.setAlignmentY(0.5f);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 16));
        right.setOpaque(false);
        right.add(profileButton(44));
        profileLayer.add(right, BorderLayout.NORTH);

        top.add(profileLayer);
        top.add(header);
        root.add(top, BorderLayout.NORTH);

        // ---------- Button grid 2 x 3 ----------
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        root.add(grid, BorderLayout.CENTER);

        String[] left  = {"Add Product", "Marketing", "After-Sales"};
        String[] rightCol = {"Inventory", "Orders", "Reports"};

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(24, 24, 24, 24);
        gc.gridx = 0; gc.gridy = 0;
        for (int i = 0; i < 3; i++) {
            grid.add(pillButton(left[i]), gc);
            gc.gridy++;
        }
        gc.gridx = 1; gc.gridy = 0;
        for (int i = 0; i < 3; i++) {
            grid.add(pillButton(rightCol[i]), gc);
            gc.gridy++;
        }
    }

    // ---------- Components ----------
    private static JButton pillButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 48;
                // fill
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
                // border
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(getModel().isRollover() ? new Color(0x9E9E9E) : new Color(0xC9C9C9));
                g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, arc, arc);

                super.paintComponent(g);
                g2.dispose();
            }
            @Override public void update(Graphics g) { paint(g); }
        };
        b.setFont(new Font("Georgia", Font.BOLD, 36));
        b.setForeground(new Color(0xF28BA1));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(360, 92)); // ปรับความกว้าง/สูงของเมนูได้ที่นี่
        b.addActionListener(e -> JOptionPane.showMessageDialog(null, text + " — coming soon.", "Info", JOptionPane.INFORMATION_MESSAGE));
        return b;
    }

    private static JButton profileButton(int size) {
        JButton p = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight(), d = Math.min(w,h);

                // Outer black circle
                g2.setColor(Color.BLACK);
                g2.fillOval(0, 0, d, d);

                // Inner white head/shoulders
                g2.setColor(Color.WHITE);
                // head
                g2.fillOval(d/2 - d/6, d/5, d/3, d/3);
                // shoulders
                g2.fillRoundRect(d/6, d/2, (int)(d*0.66), d/3, d/3, d/3);

                g2.dispose();
            }
            @Override public void update(Graphics g) { paint(g); }
        };
        p.setPreferredSize(new Dimension(size, size));
        p.setContentAreaFilled(false);
        p.setBorderPainted(false);
        p.setFocusPainted(false);
        p.setOpaque(false);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return p;
    }

    // Pink rounded header + centered title with shadow
    static class HeaderPanel extends JPanel {
        private final String title;
        HeaderPanel(String title) { this.title = title; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0xFAD1D1));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 48, 48);

            Font font = new Font("Georgia", Font.BOLD, 120);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(title);
            int h = fm.getAscent();
            int x = (getWidth() - w) / 2;
            int y = (getHeight() + h) / 2 - 10;

            g2.setColor(new Color(255,153,153)); g2.drawString(title, x+8, y+8);
            g2.setColor(new Color(0xF28BA1));     g2.drawString(title, x, y);

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
