package cz.muni.fi.group05.room03.ui.form.field;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class FieldInteger implements Field<Integer> {

    private final JSpinner spinner;
    private final int defaultValue = 1;

    public FieldInteger() {
        spinner = new JSpinner(new SpinnerNumberModel(defaultValue, 1, 10, 1));
    }

    @Override
    public JComponent getComponent() {
        return spinner;
    }

    @Override
    public Integer getData() {
        return (Integer) spinner.getValue();
    }

    @Override
    public void setData(Integer data) {
        spinner.setValue(data);
    }

    @Override
    public boolean hasData() {
        return true;
    }

    @Override
    public void reset() {
        spinner.setValue(defaultValue);
    }

    @Override
    public void addUpdateAction(Runnable action) {
        spinner.addChangeListener(changeEvent -> action.run());
    }

    @Override
    public void setEnabled(boolean enabled) {
        spinner.setEnabled(enabled);
    }
}
