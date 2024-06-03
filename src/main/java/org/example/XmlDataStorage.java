package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.*;

public class XmlDataStorage implements DataStorage {
    private static final String DIRECTORY_PATH = "src/main/resources/data/";

    @Override
    public void save(DefaultTableModel model, String tableName) throws IOException {
        XmlMapper mapper = new XmlMapper();
        File file = new File(DIRECTORY_PATH + tableName + ".xml");
        Vector<Vector> vectorRow = model.getDataVector();
        for (Vector<String> vector : vectorRow) {
            boolean isEmpty = true;
            for (String string : vector) {
                if(string == null || string.isEmpty()) continue;
                isEmpty = false;
                break;
            }
            if(isEmpty) vectorRow.remove(vector);
        }
        mapper.writeValue(file, vectorRow);
    }

    @Override
    public void load(DefaultTableModel model, String tableName) throws IOException {
        XmlMapper mapper = new XmlMapper();
        File file = new File(DIRECTORY_PATH + tableName + ".xml");
        if (file.exists()) {
            StringBuilder xmlContent = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                xmlContent.append(line);
            }
            String xmlString = xmlContent.toString().replaceAll(">\\s*<", "><"); // Entfernen von Leerzeichen zwischen Tags
            Vector<String> data;
            data = mapper.readValue(xmlString, new TypeReference<>() {
            });

            // Überprüfen, ob die Anzahl der Daten korrekt ist (muss durch 4 teilbar sein)
            if (data.size() % 4 == 0) {
                int numRows = data.size() / 4; // Berechne die Anzahl der Zeilen basierend auf der Anzahl der Elemente und der Anzahl der Spalten (hier 4)
                for (int i = 0; i < numRows; i++) {
                    String[] rowData = new String[4];
                    rowData[0] = data.get(i * 4); // Erstes Element in die erste Spalte einfügen
                    rowData[1] = data.get(i * 4 + 1); // Zweites Element in die zweite Spalte einfügen
                    rowData[2] = data.get(i * 4 + 2); // Drittes Element in die dritte Spalte einfügen
                    rowData[3] = data.get(i * 4 + 3); // Viertes Element in die vierte Spalte einfügen
                    model.addRow(rowData);
                }
            }
        }
    }
}
