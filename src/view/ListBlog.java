package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.sql.*;
import utils.DBConnection; // Assuming this class handles DB connection

public class ListBlog extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private JTextField txtTitle, txtContent, txtImgLink;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear;
    private DefaultTableModel tableModel;
    private JPanel detailPanel; // Panel to show blog details
    private Connection connection;

    public ListBlog() {
        setLayout(new BorderLayout()); // Use BorderLayout for resizing components

        // Initialize table with columns: id, user_id, title, content, image link,
        // create_at, and Show Detail
        tableModel = new DefaultTableModel(
                new Object[] { "ID", "User ID", "Title", "Content", "Image Link", "Created At", "Show Detail" }, 0);
        table = new JTable(tableModel);

        // Create JScrollPane to wrap the JTable
        JScrollPane scrollPane = new JScrollPane(table);

        // Create the detail panel where blog details will be shown
        detailPanel = new JPanel();
        detailPanel.setLayout(new BorderLayout());

        // Create a JSplitPane to separate table and details panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, detailPanel);
        splitPane.setDividerLocation(600); // Adjust the divider to fit your needs
        add(splitPane, BorderLayout.CENTER);

        // Panel for input fields (add/update blog details)
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2, 10, 10)); // 4 rows, 2 columns
        add(inputPanel, BorderLayout.SOUTH); // Add at the bottom

        // Input fields for blog details
        inputPanel.add(new JLabel("Title:"));
        txtTitle = new JTextField();
        inputPanel.add(txtTitle);

        inputPanel.add(new JLabel("Content:"));
        txtContent = new JTextField();
        inputPanel.add(txtContent);

        inputPanel.add(new JLabel("Image Link:"));
        txtImgLink = new JTextField();
        inputPanel.add(txtImgLink);

        // Buttons for CRUD operations
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        inputPanel.add(buttonPanel);

        btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBlog();
            }
        });
        buttonPanel.add(btnAdd);

        btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateBlog();
            }
        });
        buttonPanel.add(btnUpdate);

        btnDelete = new JButton("Delete");
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteBlog();
            }
        });
        buttonPanel.add(btnDelete);

        btnClear = new JButton("Clear");
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        buttonPanel.add(btnClear);

        // Add MouseListener for table row selection
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    int id = (int) tableModel.getValueAt(row, 0); // Get selected blog ID
                    loadBlogDetails(id);
                }
            }
        });

        // Load all blog entries from the database
        loadtbl_post();
    }

    // Method to load all tbl_post into the table
    private void loadtbl_post() {
        try {
            connection = DBConnection.getConnection();
            // Modified query to join tbl_post and tbl_user
            String query = "SELECT p.id, p.user_id, p.title, p.content, p.hash_img, p.create_at, u.username, u.email, u.gender, u.address FROM tbl_post p JOIN tbl_user u ON p.user_id = u.id";
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            // Clear existing rows
            tableModel.setRowCount(0);

            // Iterate through the result set and add data to the table
            while (rs.next()) {
                Object[] row = { rs.getInt("id"), rs.getInt("user_id"), rs.getString("title"), rs.getString("content"),
                        rs.getString("hash_img"), rs.getTimestamp("create_at"), "Show Detail" };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to add a new blog
    private void addBlog() {
        String title = txtTitle.getText();
        String content = txtContent.getText();
        String imgLink = txtImgLink.getText();

        try {
            connection = DBConnection.getConnection();
            String query = "INSERT INTO tbl_post (title, content, hash_img) VALUES (?, ?, ?)";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, imgLink);
            ps.executeUpdate();
            loadtbl_post(); // Reload tbl_post after adding
            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to update a blog
    private void updateBlog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String title = txtTitle.getText();
            String content = txtContent.getText();
            String imgLink = txtImgLink.getText();

            try {
                connection = DBConnection.getConnection();
                String query = "UPDATE tbl_post SET title = ?, content = ?, hash_img = ? WHERE id = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setString(1, title);
                ps.setString(2, content);
                ps.setString(3, imgLink);
                ps.setInt(4, id);
                ps.executeUpdate();
                loadtbl_post(); // Reload tbl_post after updating
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to delete a blog
    private void deleteBlog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);

            try {
                connection = DBConnection.getConnection();
                String query = "DELETE FROM tbl_post WHERE id = ?";
                PreparedStatement ps = connection.prepareStatement(query);
                ps.setInt(1, id);
                ps.executeUpdate();
                loadtbl_post(); // Reload tbl_post after deleting
                clearFields();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to clear the input fields
    private void clearFields() {
        txtTitle.setText("");
        txtContent.setText("");
        txtImgLink.setText("");
    }

    // Method to load blog details into the detail panel when a blog is clicked in the table
    private void loadBlogDetails(int id) {
        try {
            connection = DBConnection.getConnection();
            // Modified query to join tbl_post with tbl_user to get username, email, gender, and address
            String query = "SELECT p.id, p.title, p.content, p.hash_img, u.username, u.email, u.gender, u.address FROM tbl_post p JOIN tbl_user u ON p.user_id = u.id WHERE p.id = ?";
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String title = rs.getString("title");
                String content = rs.getString("content");
                String imgLink = rs.getString("hash_img");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String gender = rs.getString("gender");
                String address = rs.getString("address");

                // Clear previous details
                detailPanel.removeAll();

                // Set BoxLayout with Y_AXIS to arrange components vertically
                detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));

                // Title panel (top)
                JPanel titlePanel = new JPanel();
                JLabel lblTitle = new JLabel("Title: " + title);
                lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
                titlePanel.add(lblTitle);

                // User info panel (below title)
                JPanel userPanel = new JPanel();
                JLabel lblUser = new JLabel("Author: " + username);
                lblUser.setFont(new Font("Arial", Font.ITALIC, 12));
                userPanel.add(lblUser);

                JLabel lblEmail = new JLabel("Email: " + email);
                lblEmail.setFont(new Font("Arial", Font.ITALIC, 12));
                userPanel.add(lblEmail);

                JLabel lblGender = new JLabel("Gender: " + gender);
                lblGender.setFont(new Font("Arial", Font.ITALIC, 12));
                userPanel.add(lblGender);

                JLabel lblAddress = new JLabel("Address: " + address);
                lblAddress.setFont(new Font("Arial", Font.ITALIC, 12));
                userPanel.add(lblAddress);

                // Content panel (middle)
                JPanel contentPanel = new JPanel();
                JLabel lblContent = new JLabel("<html>Content: " + content + "</html>");
                lblContent.setFont(new Font("Arial", Font.PLAIN, 12));
                contentPanel.add(lblContent);

                // Add title, user info, and content panels to the detailPanel
                detailPanel.add(titlePanel);
                detailPanel.add(userPanel);
                detailPanel.add(contentPanel);

                // Check if there is an image link and add image panel if valid
                if (imgLink != null && !imgLink.trim().isEmpty()) {
                    JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Center the image

                    // Load image and resize it to fit the panel
                    ImageIcon imgIcon = new ImageIcon(new URL(imgLink));
                    Image img = imgIcon.getImage();
                    Image scaledImg = img.getScaledInstance(700, 400, Image.SCALE_SMOOTH); // Resize to fit
                    imgIcon = new ImageIcon(scaledImg);
                    JLabel imgLabel = new JLabel(imgIcon);

                    imagePanel.add(imgLabel);

                    // Add image panel to the detailPanel
                    detailPanel.add(imagePanel);
                }

                // Revalidate and repaint the detail panel
                detailPanel.revalidate();
                detailPanel.repaint();
            }
        } catch (SQLException | java.net.MalformedURLException e) {
            e.printStackTrace();
        }
    }
}