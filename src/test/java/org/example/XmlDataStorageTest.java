package org.example;

import static org.junit.Assert.*;

import org.example.XmlDataStorage;
import org.junit.*;
import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Vector;

public class XmlDataStorageTest {
    private XmlDataStorage xmlDataStorage;
    private static final String TEST_DIRECTORY_PATH = "src/test/resources/";

    @Before
    public void setUp() {
        xmlDataStorage = new XmlDataStorage();
    }

    @Test
    public void testSave() throws IOException {
        DefaultTableModel model = new DefaultTableModel( new Object[]{"Name", "Age"}, 0 );
        model.addRow( new Object[]{"Alice", 30} );
        model.addRow( new Object[]{"Bob", 25} );

        String tableName = "testTable";
        xmlDataStorage.save( model, tableName );

        // Verify that the file was created
        File file = new File( TEST_DIRECTORY_PATH + tableName + ".xml" );
        assertTrue( file.exists() );

        // Clean up
        file.delete();
    }


    @Test
    public void testLoad() throws IOException {
        XmlDataStorage xmlDataStorage = new XmlDataStorage();

        // Create a sample XML file
        String tableName = "testTable";
        File file = new File( TEST_DIRECTORY_PATH + tableName + ".xml" );
        file.createNewFile();
        String xmlContent = "<Vector><item>Alice</item><item>30</item><item>female</item><item>165</item><item>60</item></Vector><Vector><item>Bob</item><item>25</item><item>male</item><item>180</item><item>75</item></Vector>";
        Files.write( file.toPath(), xmlContent.getBytes() );

        // Load the data
        DefaultTableModel model = new DefaultTableModel();
        xmlDataStorage.load( model, tableName );

        // Verify that the data was loaded correctly
        assertEquals( 2, model.getRowCount() );
        assertEquals( "Alice", model.getValueAt( 0, 0 ) );
        assertEquals( "30", model.getValueAt( 0, 1 ) );
        assertEquals( "female", model.getValueAt( 0, 2 ) );
        assertEquals( "165", model.getValueAt( 0, 3 ) );
        assertEquals( "60", model.getValueAt( 0, 4 ) );

        assertEquals( "Bob", model.getValueAt( 1, 0 ) );
        assertEquals( "25", model.getValueAt( 1, 1 ) );
        assertEquals( "male", model.getValueAt( 1, 2 ) );
        assertEquals( "180", model.getValueAt( 1, 3 ) );
        assertEquals( "75", model.getValueAt( 1, 4 ) );

        // Clean up
        file.delete();
    }
}