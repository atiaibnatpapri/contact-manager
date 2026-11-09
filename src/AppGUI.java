import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.awt.event.*;
import java.io.IOException;

public class AppGUI extends JFrame {
    private ContactManager manager;
    private JPanel mainPanel, contactsPanel, dialPanel, favoritesPanel, emergencyPanel;
    private JPanel contactsListPanel, favoritesListPanel, emergencyListPanel;
    private JTextField searchField, dialField;

    public AppGUI() {
        manager = new ContactManager();
        manager.getAllContacts().addAll(FileHandler.loadContacts());

        setTitle("My Contacts App");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 250));

        // Side Navigation
        JPanel sideNav = new JPanel(new GridLayout(4, 1, 0, 10));
        sideNav.setBackground(new Color(50, 50, 70));
        sideNav.setPreferredSize(new Dimension(150, 0));

        JButton contactsBtn = createNavButton("Contacts");
        JButton favoritesBtn = createNavButton("Favorites");
        JButton emergencyBtn = createNavButton("Emergency");
        JButton dialBtn = createNavButton("Dial Pad");

        sideNav.add(contactsBtn);
        sideNav.add(favoritesBtn);
        sideNav.add(emergencyBtn);
        sideNav.add(dialBtn);

        add(sideNav, BorderLayout.WEST);

        // Main Panel
        mainPanel = new JPanel(new CardLayout());
        mainPanel.setBackground(new Color(245, 245, 250));

        contactsPanel = createContactsPanel();
        favoritesPanel = createFavoritesPanel();
        emergencyPanel = createEmergencyPanel();
        dialPanel = createDialPadPanel();

        mainPanel.add(contactsPanel, "Contacts");
        mainPanel.add(favoritesPanel, "Favorites");
        mainPanel.add(emergencyPanel, "Emergency");
        mainPanel.add(dialPanel, "Dial Pad");

        add(mainPanel, BorderLayout.CENTER);

        // Navigation Actions
        contactsBtn.addActionListener(e -> switchPanel("Contacts"));
        favoritesBtn.addActionListener(e -> switchPanel("Favorites"));
        emergencyBtn.addActionListener(e -> switchPanel("Emergency"));
        dialBtn.addActionListener(e -> switchPanel("Dial Pad"));

        // Keyboard Shortcuts
        addKeyboardShortcuts();

        // Save on exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                FileHandler.saveContacts(manager.getAllContacts());
            }
        });

        setVisible(true);
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(70, 70, 100));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(90, 90, 120)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(70, 70, 100)); }
        });
        return btn;
    }

    private void addKeyboardShortcuts() {
        // Ctrl+S to save
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "save");
        getRootPane().getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileHandler.saveContacts(manager.getAllContacts());
                JOptionPane.showMessageDialog(AppGUI.this, "Contacts saved!");
            }
        });

        // Esc to close
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private void switchPanel(String name) {
        clearSelection(contactsListPanel);
        clearSelection(favoritesListPanel);
        clearSelection(emergencyListPanel);
        CardLayout cl = (CardLayout) (mainPanel.getLayout());
        cl.show(mainPanel, name);
        if (name.equals("Contacts")) refreshContactsList();
        else if (name.equals("Favorites")) refreshFavoritesList();
        else if (name.equals("Emergency")) refreshEmergencyList();
    }

    // -------------------- Contacts Panel --------------------
    private JPanel createContactsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setBackground(new Color(245, 245, 250));
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterContacts(); }
        });
        top.add(searchField, BorderLayout.CENTER);
        panel.add(top, BorderLayout.NORTH);

        contactsListPanel = new JPanel();
        contactsListPanel.setLayout(new BoxLayout(contactsListPanel, BoxLayout.Y_AXIS));
        contactsListPanel.setBackground(new Color(245, 245, 250));
        JScrollPane scroll = new JScrollPane(contactsListPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        btnPanel.setBackground(new Color(245, 245, 250));

        JButton addBtn = createActionButton("Add Contact", new Color(46, 204, 113));
        addBtn.addActionListener(e -> addContact());

        JButton editBtn = createActionButton("Edit Selected", new Color(241, 196, 15));
        editBtn.addActionListener(e -> editSelectedContact(contactsListPanel));

        JButton delBtn = createActionButton("Delete Selected", new Color(231, 76, 60));
        delBtn.addActionListener(e -> deleteSelectedContact(contactsListPanel));

        JButton exportBtn = createActionButton("Export CSV", new Color(155, 89, 182));
        exportBtn.addActionListener(e -> {
            try {
                FileHandler.exportToCSV(manager.getAllContacts());
                JOptionPane.showMessageDialog(this, "Exported to contacts.csv!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton importBtn = createActionButton("Import CSV", new Color(26, 188, 156));
        importBtn.addActionListener(e -> {
            try {
                List<Contact> imported = FileHandler.importFromCSV();
                manager.getAllContacts().addAll(imported);
                refreshContactsList();
                JOptionPane.showMessageDialog(this, "Imported " + imported.size() + " contacts!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Import failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(exportBtn);
        btnPanel.add(importBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        refreshContactsList();
        return panel;
    }

    private void refreshContactsList() {
        contactsListPanel.removeAll();
        for (Contact c : manager.getAllContacts()) {
            contactsListPanel.add(createContactCard(c, contactsListPanel));
        }
        contactsListPanel.revalidate();
        contactsListPanel.repaint();
    }

    private void filterContacts() {
        String key = searchField.getText().trim().toLowerCase();
        contactsListPanel.removeAll();
        for (Contact c : manager.getAllContacts()) {
            if (c.getName().toLowerCase().contains(key) || c.getPhoneNumber().contains(key))
                contactsListPanel.add(createContactCard(c, contactsListPanel));
        }
        contactsListPanel.revalidate();
        contactsListPanel.repaint();
    }

    // -------------------- Favorites Panel --------------------
    private JPanel createFavoritesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        favoritesListPanel = new JPanel();
        favoritesListPanel.setLayout(new BoxLayout(favoritesListPanel, BoxLayout.Y_AXIS));
        favoritesListPanel.setBackground(new Color(245, 245, 250));
        panel.add(new JScrollPane(favoritesListPanel), BorderLayout.CENTER);

        refreshFavoritesList();
        return panel;
    }

    private void refreshFavoritesList() {
        favoritesListPanel.removeAll();
        for (Contact c : manager.getFavoriteContacts()) {
            favoritesListPanel.add(createContactCard(c, favoritesListPanel));
        }
        favoritesListPanel.revalidate();
        favoritesListPanel.repaint();
    }

    // -------------------- Emergency Panel --------------------
    private JPanel createEmergencyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        emergencyListPanel = new JPanel();
        emergencyListPanel.setLayout(new BoxLayout(emergencyListPanel, BoxLayout.Y_AXIS));
        emergencyListPanel.setBackground(new Color(245, 245, 250));
        panel.add(new JScrollPane(emergencyListPanel), BorderLayout.CENTER);

        refreshEmergencyList();
        return panel;
    }

    private void refreshEmergencyList() {
        emergencyListPanel.removeAll();
        for (Contact c : manager.getEmergencyContacts()) {
            emergencyListPanel.add(createContactCard(c, emergencyListPanel));
        }
        emergencyListPanel.revalidate();
        emergencyListPanel.repaint();
    }

    // -------------------- Dial Pad Panel --------------------
    private JPanel createDialPadPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 245, 250));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        dialField = new JTextField();
        dialField.setFont(new Font("Segoe UI", Font.BOLD, 28));
        dialField.setEditable(false);
        dialField.setHorizontalAlignment(JTextField.CENTER);
        dialField.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 2));
        panel.add(dialField, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(4, 3, 10, 10));
        grid.setBackground(new Color(245, 245, 250));
        String[] nums = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#"};
        for (String n : nums) grid.add(createNumButton(n));
        panel.add(grid, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(1, 3, 10, 10));
        bottom.setBackground(new Color(245, 245, 250));

        JButton callBtn = createActionButton("Call", new Color(46, 204, 113));
        callBtn.addActionListener(e -> {
            String number = dialField.getText();
            if (!number.isEmpty()) JOptionPane.showMessageDialog(this, "Calling " + number);
        });

        JButton saveBtn = createActionButton("Save", new Color(52, 152, 219));
        saveBtn.addActionListener(e -> {
            String number = dialField.getText();
            if (number.isEmpty()) return;
            String name = JOptionPane.showInputDialog(this, "Enter Name for " + number);
            if (name == null || name.trim().isEmpty()) return;
            try {
                manager.addContact(new Contact(name, number));
                dialField.setText("");
                refreshContactsList();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton delBtn = createActionButton("Del", new Color(231, 76, 60));
        delBtn.addActionListener(e -> {
            String text = dialField.getText();
            if (text.length() > 0) dialField.setText(text.substring(0, text.length() - 1));
        });

        bottom.add(callBtn);
        bottom.add(saveBtn);
        bottom.add(delBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JButton createNumButton(String n) {
        JButton btn = new JButton(n);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 24));
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(41, 128, 185)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(52, 152, 219)); }
        });
        btn.addActionListener(e -> dialField.setText(dialField.getText() + n));
        return btn;
    }

    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    // -------------------- Contact Card --------------------
    private JPanel createContactCard(Contact c, JPanel parentPanel) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new EmptyBorder(5, 5, 5, 5),  // Fixed: removed parameter names
                new LineBorder(new Color(200, 200, 200), 1, true)  // Fixed: removed parameter names
        ));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));  // Fixed: removed parameter name

        JLabel nameLabel = new JLabel(c.getName() + " - " + c.getPhoneNumber());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        card.add(nameLabel, BorderLayout.CENTER);

        JPanel icons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        icons.setOpaque(false);

        JButton favBtn = new JButton(c.isFavorite() ? "❤️" : "♡");
        favBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        favBtn.setFocusPainted(false);
        favBtn.setBorder(null);
        favBtn.setContentAreaFilled(false);
        favBtn.setForeground(new Color(255, 215, 0));
        favBtn.addActionListener(e -> {
            manager.markFavorite(c.getPhoneNumber(), !c.isFavorite());
            refreshContactsList();
            refreshFavoritesList();
        });

        JButton emBtn = new JButton(c.isEmergency() ? "⚠" : "⛔");
        emBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        emBtn.setForeground(c.isEmergency() ? Color.RED : Color.GRAY);
        emBtn.addActionListener(e -> {
            boolean newEmergencyState = !c.isEmergency();
            manager.markEmergency(c.getPhoneNumber(), newEmergencyState);
            emBtn.setText(newEmergencyState ? "⚠" : "⛔");
            emBtn.setForeground(newEmergencyState ? Color.RED : Color.GRAY);

        });

        JButton callBtn = new JButton("Call");
        callBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        callBtn.setBackground(new Color(46, 204, 113));
        callBtn.setForeground(Color.WHITE);
        callBtn.setFocusPainted(false);
        callBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, "Calling " + c.getName()));

        icons.add(favBtn);
        icons.add(emBtn);
        icons.add(callBtn);
        card.add(icons, BorderLayout.EAST);

        card.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                clearSelection(parentPanel);
                card.setBackground(new Color(220, 235, 255));
                card.putClientProperty("selected", true);
            }
        });

        return card;
    }

    private void clearSelection(JPanel parentPanel) {
        for (Component comp : parentPanel.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(Color.WHITE);
                ((JPanel) comp).putClientProperty("selected", false);
            }
        }
    }

    // -------------------- Global Actions --------------------
    private void addContact() {
        String name = JOptionPane.showInputDialog(this, "Enter Name:");
        if (name == null || name.trim().isEmpty()) return;
        String phone = JOptionPane.showInputDialog(this, "Enter Phone:");
        if (phone == null || phone.trim().isEmpty()) return;
        try {
            manager.addContact(new Contact(name, phone));
            refreshContactsList();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelectedContact(JPanel parentPanel) {
        Contact c = getSelectedContact(parentPanel);
        if (c == null) return;
        String name = JOptionPane.showInputDialog(this, "Edit Name:", c.getName());
        if (name == null || name.trim().isEmpty()) return;
        String phone = JOptionPane.showInputDialog(this, "Edit Phone:", c.getPhoneNumber());
        if (phone == null || phone.trim().isEmpty()) return;
        manager.editContact(c.getPhoneNumber(), name, phone);
        refreshContactsList();
        refreshFavoritesList();
        refreshEmergencyList();
    }

    private void deleteSelectedContact(JPanel parentPanel) {
        Contact c = getSelectedContact(parentPanel);
        if (c == null) return;
        int confirm = JOptionPane.showConfirmDialog(this, "Delete " + c.getName() + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            manager.deleteContact(c.getPhoneNumber());
            refreshContactsList();
            refreshFavoritesList();
            refreshEmergencyList();
        }
    }

    private Contact getSelectedContact(JPanel parentPanel) {
        for (Component comp : parentPanel.getComponents()) {
            if (comp instanceof JPanel) {
                Boolean sel = (Boolean) ((JPanel) comp).getClientProperty("selected");
                if (sel != null && sel) {
                    JLabel label = (JLabel) ((JPanel) comp).getComponent(0);
                    String text = label.getText();
                    String phone = text.split(" - ")[1];
                    return manager.getAllContacts().stream()
                            .filter(contact -> contact.getPhoneNumber().equals(phone))
                            .findFirst().orElse(null);
                }
            }
        }
        JOptionPane.showMessageDialog(this, "No contact selected");
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppGUI());
    }
}