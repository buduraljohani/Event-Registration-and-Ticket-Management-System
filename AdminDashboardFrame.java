
package eventsystem;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.io.FileWriter;

public class AdminDashboardFrame extends JFrame {

    private JTable eventTable, userTable;
    private JButton refreshEventsBtn, addEventBtn, deleteEventBtn;
    private JButton refreshUsersBtn, addOrganizerBtn, deleteUserBtn;
    private JTextArea reportArea;

    public AdminDashboardFrame() {

        setTitle("Admin Dashboard");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

        
        JPanel eventsPanel = new JPanel(new BorderLayout());
        eventTable = new JTable();
        JScrollPane eventScroll = new JScrollPane(eventTable);

        JPanel eventBtns = new JPanel();
        refreshEventsBtn = new JButton("Refresh Events");
        addEventBtn = new JButton("Add Event");
        deleteEventBtn = new JButton("Delete Event");

        eventBtns.add(refreshEventsBtn);
        eventBtns.add(addEventBtn);
        eventBtns.add(deleteEventBtn);

        eventsPanel.add(eventScroll, BorderLayout.CENTER);
        eventsPanel.add(eventBtns, BorderLayout.SOUTH);

        tabs.add("Events", eventsPanel);

       
        JPanel usersPanel = new JPanel(new BorderLayout());
        userTable = new JTable();
        JScrollPane userScroll = new JScrollPane(userTable);

        JPanel userBtns = new JPanel();
        refreshUsersBtn = new JButton("Refresh Users");
        addOrganizerBtn = new JButton("Add Organizer");
        deleteUserBtn = new JButton("Delete User");

        userBtns.add(refreshUsersBtn);
        userBtns.add(addOrganizerBtn);
        userBtns.add(deleteUserBtn);

        usersPanel.add(userScroll, BorderLayout.CENTER);
        usersPanel.add(userBtns, BorderLayout.SOUTH);

        tabs.add("Users", usersPanel);

        // REPORTS TAB
        JPanel reportsPanel = new JPanel(new BorderLayout());
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        JScrollPane reportScroll = new JScrollPane(reportArea);
        JButton generateReportBtn = new JButton("Generate Report");

        reportsPanel.add(reportScroll, BorderLayout.CENTER);
        reportsPanel.add(generateReportBtn, BorderLayout.SOUTH);

        tabs.add("Reports", reportsPanel);

        add(tabs);

        // ACTIONS
        refreshEventsBtn.addActionListener(e -> loadEvents());
        refreshUsersBtn.addActionListener(e -> loadUsers());
        addEventBtn.addActionListener(e -> addEvent());
        deleteEventBtn.addActionListener(e -> deleteEvent());
        deleteUserBtn.addActionListener(e -> deleteUser());
        addOrganizerBtn.addActionListener(e -> addOrganizer());
        generateReportBtn.addActionListener(e -> generateReport());

        loadEvents();
        loadUsers();

        setLocationRelativeTo(null);
        setVisible(true);
    }

  
    private void loadEvents() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Title", "Category", "Location", "Date", "Capacity"}, 0
        );

        String sql = "SELECT * FROM events ORDER BY event_date ASC";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("event_id"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("location"),
                        rs.getString("event_date"),
                        rs.getInt("seat_capacity")
                });
            }

            eventTable.setModel(model);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading events: " + ex.getMessage());
        }
    }

    // ================================
    // LOAD USERS
    // ================================
    private void loadUsers() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Full Name", "Email", "Role"}, 0
        );

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT user_id, full_name, email, role FROM users")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("role")
                });
            }

            userTable.setModel(model);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage());
        }
    }

  
    // ADD EVENT
  
    private void addEvent() {
        String title = JOptionPane.showInputDialog("Event Title:");
        String category = JOptionPane.showInputDialog("Category:");
        String location = JOptionPane.showInputDialog("Location:");
        String date = JOptionPane.showInputDialog("Event Date (YYYY-MM-DD HH:MM:SS):");
        String capacityTxt = JOptionPane.showInputDialog("Capacity:");

        if (title == null || category == null || location == null || date == null || capacityTxt == null) {
            JOptionPane.showMessageDialog(this, "Operation cancelled!");
            return;
        }

        title = title.trim();
        category = category.trim();
        location = location.trim();
        date = date.trim();
        capacityTxt = capacityTxt.trim();

        if (title.isEmpty() || category.isEmpty() || location.isEmpty() || date.isEmpty() || capacityTxt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty!");
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityTxt);
            if (capacity <= 0) {
                JOptionPane.showMessageDialog(this, "Capacity must be positive!");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Capacity must be a number!");
            return;
        }

        if (!date.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Invalid date format!");
            return;
        }

        String sql = "INSERT INTO events (title, category, location, event_date, seat_capacity, organizer_id) VALUES (?, ?, ?, ?, ?, 2)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, category);
            ps.setString(3, location);
            ps.setString(4, date);
            ps.setInt(5, capacity);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Event Added!");
            loadEvents();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding event: " + ex.getMessage());
        }
    }

   
    // DELETE EVENT
  
    private void deleteEvent() {
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event!");
            return;
        }

        int id = (int) eventTable.getValueAt(row, 0);

        // Check registrations
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM registrations WHERE event_id=?")) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete this event because users are registered.",
                        "Blocked",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error checking event: " + ex.getMessage());
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this event?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM events WHERE event_id=?")) {

            ps.setInt(1, id);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Event deleted!");
            loadEvents();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting event: " + ex.getMessage());
        }
    }

  
    // DELETE USER
  
    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a user!");
            return;
        }

        int userId = (int) userTable.getValueAt(row, 0);
        String role = (String) userTable.getValueAt(row, 3);

        if (role.equalsIgnoreCase("admin")) {
            try (Connection con = DBConnection.getConnection();
                 Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) AS c FROM users WHERE role='admin'")) {

                rs.next();
                if (rs.getInt("c") <= 1) {
                    JOptionPane.showMessageDialog(this, "Cannot delete the only admin!", "Blocked", JOptionPane.ERROR_MESSAGE);
                    return;
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
                return;
            }
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM registrations WHERE user_id=?")) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "User has registrations and cannot be deleted!", "Blocked", JOptionPane.ERROR_MESSAGE);
                return;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this user?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE user_id=?")) {

            ps.setInt(1, userId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "User deleted!");
            loadUsers();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting user: " + ex.getMessage());
        }
    }

  
    // ADD ORGANIZER

    private void addOrganizer() {
        String fullName = JOptionPane.showInputDialog("Organizer Full Name:");
        String email = JOptionPane.showInputDialog("Email:");
        String password = JOptionPane.showInputDialog("Password:");

        if (fullName == null || email == null || password == null) {
            JOptionPane.showMessageDialog(this, "Operation cancelled!");
            return;
        }

        fullName = fullName.trim();
        email = email.trim();
        password = password.trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required!");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this, "Invalid email format!");
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password too short!");
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE email=?")) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Email already exists!");
                return;
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error checking email: " + ex.getMessage());
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, 'ORGANIZER')"
             )) {

            ps.setString(1, fullName);
            ps.setString(2, email);
            ps.setString(3, password);

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Organizer added!");
            loadUsers();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding organizer: " + ex.getMessage());
        }
    }

  
    // GENERATE REPORT
 
    private void generateReport() {
        StringBuilder r = new StringBuilder();
        r.append("==== EVENT MANAGEMENT SYSTEM REPORT ====\n\n");

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement()) {

            // users per role
            r.append("Users per Role:\n");
            ResultSet rs1 = st.executeQuery("SELECT role, COUNT(*) AS c FROM users GROUP BY role");
            while (rs1.next()) {
                r.append("- " + rs1.getString("role") + ": " + rs1.getInt("c") + "\n");
            }
            r.append("\n");

            // total events
            ResultSet rs2 = st.executeQuery("SELECT COUNT(*) AS c FROM events");
            rs2.next();
            r.append("Total Events: " + rs2.getInt("c") + "\n\n");

            // total registrations
            ResultSet rs3 = st.executeQuery("SELECT COUNT(*) AS c FROM registrations");
            rs3.next();
            r.append("Total Registrations: " + rs3.getInt("c") + "\n\n");

            // most popular event
            ResultSet rs4 = st.executeQuery(
                    "SELECT e.title, COUNT(r.registration_id) AS regCount " +
                            "FROM events e LEFT JOIN registrations r ON e.event_id=r.event_id " +
                            "GROUP BY e.event_id ORDER BY regCount DESC LIMIT 1"
            );
            if (rs4.next())
                r.append("Most Popular Event: " + rs4.getString("title") +
                        " (" + rs4.getInt("regCount") + " registrations)\n\n");

            // most common category
            ResultSet rs5 = st.executeQuery(
                    "SELECT category, COUNT(*) AS c FROM events GROUP BY category ORDER BY c DESC LIMIT 1"
            );
            if (rs5.next())
                r.append("Top Category: " + rs5.getString("category") +
                        " (" + rs5.getInt("c") + " events)\n\n");

            // write file
            try (FileWriter writer = new FileWriter("admin_report.txt")) {
                writer.write(r.toString());
            }

            reportArea.setText(r.toString());
            JOptionPane.showMessageDialog(this, "Report saved to admin_report.txt");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error generating report: " + ex.getMessage());
        }
    }
}