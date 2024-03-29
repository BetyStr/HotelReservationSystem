package cz.muni.fi.group05.room03.ui.table;

import cz.muni.fi.group05.room03.data.HotelSystemDao;
import cz.muni.fi.group05.room03.model.Reservation;
import cz.muni.fi.group05.room03.ui.I18N;
import cz.muni.fi.group05.room03.ui.Menu;
import cz.muni.fi.group05.room03.ui.Message;
import cz.muni.fi.group05.room03.ui.form.ReservationForm;
import cz.muni.fi.group05.room03.ui.ContentPanelController.ContentPanelNames;
import cz.muni.fi.group05.room03.ui.HotelSystemUI;
import cz.muni.fi.group05.room03.data.ReservationDao;
import cz.muni.fi.group05.room03.ui.table.util.Column;
import cz.muni.fi.group05.room03.ui.table.util.Table;
import cz.muni.fi.group05.room03.ui.table.util.TableUI;

import javax.swing.JPanel;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

public class ReservationTable {

    private static final I18N I18N = new I18N(ReservationTable.class);

    private final ReservationDao reservationDao;
    private final Table table;
    private final TableUI tableUI;

    private final Column<String> name = Column.of(String.class, I18N.getString("name"));
    private final Column<LocalDate> dateFrom = Column.of(LocalDate.class, I18N.getString("dateFrom"));
    private final Column<LocalDate> dateTo = Column.of(LocalDate.class, I18N.getString("dateTo"));
    private final Column<String> telephone = Column.of(String.class, I18N.getString("telephone"));
    private final Column<String> email = Column.of(String.class, I18N.getString("email"));
    private final Column<Integer> people = Column.of(Integer.class, I18N.getString("people"));
    private final Column<String> info = Column.of(String.class, I18N.getString("info"));
    private final Column<Reservation.ReservationState> state = Column.of(Reservation.ReservationState.class, I18N.getString("state"));
    private final Column<Integer> daysToPerform = Column.of(Integer.class, I18N.getString("days"));
    private final Column<Long> id = Column.of(Long.class, I18N.getString("id"));

    public ReservationTable() {
        table = new Table(name, dateFrom, dateTo, telephone, email, people, info, state, daysToPerform, id);
        reservationDao = HotelSystemDao.getReservationDao();
        reservationDao.findAll().forEach(this::addReservation);
        Menu menu = new Menu.Builder()
                .addMenuItem(Menu.createMenuItem(I18N.getString("showInfo"), KeyEvent.VK_S, I18N.getString("showInfoDes"), 'i', this::showPersonalizedInfo), Menu.MenuCondition.EXACTLY_ONE)
                .addMenuItem(Menu.createMenuItem(I18N.getString("checkIn"), KeyEvent.VK_I, I18N.getString("checkInDes"), 'i', this::createCheckInButtonAction), Menu.MenuCondition.EXACTLY_ONE)
                .addMenuItem(Menu.createMenuItem(I18N.getString("edit"), KeyEvent.VK_E, I18N.getString("editDes"), 'e', this::reservationEditAction), Menu.MenuCondition.EXACTLY_ONE)
                .addMenuItem(Menu.createMenuItem(I18N.getString("cancel"), KeyEvent.VK_C, I18N.getString("cancelDes"), 'c', this::cancelAction), Menu.MenuCondition.EXACTLY_ONE)
                .build();
        tableUI = new TableUI(menu, table);
        tableUI.hideColumns(List.of(info, people, daysToPerform, id));
        tableUI.addDoubleClickAction(this::showPersonalizedInfo);
        tableUI.sortColumnsOnShow(List.of(daysToPerform, state));
    }

    private void showPersonalizedInfo() {
        tableUI.showPersonalizedInfo(List.of(name, telephone, people, state, info));
    }

