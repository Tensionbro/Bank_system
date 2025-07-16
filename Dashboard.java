/**
 *
 * @author Rahul Yadav
 */
public class Dashboard extends JFrame {
    int userId;

    public Dashboard(int userId, String username, double balance) {
        this.userId = userId;

        setTitle("Bank Dashboard");
        setSize(400, 300);
        setLayout(new GridLayout(5, 1));

        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton transferBtn = new JButton("Transfer");
        JButton balanceBtn = new JButton("Check Balance");

        add(depositBtn);
        add(withdrawBtn);
        add(transferBtn);
        add(balanceBtn);

        depositBtn.addActionListener(e -> deposit());
        withdrawBtn.addActionListener(e -> withdraw());
        transferBtn.addActionListener(e -> transfer());
        balanceBtn.addActionListener(e -> checkBalance());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    void deposit() {
        String amountStr = JOptionPane.showInputDialog("Enter amount to deposit:");
        try (Connection conn = DBConnection.connect()) {
            double amount = Double.parseDouble(amountStr);
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = balance + ? WHERE id = ?");
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deposited Successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void withdraw() {
        String amountStr = JOptionPane.showInputDialog("Enter amount to withdraw:");
        try (Connection conn = DBConnection.connect()) {
            double amount = Double.parseDouble(amountStr);

            PreparedStatement checkBalance = conn.prepareStatement("SELECT balance FROM users WHERE id=?");
            checkBalance.setInt(1, userId);
            ResultSet rs = checkBalance.executeQuery();
            if (rs.next() && rs.getDouble("balance") >= amount) {
                PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = balance - ? WHERE id = ?");
                ps.setDouble(1, amount);
                ps.setInt(2, userId);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Withdrawn Successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Insufficient Balance.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void transfer() {
        String toUser = JOptionPane.showInputDialog("Enter recipient username:");
        String amountStr = JOptionPane.showInputDialog("Enter amount to transfer:");

        try (Connection conn = DBConnection.connect()) {
            double amount = Double.parseDouble(amountStr);

            // Check balance
            PreparedStatement checkBalance = conn.prepareStatement("SELECT balance FROM users WHERE id=?");
            checkBalance.setInt(1, userId);
            ResultSet rs = checkBalance.executeQuery();

            if (rs.next() && rs.getDouble("balance") >= amount) {
                // Get recipient ID
                PreparedStatement getRecipient = conn.prepareStatement("SELECT id FROM users WHERE username=?");
                getRecipient.setString(1, toUser);
                ResultSet rsRecipient = getRecipient.executeQuery();

                if (rsRecipient.next()) {
                    int recipientId = rsRecipient.getInt("id");

                    conn.setAutoCommit(false);  // Start transaction

                    PreparedStatement deduct = conn.prepareStatement("UPDATE users SET balance = balance - ? WHERE id = ?");
                    deduct.setDouble(1, amount);
                    deduct.setInt(2, userId);
                    deduct.executeUpdate();

                    PreparedStatement credit = conn.prepareStatement("UPDATE users SET balance = balance + ? WHERE id = ?");
                    credit.setDouble(1, amount);
                    credit.setInt(2, recipientId);
                    credit.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Transfer Successful.");
                } else {
                    JOptionPane.showMessageDialog(this, "Recipient not found.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Insufficient Balance.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void checkBalance() {
        try (Connection conn = DBConnection.connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT balance FROM users WHERE id=?");
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Current Balance: $" + rs.getDouble("balance"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
