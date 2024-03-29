package cz.muni.fi.group05.room03.ui.form.field;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import cz.muni.fi.group05.room03.ui.FontsHotelUI;
import cz.muni.fi.group05.room03.ui.I18N;

import javax.swing.JButton;
import javax.swing.JComponent;
import java.awt.Color;
import java.time.LocalDate;
import java.util.Locale;

public class FieldLocalDate implements Field<LocalDate> {

    private static final I18N I18N = new I18N(FieldLocalDate.class);

    private final DatePicker datePicker;

    public FieldLocalDate() {
        datePicker = new DatePicker(getDatePickerSettings());
        configureToggleButton(datePicker.getComponentToggleCalendarButton());
    }

    @Override
    public JComponent getComponent() {
        return datePicker;
    }

    @Override
    public LocalDate getData() {
        return datePicker.getDate();
    }

    @Override
    public void setData(LocalDate data) {
        datePicker.setDate(data);
    }

    @Override
    public boolean hasData() {
        return getData() != null;
    }

    @Override
    public void reset() {
        datePicker.getComponentDateTextField().setText("");
    }

    @Override
    public void addUpdateAction(Runnable action) {
        datePicker.addDateChangeListener(changeEvent -> action.run());
    }

    @Override
    public void setEnabled(boolean enabled) {
        datePicker.setEnabled(enabled);
    }

    private void configureToggleButton(JButton toggle) {
        toggle.setText(I18N.getString("chooseDate"));
        toggle.setFont(FontsHotelUI.FORM);
        toggle.setBorderPainted(false);
        toggle.setFocusPainted(false);
        toggle.setBackground(Color.decode("#7f95a3"));
        toggle.setForeground(Color.WHITE);
    }

    private DatePickerSettings getDatePickerSettings() {
        var datePickerSettings = new DatePickerSettings();
        datePickerSettings.setLocale(Locale.UK);
        datePickerSettings.setFontTodayLabel(FontsHotelUI.FORM);
        datePickerSettings.setFontValidDate(FontsHotelUI.FORM);
        datePickerSettings.setFontCalendarDateLabels(FontsHotelUI.FORM);
        datePickerSettings.setFontClearLabel(FontsHotelUI.FORM);
        datePickerSettings.setFontCalendarWeekdayLabels(FontsHotelUI.FORM);
        datePickerSettings.setFontCalendarWeekNumberLabels(FontsHotelUI.FORM);
        datePickerSettings.setFontInvalidDate(FontsHotelUI.FORM);
        datePickerSettings.setFontMonthAndYearMenuLabels(FontsHotelUI.FORM);
        datePickerSettings.setFontMonthAndYearNavigationButtons(FontsHotelUI.FORM);
        return datePickerSettings;
    }
}
