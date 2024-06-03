package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FastCash extends JFrame implements ActionListener {

    JButton hundred, fivehundred, onethousand, twothousand, fivethousand, tenthousand, back;
    String pinnumber;

    FastCash(String pinnumber) {
        this.pinnumber = pinnumber;

        setLayout(null);

        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/atm.jpg"));
        Image i2 = i1.getImage().getScaledInstance(900, 900, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel image = new JLabel(i3);
        image.setBounds(0, 0, 900, 680);
        add(image);

        JLabel text = new JLabel("SELECT WITHDRAWL AMOUNT");
        text.setBounds(180, 200, 700, 35);
        text.setForeground(Color.WHITE);
        text.setFont(new Font("System", Font.BOLD, 16));
        image.add(text);

        hundred = new JButton("Rs 100");
        hundred.setBounds(170, 305, 150, 30);
        hundred.addActionListener(this);
        image.add(hundred);

        fivehundred = new JButton("Rs 500");
        fivehundred.setBounds(350, 305, 150, 30);
        fivehundred.addActionListener(this);
        image.add(fivehundred);

        onethousand = new JButton("Rs 1000");
        onethousand.setBounds(170, 340, 150, 30);
        onethousand.addActionListener(this);
        image.add(onethousand);

        twothousand = new JButton("Rs 2000");
        twothousand.setBounds(350, 340, 150, 30);
        twothousand.addActionListener(this);
        image.add(twothousand);

        fivethousand = new JButton("Rs 5000");
        fivethousand.setBounds(170, 375, 150, 30);
        fivethousand.addActionListener(this);
        image.add(fivethousand);

        tenthousand = new JButton("Rs 10000");
        tenthousand.setBounds(350, 375, 150, 30);
        tenthousand.addActionListener(this);
        image.add(tenthousand);

        back = new JButton("BACK");
        back.setBounds(350, 410, 150, 30);
        back.addActionListener(this);
        image.add(back);

        setSize(900, 900);
        setLocation(300, 0);
        setUndecorated(true);
        setVisible(true);
    }

    public static void main(String args[]) {
        new FastCash("");
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == back) {
            setVisible(false);
            new Transactions(pinnumber).setVisible(true);
        } else {
            String amount = ((JButton) ae.getSource()).getText().substring(3);  // Rs 500  // Minus first 3 index.
            Conn c = null;
            try {
                c = new Conn();
            } catch (SQLException ex) {
                Logger.getLogger(FastCash.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                // Checking balance before Withdrawing money.
                ResultSet rs = c.getData("select * from bank where pin = '" + pinnumber + "'");
                int balance = 0;
                while (rs.next()) {
                    // We are looping through each rows.
                    if (rs.getString("type").equals("Deposit")) {
                        balance += Integer.parseInt(rs.getString("amount"));
                    } else {
                        balance -= Integer.parseInt(rs.getString("amount"));
                    }
                }

                if (ae.getSource() != null && balance < Integer.parseInt(amount)) {
                    JOptionPane.showMessageDialog(null, "Insufficient Balance");
                    return;
                }

                Date date = new Date();
                c.insertData("insert into bank values('" + pinnumber + "','" + date + "','Withdrawl','" + amount + "')");
                JOptionPane.showMessageDialog(null, "Rs " + amount + " Withdrawn Successfully");
                setVisible(false);
                new Transactions(pinnumber).setVisible(true);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}
