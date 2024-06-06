package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.*;
import com.toedter.calendar.JDateChooser;
import java.awt.event.*;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SignupOne extends JFrame implements ActionListener {

    long random;
    JTextField nameTextField, fnameTextField, emailTextField, addressTextField, cityTextField, stateTextField, pinTextField;
    JButton next, back, selectImage;
    JRadioButton male, female, admin, user;
    JDateChooser dateChooser;
    JLabel imageLabel;
    String imagePath = "";
    File selectedFile;
    String destination = "";

    SignupOne() {

        setLayout(null);

        // Random Class is defined inside java.util
        Random ran = new Random();
        random = Math.abs((ran.nextLong() % 9000L) + 1000L);  // Math.abs will convert the negative to positive Integer.

        JLabel formno = new JLabel("APPLICATION FORM NO. " + random);
        formno.setFont(new Font("Raleway", Font.BOLD, 38));
        // If we don't do setLayout(null); Then default Border Layout is used.  setBounds() only works with setLayout(null);
        formno.setBounds(140, 20, 600, 40);
        add(formno);

        JLabel personalDetails = new JLabel("Page 1: Personal Details");
        personalDetails.setFont(new Font("Raleway", Font.BOLD, 22));
        personalDetails.setBounds(290, 80, 400, 20);
        add(personalDetails);

        JLabel dob = new JLabel("Date of Birth:");
        dob.setFont(new Font("Raleway", Font.BOLD, 20));
        dob.setBounds(100, 240, 200, 30);
        add(dob);

        dateChooser = new JDateChooser();
        dateChooser.setBounds(300, 240, 400, 30);
        dateChooser.setForeground(new Color(105, 105, 105));
        add(dateChooser);

        JLabel gender = new JLabel("Gender:");
        gender.setFont(new Font("Raleway", Font.BOLD, 20));
        gender.setBounds(100, 290, 200, 30);
        add(gender);

        male = new JRadioButton("Male");
        male.setBounds(300, 290, 60, 30);
        male.setBackground(Color.WHITE);
        add(male);

        female = new JRadioButton("Female");
        female.setBounds(450, 290, 120, 30);
        female.setBackground(Color.WHITE);
        add(female);

        ButtonGroup gendergroup = new ButtonGroup();
        gendergroup.add(male);
        gendergroup.add(female);

        JLabel email = new JLabel("Email Address:");
        email.setFont(new Font("Raleway", Font.BOLD, 20));
        email.setBounds(100, 340, 200, 30);
        add(email);

        emailTextField = new JTextField();
        emailTextField.setFont(new Font("Raleway", Font.BOLD, 14));
        emailTextField.setBounds(300, 340, 400, 30);
        add(emailTextField);

        JLabel userType = new JLabel("User Type:");
        userType.setFont(new Font("Raleway", Font.BOLD, 20));
        userType.setBounds(100, 390, 200, 30);
        add(userType);

        admin = new JRadioButton("admin");
        admin.setBounds(300, 390, 100, 30);
        admin.setBackground(Color.WHITE);
        add(admin);

        user = new JRadioButton("user");
        user.setBounds(450, 390, 100, 30);
        user.setBackground(Color.WHITE);
        add(user);

        ButtonGroup types = new ButtonGroup();
        types.add(admin);
        types.add(user);

        JLabel pin = new JLabel("Pin Code:");
        pin.setFont(new Font("Raleway", Font.BOLD, 20));
        pin.setBounds(100, 590, 200, 30);
        add(pin);

        pinTextField = new JTextField();
        pinTextField.setFont(new Font("Raleway", Font.BOLD, 14));
        pinTextField.setBounds(300, 590, 400, 30);
        add(pinTextField);

        selectImage = new JButton("Select Image");
        selectImage.setBackground(Color.BLACK);
        selectImage.setForeground(Color.WHITE);
        selectImage.setFont(new Font("Raleway", Font.BOLD, 14));
        selectImage.setBounds(100, 630, 150, 30);
        selectImage.addActionListener(this);
        add(selectImage);

        imageLabel = new JLabel();
        imageLabel.setBounds(300, 630, 100, 100);
        add(imageLabel);

        next = new JButton("Next");
        next.setBackground(Color.BLACK);
        next.setForeground(Color.WHITE);
        next.setFont(new Font("Raleway", Font.BOLD, 14));
        next.setBounds(620, 630, 80, 30);  // Before submitting on Github change JButton's x = 620, y = 660.
        next.addActionListener(this);
        add(next);

        back = new JButton("Back");
        back.setBackground(Color.BLACK);
        back.setForeground(Color.WHITE);
        back.setFont(new Font("Raleway", Font.BOLD, 14));
        back.setBounds(520, 630, 80, 30);  // Before submitting on Github change JButton's x = 620, y = 660.
        back.addActionListener(this);
        add(back);

        getContentPane().setBackground(Color.WHITE);   // Color class is inside awt.

        setSize(850, 800);
        setLocation(350, 10);
        setUndecorated(true);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource() == back) {
            setVisible(false);
            new Login().setVisible(true);
            return;
        }

        if (ae.getSource() == selectImage) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                try {
                    selectedFile = fileChooser.getSelectedFile();
                    destination = "src/UsersImage/" + selectedFile.getName();
                    imagePath = selectedFile.getAbsolutePath();

                    if (new File(destination).exists()) {
                        JOptionPane.showMessageDialog(null, "File Already Exists, Rename or Choose another!");
                        destination = "";
                        imagePath = "";
                    } else {
                        ImageIcon imageIcon = new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));
                        imageLabel.setIcon(imageIcon);
                    }
                } catch (Exception ex) {
                    System.out.println("File Error: " + ex);
                }
            }
            return;
        }

        // Existing actionPerformed code for "Next" button
        String formno = "" + random;  // String <---- long
        String cardnumber = "" + random;
        String dob = ((JTextField) dateChooser.getDateEditor().getUiComponent()).getText();
        String gender = null;
        if (male.isSelected()) {
            gender = "Male";
        } else if (female.isSelected()) {
            gender = "Female";
        }

        String email = emailTextField.getText();
        String xtype = null;
        if (admin.isSelected()) {
            xtype = "admin";
        } else if (user.isSelected()) {
            xtype = "user";
        }

        if (dob.equals("") || email.equals("")) {
            // Validation
            JOptionPane.showMessageDialog(null, "Please Enter The Details !!");
            return;
        }

        if (imagePath.equals("")) {
            JOptionPane.showMessageDialog(null, "Please Select an Image!");
            return;
        }

        try {
            Conn c = new Conn();
            String xxpin = passwordHashing.hashPassword(pinTextField.getText());
            c.insertData("insert into signup (formno, dob, gender, email, type, pin, status, image) "
                    + "values('" + formno + "',"
                    + "'" + dob + "','" + gender + "', '" + email + "', '" + xtype + "','" + xxpin + "', 'pending', '" + destination + "')");

            c.insertData("insert into login (formno, cardnumber, pin, status, type) values('" + formno + "', '" + cardnumber + "', '" + xxpin + "', 'pending', '" + xtype + "')");

            if (destination != null && imagePath != null) {
                Files.copy(selectedFile.toPath(), new File(destination).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            JOptionPane.showMessageDialog(null, "Registration successful! Your Form Number is: " + formno);

            setVisible(false);
            new Login().setVisible(true);

        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error creating account!");
            System.out.println(ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SignupOne.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String args[]) {
        new SignupOne();
    }
}
