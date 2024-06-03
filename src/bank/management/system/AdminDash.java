/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bank.management.system;

import com.toedter.calendar.JDateChooser;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import net.proteanit.sql.DbUtils;

public class AdminDash extends javax.swing.JFrame {

    public File selectedFile;
    public String path2 = null;
    public String destination = "";
    public String oldPath;
    public String path;
    JDateChooser dateChooser;

    public AdminDash() {
        initComponents();
        displayData();
        pendingAccounts();
        UserLogs();
        remove.setEnabled(false);
    }

    private boolean validationChecker() {
        if (newPassword.getText().isEmpty() || oldPassword.getText().isEmpty() || cpassword.getText().isEmpty()) {
            JOptionPane.showMessageDialog(null, "FILL ALL THE REQUIREMENTS!");
            return false;
        } else if (newPassword.getText().length() < 8) {
            JOptionPane.showMessageDialog(null, "PASSWORD MUST ATLEAST 8 CHARACTERS!");
            return false;
        } else if (!newPassword.getText().equals(cpassword.getText())) {
            JOptionPane.showMessageDialog(null, "PASSWORD DO NOT MATCH!");
            return false;
        } else {
            return true;
        }
    }

    private void displayData() {
        try {
            Session sess = Session.getInstance();
            ResultSet rs = new Conn().getData("select * from signup where signID != '" + sess.getSignID() + "'");
            usersTB.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            System.err.println("An error occurred while fetching data: " + e.getMessage());
        }
    }

    private void UserLogs() {
        try {
            ResultSet rs = new Conn().getData("select * from logs");
            logs.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            System.err.println("An error occurred while fetching data: " + e.getMessage());
        }
    }

    private void pendingAccounts() {
        try {
            ResultSet rs = new Conn().getData("select * from signup where status = 'pending'");
            pendings.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException e) {
            System.err.println("An error occurred while fetching data: " + e.getMessage());
        }
    }

    private void acceptAccount() throws SQLException {
        int rowIndex = pendings.getSelectedRow();
        if (rowIndex < 0) {
            JOptionPane.showMessageDialog(null, "PLEASE SELECT AN INDEX!");
        } else {
            TableModel tbl = pendings.getModel();
            new Conn().updateData("UPDATE signup SET status = 'ACTIVE' WHERE signID = '" + tbl.getValueAt(rowIndex, 0).toString() + "'");
            JOptionPane.showMessageDialog(null, "ACCOUNT APPROVED SUCCESSFULLY!!");
            displayData();
        }
    }

    private void declineAccount() throws SQLException {
        int rowIndex = pendings.getSelectedRow();
        if (rowIndex < 0) {
            JOptionPane.showMessageDialog(null, "PLEASE SELECT AN INDEX!");
        } else {
            TableModel tbl = pendings.getModel();
            new Conn().updateData("UPDATE signup SET status = 'declined' WHERE signID = '" + tbl.getValueAt(rowIndex, 0).toString() + "'");
            JOptionPane.showMessageDialog(null, "ACCOUNT APPROVED SUCCESSFULLY!!");
            displayData();
        }
    }

