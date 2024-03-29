package cz.muni.fi.group05.room03.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Comparator;

public class Message {

    private static final I18N I18N = new I18N(Message.class);

    private static JPanel createPanel(String string) {
        String[][] table = Arrays.stream(string.split("\n")).map(s -> s.split("\t")).toArray(String[][]::new);
        var max = Arrays.stream(table).max(Comparator.comparingInt(strings -> strings.length)).orElseThrow().length;
        var min = Arrays.stream(table).min(Comparator.comparingInt(strings -> strings.length)).orElseThrow().length;
        if (max != min)
            throw new IllegalArgumentException("Message has wrong formatting, table must be fully filled with data");
        JPanel panel = new JPanel(new GridLayout(table.length, max, 5, 5));
        for (var row : table) {
            for (var column : row) {
                panel.add(new JLabel(column));
            }
        }
        return panel;
    }

    public static void showInformationDialog(String title, String info) {
        JOptionPane.showMessageDialog(new Container(), createPanel(info), title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showWarningDialog(String info) {
        JOptionPane.showMessageDialog(new Container(), createPanel(info), I18N.getString("warn"), JOptionPane.WARNING_MESSAGE);
    }

    public static void showErrorDialog(String info) {
        JOptionPane.showMessageDialog(new Container(), createPanel(info), I18N.getString("error"), JOptionPane.ERROR_MESSAGE);
    }

    public static boolean showYesNoDialog(String title, JComponent component) {
        String[] options = { I18N.getString("yes"), I18N.getString("no") };
        return JOptionPane.showOptionDialog(null, component, title, JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[1]) == JOptionPane.OK_OPTION;
    }

    public static boolean showYesNoDialog(String title, String info) {
        return showYesNoDialog(title, createPanel(info));
    }

    public static int showOptions(String title, String message, String first, String second) {
        String[] options = { first, second, I18N.getString("cancel") };
        return JOptionPane.showOptionDialog(new Container(), message, title, JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,null, options, options[2]);
    }

    public static String showAllOptions(String title, String message, Object[] options) {
        return (String) JOptionPane.showInputDialog(new Container(), title, message, JOptionPane.PLAIN_MESSAGE,
                null, options,null);
    }
}
