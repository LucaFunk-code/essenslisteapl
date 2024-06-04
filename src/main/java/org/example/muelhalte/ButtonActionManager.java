package org.example.muelhalte;

/*public class ButtonActionManager {
    private final LanguageManager languageManager;
    private final TablePanel tablePanel;

    public ButtonActionManager(LanguageManager languageManager, TablePanel tablePanel) {
        this.languageManager = languageManager;
        this.tablePanel = tablePanel;
    }

    public void initializeButtons(JButton addButton, JButton addRowButton, JButton renameButton, JButton sortButton, JButton deleteButton, JButton saveButton, JButton languageButton) {
        addButton.addActionListener(e -> tablePanel.addNewTable());
        addRowButton.addActionListener(e -> tablePanel.addNewRowToSelectedTable());
        renameButton.addActionListener(e -> tablePanel.changeTableName());
        sortButton.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(languageManager.getBundle().getString("sort_table_prompt"));
            tablePanel.sortTableListByName(name);
        });
        deleteButton.addActionListener(e -> tablePanel.deleteTable());
        saveButton.addActionListener(e -> tablePanel.saveAllTables());
        languageButton.addActionListener(e -> tablePanel.changeLanguage());
    }
}
*/