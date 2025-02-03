package view;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.net.URL;
import utils.DBConnection;

public class ProfileAdmin extends JPanel {
    private static final long serialVersionUID = 1L;
    private JTextField txtUsername, txtEmail, txtAddress;
    private JPasswordField txtCurrentPassword, txtNewPassword, txtConfirmPassword;
    private JRadioButton rbtnMale, rbtnFemale;
    private ButtonGroup genderGroup;
    private JButton btnUpdate, btnChangePassword, btnLogout;
    private Connection connection;
    private int userId; // Store current user ID
    
    public ProfileAdmin(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        
        // Left panel for avatar and basic info
        JPanel leftPanel = createLeftPanel();
        
        // Right panel for password change
        JPanel rightPanel = createRightPanel();
        
        // Add panels to main panel
        mainPanel.add(leftPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Load user data
        loadUserData();
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Profile Information",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Username
        addFormField(formPanel, "Username:", txtUsername = new JTextField(), gbc, 0);
        
        // Email
        addFormField(formPanel, "Email:", txtEmail = new JTextField(), gbc, 1);
        
        // Address
        addFormField(formPanel, "Address:", txtAddress = new JTextField(), gbc, 2);
        
        // Gender
        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rbtnMale = new JRadioButton("Male");
        rbtnFemale = new JRadioButton("Female");
        genderGroup = new ButtonGroup();
        genderGroup.add(rbtnMale);
        genderGroup.add(rbtnFemale);
        genderPanel.add(rbtnMale);
        genderPanel.add(rbtnFemale);
        addFormField(formPanel, "Gender:", genderPanel, gbc, 3);
        
        // Update button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        
        // Update Profile button
        btnUpdate = new JButton("Update Profile");
        styleButton(btnUpdate, new Color(52, 152, 219));
        btnUpdate.addActionListener(e -> updateProfile());
        
        // Logout button
        btnLogout = new JButton("Logout");
        styleButton(btnLogout, new Color(231, 76, 60)); // Màu đỏ cho nút logout
        btnLogout.addActionListener(e -> logout());
        
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnLogout);
        
        // Add components to panel
        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Change Password",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14)
        ));
        panel.setPreferredSize(new Dimension(300, 0));
        
        JPanel passwordPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Current password
        addFormField(passwordPanel, "Current Password:", txtCurrentPassword = new JPasswordField(), gbc, 0);
        
        // New password
        addFormField(passwordPanel, "New Password:", txtNewPassword = new JPasswordField(), gbc, 1);
        
        // Confirm password
        addFormField(passwordPanel, "Confirm Password:", txtConfirmPassword = new JPasswordField(), gbc, 2);
        
        // Change password button
        btnChangePassword = new JButton("Change Password");
        styleButton(btnChangePassword, new Color(46, 204, 113));
        btnChangePassword.addActionListener(e -> changePassword());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnChangePassword);
        
        panel.add(passwordPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void addFormField(JPanel panel, String label, JComponent field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.9;
        field.setPreferredSize(new Dimension(200, 25));
        panel.add(field, gbc);
    }
    
    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Thêm padding
        button.setPreferredSize(new Dimension(120, 35));
        button.setMargin(new Insets(5, 15, 5, 15));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });
    }
    
    private void loadUserData() {
        try {
            connection = DBConnection.getConnection();
            String query = "SELECT * FROM tbl_admin WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                txtUsername.setText(rs.getString("username"));
                txtEmail.setText(rs.getString("email"));
                txtAddress.setText(rs.getString("address"));
                
                String gender = rs.getString("gender");
                if ("Male".equals(gender)) {
                    rbtnMale.setSelected(true);
                } else if ("Female".equals(gender)) {
                    rbtnFemale.setSelected(true);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error loading admin data: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateProfile() {
        try {
            if (!validateProfileInput()) return;
            
            connection = DBConnection.getConnection();
            String query = "UPDATE tbl_admin SET username = ?, email = ?, address = ?, gender = ? WHERE id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            
            ps.setString(1, txtUsername.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, txtAddress.getText().trim());
            ps.setString(4, rbtnMale.isSelected() ? "Male" : "Female");
            ps.setInt(5, userId);
            
            int result = ps.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                    "Profile updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error updating profile: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void changePassword() {
        try {
            if (!validatePasswordInput()) return;
            
            connection = DBConnection.getConnection();
            
            // Verify current password
            String verifyQuery = "SELECT password FROM tbl_admin WHERE id = ?";
            PreparedStatement verifyPs = connection.prepareStatement(verifyQuery);
            verifyPs.setInt(1, userId);
            ResultSet rs = verifyPs.executeQuery();
            
            if (rs.next()) {
                String currentPassword = new String(txtCurrentPassword.getPassword());
                if (!rs.getString("password").equals(currentPassword)) {
                    JOptionPane.showMessageDialog(this,
                        "Current password is incorrect",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Update password
            String updateQuery = "UPDATE tbl_admin SET password = ? WHERE id = ?";
            PreparedStatement updatePs = connection.prepareStatement(updateQuery);
            updatePs.setString(1, new String(txtNewPassword.getPassword()));
            updatePs.setInt(2, userId);
            
            int result = updatePs.executeUpdate();
            if (result > 0) {
                JOptionPane.showMessageDialog(this,
                    "Password changed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                clearPasswordFields();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error changing password: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateProfileInput() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        
        if (username.isEmpty()) {
            showError("Username is required");
            return false;
        }
        
        if (email.isEmpty()) {
            showError("Email is required");
            return false;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format");
            return false;
        }
        
        if (!rbtnMale.isSelected() && !rbtnFemale.isSelected()) {
            showError("Please select a gender");
            return false;
        }
        
        return true;
    }
    
    private boolean validatePasswordInput() {
        String currentPassword = new String(txtCurrentPassword.getPassword());
        String newPassword = new String(txtNewPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        
        if (currentPassword.isEmpty()) {
            showError("Current password is required");
            return false;
        }
        
        if (newPassword.isEmpty()) {
            showError("New password is required");
            return false;
        }
        
        if (confirmPassword.isEmpty()) {
            showError("Confirm password is required");
            return false;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            showError("New password and confirm password do not match");
            return false;
        }
        
        return true;
    }
    
    private void clearPasswordFields() {
        txtCurrentPassword.setText("");
        txtNewPassword.setText("");
        txtConfirmPassword.setText("");
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void logout() {
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            // Tìm JFrame cha
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof JFrame) {
                window.dispose(); // Đóng cửa sổ hiện tại
                
                // Hiển thị màn hình login
                EventQueue.invokeLater(() -> {
                    new Login().setVisible(true);
                });
            }
        }
    }
} 