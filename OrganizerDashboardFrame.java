package eventsystem;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class OrganizerDashboardFrame extends JFrame {

    private JTable eventsTable, attendeesTable;
    private JButton refreshEventsBtn, addEventBtn, editEventBtn, deleteEventBtn, loadAttendeesBtn;

    private int organizerId; // جاي من LoginFrame

    public OrganizerDashboardFrame(int organizerId) {

        this.organizerId = organizerId;

        setTitle("Organizer Dashboard");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabs = new JTabbedPane();

 
        // EVENTS TAB
       
        JPanel eventsPanel = new JPanel(new BorderLayout());

        eventsTable = new JTable();
        JScrollPane eventScroll = new JScrollPane(eventsTable);

        JPanel eventBtns = new JPanel();
        refreshEventsBtn = new JButton("Refresh Events");
        addEventBtn = new JButton("Add Event");
        editEventBtn = new JButton("Edit Event");
        deleteEventBtn = new JButton("Delete Event");

        eventBtns.add(refreshEventsBtn);
        eventBtns.add(addEventBtn);
        eventBtns.add(editEventBtn);
        eventBtns.add(deleteEventBtn);

        eventsPanel.add(eventScroll, BorderLayout.CENTER);
        eventsPanel.add(eventBtns, BorderLayout.SOUTH);

        tabs.add("My Events", eventsPanel);

      
        // ATTENDEES TAB
  
        JPanel attendeesPanel = new JPanel(new BorderLayout());

        attendeesTable = new JTable();
        JScrollPane attendeeScroll = new JScrollPane(attendeesTable);

        JPanel attendeeBtns = new JPanel();
        loadAttendeesBtn = new JButton("Load Attendees");
        attendeeBtns.add(loadAttendeesBtn);

        attendeesPanel.add(attendeeScroll, BorderLayout.CENTER);
        attendeesPanel.add(attendeeBtns, BorderLayout.SOUTH);

        tabs.add("Event Attendees", attendeesPanel);

        add(tabs);

        // ACTIONS
        refreshEventsBtn.addActionListener(e -> loadEvents());
        addEventBtn.addActionListener(e -> addEvent());
        editEventBtn.addActionListener(e -> editEvent());
        deleteEventBtn.addActionListener(e -> deleteEvent());
        loadAttendeesBtn.addActionListener(e -> loadAttendees());

        loadEvents();

        setLocationRelativeTo(null);
        setVisible(true);
    }

 
    // HELPERS
   
    private boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isValidDateFormat(String date) {
        return date.matches("\\d{4}-\\d{2}-\\d{2}.*");
    }

   
    // LOAD EVENTS
   
    private void loadEvents() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Title", "Category", "Location", "Date", "Capacity"}, 0
        );

        String sql = "SELECT * FROM events WHERE organizer_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, organizerId);
            ResultSet rs = ps.executeQuery();

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

            eventsTable.setModel(model);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading events:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

   
    // ADD EVENT
    
    private void addEvent() {
        String title = JOptionPane.showInputDialog(this, "Event Title:");
        String category = JOptionPane.showInputDialog(this, "Event Category:");
        String location = JOptionPane.showInputDialog(this, "Location:");
        String date = JOptionPane.showInputDialog(this, "Event Date (YYYY-MM-DD HH:MM:SS):");
        String capacity = JOptionPane.showInputDialog(this, "Capacity:");

        if (title == null || category == null || location == null || date == null || capacity == null) {
            JOptionPane.showMessageDialog(this, "Operation cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        title = title.trim();
        category = category.trim();
        location = location.trim();
        date = date.trim();
        capacity = capacity.trim();

        if (isNullOrEmpty(title) || isNullOrEmpty(category) || isNullOrEmpty(location) || isNullOrEmpty(date) || isNullOrEmpty(capacity)) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isValidDateFormat(date)) {
            JOptionPane.showMessageDialog(this, "Date format should be YYYY-MM-DD HH:MM:SS", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cap;
        try {
            cap = Integer.parseInt(capacity);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Capacity must be an integer.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cap <= 0) {
            JOptionPane.showMessageDialog(this, "Capacity must be positive.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO events (title, category, location, event_date, seat_capacity, organizer_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, category);
            ps.setString(3, location);
            ps.setString(4, date);
            ps.setInt(5, cap);
            ps.setInt(6, organizerId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Event added successfully!");
            loadEvents();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding event:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    // EDIT EVENT
 
    private void editEvent() {
        int row = eventsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event to edit!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int eventId = (int) eventsTable.getValueAt(row, 0);

        String title = JOptionPane.showInputDialog(this, "New Title:", eventsTable.getValueAt(row, 1));
        String category = JOptionPane.showInputDialog(this, "New Category:", eventsTable.getValueAt(row, 2));
        String location = JOptionPane.showInputDialog(this, "New Location:", eventsTable.getValueAt(row, 3));
        String date = JOptionPane.showInputDialog(this, "New Date (YYYY-MM-DD HH:MM:SS):", eventsTable.getValueAt(row, 4));
        String capacity = JOptionPane.showInputDialog(this, "New Capacity:", eventsTable.getValueAt(row, 5));

        if (title == null || category == null || location == null || date == null || capacity == null) {
            JOptionPane.showMessageDialog(this, "Operation cancelled.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        title = title.trim();
        category = category.trim();
        location = location.trim();
        date = date.trim();
        capacity = capacity.trim();

        if (isNullOrEmpty(title) || isNullOrEmpty(category) || isNullOrEmpty(location) || isNullOrEmpty(date) || isNullOrEmpty(capacity)) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!isValidDateFormat(date)) {
            JOptionPane.showMessageDialog(this, "Date format should be YYYY-MM-DD HH:MM:SS", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cap;
        try {
            cap = Integer.parseInt(capacity);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Capacity must be an integer.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cap <= 0) {
            JOptionPane.showMessageDialog(this, "Capacity must be positive.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "UPDATE events SET title=?, category=?, location=?, event_date=?, seat_capacity=? WHERE event_id=?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, category);
            ps.setString(3, location);
            ps.setString(4, date);
            ps.setInt(5, cap);
            ps.setInt(6, eventId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Event updated successfully!");
            loadEvents();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating event:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

   
    // DELETE EVENT
   
    private void deleteEvent() {
        int row = eventsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int eventId = (int) eventsTable.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this event and its registrations?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection con = DBConnection.getConnection()) {

            // حذف التسجيلات المرتبطة أولاً
            try (PreparedStatement psReg = con.prepareStatement("DELETE FROM registrations WHERE event_id = ?")) {
                psReg.setInt(1, eventId);
                psReg.executeUpdate();
            }

            // حذف الحدث
            try (PreparedStatement psEvt = con.prepareStatement("DELETE FROM events WHERE event_id = ?")) {
                psEvt.setInt(1, eventId);
                psEvt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Event and its registrations deleted successfully!");
            loadEvents();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting event:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    // LOAD ATTENDEES
 
    private void loadAttendees() {
        int row = eventsTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int eventId = (int) eventsTable.getValueAt(row, 0);

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Registration ID", "Name", "Email", "Status"}, 0
        );

        String sql = "SELECT r.registration_id, u.full_name, u.email, r.status " +
                     "FROM registrations r JOIN users u ON r.user_id = u.user_id " +
                     "WHERE r.event_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("registration_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("status")
                });
            }

            attendeesTable.setModel(model);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading attendees:\n" + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}