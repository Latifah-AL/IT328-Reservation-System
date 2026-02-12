
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// كلاس واجهة العميل
public class ClientGUI extends JFrame {
    
    // انشاء object من نوع NetworkClient لارسال الأوامر واستقبال الردود
    private final NetworkClient client = new NetworkClient();
    private String currentUser = null; // المستخدم الحالي بعد التسجيل

    // إدارة الصفحات داخل نافذة واحدة
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    // صفحة الترحيب (welcome page)
    private final JButton btnGoSignup = new JButton("Sign up");
    private final JButton btnGoLogin  = new JButton("Log in");

    // صفحة التسجيل (signup page)
    private final JTextField tfUser = new JTextField();
    private final JPasswordField pfPass = new JPasswordField();
    private final JButton btnSignUp = new JButton("Sign up");
    private final JButton btnBackSignup = new JButton("Back");
    private final JLabel  lbSignInStatus = new JLabel(" ");

    // صفحة التسجيل الدخول(log in page)
    private final JTextField tfLoginUser = new JTextField();
    private final JPasswordField pfLoginPass = new JPasswordField();
    private final JButton btnLogin = new JButton("Log in");
    private final JButton btnBackLogin = new JButton("Back");
    private final JLabel  lbLoginStatus = new JLabel(" ");

    // صفحة الحجز (reservation page)
    private final JComboBox<String> cbR = new JComboBox<>(new String[]{"Lazy Cat","Entrecote","Lunch Room"});
    private final JComboBox<String> cbD = new JComboBox<>(new String[]{"Sat","Sun","Mon","Tue","Wed","Thu","Fri"});
    private final JComboBox<String> cbS = new JComboBox<>(new String[]{});
    private final JButton btnCheck = new JButton("Check availability");
    private final JLabel  lbAvail  = new JLabel("Select restaurant and day, then press Check");
    private final JButton btnReserve = new JButton("Reserve");
    private final JButton btnMyRes   = new JButton("My reservations");
    private final JLabel  lbRes   = new JLabel(" ");

    // الأوقات الثابتة للحجوزات
    private final List<String> ALL_SLOTS = Arrays.asList("17:00","18:00","19:00","20:00","21:00");

