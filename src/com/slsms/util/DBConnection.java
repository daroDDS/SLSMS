package com.slsms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection = null;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/slsms_db", "root", "Lion06084");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return connection;
    }
}
