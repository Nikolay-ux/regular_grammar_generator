package org;

import org.controller.Controller;
import org.view.MainFrame;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                UIManager.put("TabbedPane.selected", new Color(200, 220, 255));
                UIManager.put("TabbedPane.contentAreaColor", new Color(240, 240, 240));

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                MainFrame view = new MainFrame();
                new Controller(view);

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                Dimension windowSize = view.getSize();
                view.setLocation(
                        (screenSize.width - windowSize.width) / 2,
                        (screenSize.height - windowSize.height) / 2
                );

                view.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Ошибка при запуске приложения:\n" + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        });
    }
}