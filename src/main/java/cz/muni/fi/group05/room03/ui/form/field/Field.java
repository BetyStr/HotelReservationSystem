package cz.muni.fi.group05.room03.ui.form.field;

import javax.swing.JComponent;

public interface Field<T> {

    JComponent getComponent();

    T getData();

    void setData(T data);

    boolean hasData();

    void reset();

    void addUpdateAction(Runnable action);

    void setEnabled(boolean enabled);
}