    private void cancelAction() {
        int selectedRow = tableUI.getSelectedPosition();
        Reservation reservation = getSelectedReservation();
        if (reservation.getState() != Reservation.ReservationState.UPCOMING) {
            Message.showWarningDialog(I18N.getString("cancelNotUpcomingResWarn"));
            return;
        }
        if (Message.showYesNoDialog(I18N.getString("checkCancelReservationMessage"),
                I18N.getString("checkCancelReservationTitle"))) {
            table.setValueAt(Reservation.ReservationState.CANCELED, selectedRow, state);
            table.setValueAt(Integer.MAX_VALUE, selectedRow, daysToPerform);
            reservation.setState(Reservation.ReservationState.CANCELED);
            reservationDao.update(reservation);
            table.fireTableRowsInserted(selectedRow, selectedRow);
        }
    }

    private void addReservation(Reservation reservation) {
        table.addRow(reservation.getName(),
                reservation.getDateFrom(),
                reservation.getDateTo(),
                reservation.getTelephone(),
                reservation.getEmail(),
                reservation.getPeople(),
                reservation.getInfo(),
                reservation.getState(),
                reservation.getDaysToPerform(),
                reservation.getId());
    }

    public void createReservation(Reservation reservation) {
        reservationDao.create(reservation);
        addReservation(reservation);
    }

    public void checkInReservation() {
        Reservation reservation = getSelectedReservation();
        reservation.setState(Reservation.ReservationState.DOING);
        reservationDao.update(reservation);
        table.setValueAt(Reservation.ReservationState.DOING, tableUI.getSelectedPosition(), state);
        table.setValueAt(reservation.getDaysToPerform(), tableUI.getSelectedPosition(), daysToPerform);
        table.fireTableDataChanged();
        HotelSystemUI.getFooter().calculate();
    }

    public JPanel getPanel() {
        return tableUI.getPanel();
    }

    public void checkOutReservation(Reservation reservation) {
        Long reservationId = reservation.getId();
        reservation.setState(Reservation.ReservationState.ENDED);
        reservationDao.update(reservation);
        int rowIndex = IntStream.range(0, table.getRowCount())
                .filter(i -> table.getRowValue(i, id).equals(reservationId)).findFirst().orElse(-1);
        table.setValueAt(Reservation.ReservationState.ENDED, rowIndex, state);
        table.setValueAt(reservation.getDaysToPerform(), rowIndex, daysToPerform);
        table.fireTableDataChanged();
    }

    public void removeIfSelected() {
        var row = tableUI.getSelectedPosition();
        if (row >= 0) {
            table.removeRow(row);
        }
    }

    private void createCheckInButtonAction() {
        int selectedPosition = tableUI.getSelectedPosition();
        if (Reservation.ReservationState.UPCOMING == table.getRowValue(selectedPosition, state)) {
            HotelSystemUI.getGuestForm().setNumberToCheckIn(table.getRowValue(selectedPosition, people));
            HotelSystemUI.getGuestForm().setReservationId(table.getRowValue(selectedPosition, id));
            HotelSystemUI.getPanelController().switchPanel(ContentPanelNames.GUEST_FORM);
        } else {
            Message.showWarningDialog(I18N.getString("checkInNotUpcomingResWarn"));
        }
    }

    private void reservationEditAction() {
        Reservation reservation = getSelectedReservation();
        HotelSystemUI.getPanelController().switchPanel(ContentPanelNames.RESERVATION_FORM);
        ReservationForm reservationForm = HotelSystemUI.getReservationForm();
        reservationForm.fillBasedOn(reservation);
        reservationForm.setState(reservation.getState());
    }

    private Reservation getRowReservation(int row) {
        return new Reservation(
                table.getRowValue(row, name),
                table.getRowValue(row, dateFrom),
                table.getRowValue(row, dateTo),
                table.getRowValue(row, telephone),
                table.getRowValue(row, email),
                table.getRowValue(row, people),
                table.getRowValue(row, info),
                table.getRowValue(row, state),
                table.getRowValue(row, id)
        );
    }

    private Reservation getSelectedReservation() {
        return getRowReservation(tableUI.getSelectedPosition());
    }
}
