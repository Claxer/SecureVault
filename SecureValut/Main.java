package com.securevault;

import com.securevault.db.Database;
import com.securevault.ui.LoginFrame;
import com.securevault.ui.MainFrame;

import javax.crypto.SecretKey;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::openLogin);
    }

    private static void openLogin() {
        try {
            Database database = new Database();

            new LoginFrame(database, Main::openDashboard);

        } catch (Exception e) {
            e.printStackTrace();

            javax.swing.JOptionPane.showMessageDialog(
                    null,
                    "Could not start SecureVault:\n" + e.getMessage(),
                    "Startup Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private static void openDashboard(Database database, SecretKey key) {
        new MainFrame(database, key, Main::openLogin);
    }
}
