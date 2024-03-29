package cz.muni.fi.group05.room03.ui.table.util;

import cz.muni.fi.group05.room03.ui.ColoredButton;
import cz.muni.fi.group05.room03.ui.ContentPanelController;
import cz.muni.fi.group05.room03.ui.FontsHotelUI;
import cz.muni.fi.group05.room03.ui.HotelSystemUI;
import cz.muni.fi.group05.room03.ui.I18N;
import cz.muni.fi.group05.room03.ui.Message;
import cz.muni.fi.group05.room03.ui.SimpleDocumentListener;
import cz.muni.fi.group05.room03.ui.Menu;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class TableUI {

    private static final I18N I18N = new I18N(TableUI.class);

    private final JPanel panel;
    private final JTable jTable;
    private final Table model;
    private final TableRowSorter<TableModel> sorter;
    private final TableColumnModel tableColumnModel;
    private final Menu menu;

    public TableUI(Menu menu, Table model) {
        this.menu = menu;
        this.menu.updateActions(0);
        jTable = createTable(menu.getPopMenu(), model);
        jTable.getSelectionModel().addListSelectionListener(this::rowSelectionChanged);
        panel = createPanel(jTable);
        tableColumnModel = jTable.getColumnModel();
        sorter = new TableRowSorter<>(model);
        jTable.setRowSorter(sorter);
        this.model = model;
    }

    public static String truncatedString(String value) {
        StringBuilder builder = new StringBuilder().append(value, 0, Math.min(value.length(), 20));
        if (value.length() > 20) {
            builder.append("...");
        }
        return builder.toString();
    }

    private void rowSelectionChanged(ListSelectionEvent listSelectionEvent) {
        var selectionModel = (ListSelectionModel) listSelectionEvent.getSource();
        menu.updateActions(selectionModel.getSelectedItemsCount());
    }

    public void hideColumn(Object columnName) {
        if (columnName == null) {
            return;
        }
        for (int i = 0; i < tableColumnModel.getColumnCount(); i++) {
            TableColumn column = tableColumnModel.getColumn(i);
            if (columnName.equals(column.getHeaderValue())) {
                if (tableColumnModel.getColumnCount() == 1) {
                    return;
                }
                tableColumnModel.removeColumn(column);
                break;
            }
        }
    }

    public void hideColumns(List<Column<?>> columns) {
        columns.forEach(column -> this.hideColumn(column.getName()));
    }

    public void filterRows(String regex) {
        List<String> visibleColumns = new ArrayList<>();
        for (Enumeration<TableColumn> e = jTable.getColumnModel().getColumns(); e.hasMoreElements(); ) {
            visibleColumns.add((String) e.nextElement().getIdentifier());
        }
        List<String> allColumns = model.getColumnNames();
        int[] visibleIndexes = visibleColumns.stream().map(allColumns::indexOf).mapToInt(i -> i).toArray();
        try {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + regex, visibleIndexes));
        } catch (PatternSyntaxException ignored) {
            // illegal regex will be ignored
        }
    }

    private void deselectAllRows() {
        jTable.getSelectionModel().clearSelection();
    }

    public int getSelectedPosition() {
        var row = jTable.getSelectedRow();
        return row >= 0 ? sorter.convertRowIndexToModel(row) : row;
    }

    public int[] getSelectedPositions() {
        final int[] selectedRows = jTable.getSelectedRows();
        for (int i = 0; i < selectedRows.length; i++) {
            selectedRows[i] = sorter.convertRowIndexToModel(selectedRows[i]);
        }
        return selectedRows;
    }

    public void showPersonalizedInfo(List<Column<?>> columns) {
        StringBuilder builder = new StringBuilder();
        int position = getSelectedPosition();
        for (Column<?> column : columns) {
            String value = model.getRowValue(position, column).toString();
            if (!value.isEmpty()) {
                builder.append(column.getName()).append(":\t").append(truncatedString(value)).append('\n');
            }
        }
        Message.showInformationDialog(I18N.getString("infoAboutGuest"), builder.toString());
    }

    public void sortColumnsOnShow(List<Column<?>> columns) {
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                sortColumns(columns);
            }
        });
    }

    public void addDoubleClickAction(Runnable action){
        jTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked (MouseEvent me){
                if (me.getClickCount() == 2) {
                    action.run();
                }
            }
        });
    }

    public JPanel getPanel() {
        return panel;
    }

    private JPanel createPanel(JTable jTable) {
        var panel = new JPanel(new BorderLayout());
        var toolPanel = new JPanel(new GridBagLayout());
        var constraints = getGridBagConstraints();
        var searchField = getSearchField(toolPanel, constraints);
        createNewReservationButton(toolPanel, constraints);
        panel.add(toolPanel, BorderLayout.PAGE_START);
        panel.add(new JScrollPane(jTable));
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                deselectAllRows();
                searchField.setText("");
            }
        });
        return panel;
    }

    private void createNewReservationButton(JPanel toolPanel, GridBagConstraints constraints) {
        var buttonCreateNewRes = ColoredButton.large(I18N.getString("createNewRes"));
        buttonCreateNewRes.setFont(FontsHotelUI.TABLE_CONTENT);
        buttonCreateNewRes.addActionListener(this::createReservationButtonAction);
        toolPanel.add(buttonCreateNewRes, constraints);
    }

    private JTextField getSearchField(JPanel toolPanel, GridBagConstraints constraints) {
        var searchField = new JTextField();
        searchField.setFont(FontsHotelUI.TABLE_CONTENT);
        searchField.setColumns(20);
        toolPanel.add(searchField, constraints);
        constraints.weightx = 0;
        constraints.gridx = 1;
        searchField.getDocument().addDocumentListener((SimpleDocumentListener) e -> filterRows(searchField.getText()));
        toolPanel.add(new JLabel(new ImageIcon("src/main/resources/searchIcon.png")), constraints);
        constraints.gridx = 2;
        constraints.weightx = 1;
        toolPanel.add(new JLabel(), constraints);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 3;
        return searchField;
    }

    private GridBagConstraints getGridBagConstraints() {
        var constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 10, 3);
        constraints.weightx = 1.0;
        constraints.gridy = 0;
        constraints.gridx = 0;
        return constraints;
    }

    private JTable createTable(JPopupMenu popMenu, TableModel model) {
        JTable jTable = new JTable(model);
        jTable.setFont(FontsHotelUI.TABLE_CONTENT);
        jTable.setRowHeight(25);
        jTable.setFocusable(false);
        jTable.setComponentPopupMenu(popMenu);
        var header = jTable.getTableHeader();
        header.setBackground(Color.decode("#C6C7C8"));
        header.setForeground(Color.decode("#3B3B3B"));
        header.setFont(FontsHotelUI.TABLE_HEADER);
        for (int i = 0; i < jTable.getColumnCount(); i++) {
            DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
            leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
            jTable.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
            if (model.getColumnClass(i).equals(LocalDate.class)) {
                jTable.getColumnModel().getColumn(i).setCellRenderer(new DateCellRenderer());
            }
        }
        return jTable;
    }

    private void sortColumns(List<Column<?>> columns) {
        List<RowSorter.SortKey> sortKeys = new ArrayList<>(25);
        for (var column : columns) {
            sortKeys.add(new RowSorter.SortKey(model.findColumn(column), SortOrder.ASCENDING));
        }
        try {
            sorter.setSortKeys(sortKeys);
        } catch (IndexOutOfBoundsException ignored) {
            throw new IllegalArgumentException("Calling sortColumn on empty data! First put values in the table, then sort them.");
        } catch (IllegalArgumentException ignored) {
            throw new IllegalArgumentException("Calling sortColumn with not exiting columnName!");
        }
    }

    private void createReservationButtonAction(ActionEvent actionEvent) {
        deselectAllRows();
        HotelSystemUI.getPanelController().switchPanel(ContentPanelController.ContentPanelNames.RESERVATION_FORM);
    }

    private static class DateCellRenderer extends DefaultTableCellRenderer {
        public DateCellRenderer() { super(); }

        @Override
        public void setValue(Object value) {
            setText((value == null) ? "" : LocalDate.parse(value.toString()).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }
    }
}
