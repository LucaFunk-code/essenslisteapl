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
    private final LanguageManager languageManager;
    private final ButtonActionManager buttonActionManager;

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
        this.languageManager = new LanguageManager();
        this.buttonActionManager = new ButtonActionManager(languageManager, this);

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

        // Initialize Buttons
        addButton = new JButton();
        addRowButton = new JButton();
        renameButton = new JButton();
        sortButton = new JButton();
        deleteButton = new JButton();
        saveButton = new JButton();
        languageButton = new JButton("Change Language");

        // Set button actions
        buttonActionManager.initializeButtons(addButton, addRowButton, renameButton, sortButton, deleteButton, saveButton, languageButton);

        // Panel for bottom buttons
        JPanel bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        bottomButtonPanel.add(addButton);
        bottomButtonPanel.add(addRowButton);
        bottomButtonPanel.add(renameButton);
        bottomButtonPanel.add(sortButton);
        bottomButtonPanel.add(deleteButton);
        bottomButtonPanel.add(saveButton);
        bottomButtonPanel.add(languageButton);

        add(bottomButtonPanel, BorderLayout.SOUTH);

        updateButtonLabels();
    }

    public void updateButtonLabels() {
        ResourceBundle bundle = languageManager.getBundle();
        addButton.setText(bundle.getString("add_table"));
        addRowButton.setText(bundle.getString("add_row"));
        renameButton.setText(bundle.getString("rename_table"));
        sortButton.setText(bundle.getString("sort_table"));
        deleteButton.setText(bundle.getString("delete_table"));
        saveButton.setText(bundle.getString("save_tables"));
        languageButton.setText("Change Language");
    }

    public void changeLanguage() {
        String[] options = {"English", "Deutsch"};
        int choice = JOptionPane.showOptionDialog(this, "Choose Language", "Language", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            languageManager.changeLanguage(new Locale("en"));
        } else if (choice == 1) {
            languageManager.changeLanguage(new Locale("de"));
        }

        updateButtonLabels();
    }

    public void addNewTable() {
        tableCount++;
        String tableName = languageManager.getBundle().getString("name") + " " + tableCount;
        tableListModel.addElement(tableName);

        DefaultTableModel model = new DefaultTableModel(new Object[]{
                languageManager.getBundle().getString("name"),
                languageManager.getBundle().getString("calories"),
                languageManager.getBundle().getString("amount"),
                languageManager.getBundle().getString("price")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(model);
        tableDetailPanel.add(new JScrollPane(table), tableName);
        ((CardLayout) tableDetailPanel.getLayout()).show(tableDetailPanel, tableName);
    }

    public void addNewRowToSelectedTable() {
        int selectedIndex = tableList.getSelectedIndex();
        if (selectedIndex != -1) {
            String tableName = tableListModel.getElementAt(selectedIndex);
            Component[] components = tableDetailPanel.getComponents();
            for (Component component : components) {
                if (component instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) component;
                    JViewport viewport = scrollPane.getViewport();
                    Component view = viewport.getView();
                    if (view instanceof JTable) {
                        JTable table = (JTable) view;
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        model.addRow(new Object[]{"", "", "", ""});
                        break;
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, languageManager.getBundle().getString("select_table_to_add_row"), languageManager.getBundle().getString("warning"), JOptionPane.WARNING_MESSAGE);
        }
    }

    public void changeTableName() {
        int selectedIndex = tableList.getSelectedIndex();
        if (selectedIndex != -1) {
            String currentTableName = tableListModel.getElementAt(selectedIndex);
            String newTableName = JOptionPane.showInputDialog(this, languageManager.getBundle().getString("new_table_name_prompt"), currentTableName);
            if (newTableName != null && !newTableName.isEmpty()) {
                tableListModel.setElementAt(newTableName, selectedIndex);

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
                        JOptionPane.showMessageDialog(this, languageManager.getBundle().getString("file_rename_error"));
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, languageManager.getBundle().getString("select_table_to_rename"), languageManager.getBundle().getString("warning"), JOptionPane.WARNING_MESSAGE);
        }
    }

    public void sortTableListByName(String name) {
        ArrayList<String> tableNames = Collections.list(tableListModel.elements());
        ArrayList<String> matchingNames = new ArrayList<>();
        for (String tableName : tableNames) {
            if (tableName.toLowerCase().contains(name.toLowerCase())) {
                matchingNames.add(tableName);
            }
        }
        if (matchingNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, languageManager.getBundle().getString("no_table_found"), languageManager.getBundle().getString("hint"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Collections.sort(matchingNames);
        tableListModel.clear();
        for (String tableName : matchingNames) {
            tableListModel.addElement(tableName);
        }
    }

    public void deleteTable() {
        int selectedIndex = tableList.getSelectedIndex();
        if (selectedIndex != -1) {
            String tableName = tableListModel.getElementAt(selectedIndex);
            tableListModel.remove(selectedIndex);
            tableDetailPanel.remove(selectedIndex);
            revalidate();
            repaint();

            // Delete the corresponding XML file
            File file = new File(DIRECTORY_PATH + tableName + ".xml");
            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    JOptionPane.showMessageDialog(this, languageManager.getBundle().getString("file_delete_error"));
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, languageManager.getBundle().getString("select_table_to_delete"), languageManager.getBundle().getString("warning"), JOptionPane.WARNING_MESSAGE);
        }
    }

    public void saveAllTables() {
        // Implement the logic to save all tables
        JOptionPane.showMessageDialog(this, "Tables saved!", languageManager.getBundle().getString("hint"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void showTableDetail(int index) {
        if (index != -1) {
            String tableName = tableListModel.getElementAt(index);
            ((CardLayout) tableDetailPanel.getLayout()).show(tableDetailPanel, tableName);
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
                            DefaultTableModel model = new DefaultTableModel(new Object[]{
                                    languageManager.getBundle().getString("name"),
                                    languageManager.getBundle().getString("calories"),
                                    languageManager.getBundle().getString("amount"),
                                    languageManager.getBundle().getString("price")
                            }, 0) {
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

}
