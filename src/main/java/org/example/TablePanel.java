package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

public class TablePanel extends JPanel {
    private static final String DIRECTORY_PATH = "src/main/resources/data/";
    private final DefaultListModel<String> tableListModel;
    private final JList<String> tableList;
    private final JPanel tableDetailPanel;
    private int tableCount = 0;
    private final DataStorage dataStorage;
    private ResourceBundle bundle;

    // Buttons to be updated when language changes
    private JButton addButton;
    private JButton addRowButton;
    private JButton renameButton;
    private JButton sortButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton languageButton;

    public TablePanel() {
        this.dataStorage = new XmlDataStorage();
        this.bundle = ResourceBundle.getBundle("messages", Locale.getDefault());

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

        // Buttons
        addButton = new JButton();
        addButton.addActionListener(_ -> addNewTable());

        addRowButton = new JButton();
        addRowButton.addActionListener(_ -> addNewRowToSelectedTable());

        renameButton = new JButton();
        renameButton.addActionListener(_ -> changeTableName());

        sortButton = new JButton();
        sortButton.addActionListener(_ -> {
            String name = JOptionPane.showInputDialog(bundle.getString("sort_table_prompt"));
            sortTableListByName(name);
        });

        deleteButton = new JButton();
        deleteButton.addActionListener(_ -> deleteTable());

        saveButton = new JButton();
        saveButton.addActionListener(_ -> saveAllTables());

        // Panel for bottom buttons
        JPanel bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        bottomButtonPanel.add(addButton);
        bottomButtonPanel.add(addRowButton);
        bottomButtonPanel.add(renameButton);
        bottomButtonPanel.add(sortButton);
        bottomButtonPanel.add(deleteButton);
        bottomButtonPanel.add(saveButton);

        add(bottomButtonPanel, BorderLayout.SOUTH);

        // Language switch button
        languageButton = new JButton("\uD83C\uDDE9\uD83C\uDDEA \uD83C\uDDEC\uD83C\uDDE7");
        languageButton.addActionListener(_ -> changeLanguage());
        bottomButtonPanel.add(languageButton);

        updateButtonLabels();
    }

    private void updateButtonLabels() {
        addButton.setText(bundle.getString("add_table"));
        addRowButton.setText(bundle.getString("add_row"));
        renameButton.setText(bundle.getString("rename_table"));
        sortButton.setText(bundle.getString("sort_table"));
        deleteButton.setText(bundle.getString("delete_table"));
        saveButton.setText(bundle.getString("save_tables"));

    }

    private void changeLanguage() {
        String[] options = {"English", "Deutsch"};
        int choice = JOptionPane.showOptionDialog(this, bundle.getString("choose_language"), bundle.getString("language"), JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            bundle = ResourceBundle.getBundle("messages", new Locale("en"));

        } else if (choice == 1) {
            bundle = ResourceBundle.getBundle("messages", new Locale("de"));

        }

        updateButtonLabels();
    }

    private void sortTableListByName(String name) {
        if (name != null && !name.isEmpty()) {
            ArrayList<String> tableNames = Collections.list(tableListModel.elements());
            ArrayList<String> matchingNames = new ArrayList<>();
            for (String tableName : tableNames) {
                if (tableName.toLowerCase().contains(name.toLowerCase())) {
                    matchingNames.add(tableName);
                }
            }
            if (matchingNames.isEmpty()) {
                JOptionPane.showMessageDialog(this, bundle.getString("no_table_found"), bundle.getString("hint"), JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            Collections.sort(matchingNames);
            tableListModel.clear();
            for (String tableName : matchingNames) {
                tableListModel.addElement(tableName);
            }
        } else {
            JOptionPane.showMessageDialog(this, bundle.getString("sort_table_prompt"), bundle.getString("warning"), JOptionPane.WARNING_MESSAGE);
        }
    }


    private void changeTableName() {
        int selectedIndex = tableList.getSelectedIndex();
        if (selectedIndex != -1) {
            String currentTableName = tableListModel.getElementAt(selectedIndex);
            String newTableName = JOptionPane.showInputDialog(this, bundle.getString("new_table_name_prompt"), currentTableName);
            if (newTableName != null && !newTableName.isEmpty()) {
                tableListModel.setElementAt(newTableName, selectedIndex);
                // Update the table name in the UI if necessary

                // Update the file name of the XML file
                String oldFileName = currentTableName + ".xml";
                String newFileName = newTableName + ".xml";
                File oldFile = new File(DIRECTORY_PATH + oldFileName);
                File newFile = new File(DIRECTORY_PATH + newFileName);
                if (oldFile.exists() && oldFile.isFile()) {
                    try {
                        Files.move(oldFile.toPath(), newFile.toPath());
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                        JOptionPane.showMessageDialog(this, bundle.getString("file_rename_error"));
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, bundle.getString("select_table_to_rename"), bundle.getString("warning"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void addNewTable() {
        tableCount++;
        String tableName = bundle.getString("table") + " " + tableCount;
        tableListModel.addElement(tableName);

        DefaultTableModel model = new DefaultTableModel(new Object[]{bundle.getString("name"), bundle.getString("calories"), bundle.getString("amount"), bundle.getString("price")}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // All cells are editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Specify the data type for each column
                switch (columnIndex) {
                    case 0: return String.class;  // Name column accepts text
                    case 1: return Integer.class; // Calories column accepts integers
                    case 2: return Integer.class; // Amount column accepts integers
                    case 3: return Double.class;  // Price column accepts doubles
                    default: return Object.class;
                }
            }
        };

        // Add an empty row with initial values
        model.addRow(new Object[]{"", 0, 0, 0.0});

        JTable table = new JTable(model);
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableDetailPanel.add(tableScrollPane, tableName);

        // Refresh the table view
        table.revalidate();
        table.repaint();
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
        File dataDirectory = new File(DIRECTORY_PATH);
        if (dataDirectory.exists() && dataDirectory.isDirectory()) {
            File[] files = dataDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".xml")) {
                        String tableName = file.getName().replace(".xml", "");
                        try {
                            DefaultTableModel model = new DefaultTableModel(new Object[]{bundle.getString("name"), bundle.getString("calories"), bundle.getString("amount"), bundle.getString("price")}, 0) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return true; // All cells are editable
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

            // Add an empty row with initial values of appropriate data types
            model.addRow(new Object[]{"", 0, 0, 0.0});

            // Refresh the table view
            table.revalidate();
            table.repaint();
        } else {
            JOptionPane.showMessageDialog(this, bundle.getString("select_table_to_add_row"), bundle.getString("warning"), JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteTable() {
        int selectedIndex = tableList.getSelectedIndex();
        if (selectedIndex != -1) {
            String tableName = tableListModel.getElementAt(selectedIndex);
            tableListModel.remove(selectedIndex);
            tableDetailPanel.remove(selectedIndex);
            File file = new File(DIRECTORY_PATH + tableName + ".xml");
            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    JOptionPane.showMessageDialog(this, bundle.getString("file_delete_error"), bundle.getString("warning"), JOptionPane.WARNING_MESSAGE);
                }
            }
            tableDetailPanel.revalidate();
            tableDetailPanel.repaint();
        } else {
            JOptionPane.showMessageDialog(this, bundle.getString("select_table_to_delete"), bundle.getString("warning"), JOptionPane.WARNING_MESSAGE);
        }
    }
}
