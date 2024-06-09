package org.example;

import javax.swing.table.DefaultTableModel;
import java.io.IOException;

/**
 * Interface defines methods for saving and loading table data
 */
public interface DataStorage {

    void save(DefaultTableModel model, String tableName) throws IOException;

    void load(DefaultTableModel model, String tableName) throws IOException;
}