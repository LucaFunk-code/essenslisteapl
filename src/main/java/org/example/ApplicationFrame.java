package org.example;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window
 * TablePanel component to portray the tables
 */
public class ApplicationFrame extends JFrame {
    public ApplicationFrame(String title) {
        super( title );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setSize( 1200, 800 );
        TablePanel tablePanel = new TablePanel();
        add( tablePanel );
        tablePanel.loadAllTables(); // loads the saved tables
        setVisible( true );
    }

    @Override
    public String toString() {
        return "ApplicationFrame{}";
    }
}