    // خلفية بصورة + طبقة شفافة
    static class BackgroundPanel extends JPanel {
        private final Image bg;
        BackgroundPanel() {
            setLayout(new BorderLayout());
            java.net.URL url = ClientGUI.class.getResource("/img/restaurant.jpg");
            bg = (url==null)?null:new ImageIcon(url).getImage();
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg!=null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(0,0,0,120));
                g.fillRect(0,0,getWidth(),getHeight());
            }
        }
    }

    public ClientGUI() {
        setTitle("Online Restaurant Reservation - Phase 2");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 420);
        setLocationRelativeTo(null);
        
        // إضافة الصفحات
        root.add(buildWelcomePage(), "welcome");
        root.add(buildSignUpPage(),  "signup");
        root.add(buildLoginPage(),   "login");
        root.add(buildReservePage(), "reserve");
        add(root);
        
        // تنقل بين الصفحات
        btnGoSignup.addActionListener(e -> cards.show(root, "signup"));
        btnGoLogin.addActionListener(e  -> cards.show(root, "login"));

        // الاحداث : التسجيل - التحقق من المواعيد المتاحة - الحجز
        btnSignUp.addActionListener(e -> doSignUp());
        btnLogin.addActionListener(e  -> doLogin());
        btnCheck.addActionListener(e  -> doCheckAndFillSlots());
        btnReserve.addActionListener(e -> doReserve());
        btnMyRes.addActionListener(e   -> showMyReservationsDialog());

        // أزرار Back ترجع للـ Welcome
        btnBackSignup.addActionListener(e -> cards.show(root, "welcome"));
        btnBackLogin.addActionListener(e  -> cards.show(root, "welcome"));
        
        // بدء التطبيق من شاشة الترحيب
        cards.show(root, "welcome");
        btnReserve.setEnabled(false);
    }

    private JPanel buildWelcomePage() {
        JPanel panel = new BackgroundPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(24,24,24,24));

        JLabel title = new JLabel("Welcome to the Online Restaurant Reservation System");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        panel.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.add(Box.createVerticalStrut(30));

        btnGoSignup.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnGoLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension btnSize = new Dimension(200, 38);
        btnGoSignup.setPreferredSize(btnSize);
        btnGoLogin.setPreferredSize(btnSize);
        btnGoSignup.setBackground(new Color(255,255,255,200));
        btnGoLogin.setBackground(new Color(255,255,255,200));

        center.add(btnGoSignup);
        center.add(Box.createVerticalStrut(12));
        center.add(btnGoLogin);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildSignUpPage() {
        JPanel panel = new BackgroundPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));

        JLabel title = new JLabel("Sign up");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(Color.WHITE);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8));
        top.setOpaque(false);
        top.add(title);
        panel.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.anchor = GridBagConstraints.WEST;

        JLabel userLabel = new JLabel("Username:"); userLabel.setForeground(Color.WHITE);
        c.gridx=0; c.gridy=0; form.add(userLabel, c);
        c.gridx=1; c.gridy=0; c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1; form.add(tfUser, c);

        JLabel passLabel = new JLabel("Password:"); passLabel.setForeground(Color.WHITE);
        c.gridx=0; c.gridy=1; c.fill=GridBagConstraints.NONE; c.weightx=0; form.add(passLabel, c);
        c.gridx=1; c.gridy=1; c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1; form.add(pfPass, c);

        // صف الأزرار: Sign up + Back
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnSignUp.setBackground(new Color(255,255,255,200));
        btnBackSignup.setBackground(new Color(220,220,220,200)); // رمادي فاتح شفاف
        btnRow.add(btnSignUp);
        btnRow.add(btnBackSignup);
        c.gridx=1; c.gridy=2; c.fill=GridBagConstraints.NONE; c.weightx=0; form.add(btnRow, c);

        lbSignInStatus.setForeground(Color.WHITE);
        c.gridx=0; c.gridy=3; c.gridwidth=2; c.fill=GridBagConstraints.HORIZONTAL; form.add(lbSignInStatus, c);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLoginPage() {
        JPanel p = new BackgroundPanel();
        p.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel t = new JLabel("Log in"); t.setForeground(Color.WHITE);
        t.setFont(t.getFont().deriveFont(Font.BOLD,18f));
        JPanel top=new JPanel(new FlowLayout(FlowLayout.LEFT,8,8)); top.setOpaque(false); top.add(t);
        p.add(top, BorderLayout.NORTH);

        JLabel uLabel = new JLabel("Username:"); uLabel.setForeground(Color.WHITE);
        c.gridx=0; c.gridy=0; form.add(uLabel, c);
        c.gridx=1; c.gridy=0; form.add(tfLoginUser, c);

        JLabel pLabel = new JLabel("Password:"); pLabel.setForeground(Color.WHITE);
        c.gridx=0; c.gridy=1; form.add(pLabel, c);
        c.gridx=1; c.gridy=1; form.add(pfLoginPass, c);

        // صف الأزرار: Log in + Back
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);
        btnLogin.setBackground(new Color(255,255,255,200));
        btnBackLogin.setBackground(new Color(220,220,220,200)); // رمادي فاتح شفاف
        btnRow.add(btnLogin);
        btnRow.add(btnBackLogin);
        c.gridx=1; c.gridy=2; form.add(btnRow, c);

        lbLoginStatus.setForeground(Color.WHITE);
        c.gridx=0; c.gridy=3; c.gridwidth=2; form.add(lbLoginStatus, c);

        p.add(form, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildReservePage() {
        JPanel panel = new BackgroundPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(18,18,18,18));

        JLabel title = new JLabel("Make a reservation");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        title.setForeground(Color.WHITE);
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.setOpaque(false);
        top.add(title);
        panel.add(top, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6,6,6,6);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel rLabel = new JLabel("Restaurant:"); rLabel.setForeground(Color.WHITE);
        c.gridx=0; c.gridy=0; form.add(rLabel, c);
        c.gridx=1; c.gridy=0; form.add(cbR, c);

        JLabel dLabel = new JLabel("Day:"); dLabel.setForeground(Color.WHITE);
        c.gridx=0; c.gridy=1; form.add(dLabel, c);
        c.gridx=1; c.gridy=1; form.add(cbD, c);

        JLabel sLabel = new JLabel("Slot:"); sLabel.setForeground(Color.WHITE);
        c.gridx=0; c.gridy=2; form.add(sLabel, c);
        c.gridx=1; c.gridy=2; form.add(cbS, c);

        c.gridx=0; c.gridy=3; form.add(btnCheck, c);
        btnCheck.setBackground(new Color(255,255,255,200));
        c.gridx=1; c.gridy=3; form.add(lbAvail, c);
        lbAvail.setForeground(Color.WHITE);

        c.gridx=1; c.gridy=4; form.add(btnReserve, c);
        btnReserve.setBackground(new Color(255,255,255,200));

        c.gridx=0; c.gridy=4; form.add(btnMyRes, c);
        btnMyRes.setBackground(new Color(255,255,255,200));

        c.gridx=0; c.gridy=5; c.gridwidth=2; form.add(lbRes, c);
        lbRes.setForeground(Color.WHITE);

        panel.add(form, BorderLayout.CENTER);
        return panel;
    }

    //    NetworkClient تنفيذ عملية التسجيل عبر
    private void doSignUp() {
        String user = tfUser.getText().trim();
        String pass = new String(pfPass.getPassword());
        if (user.isEmpty() || pass.isEmpty()) { lbSignInStatus.setText("Please fill username and password"); return; }
        btnSignUp.setEnabled(false);
        new Thread(() -> {
            String res = client.register(user, pass);
            SwingUtilities.invokeLater(() -> {
                if ("OK|Registered".equals(res)) {
                    lbSignInStatus.setText("Signed up successfully");
                    currentUser = user;
                    goReserve();
                } else { lbSignInStatus.setText(res); btnSignUp.setEnabled(true); }
            });
        }).start();
    }

    //    NetworkClient تنفيذ عملية تسجيل الدخول عبر
    private void doLogin() {
        String user = tfLoginUser.getText().trim();
        String pass = new String(pfLoginPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            lbLoginStatus.setText("Please fill in both username and password.");
            return;
    }

    btnLogin.setEnabled(false);

    new Thread(() -> {
        String res = client.login(user, pass);

        SwingUtilities.invokeLater(() -> {
            btnLogin.setEnabled(true);

            if ("OK|LoggedIn".equals(res)) {
                currentUser = user;
                lbLoginStatus.setText("Welcome back, " + currentUser + "!");
                goReserve();
            } 
            else if ("ERR|AuthFailed".equals(res)) {
                lbLoginStatus.setText("Incorrect username or password.");
            } 
            else if ("ERR|ConnectFailed".equals(res)) {
                lbLoginStatus.setText("Failed to connect to the server.");
            } 
            else if ("ERR|EmptyFields".equals(res)) {
                lbLoginStatus.setText("Please fill in all fields before logging in.");
            } 
            else {
                lbLoginStatus.setText( res);
            }
        });
    }).start();
}
    // الانتقال لصفحة الحجز وتجهيز الحالة
    private void goReserve() {
        cards.show(root, "reserve");
        lbAvail.setText("Select restaurant and day, then press Check");
        cbS.setModel(new DefaultComboBoxModel<>(new String[]{}));
        btnReserve.setEnabled(false);
    }

    // فحص الأوقات المتاحة
    private void doCheckAndFillSlots() {
        String r = (String) cbR.getSelectedItem();
        String d = (String) cbD.getSelectedItem();
        if (r == null || d == null) { lbAvail.setText("Please select restaurant and day"); return; }
        btnCheck.setEnabled(false);
        new Thread(() -> {
            List<String> available = new ArrayList<>();
            for (String s : ALL_SLOTS) {
                String res = client.avail(r, d, s);
                if (res != null && res.startsWith("AVAIL_RES|")) {
                    try {
                        int n = Integer.parseInt(res.substring("AVAIL_RES|".length()).trim());
                        if (n > 0) available.add(s);
                    } catch (NumberFormatException ignored) {}
                }
            }
            SwingUtilities.invokeLater(() -> {
                if (available.isEmpty()) {
                    cbS.setModel(new DefaultComboBoxModel<>(new String[]{}));
                    lbAvail.setText("No available times for this day");
                    btnReserve.setEnabled(false);
                } else {
                    cbS.setModel(new DefaultComboBoxModel<>(available.toArray(new String[0])));
                    lbAvail.setText("Choose a time from the list");
                    btnReserve.setEnabled(true);
                }
                btnCheck.setEnabled(true);
            });
        }).start();
    }

    // إرسال طلب الحجز إلى السيرفر
    private void doReserve() {
        if (currentUser == null || currentUser.isEmpty()) { JOptionPane.showMessageDialog(this, "Sign up or log in first"); return; }
        String r = (String) cbR.getSelectedItem();
        String d = (String) cbD.getSelectedItem();
        String s = (String) cbS.getSelectedItem();
        if (s == null || s.isEmpty()) { lbRes.setText("Please choose a time"); return; }

        btnReserve.setEnabled(false);
        new Thread(() -> {
            String res = client.reserve(currentUser, r, d, s);
            SwingUtilities.invokeLater(() -> {
                if ("OK|Reserved".equals(res)) {
                    lbRes.setText("Reserved successfully");
                    doCheckAndFillSlots();
                } else if ("ERR|NoAvailability".equals(res)) {
                    lbRes.setText("Not available for this slot");
                    doCheckAndFillSlots();
                } else if ("ERR|NotRegistered".equals(res)) {
                    lbRes.setText("Please log in first");
                } else {
                    lbRes.setText(res);
                }
                btnReserve.setEnabled(true);
            });
        }).start();
    }

    // تحميل الحجوزات في Model (بديل زر Refresh)
    private void loadReservations(DefaultListModel<String> model) {
        model.clear();
        String res = client.getReservations(currentUser);
        if (res != null && res.startsWith("RES_LIST")) {
            String[] parts = res.split("\\|");
            if (parts.length==1) model.addElement("No reservations");
            for (int i=1;i<parts.length;i++) model.addElement(parts[i].replace(';','|'));
        } else model.addElement("No reservations");
    }

    // حواري "حجوزاتي" بدون زر Refresh
    private void showMyReservationsDialog() {
        if (currentUser == null) { JOptionPane.showMessageDialog(this, "Sign up or log in first"); return; }

        JDialog d = new JDialog(this, "My reservations", true);
        d.setSize(420, 300);
        d.setLocationRelativeTo(this);

        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        JScrollPane sp = new JScrollPane(list);

        JButton btnCancel  = new JButton("Cancel selected");
        JButton btnClose   = new JButton("Close");
        JPanel bottom = new JPanel();
        bottom.add(btnCancel);
        bottom.add(btnClose);

        d.setLayout(new BorderLayout());
        d.add(sp, BorderLayout.CENTER);
        d.add(bottom, BorderLayout.SOUTH);

        // تحميل تلقائي عند الفتح
        loadReservations(model);

        btnCancel.addActionListener(e -> {
            String selected = list.getSelectedValue();
            if (selected == null || !selected.contains("|")) return;
            String[] p = selected.split("\\|");
            if (p.length != 3) return;
            int confirm = JOptionPane.showConfirmDialog(d, "Cancel this reservation?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;
            new Thread(() -> {
                String r = client.cancelReservation(currentUser, p[0], p[1], p[2]);
                SwingUtilities.invokeLater(() -> {
                    if ("OK|Cancelled".equals(r)) {
                        JOptionPane.showMessageDialog(d, "Cancelled");
                        loadReservations(model);   // تحديث القائمة بعد الإلغاء
                        doCheckAndFillSlots();     // تحديث التوفر في صفحة الحجز
                    } else {
                        JOptionPane.showMessageDialog(d, "Failed: " + r);
                    }
                });
            }).start();
        });

        btnClose.addActionListener(e -> d.dispose());

        d.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI().setVisible(true));
    }
}





