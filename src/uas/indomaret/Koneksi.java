package uas.indomaret;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    
    private static final String URL = "jdbc:mysql://localhost:3306/db_indomaret_uas";
    private static final String USER = "root";
    private static final String PASS = "";

    // PERBAIKAN 1: Ubah 'Koneksi' menjadi 'Connection' pada tipe method
    public static Connection getConnection() {
        // PERBAIKAN 2: Ubah 'Koneksi' menjadi 'Connection' pada deklarasi variabel
        Connection conn = null; 
        try {
            conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Koneksi Berhasil");
        } catch (SQLException e) {
            System.out.println("Koneksi Gagal: " + e.getMessage());
        }
        return conn;
    }
}