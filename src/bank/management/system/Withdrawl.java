package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

public class Withdrawl extends JFrame implements ActionListener {

    JTextField amount;
    JButton withdraw, back;
    String pinnumber;

    Withdrawl(String pinnumber) {

        this.pinnumber = pinnumber;

        setLayout(null);

        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/atm.jpg"));
        Image i2 = i1.getImage().getScaledInstance(900, 900, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel image = new JLabel(i3);
        image.setBounds(0, 0, 900, 680);
        add(image);

        JLabel text = new JLabel("Enter the amount you want to withdraw");
        text.setBounds(170, 200, 700, 35);
        text.setForeground(Color.WHITE);
        text.setFont(new Font("System", Font.BOLD, 16));
        image.add(text);

        amount = new JTextField();
        amount.setFont(new Font("Raleway", Font.BOLD, 22));
        amount.setBounds(170, 250, 320, 25);
        image.add(amount);

        withdraw = new JButton("Withdraw");
        withdraw.setBounds(355, 375, 150, 30);
        withdraw.addActionListener(this);
        image.add(withdraw);

        back = new JButton("Back");
        back.setBounds(355, 408, 150, 30);
        back.addActionListener(this);
        image.add(back);

        setSize(900, 900);
        setLocation(300, 0);
        setUndecorated(true);
        setVisible(true);
    }

    public static void main(String args[]) {
        new Withdrawl("");
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == withdraw) {
            String number = amount.getText();
            if (number.equals("")) {
                JOptionPane.showMessageDialog(null, "Please enter the amount you want to withdraw");
            } else {
                try {
                    // Assuming these methods and classes are properly defined
                    Session sess = Session.getInstance();
                    String hashedPass = passwordHashing.hashPassword(pinnumber);
                    Conn cn = new Conn();

                    // Using PreparedStatement to avoid SQL injection
                    String sql = "INSERT INTO bank (pin, date, type, amount) VALUES (?, ?, ?, ?)";
                    PreparedStatement pst = cn.getConnection().prepareStatement(sql);

                    // Set the values for the PreparedStatement
                    pst.setString(1, hashedPass);
                    pst.setDate(2, new java.sql.Date(new Date().getTime()));
                    pst.setString(3, "Withdraw");
                    pst.setString(4, number);

                    // Execute the update
                    pst.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Rs " + number + " withdrawn successfully");

                } catch (SQLIntegrityConstraintViolationException duplicateKeyException) {
                    JOptionPane.showMessageDialog(null, "Failed to withdraw. Duplicate primary key ID detected. " + duplicateKeyException.getMessage());
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "An error occurred: " + e.getMessage());
                }
            }
        } else if (ae.getSource() == back) {
            setVisible(false);
            new Transactions(pinnumber).setVisible(true);
        }
    }

}
