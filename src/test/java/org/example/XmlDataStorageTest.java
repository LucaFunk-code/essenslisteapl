package org.example;

import org.junit.jupiter.api.*;

import javax.swing.table.DefaultTableModel;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Methods for saving and loading table data test
 */
class XmlDataStorageTest {
    private static final String TABLE_NAME = "TestTable";
    private File file;
    private ResourceBundle resourceBundle;
    private XmlDataStorage xmlDataStorage;
    private DefaultTableModel defaultTableModel;

    @BeforeEach
    void setUp() {
        file = new File(XmlDataStorage.DIRECTORY_PATH + TABLE_NAME + ".xml");
        resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        xmlDataStorage = new XmlDataStorage();
        defaultTableModel = new DefaultTableModel(new Object[]{resourceBundle.getString("name"),
                resourceBundle.getString("calories"), resourceBundle.getString("amount"),
                resourceBundle.getString("price"), resourceBundle.getString("tags")}, 0);
        Vector<String> stringVector = new Vector<>();
        stringVector.add("Apfel");
        stringVector.add("10");
        stringVector.add("1");
        stringVector.add("3.0");
        stringVector.add("Fruit");
        defaultTableModel.addRow(stringVector);
    }

    @AfterEach
    void tearDown() {
        if (!file.delete()) {
            System.out.println("Test file couldn't be deleted.");
        }
    }

    @Test
    void save() {
        try {
            xmlDataStorage.save(defaultTableModel, TABLE_NAME);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void load() {
        // initialize testDefaultTableModel
        DefaultTableModel testDefaultTableModel = new DefaultTableModel(new Object[]{
                resourceBundle.getString("name"),
                resourceBundle.getString("calories"), resourceBundle.getString("amount"),
                resourceBundle.getString("price"), resourceBundle.getString("tags")}, 0);
        try {
            //write test file
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("<Vector><item>Apfel</item><item>10</item><item>1</item><item>3.0</item>" +
                    "<item>Fruit</item></Vector>");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            //test method
            xmlDataStorage.load(testDefaultTableModel, TABLE_NAME);
        } catch (IOException e) {
            fail();
        }

        Assertions.assertEquals(defaultTableModel.getDataVector(), testDefaultTableModel.getDataVector());
    }
}