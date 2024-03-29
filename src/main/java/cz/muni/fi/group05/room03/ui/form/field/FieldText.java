package cz.muni.fi.group05.room03.ui.form.field;

import cz.muni.fi.group05.room03.ui.FontsHotelUI;
import cz.muni.fi.group05.room03.ui.SimpleDocumentListener;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class FieldText implements Field<String> {

    private final JTextArea textArea;
    private final JScrollPane scrollBar;

    public FieldText() {
        textArea = new JTextArea(5, 40);
        textArea.setLineWrap(true);
        textArea.setFont(FontsHotelUI.FORM);
        scrollBar = new JScrollPane(textArea);
    }

    @Override
    public JComponent getComponent() {
        return scrollBar;
    }

    @Override
    public String getData() {
        return textArea.getText();
    }

    @Override
    public void setData(String data) {
        textArea.setText(data);
    }

    @Override
    public boolean hasData() {
        return !getData().isBlank();
    }

    @Override
    public void reset() {
        textArea.setText("");
    }

    @Override
    public void addUpdateAction(Runnable action) {
        textArea.getDocument().addDocumentListener((SimpleDocumentListener) documentEvent -> action.run());
    }

    @Override
    public void setEnabled(boolean enabled) {
        textArea.setEnabled(enabled);
    }
}
