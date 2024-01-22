package telephoneProject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TelephoneManagerApp extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=UserManagement;encrypt=true;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "reallyStrongPwd123";

    private JButton btnLoadData;
    private JButton btnAddSubscriber;
    private JButton btnEditSubscriber;
    private JButton btnDeleteSubscriber;
    private JButton btnTotalCharge;
    private JTextField txtSearch;
    private JTable tblSubscribers;
    private JScrollPane scrollPane;
    private JComboBox<String> cmbCustomerType;

    public TelephoneManagerApp() {
        initializeUI();

        Connection connection = connect();
        if (connection != null) {
            loadDataFromDatabase();
            disconnect(connection);
        }
    }

    private Connection connect() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void disconnect(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDataFromDatabase() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("CustomerID");
        model.addColumn("FirstName");
        model.addColumn("LastName");
        model.addColumn("PhoneNumber");
        model.addColumn("Email");
        model.addColumn("Address");
        model.addColumn("CustomerType");
        model.addColumn("Charge");

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM Customers");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                model.addRow(new Object[]{
                        resultSet.getInt("CustomerID"),
                        resultSet.getString("FirstName"),
                        resultSet.getString("LastName"),
                        resultSet.getString("PhoneNumber"),
                        resultSet.getString("Email"),
                        resultSet.getString("Address"),
                        resultSet.getString("CustomerType"),
                        resultSet.getDouble("Charge")
                });
            }

            tblSubscribers.setModel(model);

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private double calculateCharge(String customerType) {
        switch (customerType) {
            case "Gold":
                return 100000.0;
            case "Platinum":
                return 200000.0;
            case "Diamond":
                return 500000.0;
            default:
                return 0.0;
        }
    }

    private void initializeUI() {
        setTitle("Telephone Manager");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        btnLoadData = new JButton("Load Data");
        btnAddSubscriber = new JButton("Add Subscriber");
        btnEditSubscriber = new JButton("Edit Subscriber");
        btnDeleteSubscriber = new JButton("Delete Subscriber");
        btnTotalCharge = new JButton("Calculate Total Charge");

        cmbCustomerType = new JComboBox<>(new String[]{"Gold", "Platinum", "Diamond"});

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.add(btnLoadData);
        buttonsPanel.add(btnAddSubscriber);
        buttonsPanel.add(btnEditSubscriber);
        buttonsPanel.add(btnDeleteSubscriber);
        buttonsPanel.add(btnTotalCharge);

        add(buttonsPanel, BorderLayout.NORTH);

        tblSubscribers = new JTable();
        scrollPane = new JScrollPane(tblSubscribers);
        add(scrollPane, BorderLayout.CENTER);

        btnLoadData.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadDataFromDatabase();
            }
        });

        btnAddSubscriber.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addSubscriberDialog();
            }
        });

        btnEditSubscriber.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSubscriberDialog();
            }
        });

        btnDeleteSubscriber.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSubscriber();
            }
        });

        btnTotalCharge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateTotalCharge();
            }
        });
    }

    private void addSubscriberDialog() {
        JTextField txtCustomerID = new JTextField();
        JTextField txtFirstName = new JTextField();
        JTextField txtLastName = new JTextField();
        JTextField txtPhoneNumber = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtAddress = new JTextField();

        Object[] message = {
                "CustomerID:", txtCustomerID,
                "FirstName:", txtFirstName,
                "LastName:", txtLastName,
                "PhoneNumber:", txtPhoneNumber,
                "Email:", txtEmail,
                "Address:", txtAddress,
                "CustomerType:", cmbCustomerType
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Add Subscriber", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String customerType = cmbCustomerType.getSelectedItem().toString();
            double charge = calculateCharge(customerType);
            addSubscriber(txtCustomerID.getText(), txtFirstName.getText(), txtLastName.getText(),
                    txtPhoneNumber.getText(), txtEmail.getText(), txtAddress.getText(), customerType, charge);
        }
    }

    private void editSubscriberDialog() {
        int selectedRow = tblSubscribers.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subscriber to edit.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JTextField txtCustomerID = new JTextField(tblSubscribers.getValueAt(selectedRow, 0).toString());
        JTextField txtFirstName = new JTextField(tblSubscribers.getValueAt(selectedRow, 1).toString());
        JTextField txtLastName = new JTextField(tblSubscribers.getValueAt(selectedRow, 2).toString());
        JTextField txtPhoneNumber = new JTextField(tblSubscribers.getValueAt(selectedRow, 3).toString());
        JTextField txtEmail = new JTextField(tblSubscribers.getValueAt(selectedRow, 4).toString());
        JTextField txtAddress = new JTextField(tblSubscribers.getValueAt(selectedRow, 5).toString());

        cmbCustomerType.setSelectedItem(tblSubscribers.getValueAt(selectedRow, 6).toString());

        Object[] message = {
                "CustomerID:", txtCustomerID,
                "FirstName:", txtFirstName,
                "LastName:", txtLastName,
                "PhoneNumber:", txtPhoneNumber,
                "Email:", txtEmail,
                "Address:", txtAddress,
                "CustomerType:", cmbCustomerType
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Edit Subscriber", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String customerType = cmbCustomerType.getSelectedItem().toString();
            double charge = calculateCharge(customerType);
            editSubscriber(txtCustomerID.getText(), txtFirstName.getText(), txtLastName.getText(),
                    txtPhoneNumber.getText(), txtEmail.getText(), txtAddress.getText(), customerType, charge);
        }
    }

    private void deleteSubscriber() {
        int selectedRow = tblSubscribers.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a subscriber to delete.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String customerID = tblSubscribers.getValueAt(selectedRow, 0).toString();

        int option = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this subscriber?", "Delete Subscriber", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            deleteSubscriber(customerID);
        }
    }

    private void deleteSubscriber(String customerID) {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM Customers WHERE CustomerID = ?")) {

            statement.setString(1, customerID);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Subscriber deleted successfully!");
                loadDataFromDatabase();
            } else {
                System.out.println("Failed to delete subscriber.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void addSubscriber(String customerID, String firstName, String lastName,
                               String phoneNumber, String email, String address, String customerType, double charge) {
        if (isCustomerIDExists(customerID)) {
            JOptionPane.showMessageDialog(this, "CustomerID already exists. Please choose a different one.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO Customers (CustomerID, FirstName, LastName, PhoneNumber, Email, Address, CustomerType, Charge) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

            statement.setString(1, customerID);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setString(4, phoneNumber);
            statement.setString(5, email);
            statement.setString(6, address);
            statement.setString(7, customerType);
            statement.setDouble(8, charge);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Subscriber added successfully!");
                loadDataFromDatabase();
            } else {
                System.out.println("Failed to add subscriber.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private boolean isCustomerIDExists(String customerID) {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement("SELECT CustomerID FROM Customers WHERE CustomerID = ?")) {

            statement.setString(1, customerID);
            ResultSet resultSet = statement.executeQuery();

            return resultSet.next();

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void editSubscriber(String customerID, String firstName, String lastName,
                                String phoneNumber, String email, String address, String customerType, double charge) {
        if (!customerID.equals(tblSubscribers.getValueAt(tblSubscribers.getSelectedRow(), 0).toString())
                && isCustomerIDExists(customerID)) {
            JOptionPane.showMessageDialog(this, "CustomerID already exists. Please choose a different one.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE Customers SET FirstName = ?, LastName = ?, PhoneNumber = ?, Email = ?, Address = ?, CustomerType = ?, Charge = ? WHERE CustomerID = ?")) {

            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, phoneNumber);
            statement.setString(4, email);
            statement.setString(5, address);
            statement.setString(6, customerType);
            statement.setDouble(7, charge);
            statement.setString(8, customerID);

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Subscriber updated successfully!");
                loadDataFromDatabase();
            } else {
                System.out.println("Failed to update subscriber.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void calculateTotalCharge() {
        double totalCharge = 0;

        for (int i = 0; i < tblSubscribers.getRowCount(); i++) {
            totalCharge += (double) tblSubscribers.getValueAt(i, 7); // Cột cước phí là cột thứ 7 (đếm từ 0)
        }

        JOptionPane.showMessageDialog(this, "Total Charge: " + totalCharge, "Total Charge", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new TelephoneManagerApp().setVisible(true);
            }
        });
    }
}
