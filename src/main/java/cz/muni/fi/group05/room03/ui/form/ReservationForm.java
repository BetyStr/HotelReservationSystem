package cz.muni.fi.group05.room03.ui.form;

import cz.muni.fi.group05.room03.data.HotelSystemDao;
import cz.muni.fi.group05.room03.model.Reservation;
import cz.muni.fi.group05.room03.model.Room;
import cz.muni.fi.group05.room03.ui.ColoredButton;
import cz.muni.fi.group05.room03.ui.ContentPanelController.ContentPanelNames;
import cz.muni.fi.group05.room03.ui.HotelSystemUI;
import cz.muni.fi.group05.room03.ui.I18N;
import cz.muni.fi.group05.room03.ui.Message;
import cz.muni.fi.group05.room03.ui.form.field.Field;
import cz.muni.fi.group05.room03.ui.form.field.FieldInteger;
import cz.muni.fi.group05.room03.ui.form.field.FieldLocalDate;
import cz.muni.fi.group05.room03.ui.form.field.FieldString;
import cz.muni.fi.group05.room03.ui.form.field.FieldText;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class ReservationForm {

    private static final I18N I18N = new I18N(ReservationForm.class);
    private final Form form;

    private final Field<String> name = new FieldString();
    private final Field<String> telephone = new FieldString();
    private final Field<String> email = new FieldString();
    private final Field<LocalDate> dateFrom = new FieldLocalDate();
    private final Field<LocalDate> dateTo = new FieldLocalDate();
    private final Field<Integer> people = new FieldInteger();
    private final Field<String> info = new FieldText();
    private final ColoredButton confirm = ColoredButton.form(I18N.getString("confirm"));
    private Reservation.ReservationState state = Reservation.ReservationState.UPCOMING;
    private Long id = null;

    public ReservationForm() {
        confirm.addConfirmReason(this::reservationConfirmButtonAction, this::getFilledInfo);
        confirm.setEnabled(false);
        ColoredButton cancel = ColoredButton.form(I18N.getString("cancel"));
        cancel.addActionListener(this::reservationCancelButtonAction);
        form = new Form.FormBuilder(I18N.getString("newReservation"))
                .addComponent(name, I18N.getString("name"))
                .addComponent(telephone, I18N.getString("telephone"))
                .addComponent(email, I18N.getString("email"))
                .addComponent(dateFrom, I18N.getString("dateFrom"))
                .addComponent(dateTo, I18N.getString("dateTo"))
                .addComponent(people, I18N.getString("peopleNum"))
                .addComponent(info, I18N.getString("info"))
                .build(confirm, cancel);
        List<Field<?>> fields = Arrays.asList(name, telephone, email, dateFrom, dateTo, people, info);
        form.addResetFields(fields);
        fields.forEach(field -> field.addUpdateAction(this::colorBasedOnFilled));
        form.getPanel().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                id = null;
                state = Reservation.ReservationState.UPCOMING;
            }
        });
    }

    private static boolean overlapsWith(Reservation first, Reservation second) {
        return first.getDateFrom().isBefore(second.getDateTo()) && second.getDateFrom().isBefore(first.getDateTo());
    }

    public void setState(Reservation.ReservationState state) {
        this.state = state;
    }

    public void fillBasedOn(Reservation reservation) {
        name.setData(reservation.getName());
        telephone.setData(reservation.getTelephone());
        email.setData(reservation.getEmail());
        dateFrom.setData(reservation.getDateFrom());
        dateTo.setData(reservation.getDateTo());
        people.setData(reservation.getPeople());
        info.setData(reservation.getInfo());
        id = reservation.getId();
        state = reservation.getState();
        name.setEnabled(state == Reservation.ReservationState.UPCOMING);
        telephone.setEnabled(state == Reservation.ReservationState.UPCOMING);
        email.setEnabled(state == Reservation.ReservationState.UPCOMING);
        dateFrom.setEnabled(state == Reservation.ReservationState.UPCOMING);
        info.setEnabled(state != Reservation.ReservationState.ENDED && state != Reservation.ReservationState.CANCELED);
        dateTo.setEnabled(state != Reservation.ReservationState.ENDED && state != Reservation.ReservationState.CANCELED);
        people.setEnabled(state == Reservation.ReservationState.UPCOMING);
        colorBasedOnFilled();
    }

    private boolean isEmailOK() {
        return email.hasData() && email.getData().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    private boolean isTelephoneOK() {
        return telephone.hasData() && telephone.getData()
                .replaceAll("[ \\-]", "")
                .matches("^(?:\\+[1-9][0-9]{0,2})?[0-9]{1,12}$");
    }

    private void colorBasedOnFilled() {
        confirm.setEnabled(isAllFilled());
    }

    private boolean isAllFilled() {
        return name.hasData() && isTelephoneOK() && dateFrom.hasData() && dateTo.hasData()
                && people.hasData() && dateTo.getData().isAfter(dateFrom.getData())
                && (state != Reservation.ReservationState.UPCOMING || !dateFrom.getData().isBefore(LocalDate.now()))
                && (state == Reservation.ReservationState.ENDED || !dateTo.getData().isBefore(LocalDate.now()))
                && (!email.hasData() || isEmailOK()) && (!info.hasData() || info.getData().length() <= 1000);
    }

    private String getFilledInfo() {
        StringBuilder builder = new StringBuilder();
        if (!name.hasData()) {
            appendInfo(builder, "nameNotFilled");
        }
        if (!telephone.hasData()) {
            appendInfo(builder, "phoneNotFilled");
        } else if (!isTelephoneOK()) {
            appendInfo(builder, "phoneNotCorrect");
        }
        if (email.hasData() && !isEmailOK()) {
            appendInfo(builder, "emailNotCorrect");
        }
        boolean from = dateFrom.hasData();
        boolean to = dateTo.hasData();
        if (from && to) {
            if (state == Reservation.ReservationState.UPCOMING && dateFrom.getData().isBefore(LocalDate.now())) {
                appendInfo(builder, "dateFromNotFuture");
            }
            if (state != Reservation.ReservationState.ENDED && dateTo.getData().isBefore(LocalDate.now())) {
                appendInfo(builder, "dateToNotFuture");
            }
            if (!dateTo.getData().isAfter(dateFrom.getData())) {
                appendInfo(builder, "dateToNotAfterFrom");
            }
        } else {
            if (!from) {
                appendInfo(builder, "dateFromNotFilled");
            }
            if (!to) {
                appendInfo(builder, "dateToNotFilled");
            }
        }
        if (!people.hasData()) {
            appendInfo(builder, "selectPeopleNum");
        }
        if (info.getData().length() > 1000) {
            appendInfo(builder, "infoTooLong");
        }
        return builder.toString();
    }

    private void appendInfo(StringBuilder builder, String info) {
        builder.append(I18N.getString(info));
        builder.append("\n");
    }

    private void reservationConfirmButtonAction() {
        List<Reservation> reservations = HotelSystemDao.getReservationDao().findAll();
        Reservation reservation = new Reservation(name.getData(), dateFrom.getData(), dateTo.getData(),
                telephone.getData(), email.getData(), people.getData(), info.getData(), state);
        reservation.setId(id);
        int people = reservations.stream().filter(res -> overlapsWith(res, reservation) && !res.getId().equals(reservation.getId())
                && (res.getState().equals(Reservation.ReservationState.UPCOMING) || res.getState().equals(Reservation.ReservationState.DOING)))
                .mapToInt(Reservation::getPeople).sum();
        if (people + reservation.getPeople() > HotelSystemDao.getRoomDao().findAll().stream().mapToInt(Room::getBeds).sum()) {
            Message.showWarningDialog(I18N.getString("full"));
            return;
        }
        HotelSystemUI.getReservationsTable().removeIfSelected();
        HotelSystemUI.getReservationsTable().createReservation(reservation);
        HotelSystemUI.getPanelController().switchPanelNow(ContentPanelNames.RESERVATION_TABLE);
    }

    private void reservationCancelButtonAction(ActionEvent actionEvent) {
        HotelSystemUI.getPanelController().switchPanel(ContentPanelNames.RESERVATION_TABLE);
    }

    public JPanel getPanel() {
        return form.getPanel();
    }
}
