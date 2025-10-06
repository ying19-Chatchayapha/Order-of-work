import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

public class AddProductFrame extends JFrame {

    private final JFrame parent;

    // ===== RIGHT COLUMN SIZING =====
    private static final int RIGHT_W = 360; // ← ปรับความกว้างช่องกรอกฝั่งขวา
    private static final int INPUT_H = 56;
    private static final int DESC_H  = 90;
    private static final int CARE_H  = 90;

    // form fields
    private JTextField nameField;
    private JComboBox<String> categoryBox;
    private JTextArea descArea, careArea;
    private JFormattedTextField priceField;
    private JSpinner stockSpinner;
    private JComboBox<String> statusCombo;
    private JRadioButton activeYes, activeNo;
    private ButtonGroup sizeGroup, colorGroup;
    private ImagePanel imgPanel;
    private File chosenImage;

    private static final Dimension LEFT_FIELD_SIZE = new Dimension(200, 56);

    public AddProductFrame() { this(null); }
    public AddProductFrame(JFrame parent) {
        this.parent = parent;

        setTitle("Add Product");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 760);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(10,10,10,10));
        setContentPane(root);

        // ===== Header (overlay back/profile) =====
        JPanel headWrap = new JPanel();
        headWrap.setOpaque(false);
        headWrap.setLayout(new OverlayLayout(headWrap));

        TitleHeader header = new TitleHeader("Add Product");
        header.setPreferredSize(new Dimension(0,140));

        JPanel overlay = new JPanel(new BorderLayout());
        overlay.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,18,18));
        left.setOpaque(false);
        left.add(backButton(52, e -> goBack()));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,18,18));
        right.setOpaque(false);
        right.add(profileButton(48));

        overlay.add(left, BorderLayout.WEST);
        overlay.add(right, BorderLayout.EAST);

        headWrap.add(overlay);
        headWrap.add(header);
        root.add(headWrap, BorderLayout.NORTH);

        // ===== Content (2 columns) =====
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6,6,6,6);
        gc.anchor = GridBagConstraints.NORTHWEST;

        // ซ้าย
        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 0;                 // ไม่ให้ยืด
        gc.fill = GridBagConstraints.NONE;
        content.add(leftColumn(), gc);

        // ขวา
        gc.gridx = 1; gc.gridy = 0;
        gc.weightx = 1;                 // รับพื้นที่ที่เหลือ (แต่ช่องกรอกเราล็อกไว้แล้ว)
        gc.fill = GridBagConstraints.HORIZONTAL;
        content.add(rightColumn(), gc);

        // scroll กลาง
        JScrollPane sc = new JScrollPane(content);
        sc.setBorder(new EmptyBorder(0, 0, 0, 0));
        sc.getViewport().setBackground(Color.WHITE);
        sc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sc.getVerticalScrollBar().setUnitIncrement(24);
        root.add(sc, BorderLayout.CENTER);

        loadCategoriesFromDb();
    }

    // ===== Left column =====
    private JPanel leftColumn() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12,12,12,12);
        g.anchor = GridBagConstraints.WEST;

        int r = 0;

        // Upload image panel (ขนาดเล็กคงที่ + ไม่ยืด)
        imgPanel = new ImagePanel(200, 80);
        imgPanel.setPreferredSize(new Dimension(200, 80));
        imgPanel.setMinimumSize(new Dimension(200, 80));
        imgPanel.setMaximumSize(new Dimension(200, 80));

        JButton upload = new JButton("+ Upload Image");
        upload.setFont(new Font("Segoe UI", Font.BOLD, 20));
        upload.setOpaque(false);
        upload.setContentAreaFilled(false);
        upload.setBorderPainted(false);
        upload.setFocusPainted(false);
        upload.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        upload.addActionListener(e -> chooseImage());
        imgPanel.setOverlay(upload);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2;
        g.anchor = GridBagConstraints.FIRST_LINE_START;
        g.fill = GridBagConstraints.NONE;
        g.weightx = 0; g.weighty = 0;

        JPanel imgWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        imgWrap.setOpaque(false);
        imgWrap.add(imgPanel);
        p.add(imgWrap, g);

        // คืนค่า fill/weight
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 0; g.weighty = 0;

        // Price
        r++; g.gridwidth = 1;
        priceField = new JFormattedTextField(new DecimalFormat("#,##0.##"));
        priceField.setPreferredSize(LEFT_FIELD_SIZE);
        styleField(priceField);
        addRowLeft(p, g, r, "Price :", priceField, "฿");

        // Stock
        r++;
        stockSpinner = new JSpinner(new SpinnerNumberModel(1,0,1_000_000,1));
        styleSpinner(stockSpinner);
        stockSpinner.setPreferredSize(LEFT_FIELD_SIZE);
        addRowLeft(p, g, r, "Stock Quantity :", stockSpinner, "item");

        // Status
        r++;
        statusCombo = new JComboBox<>(new String[]{"Ready for sale","Out of stock","Draft","Hidden"});
        statusCombo.setPreferredSize(LEFT_FIELD_SIZE);
        styleCombo(statusCombo);
        addRowLeft(p, g, r, "Product status :", statusCombo, null);

        // Active / Inactive
        r++;
        JPanel radio = new JPanel(new FlowLayout(FlowLayout.LEFT,18,0));
        radio.setOpaque(false);
        activeYes = new JRadioButton("Active");
        activeNo  = new JRadioButton("Inactive");
        ButtonGroup grp = new ButtonGroup(); grp.add(activeYes); grp.add(activeNo);
        activeYes.setSelected(true);
        styleRadio(activeYes); styleRadio(activeNo);
        radio.add(new JLabel("Open for sale :"));
        radio.add(activeYes); radio.add(Box.createHorizontalStrut(16)); radio.add(activeNo);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        p.add(radio, g);

        // Buttons
        r++;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT,24,0));
        btns.setOpaque(false);
        JButton save = primaryButton("Save");
        JButton cancel = primaryButton("Cancel");
        save.addActionListener(e -> onSave());
        cancel.addActionListener(e -> goBack());
        btns.add(save); btns.add(cancel);

        g.gridx = 0; g.gridy = r; g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        p.add(btns, g);

        // Filler
        r++;
        g.gridx = 0; g.gridy = r; g.gridwidth = 2;
        g.fill = GridBagConstraints.BOTH;
        g.weightx = 1; g.weighty = 1;
        p.add(Box.createVerticalGlue(), g);

        return p;
    }

    /** Row builder for left: [Label :] [Input + suffix] */
    private void addRowLeft(JPanel panel, GridBagConstraints base, int row,
                            String label, JComponent input, String suffix) {
        GridBagConstraints g = (GridBagConstraints) base.clone();

        // Label
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        g.fill = GridBagConstraints.NONE; g.weightx = 0;
        JLabel lb = new JLabel(label);
        lb.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        panel.add(lb, g);

        // Input + Suffix
        JPanel right = new JPanel(new FlowLayout(FlowLayout.LEFT,10,0));
        right.setOpaque(false);
        if (input != null) right.add(input);
        if (suffix != null && !suffix.isEmpty()) {
            JLabel sx = new JLabel(suffix);
            sx.setFont(new Font("Segoe UI", Font.PLAIN, 20));
            sx.setForeground(new Color(0x9E9E9E));
            right.add(sx);
        }
        g.gridx = 1; g.gridy = row; g.gridwidth = 1;
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1;
        panel.add(right, g);
    }

    // ===== Right column =====
    private JPanel rightColumn() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(12,12,12,12);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        int r=0;

        // Name (ล็อกขนาด)
        nameField = textField(); // textField() จะ lockSize ให้แล้ว
        addRow(panel, g, r++, "Name :", nameField);

        // Description (ล็อกขนาด)
        descArea  = textArea(3);
        JScrollPane spDesc = new JScrollPane(descArea);
        styleField(spDesc);
        lockSize(spDesc, RIGHT_W, DESC_H);
        addRow(panel, g, r++, "Description :", spDesc);

        // Category (ล็อกขนาด)
        // Category
        categoryBox = new JComboBox<>();
        categoryBox.setEditable(true);
        styleCombo(categoryBox);

        // >>> เพิ่มตัวเลือกเริ่มต้น
        String[] presets = {
            "Long sleeve silk shirt",
            "Turtleneck sweater",
            "Oversized sweater"
        };
        for (String s : presets) {
            categoryBox.addItem(s);
        }

        addRow(panel, g, r++, "Category :", categoryBox);


        // Care Instructions (ล็อกขนาด)
        careArea  = textArea(2);
        JScrollPane spCare = new JScrollPane(careArea);
        styleField(spCare);
        lockSize(spCare, RIGHT_W, CARE_H);
        addRow(panel, g, r++, "Care Instructions :", spCare);

        // Size
        JPanel sizes = new JPanel(new FlowLayout(FlowLayout.LEFT,18,0));
        sizes.setOpaque(false);
        sizeGroup = new ButtonGroup();
        sizes.add(new JLabel("Size : "));
        for(String s : new String[]{"S","M","L","XL"}) {
            JRadioButton rb = new JRadioButton(s);
            styleRadio(rb); sizeGroup.add(rb); sizes.add(rb);
        }
        ((JRadioButton)sizes.getComponent(2)).setSelected(true);
        g.gridx=0; g.gridy=r++; g.gridwidth=2; panel.add(sizes,g);

        // Color
        JPanel colors = new JPanel(new FlowLayout(FlowLayout.LEFT,18,0));
        colors.setOpaque(false);
        colorGroup = new ButtonGroup();
        colors.add(new JLabel("Color : "));
        for(String c : new String[]{"white","black","Cream","Brown"}) {
            JRadioButton rb = new JRadioButton(c);
            styleRadio(rb); colorGroup.add(rb); colors.add(rb);
        }
        g.gridy=r++; panel.add(colors,g);

        // spacer รับพื้นที่ส่วนเกิน (กันคอลัมน์ขวายืด field)
        GridBagConstraints spacer = new GridBagConstraints();
        spacer.gridx = 2; spacer.gridy = 0; spacer.gridheight = r;
        spacer.weightx = 1; spacer.weighty = 1;
        spacer.fill = GridBagConstraints.BOTH;
        panel.add(Box.createGlue(), spacer);

        return panel;
    }

    // ===== DB / Save =====
    private void loadCategoriesFromDb() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<String> cats = new ProductDao().distinctCategories();
                categoryBox.removeAllItems();
                for (String s : cats) categoryBox.addItem(s);
            } catch (Exception ex) {
                System.err.println("Load categories failed: "+ex.getMessage());
            }
        });
    }

    private void onSave() {
        try {
            Product p = new Product();
            p.name = nameField.getText().trim();
            p.description = descArea.getText().trim();
            Object catVal = categoryBox.getEditor().getItem();
            p.category = (catVal==null) ? "" : catVal.toString().trim();
            p.care = careArea.getText().trim();

            String priceText = priceField.getText().replace(",","").trim();
            p.price = priceText.isEmpty()? null : Double.valueOf(priceText);
            p.stock = (Integer) stockSpinner.getValue();
            p.status = (String) statusCombo.getSelectedItem();
            p.active = activeYes.isSelected();
            p.size = selectedText(sizeGroup);
            p.color = selectedText(colorGroup);

            if (chosenImage != null) {
                Files.createDirectories(Path.of("images"));
                String ext = chosenImage.getName().lastIndexOf('.')>0
                        ? chosenImage.getName().substring(chosenImage.getName().lastIndexOf('.')) : ".png";
                Path dest = Path.of("images", System.currentTimeMillis()+ext);
                Files.copy(chosenImage.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
                p.imagePath = dest.toString();
            }

            if (p.name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a product name.", "Missing name", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int id = new ProductDao().insert(p);
            JOptionPane.showMessageDialog(this, "Product saved successfully. ID = " + id + " ✅", "Saved", JOptionPane.INFORMATION_MESSAGE);
            goBack();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String selectedText(ButtonGroup g){
        for (var e=g.getElements(); e.hasMoreElements();) {
            AbstractButton b=e.nextElement();
            if (b.isSelected()) return b.getText();
        }
        return null;
    }

    private void chooseImage(){
        JFileChooser fc=new JFileChooser();
        int r=fc.showOpenDialog(this);
        if (r==JFileChooser.APPROVE_OPTION){
            try{
                chosenImage=fc.getSelectedFile();
                BufferedImage img= ImageIO.read(chosenImage);
                imgPanel.setImage(img);
            }catch(Exception ex){
                JOptionPane.showMessageDialog(this, "Could not load image.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void goBack(){
        if (parent!=null) parent.setVisible(true);
        else new MenuFrame().setVisible(true);
        dispose();
    }

    // ===== UI helpers =====
    private static void styleField(JComponent c){
        if (c instanceof JScrollPane sp) {
            sp.getViewport().setBackground(Color.WHITE);
            sp.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xC9C9C9),2,true),
                new EmptyBorder(0,12,0,12)
            ));
        } else {
            c.setBorder(new CompoundBorder(
                new LineBorder(new Color(0xC9C9C9),2,true),
                new EmptyBorder(0,12,0,12)
            ));
            c.setBackground(Color.WHITE);
            c.setOpaque(true);
        }
        c.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    }

    // ล็อกขนาดคอมโพเนนต์
    private static <T extends JComponent> T lockSize(T c, int w, int h) {
        Dimension d = new Dimension(w, h);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
        return c;
    }
    // ห่อให้ชิดซ้ายและไม่ยืด
    private static JPanel leftAlign(JComponent c) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    // ช่องกรอกบรรทัดเดียว (ฝั่งขวา) -> ล็อกขนาด
    private static JTextField textField(){
        JTextField tf=new JTextField();
        styleField(tf);
        return lockSize(tf, RIGHT_W, INPUT_H);
    }
    private static JTextArea textArea(int rows){
        JTextArea ta=new JTextArea(rows,20);
        ta.setLineWrap(true); ta.setWrapStyleWord(true);
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        return ta;
    }
    private static void styleSpinner(JSpinner sp){
        sp.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        if (sp.getEditor() instanceof JSpinner.DefaultEditor de) {
            de.getTextField().setBorder(new EmptyBorder(0,12,0,12));
            de.getTextField().setFont(new Font("Segoe UI", Font.PLAIN, 20));
        }
        sp.setBorder(new CompoundBorder(new LineBorder(new Color(0xC9C9C9),2,true), new EmptyBorder(0,0,0,0)));
    }
    // ไม่กำหนดขนาดในเมธอดนี้ ให้ caller เป็นคนกำหนด (ซ้ายใช้ LEFT_FIELD_SIZE, ขวาใช้ lockSize)
    private static void styleCombo(JComboBox<?> cb){
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        cb.setBorder(new CompoundBorder(new LineBorder(new Color(0xC9C9C9),2,true), new EmptyBorder(0,10,0,10)));
    }
    private static void styleRadio(JRadioButton rb){ rb.setFont(new Font("Segoe UI", Font.PLAIN, 20)); rb.setOpaque(false); }

    // ใช้วาง field ฝั่งขวา: ไม่ให้ field ยืด และห่อด้วย leftAlign
    private static void addRow(JPanel panel, GridBagConstraints g, int row, String label, JComponent field){
        g.gridx=0; g.gridy=row; g.weightx=0; g.gridwidth=1;
        JLabel lb=new JLabel(label); lb.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        panel.add(lb,g);

        g.gridx=1; g.gridwidth=1;
        g.fill = GridBagConstraints.NONE; // ไม่ยืด
        g.weightx = 0;
        panel.add(leftAlign(field), g);
    }

    private static JButton primaryButton(String text){
        JButton b=new JButton(text){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc=40; boolean hov=getModel().isRollover();
                g2.setColor(new Color(0,0,0,35));
                g2.fillRoundRect(8,10,getWidth()-16,getHeight()-16,arc,arc);
                Color base= hov? new Color(0x2A9CFF): new Color(0x168BFF);
                g2.setPaint(new GradientPaint(0,0,base.brighter(),0,getHeight(),base));
                g2.fillRoundRect(0,0,getWidth(),getHeight(),arc,arc);
                super.paintComponent(g); g2.dispose();
            }
            @Override public void update(Graphics g){ paint(g); }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 28));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(200, 64));
        return b;
    }

    private static JButton backButton(int size, ActionListener al){
        JButton p=new JButton(){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int d=Math.min(getWidth(),getHeight());
                g2.setColor(Color.BLACK); g2.fillOval(0,0,d,d);
                g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(Color.WHITE);
                int cx=d/2, cy=d/2;
                g2.drawLine(cx+8,cy,cx-6,cy-12);
                g2.drawLine(cx+8,cy,cx-6,cy+12);
                g2.dispose();
            }
            @Override public void update(Graphics g){ paint(g); }
        };
        p.addActionListener(al);
        p.setPreferredSize(new Dimension(size,size));
        p.setContentAreaFilled(false);
        p.setBorderPainted(false);
        p.setFocusPainted(false);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return p;
    }
    private static JButton profileButton(int size){
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
            @Override public void update(Graphics g){ paint(g); }
        };
        p.setPreferredSize(new Dimension(size,size));
        p.setContentAreaFilled(false);
        p.setBorderPainted(false);
        p.setFocusPainted(false);
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return p;
    }

    // ===== header & image panel =====
    static class TitleHeader extends JPanel{
        private final String title; TitleHeader(String t){ title=t; setOpaque(false); }
        @Override protected void paintComponent(Graphics g){
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0xFAD1D1));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),48,48);
            Font font=new Font("Georgia", Font.BOLD, 80);
            g2.setFont(font);
            FontMetrics fm=g2.getFontMetrics();
            int w=fm.stringWidth(title), h=fm.getAscent();
            int x=(getWidth()-w)/2, y=(getHeight()+h)/2 - 6;
            g2.setColor(new Color(255,153,153)); g2.drawString(title, x+8, y+8);
            g2.setColor(new Color(0xF28BA1));     g2.drawString(title, x, y);
            g2.dispose();
            super.paintComponent(g);
        }
    }
    static class ImagePanel extends JPanel{
        private BufferedImage img; private JComponent overlay;
        ImagePanel(int w,int h){
            setPreferredSize(new Dimension(w,h));
            setBorder(new LineBorder(new Color(0xBDBDBD),2,true));
            setBackground(Color.WHITE);
        }
        void setImage(BufferedImage i){ img=i; repaint(); }
        void setOverlay(JComponent c){ overlay=c; setLayout(new GridBagLayout()); add(c); }
        @Override protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(img!=null){
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                double sx=(double)getWidth()/img.getWidth(), sy=(double)getHeight()/img.getHeight();
                double s=Math.min(sx,sy);
                int dw=(int)(img.getWidth()*s), dh=(int)(img.getHeight()*s);
                int x=(getWidth()-dw)/2, y=(getHeight()-dh)/2;
                g2.drawImage(img,x,y,dw,dh,null);
                g2.dispose();
                if(overlay!=null) overlay.setVisible(false);
            }else if(overlay!=null){
                overlay.setVisible(true);
            }
        }
    }

    // single run
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AddProductFrame().setVisible(true));
    }
}
