import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.*;

public class StoryTellerForm2 extends JFrame {
    private JTextField nameField, emailField, phoneField, storyField;
    private JButton submitButton;

    public StoryTellerForm2() {
        // Window setup
        setTitle("Story Submission Form");
        setSize(400, 340);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 2, 10, 10));

        // Form components
        add(new JLabel("Name:"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Email:"));
        emailField = new JTextField();
        add(emailField);

        add(new JLabel("Phone:"));
        phoneField = new JTextField();
        add(phoneField);

        add(new JLabel("One Line Story:"));
        storyField = new JTextField();
        add(storyField);

        submitButton = new JButton("Submit Story");
        add(new JLabel()); // empty label to balance grid
        add(submitButton);

        // === LIVE VALIDATION for PHONE FIELD ===
        phoneField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();

                // Allow only digits and control keys (backspace, delete)
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE && c != KeyEvent.VK_DELETE) {
                    e.consume(); // ignore the key press
                    JOptionPane.showMessageDialog(
                            StoryTellerForm2.this,
                            "Only numeric digits are allowed in phone number!",
                            "Invalid Input",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

                // Optional: restrict phone number length
                if (phoneField.getText().length() >= 10 && Character.isDigit(c)) {
                    e.consume();
                    JOptionPane.showMessageDialog(
                            StoryTellerForm2.this,
                            "Phone number cannot exceed 10 digits!",
                            "Limit Reached",
                            JOptionPane.WARNING_MESSAGE
                    );
                }
            }
        });

        // Submit button action
        submitButton.addActionListener(e -> saveData());

        // Center window
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void saveData() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String story = storyField.getText().trim();

        // === VALIDATION SECTION ===

        // 1️⃣ Name validation
        if (name.isEmpty()) {
            showError("Name cannot be empty!");
            return;
        }
        if (!name.matches("[a-zA-Z ]+")) {
            showError("Name can only contain alphabets and spaces!");
            return;
        }

        // 2️⃣ Email validation
        if (email.isEmpty()) {
            showError("Email cannot be empty!");
            return;
        }
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address!");
            return;
        }

        // 3️⃣ Phone number validation
        if (phone.isEmpty()) {
            showError("Phone number cannot be empty!");
            return;
        }
        if (!phone.matches("\\d{10}")) {
            showError("Phone number must be exactly 10 digits!");
            return;
        }

        // 4️⃣ Story validation
        if (story.isEmpty()) {
            showError("Please enter your story line!");
            return;
        }
        if (story.length() > 150) {
            showError("Story too long! Please keep it under 150 characters.");
            return;
        }

        // === DATABASE INSERTION ===
        String url = "jdbc:mysql://localhost:3306/story";
        String user = "sebin";
        String password = "sebin@sql";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);

            String query = "INSERT INTO submissions (name, email, phone, story_line) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, name);
            pst.setString(2, email);
            pst.setString(3, phone);
            pst.setString(4, story);

            int rowsInserted = pst.executeUpdate();

            if (rowsInserted > 0) {
                ResultSet rs = pst.getGeneratedKeys();
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    JOptionPane.showMessageDialog(this,
                            "Story submitted successfully!\nYour ID is: " + newId,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Story submitted successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                }

                clearFields();
            }

            pst.close();
            conn.close();

        } catch (ClassNotFoundException e) {
            showError("MySQL JDBC Driver not found! Please add mysql-connector-j.jar to your project.");
        } catch (SQLException e) {
            showError("Database connection failed!\n" + e.getMessage());
        } catch (Exception e) {
            showError("Unexpected error: " + e.getMessage());
        }
    }

    // Helper for email validation
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.WARNING_MESSAGE);
    }

    private void clearFields() {
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        storyField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StoryTellerForm2::new);
    }
}