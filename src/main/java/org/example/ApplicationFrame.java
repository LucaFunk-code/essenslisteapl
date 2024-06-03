package org.example;

import javax.swing.*;
import java.awt.*;

public class ApplicationFrame extends JFrame {
    public ApplicationFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        TablePanel tablePanel = new TablePanel();
        add(tablePanel);
        tablePanel.loadAllTables(); // Laden der gespeicherten Tabellen beim Start
        setVisible(true);
    }
}
