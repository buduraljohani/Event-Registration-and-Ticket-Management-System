package eventsystem;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class LoginFrame extends JFrame {

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginBtn, signupBtn;

    public LoginFrame() {
        setTitle("Event System - Login");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1));

       
        // Email Panel
       
        JPanel p1 = new JPanel(new FlowLayout());
        p1.add(new JLabel("Email:"));
        emailField = new JTextField(20);
        p1.add(emailField);

    
        // Password Panel
     
        JPanel p2 = new JPanel(new FlowLayout());
        p2.add(new JLabel("Password:"));
        passwordField = new JPasswordField(20);
        p2.add(passwordField);

     
        // Buttons Panel
   
        JPanel p3 = new JPanel(new FlowLayout());
        loginBtn = new JButton("Login");
        signupBtn = new JButton("Sign Up");
        p3.add(loginBtn);
        p3.add(signupBtn);

        add(p1);
        add(p2);
        add(p3);

      
        // Actions
     
        loginBtn.addActionListener(e -> login());
        signupBtn.addActionListener(e -> {
            new SignupFrame();
            dispose();
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?")) {

            ps.setString(1, email);
            String hashedPass = HashUtil.hashPassword(password);
            ps.setString(2, hashedPass);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId = rs.getInt("user_id");

                JOptionPane.showMessageDialog(this, "Login Successful!", "Welcome", JOptionPane.INFORMATION_MESSAGE);

                // Open respective dashboard
                switch (role) {
                    case "ADMIN":
                        new AdminDashboardFrame();
                        break;
                    case "ORGANIZER":
                        new OrganizerDashboardFrame(userId);
                        break;
                    case "ATTENDEE":
                        new AttendeeDashboardFrame(userId);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Unknown role!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                }

                dispose(); // Close login frame

            } else {
                JOptionPane.showMessageDialog(this, "Invalid email or password!", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database Error:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}