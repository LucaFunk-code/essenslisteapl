package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.*;

public class XmlDataStorage implements DataStorage {
    public static final String DIRECTORY_PATH = "src/main/resources/data/";

    /**
     * Saves the data from the table model to an XML file
     *
     * @param model     the DefaultTableModel containing the data to save
     * @param tableName the name of the table (used as the filename)
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void save(DefaultTableModel model, String tableName) throws IOException {
        XmlMapper mapper = new XmlMapper();
        File file = new File( DIRECTORY_PATH + tableName + ".xml" ); //Path where to save XML
        Vector<Vector> vectorRow = model.getDataVector();
        Iterator<Vector> iterator = vectorRow.iterator();
        while (iterator.hasNext()) {
            Vector<?> vector = iterator.next();
            boolean isEmpty = true;
            for (Object obj : vector) {
                if (obj != null && !obj.toString().isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                iterator.remove(); //removes empty rows
            }
        }
        mapper.writeValue( file, vectorRow ); //save it
    }

    /**
     * Loads data from an XML file into the provided table model
     *
     * @param model     the DefaultTableModel to load the data into
     * @param tableName the name of the table (used as the filename)
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void load(DefaultTableModel model, String tableName) throws IOException {
        XmlMapper mapper = new XmlMapper();
        File file = new File( DIRECTORY_PATH + tableName + ".xml" );
        if (file.exists()) {
            StringBuilder xmlContent = new StringBuilder();
            BufferedReader reader = new BufferedReader( new FileReader( file ) );
            String line;
            while ((line = reader.readLine()) != null) {
                xmlContent.append( line );
            }
            String xmlString = xmlContent.toString().replaceAll( ">\\s*<", "><" ); // delete spaces between tags
            Vector<String> data;
            data = mapper.readValue( xmlString, new TypeReference<>() {
            } );

            // data is eligible if it can be divided by 5
            if (data.size() % 5 == 0) {
                int numRows = data.size() / 5; // the rows are the data size / 5
                for (int i = 0; i < numRows; i++) {
                    String[] rowData = new String[5];
                    rowData[0] = data.get( i * 5 ); // add first element in the first column
                    rowData[1] = data.get( i * 5 + 1 ); // add second element in the second column
                    rowData[2] = data.get( i * 5 + 2 ); // add third element in the third column
                    rowData[3] = data.get( i * 5 + 3 ); // add fourth element in the fourth column
                    rowData[4] = data.get( i * 5 + 4 ); // add fifth element in the fifth column
                    model.addRow( rowData );
                }
            }
            reader.close();
        }
    }

    @Override
    public String toString() {
        return "XmlDataStorage{}";
    }
}
