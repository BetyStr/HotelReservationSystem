package cz.muni.fi.group05.room03.ui.table.util;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Table extends AbstractTableModel {

    private final List<Column<?>> columns;
    private final List<List<Object>> data;

    public Table(Column<?>... columns) {
        this.columns = asArrayList(columns);
        if (this.columns.size() != this.columns.stream().map(Column::getName).collect(Collectors.toSet()).size())
            throw new IllegalArgumentException("Table Error: Columns must have unique names!");
        this.data = new ArrayList<>();
    }

    public int getColumnCount() {
        return columns.size();
    }

    public int getRowCount() {
        return data.size();
    }

    public String getColumnName(int col) {
        return columns.get(col).getName();
    }

    public Object getValueAt(int row, int col) {
        return data.get(row).get(col);
    }

    public <T> T getRowValue(int row, Column<T> column) {
        return column.valueOf(data.get(row).get(assertIndexedColumn(column)));
    }

    public Class<?> getColumnClass(int col) {
        return columns.get(col).getType();
    }

    public boolean isCellEditable(int row, int col) {
       return false;
    }

    public int findColumn(Column<?> column) {
        return super.findColumn(column.getName());
    }

    public void setValueAt(Object value, int row, Column<?> column) {
        if (!value.getClass().equals(column.getType()))
            throw new IllegalArgumentException("Set value received object of different type than column is");
        int col = assertIndexedColumn(column);
        data.get(row).set(col, value);
        fireTableCellUpdated(row, col);
    }

    public void addRow(Object... elements) {
        List<Object> row = asArrayList(elements);
        assertRowMatchesColumns(row);
        int newRowIndex = data.size();
        data.add(row);
        fireTableRowsInserted(newRowIndex, newRowIndex);
    }

    public void removeRow(int rowIndex) {
        if (data.size() <= rowIndex)
            throw new IllegalArgumentException(String.format("Row %d does not exist", rowIndex));
        data.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public List<String> getColumnNames() {
        return columns.stream().map(Column::getName).collect(Collectors.toList());
    }

    @SafeVarargs
    private static <T> List<T> asArrayList(T ... items) {
        List<T> list = new ArrayList<>();
        Collections.addAll(list, items);
        return list;
    }

    private void assertColumnMatchesColumn(int column, Object object) {
        Class<?> newType = object.getClass();
        Class<?> definedType = columns.get(column).getType();
        if (!newType.equals(definedType))
            throw new IllegalArgumentException(String.format(
                    "Column %d of type %s does not match defined table column type of %s!",
                    column, newType, definedType));
    }

    private void assertRowMatchesColumns(List<Object> row)  {
        if (row.size() != columns.size())
            throw new IllegalArgumentException("Row size does not match column size!");
        for (int i = 0; i < row.size(); i++) {
            assertColumnMatchesColumn(i, row.get(i));
        }
    }

    private int assertIndexedColumn(Column<?> column) {
        int index = columns.indexOf(column);
        if (index < 0)
            throw new IllegalArgumentException("Table Error: getSelectedRowClass could not find column");
        return index;
    }
}
