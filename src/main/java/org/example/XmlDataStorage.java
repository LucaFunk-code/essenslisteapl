package org.example;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.util.*;

public class XmlDataStorage implements DataStorage {
    public static final String DIRECTORY_PATH = "src/main/resources/data/";

    @Override
    public void save(DefaultTableModel model, String tableName) throws IOException {
        XmlMapper mapper = new XmlMapper();
        File file = new File(DIRECTORY_PATH + tableName + ".xml");
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
                iterator.remove();
            }
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

            // Überprüfen, ob die Anzahl der Daten korrekt ist (muss durch 5 teilbar sein)
            if (data.size() % 5 == 0) {
                int numRows = data.size() / 5; // Berechne die Anzahl der Zeilen basierend auf der Anzahl der Elemente und der Anzahl der Spalten (hier 4)
                for (int i = 0; i < numRows; i++) {
                    String[] rowData = new String[5];
                    rowData[0] = data.get(i * 5); // Erstes Element in die erste Spalte einfügen
                    rowData[1] = data.get(i * 5 + 1); // Zweites Element in die zweite Spalte einfügen
                    rowData[2] = data.get(i * 5 + 2); // Drittes Element in die dritte Spalte einfügen
                    rowData[3] = data.get(i * 5 + 3); // Viertes Element in die vierte Spalte einfügen
                    rowData[4] = data.get(i * 5 + 4); // Fünftes Element in die vierte Spalte einfügen
                    model.addRow(rowData);
                }
            }
            reader.close();
        }
    }
}
