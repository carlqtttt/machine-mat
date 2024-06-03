package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.util.*;

public class Deposit extends JFrame implements ActionListener {

    JTextField amount;
    JButton deposit, back;
    String pinnumber;

    Deposit(String pinnumber) {

        this.pinnumber = pinnumber;
        Session sess = Session.getInstance();
        System.out.println(sess.getSignID());
        setLayout(null);

        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icons/atm.jpg"));
        Image i2 = i1.getImage().getScaledInstance(900, 900, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel image = new JLabel(i3);
        image.setBounds(0, 0, 900, 680);
        add(image);

        JLabel text = new JLabel("Enter the amount you want to deposit");
        text.setBounds(170, 200, 700, 35);
        text.setForeground(Color.WHITE);
        text.setFont(new Font("System", Font.BOLD, 16));
        image.add(text);

        amount = new JTextField();
        amount.setFont(new Font("Raleway", Font.BOLD, 22));
        amount.setBounds(170, 250, 320, 25);
        image.add(amount);

        deposit = new JButton("Deposit");
        deposit.setBounds(355, 375, 150, 30);
        deposit.addActionListener(this);
        image.add(deposit);

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
        new Deposit("");
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        Session s = Session.getInstance();
        System.out.println(s.getSignID());
        if (ae.getSource() == deposit) {
            String number = amount.getText();
            Date date = new Date();
            if (number.equals("")) {
                JOptionPane.showMessageDialog(null, "Please, Enter the amount you want to deposit");
            } else {
                try {

                    Conn conn = new Conn();
                    Session sess = Session.getInstance();
//                    conn.insertData("insert into bank values('" + sess.getSignID() + "','" + pinnumber + "','" + date + "', 'Deposit' ,'" + number + "')");
                    conn.insertData("insert into bank (signID, pin, date ,type, amount)"
                            + "values('" + sess.getSignID() + "','" + pinnumber + "','" + date + "', 'Deposit' ,'" + number + "')");
                    JOptionPane.showMessageDialog(null, "Peso " + number + " Deposited Successfully");

                    System.out.println(sess.getSignID());

                    setVisible(false);
                    new Transactions(pinnumber).setVisible(true);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        } else if (ae.getSource() == back) {
            setVisible(false);
            new Transactions(pinnumber).setVisible(true);
        }
    }
}
