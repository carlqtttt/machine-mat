package bank.management.system;

import java.sql.*;

public class Conn {

    final private com.mysql.jdbc.Connection cn;

    public Conn() throws SQLException {
        cn = (com.mysql.jdbc.Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/atm", "root", "");
    }

    public ResultSet getData(String sql) throws SQLException {
        return cn.createStatement().executeQuery(sql);
    }

    public com.mysql.jdbc.Connection getConnection() throws SQLException {
        return cn;
    }

    public int insertData(String sql) throws SQLException {
        return cn.prepareStatement(sql).executeUpdate();
    }

    public int updateData(String sql) throws SQLException {
        return cn.prepareStatement(sql).executeUpdate();
    }
}
