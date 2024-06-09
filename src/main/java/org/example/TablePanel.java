package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.example.util.Options.DIRECTORY_PATH;

/**
 * TablePanel class is custom JPanel that manages list of tables
 * Functionality to  add, rename, delete, and save tables, switch languages
 */
public class TablePanel extends JPanel {
    //Variables for
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

    /**
     * Creates new TablePanel with list of table and buttons
     * Initializes GUI components and sets up even listeners
     */
    public TablePanel() {
        this.dataStorage = new XmlDataStorage();
        this.bundle = ResourceBundle.getBundle( "messages", Locale.getDefault() );

        //Inner class to change the color of a table name
        class CustomCellRenderer extends DefaultListCellRenderer {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );


                if (index == 0) {
                    component.setBackground( Color.YELLOW );
                }

                return component;
            }
        }

        //Configuring the layout
        setLayout( new BorderLayout() );
        // List Panel
        tableListModel = new DefaultListModel<>();
        tableList = new JList<>( tableListModel );
        tableList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        tableList.addListSelectionListener( _ -> showTableDetail( tableList.getSelectedIndex() ) );
        JScrollPane listScrollPane = new JScrollPane( tableList );
        listScrollPane.setPreferredSize( new Dimension( 200, 0 ) );
        add( listScrollPane, BorderLayout.WEST );

        CustomCellRenderer customCellRenderer = new CustomCellRenderer();
        tableList.setCellRenderer( customCellRenderer );

        tableDetailPanel = new JPanel( new CardLayout() );
        add( tableDetailPanel, BorderLayout.CENTER );

        // Buttons -> EventListener
        addButton = new JButton();
        addButton.addActionListener( _ -> addNewTable() );

        addRowButton = new JButton();
        addRowButton.addActionListener( _ -> addNewRowToSelectedTable() );

        renameButton = new JButton();
        renameButton.addActionListener( _ -> changeTableName() );

        sortButton = new JButton();
        sortButton.addActionListener( _ -> {
            String name = JOptionPane.showInputDialog( bundle.getString( "sort_table_prompt" ) ); //prompt to type in name
            sortTableListByName( name );
        } );

        deleteButton = new JButton();
        deleteButton.addActionListener( _ -> deleteTable() );

        saveButton = new JButton();
        saveButton.addActionListener( _ -> saveAllTables() );

        // Panel for bottom buttons
        JPanel bottomButtonPanel = new JPanel();
        bottomButtonPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) );
        bottomButtonPanel.add( addButton );
        bottomButtonPanel.add( addRowButton );
        bottomButtonPanel.add( renameButton );
        bottomButtonPanel.add( sortButton );
        bottomButtonPanel.add( deleteButton );
        bottomButtonPanel.add( saveButton );
        add( bottomButtonPanel, BorderLayout.SOUTH );

        // Language switch button
        languageButton = new JButton( "\uD83C\uDDE9\uD83C\uDDEA \uD83C\uDDEC\uD83C\uDDE7" );
        languageButton.addActionListener( _ -> changeLanguage() );
        bottomButtonPanel.add( languageButton );


        updateButtonLabels();
    }

    /**
     * Updates the names of the buttons according to language
     */
    private void updateButtonLabels() {
        addButton.setText( bundle.getString( "add_table" ) );
        addRowButton.setText( bundle.getString( "add_row" ) );
        renameButton.setText( bundle.getString( "rename_table" ) );
        sortButton.setText( bundle.getString( "sort_table" ) );
        deleteButton.setText( bundle.getString( "delete_table" ) );
        saveButton.setText( bundle.getString( "save_tables" ) );
    }

    /**
     * Changes language
     * Prompt for the user to select language and update UI
     */
    private void changeLanguage() {
        String[] options = {"English", "Deutsch"};
        int choice = JOptionPane.showOptionDialog( this, bundle.getString( "choose_language" ), bundle.getString( "language" ), JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0] );

        if (choice == 0) {
            bundle = ResourceBundle.getBundle( "messages", new Locale( "en" ) ); //English Package

        } else if (choice == 1) {
            bundle = ResourceBundle.getBundle( "messages", new Locale( "de" ) ); //German Package
        }
        updateButtonLabels();
    }

    /**
     * Sorts the list of tables by name
     * Prompt for the user to write a name to filter the tables
     *
     * @param name the name the user gives to filter tables
     */
    private void sortTableListByName(String name) {
        if (name != null && !name.isEmpty()) {
            ArrayList<String> tableNames = Collections.list( tableListModel.elements() ); //ArrayList for tables
            ArrayList<String> matchingNames = new ArrayList<>();
            for (String tableName : tableNames) {
                if (tableName.toLowerCase().contains( name.toLowerCase() )) {
                    matchingNames.add( tableName );
                }
            }
            if (matchingNames.isEmpty()) {
                JOptionPane.showMessageDialog( this, bundle.getString( "no_table_found" ), bundle.getString( "hint" ), JOptionPane.INFORMATION_MESSAGE );
                return;
            }
            Collections.sort( matchingNames );
            tableListModel.clear();
            for (String tableName : matchingNames) {
                tableListModel.addElement( tableName );
            }
        } else { //Warning
            JOptionPane.showMessageDialog( this, bundle.getString( "sort_table_prompt" ), bundle.getString( "warning" ), JOptionPane.WARNING_MESSAGE );
        }
    }

    /**
     * Changes name of selected table
     * Prompt to rename the table
     */
    private void changeTableName() {
        int selectedIndex = tableList.getSelectedIndex();
        if (selectedIndex != -1) {
            String currentTableName = tableListModel.getElementAt( selectedIndex );
            String newTableName = JOptionPane.showInputDialog( this, bundle.getString( "new_table_name_prompt" ), currentTableName );
            if (newTableName != null && !newTableName.isEmpty()) {
                tableListModel.setElementAt( newTableName, selectedIndex );

                // Update the file name of the XML file
                String oldFileName = currentTableName + ".xml";
                String newFileName = newTableName + ".xml";
                File oldFile = new File( DIRECTORY_PATH + oldFileName );
                File newFile = new File( DIRECTORY_PATH + newFileName );
                if (oldFile.exists() && oldFile.isFile()) {
                    try {
                        Files.move( oldFile.toPath(), newFile.toPath() );
                    } catch (IOException e) {
                        System.out.println( e.getMessage() );
                        JOptionPane.showMessageDialog( this, bundle.getString( "file_rename_error" ) );
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog( this, bundle.getString( "select_table_to_rename" ), bundle.getString( "warning" ), JOptionPane.WARNING_MESSAGE );
        }
    }

    /**
     * Adds new table to list and display in detail panel
     * Table has predefined columns and JComboBox editor for tags
     */
    private void addNewTable() {
        tableCount++;
        String tableName = bundle.getString( "table" ) + " " + tableCount;
        tableListModel.addElement( tableName );

        // Modify the column names to include the "tags" column
        DefaultTableModel model = new DefaultTableModel( new Object[]{bundle.getString( "name" ), bundle.getString( "calories" ), bundle.getString( "amount" ), bundle.getString( "price" ), bundle.getString( "tags" )}, 0 ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true; // All cells are editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Specify the data type for each column
                switch (columnIndex) {
                    case 0:
                        return String.class;  // Name column accepts text
                    case 1:
                        return Integer.class; // Calories column accepts integers
                    case 2:
                        return Integer.class; // Amount column accepts integers
                    case 3:
                        return Double.class;  // Price column accepts doubles
                    case 4:
                        return String.class;  // Tags column accepts strings
                    default:
                        return Object.class;
                }
            }
        };
        model.addRow( new Object[]{"", 0, 0, 0.0, ""} );
        JTable table = new JTable( model );
        String[] tags = {bundle.getString( "tag_vegetable" ), bundle.getString( "tag_fruit" ), bundle.getString( "tag_meat" )};
        // Add JComboBox as editor for tags column
        JComboBox<String> tagsComboBox = new JComboBox<>( tags ); // Predefined tags
        TableColumn tagsColumn = table.getColumnModel().getColumn( 4 ); // tagsComboBox in 5th column
        tagsColumn.setCellEditor( new DefaultCellEditor( tagsComboBox ) );

        JScrollPane tableScrollPane = new JScrollPane( table );
        tableDetailPanel.add( tableScrollPane, tableName );

        // Refresh the table view
        table.revalidate();
        table.repaint();
    }

    /**
     * Display detail of selected table
     *
     * @param index of the selected table
     */
    private void showTableDetail(int index) {
        if (index != -1) {
            String tableName = tableListModel.getElementAt( index );
            CardLayout cl = (CardLayout) (tableDetailPanel.getLayout());
            cl.show( tableDetailPanel, tableName );
        }
    }

    /**
     * Saves all tables to XML files
     * For Loop to go through the tables and save each table model
     */
    public void saveAllTables() {
        int tableCount = tableListModel.getSize();

        for (int i = 0; i < tableCount; i++) {
            String tableName = tableListModel.getElementAt( i );
            JScrollPane scrollPane = (JScrollPane) tableDetailPanel.getComponent( i );
            JTable table = (JTable) scrollPane.getViewport().getView();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            try {
                dataStorage.save( model, tableName ); // uses the method in XmlDataStorage to save the tables
            } catch (IOException e) {
                System.err.println( e.getMessage() );
            }
        }
    }

    /**
     * Loads all tables from XML files in the specified directory
     * Creates tables and adds them to the detail panel
     */
    public void loadAllTables() {
        File dataDirectory = new File( DIRECTORY_PATH );
        if (dataDirectory.exists() && dataDirectory.isDirectory()) {
            File[] files = dataDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith( ".xml" )) {
                        String tableName = file.getName().replace( ".xml", "" );
                        try {
                            DefaultTableModel model = new DefaultTableModel( new Object[]{bundle.getString( "name" ), bundle.getString( "calories" ), bundle.getString( "amount" ), bundle.getString( "price" ), bundle.getString( "tags" )}, 0 ) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return true; // All cells are editable
                                }
                            };
                            dataStorage.load( model, tableName ); //uses the method in XmlDataStorage to load the tables
                            JTable table = new JTable( model );
                            JScrollPane tableScrollPane = new JScrollPane( table );
                            tableDetailPanel.add( tableScrollPane, tableName );
                            tableListModel.addElement( tableName );
                        } catch (IOException e) {
                            System.err.println( e.getMessage() );
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds a new row to the selected table
     * The new row includes editable cells and a JComboBox editor for tags
     */
    private void addNewRowToSelectedTable() {
        int selectedIndex = tableList.getSelectedIndex();
        if (selectedIndex != -1) {
            JScrollPane scrollPane = (JScrollPane) tableDetailPanel.getComponent( selectedIndex );
            JTable table = (JTable) scrollPane.getViewport().getView();
            DefaultTableModel model = (DefaultTableModel) table.getModel();


            String[] tags = {bundle.getString( "tag_vegetable" ), bundle.getString( "tag_fruit" ), bundle.getString( "tag_meat" )};
            JComboBox<String> tagsComboBox = new JComboBox<>( tags );
            TableColumn tagsColumn = table.getColumnModel().getColumn( 4 ); //tagsComboBox in 5th column
            tagsColumn.setCellEditor( new DefaultCellEditor( tagsComboBox ) ); //adjust DefaultCellEditor to tagComboBox


            model.addRow( new Object[]{"", 0, 0, 0.0, ""} ); //add row

            int lastRowIndex = model.getRowCount() - 1;
            model.setValueAt( "", lastRowIndex, 4 );

            // Refresh the table view
            table.revalidate();
            table.repaint();
        } else {
            JOptionPane.showMessageDialog( this, bundle.getString( "select_table_to_add_row" ), bundle.getString( "warning" ), JOptionPane.WARNING_MESSAGE ); //prompt to select table
        }
    }

    /**
     * Deletes the selected table from the list and detail panel
     * Deletes the corresponding XML file from the directory
     */
    private void deleteTable() {
        int selectedIndex = tableList.getSelectedIndex();
        if (selectedIndex != -1) {
            String tableName = tableListModel.getElementAt( selectedIndex );
            tableListModel.remove( selectedIndex ); //delete table in the model
            tableDetailPanel.remove( selectedIndex ); //delete table in the detail Table
            File file = new File( DIRECTORY_PATH + tableName + ".xml" ); //delete the table in the XML Path
            if (file.exists() && file.isFile()) {
                if (!file.delete()) {
                    JOptionPane.showMessageDialog( this, bundle.getString( "file_delete_error" ), bundle.getString( "warning" ), JOptionPane.WARNING_MESSAGE );
                }
            }
            tableDetailPanel.revalidate(); //refresh
            tableDetailPanel.repaint();
        } else {
            JOptionPane.showMessageDialog( this, bundle.getString( "select_table_to_delete" ), bundle.getString( "warning" ), JOptionPane.WARNING_MESSAGE );
        }
    }

    /**
     * Returns a string representation of the TablePanel
     *
     * @return A string describing the TablePanel
     */
    @Override
    public String toString() {
        return "TablePanel{" +
                "tableListModel=" + tableListModel +
                ", tableList=" + tableList +
                ", tableDetailPanel=" + tableDetailPanel +
                ", tableCount=" + tableCount +
                ", dataStorage=" + dataStorage +
                ", bundle=" + bundle +
                ", addButton=" + addButton +
                ", addRowButton=" + addRowButton +
                ", renameButton=" + renameButton +
                ", sortButton=" + sortButton +
                ", deleteButton=" + deleteButton +
                ", saveButton=" + saveButton +
                ", languageButton=" + languageButton +
                '}';
    }
}
