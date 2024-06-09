package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.*;

import javax.swing.table.DefaultTableModel;

import java.io.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class XmlDataStorageTest {
    private static final String TABLE_NAME = "TestTable";
    ResourceBundle resourceBundle;
    XmlDataStorage xmlDataStorage;
    DefaultTableModel defaultTableModel;

    @BeforeEach
    void setUp() {
        resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());
        xmlDataStorage = new XmlDataStorage();
        defaultTableModel = new DefaultTableModel(new Object[]{resourceBundle.getString("name"),
                resourceBundle.getString("calories"), resourceBundle.getString("amount"),
                resourceBundle.getString("price"), resourceBundle.getString("tags")}, 0);
    }

    @BeforeAll
    static void beforeAll() {

    }

    @AfterAll
    static void afterAll() {

    }

    @Test
    void save() {
        File file = new File(XmlDataStorage.DIRECTORY_PATH + TABLE_NAME + ".xml");
        XmlMapper xmlMapper = new XmlMapper();
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            fail();
            return;
        }
        String line;
            while (true) {
                try {
                    if ((line = reader.readLine()) == null) break;
                } catch (IOException e) {
                    fail();
                    return;
                }
                stringBuilder.append(line);
            }
        String string = stringBuilder.toString().replaceAll(">\\s*<", "><");
        Vector<String> stringVector;
        try {
            stringVector = xmlMapper.readValue(string, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
            fail();
            return;
        }
        stringVector.add("Apfel");
        stringVector.add("10");
        stringVector.add("1");
        stringVector.add("3.0");
        stringVector.add("Fruit");
        defaultTableModel.addRow(stringVector);
        try {
            xmlDataStorage.save(defaultTableModel, TABLE_NAME);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    void load() {
    }
}