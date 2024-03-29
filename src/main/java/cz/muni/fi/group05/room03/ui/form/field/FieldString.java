package cz.muni.fi.group05.room03.ui.form.field;

import cz.muni.fi.group05.room03.ui.SimpleDocumentListener;

import javax.swing.JComponent;
import javax.swing.JTextField;

public class FieldString implements Field<String> {

    private final JTextField textField;

    public FieldString() {
        textField = new JTextField("", 40);
    }

    @Override
    public JComponent getComponent() {
        return textField;
    }

    @Override
    public String getData() {
        return textField.getText();
    }

    @Override
    public void setData(String data) {
        textField.setText(data);
    }

    @Override
    public boolean hasData() {
        return !getData().isBlank();
    }

    @Override
    public void reset() {
        textField.setText("");
    }

    @Override
    public void addUpdateAction(Runnable action) {
        textField.getDocument().addDocumentListener((SimpleDocumentListener) documentEvent -> action.run());
    }

    @Override
    public void setEnabled(boolean enabled) {
        textField.setEnabled(enabled);
    }
}
