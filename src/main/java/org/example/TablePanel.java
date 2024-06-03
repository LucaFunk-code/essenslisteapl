package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class TablePanel extends JPanel {
    private final DefaultListModel<String> tableListModel;
    private final JList<String> tableList;
    private final JPanel tableDetailPanel;
    private int tableCount = 0;
    private final DataStorage dataStorage;

    public TablePanel() {
        this.dataStorage = new XmlDataStorage();

        setLayout(new BorderLayout());
        // List Panel
        tableListModel = new DefaultListModel<>();
        tableList = new JList<>(tableListModel);
        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableList.addListSelectionListener(_ -> showTableDetail(tableList.getSelectedIndex()));
        JScrollPane listScrollPane = new JScrollPane(tableList);
        listScrollPane.setPreferredSize(new Dimension(200, 0));
        add(listScrollPane, BorderLayout.WEST);

        // Detail Panel
        tableDetailPanel = new JPanel(new CardLayout());
        add(tableDetailPanel, BorderLayout.CENTER);

        // Button to add new table
        JButton addButton = new JButton("Neue Tabelle hinzufügen");
        addButton.addActionListener(_ -> addNewTable());

        // Button to add new row to selected table
        JButton addRowButton = new JButton("Neue Zeile hinzufügen");
        addRowButton.addActionListener(_ -> addNewRowToSelectedTable());

        // Button to rename table
        JButton renameButton = new JButton("Tabellennamen ändern");
        renameButton.addActionListener(_ -> changeTableName());

        JButton sortButton = new JButton("Tabellen nach Namen sortieren");
        sortButton.addActionListener(_ -> {
            String name = JOptionPane.showInputDialog("Geben Sie einen Namen ein:");
            sortTableListByName(name);
        });

        // Button to save table data
        JButton saveButton = new JButton("Tabellen speichern");
        saveButton.addActionListener(_ -> saveAllTables());

        // Panel for bottom buttons
        JPanel bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        bottomButtonPanel.add(addButton);
        bottomButtonPanel.add(addRowButton);
        bottomButtonPanel.add(renameButton);
        bottomButtonPanel.add(sortButton);
        bottomButtonPanel.add(saveButton);

        add(bottomButtonPanel, BorderLayout.SOUTH);
    }

    private void sortTableListByName(String name) {
        ArrayList<String> tableNames = Collections.list(tableListModel.elements());
        ArrayList<String> matchingNames = new ArrayList<>();


        for (String tableName : tableNames) {
            if (tableName.toLowerCase().contains(name.toLowerCase())) {
                matchingNames.add(tableName);
            }
        }


        if (matchingNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keine passende Tabelle gefunden.", "Hinweis", JOptionPane.INFORMATION_MESSAGE);
            return;
        }


        Collections.sort(matchingNames);


        tableListModel.clear();
        for (String tableName : matchingNames) {
            tableListModel.addElement(tableName);
        }
    }



    private void changeTableName() {
        int selectedIndex = tableList.getSelectedIndex();
        if (selectedIndex != -1) {
            String currentTableName = tableListModel.getElementAt(selectedIndex);
            String newTableName = JOptionPane.showInputDialog(this, "Neuer Tabellenname:", currentTableName);
            if (newTableName != null && !newTableName.isEmpty()) {
                tableListModel.setElementAt(newTableName, selectedIndex);
                // Aktualisieren Sie den Tabellennamen in der GUI, falls erforderlich

                // Aktualisieren des Dateinamens der XML-Datei
                String oldFileName = currentTableName + ".xml";
                String newFileName = newTableName + ".xml";
                File oldFile = new File(DIRECTORY_PATH + oldFileName);
                File newFile = new File(DIRECTORY_PATH + newFileName);
                if (!oldFile.exists() && !oldFile.isFile() && !oldFile.renameTo(newFile))
                    JOptionPane.showMessageDialog(this, "Fehler beim Umbenennen!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie eine Tabelle aus, um den Namen zu ändern.", "Warnung", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addNewTable() {
        tableCount++;
        String tableName = "Tabelle " + tableCount;
        tableListModel.addElement(tableName);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Name", "Kalorien", "Anzahl", "Preis"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // Alle Zellen sind editierbar
            }
        };

        // Füge eine leere Zeile hinzu
        model.addRow(new Object[]{"", "", "", ""});

        JTable table = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableDetailPanel.add(tableScrollPane, tableName);
    }

    private void showTableDetail(int index) {
        if (index != -1) {
            String tableName = tableListModel.getElementAt(index);
            CardLayout cl = (CardLayout) (tableDetailPanel.getLayout());
            cl.show(tableDetailPanel, tableName);
        }
    }

    private void saveAllTables() {
        for (int i = 0; i < tableListModel.getSize(); i++) {
            String tableName = tableListModel.getElementAt(i);
            JScrollPane scrollPane = (JScrollPane) tableDetailPanel.getComponent(i);
            JTable table = (JTable) scrollPane.getViewport().getView();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            try {
                dataStorage.save(model, tableName);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void loadAllTables() {
        File dataDirectory = new File("src/main/resources/data/");
        if (dataDirectory.exists() && dataDirectory.isDirectory()) {
            File[] files = dataDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".xml")) {
                        String tableName = file.getName().replace(".xml", "");
                        try {
                            DefaultTableModel model = new DefaultTableModel(new Object[]{"Name", "Kalorien","Anzahl","Preis"}, 0) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return true; // Alle Zellen sind editierbar
                                }
                            };
                            dataStorage.load(model, tableName);
                            JTable table = new JTable(model);
                            JScrollPane tableScrollPane = new JScrollPane(table);
                            tableDetailPanel.add(tableScrollPane, tableName);
                            tableListModel.addElement(tableName);
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                    }
                }
            }
        }

    }
    private void addNewRowToSelectedTable() {
        int selectedIndex = tableList.getSelectedIndex();
        if (selectedIndex != -1) {
            JScrollPane scrollPane = (JScrollPane) tableDetailPanel.getComponent(selectedIndex);
            JTable table = (JTable) scrollPane.getViewport().getView();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.addRow(new Object[]{"", ""}); // Füge eine leere Zeile hinzu
        } else {
            JOptionPane.showMessageDialog(this, "Bitte wählen Sie eine Tabelle aus, um eine Zeile hinzuzufügen.", "Warnung", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static final String DIRECTORY_PATH = "src/main/resources/data/";


}
