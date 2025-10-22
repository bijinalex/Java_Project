import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class ProducerDashboard2  extends JFrame{
	 private JTextField idField, dateField, timeField;
	    private JTextArea storyArea;
	    private JButton fetchButton, assignButton;

	    public ProducerDashboard2() {
	        setTitle("Producer Dashboard - Assign Appointment");
	        setSize(500, 320);
	        setDefaultCloseOperation(EXIT_ON_CLOSE);
	        setLayout(new BorderLayout(10, 10));

	        // ---------- Top: ID input and Fetch button ----------
	        JPanel topPanel = new JPanel(new GridLayout(1, 3, 10, 10));
	        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
	        topPanel.add(new JLabel("Enter Applicant ID:"));
	        idField = new JTextField();
	        topPanel.add(idField);
	        fetchButton = new JButton("Fetch Story");
	        topPanel.add(fetchButton);
	        add(topPanel, BorderLayout.NORTH);

	        // ---------- Center: Show One Line Story ----------
	        storyArea = new JTextArea();
	        storyArea.setEditable(false);
	        storyArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
	        storyArea.setBorder(BorderFactory.createTitledBorder("Applicant's One Line"));
	        add(new JScrollPane(storyArea), BorderLayout.CENTER);

	        // ---------- Bottom: Date & Time ----------
	        JPanel bottomPanel = new JPanel(new GridLayout(3, 2, 10, 10));
	        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

	        bottomPanel.add(new JLabel("Appointment Date (YYYY-MM-DD):"));
	        dateField = new JTextField();
	        bottomPanel.add(dateField);

	        bottomPanel.add(new JLabel("Appointment Time (HH:MM:SS):"));
	        timeField = new JTextField();
	        bottomPanel.add(timeField);

	        assignButton = new JButton("Assign Appointment");
	        bottomPanel.add(new JLabel());
	        bottomPanel.add(assignButton);

	        add(bottomPanel, BorderLayout.SOUTH);

	        // ---------- Button actions ----------
	        fetchButton.addActionListener(e -> fetchStoryLine());
	        assignButton.addActionListener(e -> assignAppointment());

	        setLocationRelativeTo(null);
	        setVisible(true);
	    }

	    
	    private void fetchStoryLine() {
	        // Check input ID
	        String idText = idField.getText().trim();
	        if (idText.isEmpty()) {
	            JOptionPane.showMessageDialog(this, "Please enter an Applicant ID.", "Error", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        int id;
	        try {
	            id = Integer.parseInt(idText);
	            if (id <= 0) throw new NumberFormatException("ID must be positive.");
	        } catch (NumberFormatException ex) {
	            JOptionPane.showMessageDialog(this, "Applicant ID must be a positive integer.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        
	        String url = "jdbc:mysql://localhost:3306/story"; 
	        String user = "sebin";
	        String password = "sebin@sql";

	        
	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	        } catch (ClassNotFoundException ignored) { /* If driver is provided via classpath, this is fine */ }

	        String query = "SELECT story_line FROM submissions WHERE id = ?";

	        try (Connection conn = DriverManager.getConnection(url, user, password);
	             PreparedStatement pst = conn.prepareStatement(query)) {

	            pst.setInt(1, id);
	            try (ResultSet rs = pst.executeQuery()) {
	                if (rs.next()) {
	                    storyArea.setText(rs.getString("story_line"));
	                } else {
	                    storyArea.setText("");
	                    JOptionPane.showMessageDialog(this, "No applicant found with ID: " + id, "Not Found", JOptionPane.WARNING_MESSAGE);
	                }
	            }

	        } catch (SQLException ex) {
	            ex.printStackTrace();
	            JOptionPane.showMessageDialog(this, "Error fetching story: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
	        }
	    }

	    // Assign appointment date and time
	    private void assignAppointment() {
	        String idText = idField.getText().trim();
	        String dateText = dateField.getText().trim();
	        String timeText = timeField.getText().trim();

	        if (idText.isEmpty() || dateText.isEmpty() || timeText.isEmpty()) {
	            JOptionPane.showMessageDialog(this, "Please fill all fields before assigning.", "Error", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        int id;
	        try {
	            id = Integer.parseInt(idText);
	            if (id <= 0) throw new NumberFormatException("ID must be positive.");
	        } catch (NumberFormatException ex) {
	            JOptionPane.showMessageDialog(this, "Applicant ID must be a positive integer.", "Invalid ID", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        // Validate date and time formats
	        LocalDate appointmentDate;
	        LocalTime appointmentTime;
	        try {
	            appointmentDate = LocalDate.parse(dateText); // expects YYYY-MM-DD
	        } catch (DateTimeParseException ex) {
	            JOptionPane.showMessageDialog(this, "Invalid date format. Use YYYY-MM-DD.", "Invalid Date", JOptionPane.ERROR_MESSAGE);
	            return;
	        }
	        try {
	            appointmentTime = LocalTime.parse(timeText); // expects HH:MM or HH:MM:SS
	        } catch (DateTimeParseException ex) {
	            JOptionPane.showMessageDialog(this, "Invalid time format. Use HH:MM:SS (or HH:MM).", "Invalid Time", JOptionPane.ERROR_MESSAGE);
	            return;
	        }

	        
	        String url = "jdbc:mysql://localhost:3306/story"; 
	        String user = "sebin";
	        String password = "sebin@sql";

	        try {
	            Class.forName("com.mysql.cj.jdbc.Driver");
	        } catch (ClassNotFoundException ignored) {}

	        String updateQuery = "UPDATE submissions SET appointment_date = ?, appointment_time = ? WHERE id = ?";

	        try (Connection conn = DriverManager.getConnection(url, user, password);
	             PreparedStatement pst = conn.prepareStatement(updateQuery)) {

	            pst.setString(1, appointmentDate.toString()); 
	            pst.setString(2, appointmentTime.toString());
	            pst.setInt(3, id);

	            int rows = pst.executeUpdate();

	            if (rows > 0) {
	                JOptionPane.showMessageDialog(this, "Appointment assigned successfully!");
	            } else {
	                JOptionPane.showMessageDialog(this, "No record updated. Please check the Applicant ID.", "Error", JOptionPane.ERROR_MESSAGE);
	            }

	        } catch (SQLException ex) {
	            ex.printStackTrace();
	            JOptionPane.showMessageDialog(this, "Error updating appointment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
	        }
	    }

	    public static void main(String[] args) {
	        
	        SwingUtilities.invokeLater(ProducerDashboard2::new);
	    }
	}