    public void deleteProduct() throws NoSuchAlgorithmException, SQLException {
        int confirmation = JOptionPane.showConfirmDialog(null, "ARE YOU SURE YOU WANT TO DELETE THIS PRODUCT?", "CONFIRMATION", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            Conn cn = new Conn();
            String query = "DELETE FROM signup WHERE signID = '" + id.getText() + "'";
            try (PreparedStatement pstmt = cn.getConnection().prepareStatement(query)) {
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "PRODUCT DELETED SUCCESSFULLY!");
                displayData();
                pendingAccounts();
                UserLogs();
                jTabbedPane1.setSelectedIndex(0);
            }
        }
    }

    public void addAccounts() throws NoSuchAlgorithmException {
        try {
            String xpprice = pprice1.getText().trim();
            String xpstocks = pstocks1.getText().trim();
            String xpstatus = pstatus1.getSelectedItem() == null ? "" : pstatus1.getSelectedItem().toString().trim();
            String xpstatuss = pstatus2.getSelectedItem() == null ? "" : pstatus2.getSelectedItem().toString().trim();
            String xpstatusss = pstatus3.getSelectedItem() == null ? "" : pstatus3.getSelectedItem().toString().trim();

            if (xpprice.isEmpty() || xpstocks.isEmpty() || xpstatus.isEmpty()) {
                JOptionPane.showMessageDialog(null, "PLEASE FILL ALL THE FIELDS!");
            } else if (destination == null || destination.isEmpty()) {
                JOptionPane.showMessageDialog(null, "PLEASE INSERT AN IMAGE FIRST!");
            } else {
                try {
                    Conn cn = new Conn();

                    long random;
                    Random ran = new Random();
                    random = Math.abs((ran.nextLong() % 9000L) + 1000L);
                    String formno = "" + random;
                    String xpin = passwordHashing.hashPassword(pin.getText());

                    cn.insertData("insert into signup (formno, dob, gender, email, type, pin, status, image) "
                            + "values('" + formno + "',"
                            + "'" + xpprice + "','" + xpstocks + "', '" + xpstatus + "', '" + xpstatuss + "','" + xpin + "', '" + xpstatusss + "', '" + destination + "')");

                    if (destination != null && path != null) {
                        Files.copy(selectedFile.toPath(), new File(destination).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }

                    JOptionPane.showMessageDialog(this, "ACCOUNT CREATED SUCCESSFULLY!");
                    displayData();
                    pendingAccounts();
                    UserLogs();
                    jTabbedPane1.setSelectedIndex(0);

                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Error!");
                }
            }
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error creating product!");
            System.out.println(ex.getMessage());
        }
    }

    public void updateProduct() throws NoSuchAlgorithmException {
        try {
            String xdob = dob.getText().trim();
            String xemail = email.getText().trim();
            String xtype = type.getSelectedItem() == null ? "" : type.getSelectedItem().toString().trim();
            String xstatus = status.getSelectedItem() == null ? "" : status.getSelectedItem().toString().trim();
            String xgender = gender.getSelectedItem() == null ? "" : gender.getSelectedItem().toString().trim();

            if (xdob.isEmpty() || xemail.isEmpty() || xtype.isEmpty() || xstatus.isEmpty() || xgender.isEmpty()) {
                JOptionPane.showMessageDialog(null, "PLEASE FILL ALL THE FIELDS!");
            } else if (destination == null || destination.isEmpty()) {
                JOptionPane.showMessageDialog(null, "PLEASE INSERT AN IMAGE FIRST!");
            } else {
                Conn cn = new Conn();
                cn.updateData("update signup set dob = '" + xdob + "', gender = '" + xgender + "',email='" + xemail + "', "
                        + "type='" + xtype + "', status = '" + xstatus + "',image= '" + destination + "' where signID = '" + id.getText() + "'");

                if (selectedFile != null) {
                    Files.copy(selectedFile.toPath(), new File(destination).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                JOptionPane.showMessageDialog(this, "PRODUCT UPDATED SUCCESSFULLY!");
                displayData();
                pendingAccounts();
                UserLogs();
                jTabbedPane1.setSelectedIndex(0);
            }
        } catch (SQLException | IOException ex) {
            JOptionPane.showMessageDialog(this, "Error updating product!");
            System.out.println(ex.getMessage());
        }
    }

    public static int getHeightFromWidth(String imagePath, int desiredWidth) {
        try {
            File imageFile = new File(imagePath);
            BufferedImage image = ImageIO.read(imageFile);

            int originalWidth = image.getWidth();
            int originalHeight = image.getHeight();

            int newHeight = (int) ((double) desiredWidth / originalWidth * originalHeight);

            return newHeight;
        } catch (IOException ex) {
            System.out.println("No image found!");
        }

        return -1;
    }

    private ImageIcon ResizeImage(String ImagePath, byte[] pic, JLabel label) {
        ImageIcon MyImage = null;
        if (ImagePath != null) {
            MyImage = new ImageIcon(ImagePath);
        } else {
            MyImage = new ImageIcon(pic);
        }

        int newHeight = getHeightFromWidth(ImagePath, label.getWidth());

        Image img = MyImage.getImage();
        Image newImg = img.getScaledInstance(label.getWidth(), newHeight, Image.SCALE_SMOOTH);
        ImageIcon image = new ImageIcon(newImg);
        return image;
    }

    private int FileExistenceChecker(String path) {
        File file = new File(path);
        String fileName = file.getName();

        Path filePath = Paths.get("src/ProductsImage", fileName);
        boolean fileExists = Files.exists(filePath);

        if (fileExists) {
            return 1;
        } else {
            return 0;
        }

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        usersTB = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        remove = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        pprice1 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        pstocks1 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        pstatus1 = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        icon1 = new javax.swing.JLabel();
        select = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jLabel21 = new javax.swing.JLabel();
        pstatus2 = new javax.swing.JComboBox<>();
        jLabel28 = new javax.swing.JLabel();
        pstatus3 = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        pin = new javax.swing.JPasswordField();
        jPanel6 = new javax.swing.JPanel();
        id = new javax.swing.JTextField();
        jButton27 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        email = new javax.swing.JTextField();
        gender = new javax.swing.JComboBox<>();
        jLabel17 = new javax.swing.JLabel();
        dob = new javax.swing.JTextField();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        type = new javax.swing.JComboBox<>();
        jLabel31 = new javax.swing.JLabel();
        status = new javax.swing.JComboBox<>();
        jPanel11 = new javax.swing.JPanel();
        icon3 = new javax.swing.JLabel();
        jButton26 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        pendings = new javax.swing.JTable();
        jButton32 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        logs = new javax.swing.JTable();
        jButton37 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        id1 = new javax.swing.JTextField();
        jButton33 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        email1 = new javax.swing.JTextField();
        gender1 = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        dob1 = new javax.swing.JTextField();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        type1 = new javax.swing.JComboBox<>();
        jLabel34 = new javax.swing.JLabel();
        status1 = new javax.swing.JComboBox<>();
        jPanel12 = new javax.swing.JPanel();
        icon4 = new javax.swing.JLabel();
        jButton36 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        showPass = new javax.swing.JCheckBox();
        cpassword = new javax.swing.JPasswordField();
        newPassword = new javax.swing.JPasswordField();
        oldPassword = new javax.swing.JPasswordField();
        jLabel20 = new javax.swing.JLabel();
        jPanel13 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(1240, 640));
        setUndecorated(true);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel1.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 110, 1240, 10));

        jLabel1.setFont(new java.awt.Font("Arial", 0, 21)); // NOI18N
        jLabel1.setText("CVL  AUTOMATED TELLER MACHINES");
        jPanel1.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 44, -1, -1));

        jButton3.setText("LOGOUT");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 40, 130, 30));

        jLabel2.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/icons8-admin-100.png"))); // NOI18N
        jLabel2.setText("ADMINS NAME");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 0, -1, 90));

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1240, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 1240, 0));

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        usersTB.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane2.setViewportView(usersTB);

        jPanel2.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 1040, 450));

        jButton1.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton1.setText("LOGOUT");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 570, 110, -1));

        jButton5.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton5.setText("MY ACCOUNT");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(910, 570, 110, -1));

        jButton9.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton9.setText("EDIT");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton9ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton9, new org.netbeans.lib.awtextra.AbsoluteConstraints(220, 570, 110, -1));

        jButton7.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton7.setText("PENDING");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton7, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 570, 110, -1));

        jButton2.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton2.setText("CREATE");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 570, 110, -1));

        jButton13.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton13.setText("VIEW LOGS");
        jButton13.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton13ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton13, new org.netbeans.lib.awtextra.AbsoluteConstraints(460, 570, 150, -1));

        jButton11.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton11.setText("PRINT");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });
        jPanel2.add(jButton11, new org.netbeans.lib.awtextra.AbsoluteConstraints(1010, 70, 130, -1));

        jTabbedPane1.addTab("tab1", jPanel2);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        remove.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        remove.setText("REMOVE");
        remove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeActionPerformed(evt);
            }
        });
        jPanel4.add(remove, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 300, 170, -1));

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Date Of Birth");
        jPanel4.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 370, 230, -1));

        pprice1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel4.add(pprice1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 390, 230, 30));

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel13.setText("Email");
        jPanel4.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 430, 230, -1));

        pstocks1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel4.add(pstocks1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 450, 230, 30));

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel14.setText("Gender");
        jPanel4.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 430, 230, -1));

        pstatus1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "male", "female", "others" }));
        jPanel4.add(pstatus1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 450, 230, 30));

        jPanel5.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel5.add(icon1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 450, 200));

        jPanel4.add(jPanel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 20, 470, 220));

        select.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        select.setText("SELECT");
        select.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectActionPerformed(evt);
            }
        });
        jPanel4.add(select, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 300, 170, -1));

        jButton22.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton22.setText("BACK");
        jButton22.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton22ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton22, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 600, 110, -1));

        jButton25.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton25.setText("ADD");
        jButton25.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton25ActionPerformed(evt);
            }
        });
        jPanel4.add(jButton25, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 600, 110, -1));

        jLabel21.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("Type");
        jPanel4.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 360, 230, -1));

        pstatus2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "user" }));
        jPanel4.add(pstatus2, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 380, 230, 30));

        jLabel28.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel28.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel28.setText("Status");
        jPanel4.add(jLabel28, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 430, 230, -1));

        pstatus3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "active", "in_active", "pending" }));
        jPanel4.add(pstatus3, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 450, 230, 30));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Pin");
        jPanel4.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 510, 230, -1));
        jPanel4.add(pin, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 540, 230, 30));

        jTabbedPane1.addTab("tab1", jPanel4);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        id.setEditable(false);
        id.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        id.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idActionPerformed(evt);
            }
        });
        jPanel6.add(id, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 320, 470, 30));

        jButton27.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton27.setText("BACK");
        jButton27.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton27ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton27, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 630, 240, -1));

        jButton28.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton28.setText("UPDATE");
        jButton28.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton28ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton28, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 630, 230, -1));

        jButton29.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton29.setText("DELETE");
        jButton29.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton29ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton29, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 630, 230, -1));

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("Email");
        jPanel6.add(jLabel16, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 450, 230, -1));

        email.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel6.add(email, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 470, 230, 30));

        gender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "male", "female", "others" }));
        jPanel6.add(gender, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 470, 230, 30));

        jLabel17.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Gender");
        jPanel6.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 450, 230, -1));

        dob.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel6.add(dob, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 400, 230, 30));

        jLabel29.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("Date Of Birth");
        jPanel6.add(jLabel29, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 380, 230, -1));

        jLabel30.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel30.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel30.setText("Type");
        jPanel6.add(jLabel30, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 380, 230, -1));

        type.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "user" }));
        jPanel6.add(type, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 400, 230, 30));

        jLabel31.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel31.setText("Status");
        jPanel6.add(jLabel31, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 450, 230, -1));

        status.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "active", "in_active", "pending" }));
        jPanel6.add(status, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 470, 230, 30));

        jPanel11.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel11.add(icon3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 450, 200));

        jPanel6.add(jPanel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 20, 470, 220));

        jButton26.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton26.setText("SELECT");
        jButton26.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton26ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton26, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 280, 170, -1));

        jButton23.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton23.setText("REMOVE");
        jButton23.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton23ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton23, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 280, 170, -1));

        jTabbedPane1.addTab("tab1", jPanel6);

        jPanel8.setBackground(new java.awt.Color(255, 255, 255));
        jPanel8.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton30.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton30.setText("BACK");
        jButton30.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton30ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton30, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 600, 260, -1));

        jButton31.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton31.setText("APPROVE");
        jButton31.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton31ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton31, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 600, 260, -1));

        pendings.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(pendings);

        jPanel8.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 1180, 530));

        jButton32.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton32.setText("PRINT");
        jButton32.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton32ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton32, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 1180, -1));

        jButton4.setText("DECLINE");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel8.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(975, 600, 230, 30));

        jTabbedPane1.addTab("tab1", jPanel8);

        jPanel10.setBackground(new java.awt.Color(255, 255, 255));
        jPanel10.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logs.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane5.setViewportView(logs);

        jPanel10.add(jScrollPane5, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 110, 1030, 440));

        jButton37.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton37.setText("BACK");
        jButton37.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton37ActionPerformed(evt);
            }
        });
        jPanel10.add(jButton37, new org.netbeans.lib.awtextra.AbsoluteConstraints(1020, 560, 110, -1));

        jButton38.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton38.setText("PRINT");
        jButton38.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton38ActionPerformed(evt);
            }
        });
        jPanel10.add(jButton38, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 70, 110, -1));

        jTabbedPane1.addTab("tab1", jPanel10);

        jPanel7.setBackground(new java.awt.Color(255, 255, 255));
        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        id1.setEditable(false);
        id1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        id1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                id1ActionPerformed(evt);
            }
        });
        jPanel7.add(id1, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 320, 470, 30));

        jButton33.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton33.setText("BACK");
        jButton33.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton33ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton33, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 630, 240, -1));

        jButton34.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton34.setText("CHANGE PIN");
        jButton34.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton34ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton34, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 630, 230, -1));

        jLabel18.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("Email");
        jPanel7.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 450, 230, -1));

        email1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel7.add(email1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 470, 230, 30));

        gender1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "male", "female", "others" }));
        jPanel7.add(gender1, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 470, 230, 30));

        jLabel19.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel19.setText("Gender");
        jPanel7.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 450, 230, -1));

        dob1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jPanel7.add(dob1, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 400, 230, 30));

        jLabel32.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel32.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel32.setText("Date Of Birth");
        jPanel7.add(jLabel32, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 380, 230, -1));

        jLabel33.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel33.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel33.setText("Type");
        jPanel7.add(jLabel33, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 380, 230, -1));

        type1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "admin", "user" }));
        jPanel7.add(type1, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 400, 230, 30));

        jLabel34.setFont(new java.awt.Font("Tahoma", 0, 15)); // NOI18N
        jLabel34.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel34.setText("Status");
        jPanel7.add(jLabel34, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 450, 230, -1));

        status1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "active", "in_active", "pending" }));
        jPanel7.add(status1, new org.netbeans.lib.awtextra.AbsoluteConstraints(730, 470, 230, 30));

        jPanel12.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel12.add(icon4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 450, 200));

        jPanel7.add(jPanel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 20, 470, 220));

        jButton36.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton36.setText("SELECT");
        jButton36.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton36ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton36, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 280, 170, -1));

        jButton24.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton24.setText("REMOVE");
        jButton24.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton24ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton24, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 280, 170, -1));

        jButton35.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton35.setText("UPDATE");
        jButton35.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton35ActionPerformed(evt);
            }
        });
        jPanel7.add(jButton35, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 630, 230, -1));

        jTabbedPane1.addTab("tab1", jPanel7);

        jPanel9.setBackground(new java.awt.Color(255, 255, 255));
        jPanel9.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jButton6.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton6.setText("CANCEL");
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });
        jPanel9.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 470, 100, -1));

        jButton8.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        jButton8.setText("CHANGE");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });
        jPanel9.add(jButton8, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 470, 100, -1));

        showPass.setBackground(new java.awt.Color(255, 255, 255));
        showPass.setFont(new java.awt.Font("Yu Gothic", 0, 11)); // NOI18N
        showPass.setText("SHOW PASSWORD");
        showPass.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPassActionPerformed(evt);
            }
        });
        jPanel9.add(showPass, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 330, 240, -1));

        cpassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        cpassword.setText("CONFIRM PASS");
        jPanel9.add(cpassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 290, 360, 30));

        newPassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        newPassword.setText("NEW PASSWORD");
        jPanel9.add(newPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 250, 360, 30));

        oldPassword.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        oldPassword.setText("OLD PASSWORD");
        jPanel9.add(oldPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 190, 360, 30));

        jLabel20.setFont(new java.awt.Font("Yu Gothic", 1, 18)); // NOI18N
        jLabel20.setText("CHANGE PASS");
        jPanel9.add(jLabel20, new org.netbeans.lib.awtextra.AbsoluteConstraints(510, 120, 140, 40));

        jTabbedPane1.addTab("tab1", jPanel9);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1235, Short.MAX_VALUE)
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 675, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab8", jPanel13);

        jPanel1.add(jTabbedPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 1240, 700));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton38ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton38ActionPerformed
        MessageFormat header = new MessageFormat("Total Success Delivery Reports");
        MessageFormat footer = new MessageFormat("Page{0,number,integer}");
        try {
            logs.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException er) {
            System.out.println("" + er.getMessage());
        }
    }//GEN-LAST:event_jButton38ActionPerformed

    private void jButton37ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton37ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jButton37ActionPerformed

    private void jButton32ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton32ActionPerformed
        MessageFormat header = new MessageFormat("Total Pending Orders Reports");
        MessageFormat footer = new MessageFormat("Page{0,number,integer}");
        try {
            pendings.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException er) {
            System.out.println("" + er.getMessage());
        }
    }//GEN-LAST:event_jButton32ActionPerformed

    private void jButton31ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton31ActionPerformed
        try {
            acceptAccount();
        } catch (SQLException ex) {
            Logger.getLogger(AdminDash.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton31ActionPerformed

    private void jButton30ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton30ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jButton30ActionPerformed

    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        destination = "";
        path = "";
        icon3.setIcon(null);
        oldPath = "";
    }//GEN-LAST:event_jButton23ActionPerformed

    private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                selectedFile = fileChooser.getSelectedFile();
                destination = "src/UsersImage/" + selectedFile.getName();
                path = selectedFile.getAbsolutePath();

                if (FileExistenceChecker(path) == 1) {
                    JOptionPane.showMessageDialog(null, "File Already Exist, Rename or Choose another!");
                    destination = "";
                    path = "";
                } else {
                    icon3.setIcon(ResizeImage(path, null, icon3));
                    remove.setEnabled(true);
                    select.setEnabled(false);
                }
            } catch (Exception ex) {
                System.out.println("File Error!");
            }
        }
    }//GEN-LAST:event_jButton26ActionPerformed

    private void jButton29ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton29ActionPerformed
        try {
            deleteProduct();
        } catch (NoSuchAlgorithmException | SQLException ex) {
            Logger.getLogger(AdminDash.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton29ActionPerformed

    private void jButton28ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton28ActionPerformed
        try {
            updateProduct();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AdminDash.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton28ActionPerformed

    private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
        displayData();
        pendingAccounts();
        UserLogs();
    }//GEN-LAST:event_jButton27ActionPerformed

    private void idActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_idActionPerformed

    private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
        try {
            addAccounts();
        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(null, "Error" + ex.getMessage());
        }
    }//GEN-LAST:event_jButton25ActionPerformed

    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jButton22ActionPerformed

    private void selectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                selectedFile = fileChooser.getSelectedFile();
                destination = "src/UsersImage/" + selectedFile.getName();
                path = selectedFile.getAbsolutePath();

                if (FileExistenceChecker(path) == 1) {
                    JOptionPane.showMessageDialog(null, "File Already Exist, Rename or Choose another!");
                    destination = "";
                    path = "";
                } else {
                    icon1.setIcon(ResizeImage(path, null, icon1));
                    remove.setEnabled(true);
                    select.setEnabled(false);
                }
            } catch (Exception ex) {
                System.out.println("File Error!");
            }
        }
    }//GEN-LAST:event_selectActionPerformed

    private void removeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeActionPerformed
        destination = "";
        icon1.setIcon(null);
        path = "";
        select.setEnabled(true);
        remove.setEnabled(false);
    }//GEN-LAST:event_removeActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        MessageFormat header = new MessageFormat("Total Accounts Registered Reports");
        MessageFormat footer = new MessageFormat("Page{0,number,integer}");
        try {
            usersTB.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (PrinterException er) {
            System.out.println("" + er.getMessage());
        }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        jTabbedPane1.setSelectedIndex(4);
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        jTabbedPane1.setSelectedIndex(1);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        jTabbedPane1.setSelectedIndex(3);
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        {
            int rowIndex = usersTB.getSelectedRow();

            if (rowIndex < 0) {
                JOptionPane.showMessageDialog(null, "Please Choose An Index!");
            } else {
                TableModel tbl = usersTB.getModel();

                String query = "SELECT * FROM signup WHERE signID = '" + tbl.getValueAt(rowIndex, 0) + "'";
                try {
                    try (ResultSet rs = new Conn().getData(query)) {
                        if (rs.next()) {
                            id.setText(rs.getString("signID"));
                            dob.setText(rs.getString("dob"));
                            email.setText(rs.getString("dob"));
                            gender.setSelectedItem(rs.getString("gender"));
                            status.setSelectedItem(rs.getString("status"));
                            type.setSelectedItem(rs.getString("type"));

                            String imagePath = rs.getString("image");

                            SwingUtilities.invokeLater(() -> {
                                jTabbedPane1.setSelectedIndex(2);
                            });

                            if (imagePath != null && !imagePath.isEmpty()) {
                                icon3.setIcon(ResizeImage(imagePath, null, icon3));
                                oldPath = imagePath;
                                path = imagePath;
                                destination = imagePath;
                                select.setEnabled(false);
                                remove.setEnabled(true);
                            } else {
                                select.setEnabled(true);
                                remove.setEnabled(false);
                            }
                        } else {
                            System.out.println("No data found for id: " + tbl.getValueAt(rowIndex, 6));
                        }
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(AdminDash.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        {
            JOptionPane.showMessageDialog(null, "Please Choose An Index!");
            TableModel tbl = usersTB.getModel();
            Session sess = Session.getInstance();
            String query = "SELECT * FROM signup WHERE signID = '" + sess.getSignID() + "'";
            try {
                try (ResultSet rs = new Conn().getData(query)) {
                    if (rs.next()) {
                        id.setText(rs.getString("signID"));
                        dob.setText(rs.getString("dob"));
                        email.setText(rs.getString("dob"));
                        gender.setSelectedItem(rs.getString("gender"));
                        status.setSelectedItem(rs.getString("status"));
                        type.setSelectedItem(rs.getString("type"));

                        String imagePath = rs.getString("image");

                        SwingUtilities.invokeLater(() -> {
                            jTabbedPane1.setSelectedIndex(5);
                        });

                        if (imagePath != null && !imagePath.isEmpty()) {
                            icon3.setIcon(ResizeImage(imagePath, null, icon3));
                            oldPath = imagePath;
                            path = imagePath;
                            destination = imagePath;
                            select.setEnabled(false);
                            remove.setEnabled(true);
                        } else {
                            select.setEnabled(true);
                            remove.setEnabled(false);
                        }
                    } else {
                        System.out.println("No data found for id: " + sess.getSignID());
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(AdminDash.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Login ld = new Login();
        ld.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        try {
            declineAccount();
        } catch (SQLException ex) {
            Logger.getLogger(AdminDash.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void id1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_id1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_id1ActionPerformed

    private void jButton33ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton33ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton33ActionPerformed

    private void jButton34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton34ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton34ActionPerformed

    private void jButton36ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton36ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton36ActionPerformed

    private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton24ActionPerformed

    private void jButton35ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton35ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton35ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        jTabbedPane1.setSelectedIndex(0);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        try {
            if (!validationChecker()) {
            } else {
                Session sess = Session.getInstance();
                ResultSet rs = new Conn().getData("select * from signup where signID = '" + sess.getSignID() + "'");
                if (rs.next()) {
                    String oldPass = rs.getString("pin");
                    String oldHash = passwordHashing.hashPassword(oldPassword.getText());

                    if (oldPass.equals(oldHash)) {
                        String newPass = passwordHashing.hashPassword(newPassword.getText());
                        new Conn().updateData("update signup set pin = '" + newPass + "' where signID = '" + sess.getSignID() + "'");
                        JOptionPane.showMessageDialog(null, "ACCOUNT SUCCESSFULLY UPDATED!");
                        new Login().setVisible(true);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "OLD PASSWORD IS INCORRECT!");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "NO ACCOUNT FOUND!");
                }
            }
        } catch (SQLException | NoSuchAlgorithmException er) {
            System.out.println("Error: " + er.getMessage());
        }
    }//GEN-LAST:event_jButton8ActionPerformed

    private void showPassActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPassActionPerformed
        char echoChar = showPass.isSelected() ? (char) 0 : '*';
        oldPassword.setEchoChar(echoChar);
        newPassword.setEchoChar(echoChar);
        cpassword.setEchoChar(echoChar);
    }//GEN-LAST:event_showPassActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jButton3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdminDash.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminDash.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminDash.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminDash.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminDash().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPasswordField cpassword;
    private javax.swing.JTextField dob;
    private javax.swing.JTextField dob1;
    private javax.swing.JTextField email;
    private javax.swing.JTextField email1;
    private javax.swing.JComboBox<String> gender;
    private javax.swing.JComboBox<String> gender1;
    private javax.swing.JLabel icon1;
    private javax.swing.JLabel icon3;
    private javax.swing.JLabel icon4;
    private javax.swing.JTextField id;
    private javax.swing.JTextField id1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable logs;
    private javax.swing.JPasswordField newPassword;
    private javax.swing.JPasswordField oldPassword;
    private javax.swing.JTable pendings;
    private javax.swing.JPasswordField pin;
    private javax.swing.JTextField pprice1;
    private javax.swing.JComboBox<String> pstatus1;
    private javax.swing.JComboBox<String> pstatus2;
    private javax.swing.JComboBox<String> pstatus3;
    private javax.swing.JTextField pstocks1;
    private javax.swing.JButton remove;
    private javax.swing.JButton select;
    private javax.swing.JCheckBox showPass;
    private javax.swing.JComboBox<String> status;
    private javax.swing.JComboBox<String> status1;
    private javax.swing.JComboBox<String> type;
    private javax.swing.JComboBox<String> type1;
    private javax.swing.JTable usersTB;
    // End of variables declaration//GEN-END:variables
}
