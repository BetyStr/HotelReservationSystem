package cz.muni.fi.group05.room03.ui.form.field;

import javax.swing.JComboBox;
import javax.swing.JComponent;

public class FieldEnum<T extends Enum<T>> implements Field<T> {

    private final JComboBox<T> comboBox;
    private final Class<T> enumType;

    public FieldEnum(Class<T> enumType) {
        T[] constants = enumType.getEnumConstants();
        if (constants.length <= 0)
            throw new IllegalArgumentException("FieldEnum Error: Provided enum has no values!");
        this.comboBox = new JComboBox<>(constants);
        this.enumType = enumType;
    }

    public static <E extends Enum<E>> FieldEnum<E> of(Class<E> enumType) {
        return new FieldEnum<>(enumType);
    }

    @Override
    public JComponent getComponent() {
        return comboBox;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getData() {
        return (T) comboBox.getSelectedItem();
    }

    @Override
    public void setData(T data) {
        comboBox.setSelectedItem(data);
    }

    @Override
    public boolean hasData() {
        return comboBox.isValid();
    }

    @Override
    public void reset() {
        comboBox.setSelectedItem(enumType.getEnumConstants()[0]);
    }

    @Override
    public void addUpdateAction(Runnable action) {
        comboBox.addActionListener(e -> action.run());
    }

    @Override
    public void setEnabled(boolean enabled) {
        comboBox.setEnabled(enabled);
    }
}
