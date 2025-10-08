import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.*;

public class InventoryFrame extends JFrame {

    private final JFrame parent;

    private JTextField tfSearch;
    private JComboBox<String> cbCategory, cbStatus, cbOpen;
    private JTable table;
    private ProductTableModel model;
    private final ProductDao dao = new ProductDao();

    public InventoryFrame(JFrame parent) {
        this.parent = parent;

        setTitle("Inventory");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 760);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        root.setBackground(Color.WHITE);
        setContentPane(root);

        // ---------- Header ----------
        JPanel topWrap = new JPanel();
        topWrap.setOpaque(false);
        topWrap.setLayout(new OverlayLayout(topWrap));

        PinkHeader header = new PinkHeader("Inventory");
        header.setPreferredSize(new Dimension(0,120));

        JPanel overlay = new JPanel(new BorderLayout());
        overlay.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,12,12));
        left.setOpaque(false);
        JButton back = circleBack(44, e -> goBack());
        left.add(back);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,12,12));
        right.setOpaque(false);
        right.add(profile(40));

        overlay.add(left, BorderLayout.WEST);
        overlay.add(right, BorderLayout.EAST);

        topWrap.add(overlay);
        topWrap.add(header);
        root.add(topWrap, BorderLayout.NORTH);

        // ---------- Filter bar ----------
        JPanel filters = new JPanel(new GridBagLayout());
        filters.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6,6,6,6);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        tfSearch = new JTextField();
        tfSearch.putClientProperty("JComponent.sizeVariant", "large");
        g.gridx=0; g.gridy=0; g.weightx=1;
        filters.add(labeledField(tfSearch, "Search for products", true), g);

        cbCategory = new JComboBox<>();
        g.gridx=1; g.weightx=0.4;
        filters.add(labeledCombo(cbCategory, "Category"), g);

        cbStatus = new JComboBox<>(new String[]{"All","Ready for sale","Pre-order","Out of stock","Draft","Hidden"});
        g.gridx=2; g.weightx=0.3;
        filters.add(labeledCombo(cbStatus, "Status"), g);

        cbOpen = new JComboBox<>(new String[]{"All","Active","Inactive"});
        g.gridx=3; g.weightx=0.3;
        filters.add(labeledCombo(cbOpen, "Open for sale"), g);

        JButton btnSearch = new JButton("\uD83D\uDD0D");
        btnSearch.setFont(btnSearch.getFont().deriveFont(Font.BOLD,16f));
        btnSearch.addActionListener(e -> refresh());
        g.gridx=4; g.weightx=0; g.fill = GridBagConstraints.NONE;
        filters.add(btnSearch, g);

        root.add(filters, BorderLayout.BEFORE_FIRST_LINE);

        // ---------- Table ----------
        model = new ProductTableModel();
        table = new JTable(model);

        // ฟอนต์ให้รองรับภาษาไทย + ปรับขนาด
        Font th = new Font("Segoe UI", Font.PLAIN, 16);   // Windows แนะนำ Segoe UI/Tahoma
        table.setFont(th);
        table.setRowHeight(120);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        // render รูป
        table.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer());

        // ปุ่มแก้ไข/ลบ/เพิ่มเติม
        table.getColumnModel().getColumn(7).setCellRenderer(new ButtonRenderer("Edit"));
        table.getColumnModel().getColumn(7).setCellEditor(new ButtonEditor("Edit", this::onEdit));
        table.getColumnModel().getColumn(8).setCellRenderer(new ButtonRenderer("Delete"));
        table.getColumnModel().getColumn(8).setCellEditor(new ButtonEditor("Delete", this::onDelete));
        table.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer("More"));
        table.getColumnModel().getColumn(9).setCellEditor(new ButtonEditor("More", this::onMore));

        // ความกว้างคอลัมน์
        int[] widths = {180, 230, 160, 140, 140, 120, 60, 70, 80, 80};
        for (int i=0;i<widths.length;i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        table.getColumnModel().getColumn(0).setMinWidth(160);
        table.getColumnModel().getColumn(0).setPreferredWidth(180);
        table.getColumnModel().getColumn(0).setMaxWidth(240); // จะไม่กว้างเกินนี้


        root.add(new JScrollPane(table), BorderLayout.CENTER);

        // โหลด category + ดึงข้อมูล
        loadCategories();
        refresh();
    }

    // ---------- Actions ----------
    private void refresh() {
        try {
            String kw   = tfSearch.getText().trim();
            String cat  = (String) cbCategory.getSelectedItem();
            String st   = (String) cbStatus.getSelectedItem();
            String open = (String) cbOpen.getSelectedItem();

            List<Product> items = dao.searchInventory(kw, cat, st, open);
            model.setData(items);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Load inventory failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCategories() {
        try {
            cbCategory.removeAllItems();
            cbCategory.addItem("All");
            for (String s : dao.distinctCategories()) cbCategory.addItem(s);

            // เพิ่ม 3 ตัวเลือกตามที่ขอ (ถ้ายังไม่มีใน DB)
            for (String s : new String[]{"Long sleeve silk shirt","Turtleneck sweater","Oversized sweater"}) {
                boolean exists = false;
                for (int i=0;i<cbCategory.getItemCount();i++) {
                    if (s.equals(cbCategory.getItemAt(i))) { exists=true; break; }
                }
                if (!exists) cbCategory.addItem(s);
            }
            cbCategory.setSelectedIndex(0);
        } catch (Exception ex) {
            System.err.println("Load categories: "+ex.getMessage());
        }
    }

    private void onEdit(int row) {
        Product p = model.getAt(row);
        JOptionPane.showMessageDialog(this, "Edit for ID " + p.id + " (coming soon)", "Info", JOptionPane.INFORMATION_MESSAGE);
        // ถ้าจะทำแก้ไขจริง ให้สร้าง AddProductFrame(p) แล้ว prefill ค่า
    }

    private void onDelete(int row) {
        Product p = model.getAt(row);
        int c = JOptionPane.showConfirmDialog(this, "Delete \""+p.name+"\" ?", "Confirm", JOptionPane.OK_CANCEL_OPTION);
        if (c == JOptionPane.OK_OPTION) {
            try { dao.deleteById(p.id); refresh(); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, "Delete failed: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void onMore(int row) {
        Product p = model.getAt(row);
        String msg = "ID: "+p.id+
                     "\nName: "+p.name+
                     "\nCategory: "+p.category+
                     "\nStock: "+p.stock+
                     "\nStatus: "+p.status+
                     "\nOpen: "+(p.active?"Active":"Inactive");
        JOptionPane.showMessageDialog(this, msg, "More", JOptionPane.INFORMATION_MESSAGE);
    }

    private void goBack() {
        if (parent != null) parent.setVisible(true);
        dispose();
    }

    // ---------- Helpers / UI ----------

    private static JPanel labeledField(JComponent field, String placeholder, boolean showIcon) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new Color(0xC9C9C9),2,true),
            BorderFactory.createEmptyBorder(8,12,8,12)
        ));
        p.add(field, BorderLayout.CENTER);
        if (showIcon) {
            JLabel icon = new JLabel("\uD83D\uDD0D");
            icon.setBorder(BorderFactory.createEmptyBorder(0,8,0,8));
            p.add(icon, BorderLayout.EAST);
        }
        p.setPreferredSize(new Dimension(260, 40));
        return p;
    }

    private static JPanel labeledCombo(JComboBox<?> cb, String hint) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        cb.setBorder(BorderFactory.createCompoundBorder(
            new javax.swing.border.LineBorder(new Color(0xC9C9C9),2,true),
            BorderFactory.createEmptyBorder(2,10,2,10)
        ));
        p.add(cb, BorderLayout.CENTER);
        p.setPreferredSize(new Dimension(220, 40));
        return p;
    }

    private static JButton circleBack(int size, ActionListener al) {
        JButton b = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int d=Math.min(getWidth(),getHeight());
                g2.setColor(Color.BLACK); g2.fillOval(0,0,d,d);
                g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx=d/2, cy=d/2;
                g2.drawLine(cx+7,cy, cx-6,cy-10);
                g2.drawLine(cx+7,cy, cx-6,cy+10);
                g2.dispose();
            }
            @Override public void update(Graphics g){ paint(g); }
        };
        b.setPreferredSize(new Dimension(size,size));
        b.setContentAreaFilled(false); b.setBorderPainted(false); b.setFocusPainted(false);
        b.addActionListener(al);
        return b;
    }

    private static JButton profile(int size) {
        JButton p=new JButton(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int d=Math.min(getWidth(),getHeight());
                g2.setColor(Color.BLACK); g2.fillOval(0,0,d,d);
                g2.setColor(Color.WHITE);
                g2.fillOval(d/2-d/6,d/5,d/3,d/3);
                g2.fillRoundRect(d/6,d/2,(int)(d*0.66),d/3,d/3,d/3);
                g2.dispose();
            }
        };
        p.setPreferredSize(new Dimension(size,size));
        p.setContentAreaFilled(false); p.setBorderPainted(false); p.setFocusPainted(false);
        return p;
    }

    static class PinkHeader extends JPanel {
        final String title;
        PinkHeader(String t){ title=t; setOpaque(false); }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xFAD1D1));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),36,36);
            Font f=new Font("Georgia",Font.BOLD,72);
            g2.setFont(f);
            FontMetrics fm=g2.getFontMetrics();
            int w=fm.stringWidth(title), h=fm.getAscent();
            int x=(getWidth()-w)/2, y=(getHeight()+h)/2-8;
            g2.setColor(new Color(255,153,153)); g2.drawString(title,x+6,y+6);
            g2.setColor(new Color(0xF28BA1));     g2.drawString(title,x,y);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------- Table Model ----------
    static class ProductTableModel extends AbstractTableModel {
        private final String[] cols = {
            "Product pictures","Name","Category","Remaining inventory","Status","Open for sale","ID","Edit","Delete","View More"
        };
        private java.util.List<Product> data = java.util.List.of();

        public void setData(java.util.List<Product> list){ this.data=list; fireTableDataChanged(); }
        public Product getAt(int row){ return data.get(row); }

        @Override public int getRowCount(){ return data.size(); }
        @Override public int getColumnCount(){ return cols.length; }
        @Override public String getColumnName(int c){ return cols[c]; }
        @Override public boolean isCellEditable(int r,int c){ return c>=7; }

        @Override public Object getValueAt(int r,int c){
            Product p = data.get(r);
            return switch(c){
                case 0 -> p.imagePath;
                case 1 -> p.name;
                case 2 -> p.category;
                case 3 -> p.stock==null?0:p.stock;
                case 4 -> p.status;
                case 5 -> p.active? "Active":"Inactive";
                case 6 -> p.id;
                case 7 -> "Edit";
                case 8 -> "Delete";
                case 9 -> "More";
                default -> "";
            };
        }
    }

    // ---------- Renderers / Editors ----------
    static class ImageRenderer extends DefaultTableCellRenderer {
        @Override public void setValue(Object value){
            setText(null);
            setHorizontalAlignment(CENTER);
            try{
                if(value!=null){
                    File f=new File(value.toString());
                    if(f.exists()){
                        BufferedImage img= ImageIO.read(f);
                        int th = Math.min(getHeight() > 0 ? getHeight() - 16 : 120, 160);
                        int tw=(int)(img.getWidth()* (th/(double)img.getHeight()));
                        Image scaled=img.getScaledInstance(tw, th, Image.SCALE_SMOOTH);
                        setIcon(new ImageIcon(scaled));
                        return;
                    }
                }
            }catch(Exception ignored){}
            setIcon(null);
            setText("—");
        }
    }

    static class ButtonRenderer extends DefaultTableCellRenderer {
        private final String text;
        ButtonRenderer(String t){
            text=t;
            setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
        }
        @Override public void setValue(Object v){ setText(text); }
    }

    static class ButtonEditor extends DefaultCellEditor {
        private final JButton btn = new JButton();
        private final java.util.function.IntConsumer onClick;
        private int row = -1;

        ButtonEditor(String text, java.util.function.IntConsumer onClick){
            super(new JCheckBox());
            this.onClick = onClick;
            btn.setText(text);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            btn.addActionListener(e -> {
                if(row>=0) onClick.accept(row);
                fireEditingStopped();
            });
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
            this.row=row;
            return btn;
        }
    }

    // single run
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InventoryFrame(null).setVisible(true));
    }
}
