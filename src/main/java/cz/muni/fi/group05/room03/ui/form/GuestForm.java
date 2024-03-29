package cz.muni.fi.group05.room03.ui.form;

import cz.muni.fi.group05.room03.model.Guest;
import cz.muni.fi.group05.room03.ui.ColoredButton;
import cz.muni.fi.group05.room03.ui.ContentPanelController.ContentPanelNames;
import cz.muni.fi.group05.room03.ui.HotelSystemUI;
import cz.muni.fi.group05.room03.ui.I18N;
import cz.muni.fi.group05.room03.ui.form.field.Field;
import cz.muni.fi.group05.room03.ui.form.field.FieldEnum;
import cz.muni.fi.group05.room03.ui.form.field.FieldString;
import cz.muni.fi.group05.room03.ui.form.field.FieldText;

import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuestForm {

    private static final I18N I18N = new I18N(GuestForm.class);

    private final Form form;
    private final Field<String> name = new FieldString();
    private final Field<String> idCard = new FieldString();
    private final Field<Guest.GuestGeneration> generation = FieldEnum.of(Guest.GuestGeneration.class);
    private final Field<String> info = new FieldText();
    private final List<Field<?>> fields = Arrays.asList(name, idCard, generation, info);
    private final ColoredButton confirm = ColoredButton.form(I18N.getString("addNext"));
    private int guestNum = 1;
    private String room = "";
    private boolean editing = false;
    private Long reservationId;
    private int numberToCheckIn = 0;
    private Long id = null;

    private List<Guest> checkInGuests = new ArrayList<>();

    public GuestForm() {
        confirm.addConfirmReason(this::GuestAddNextButtonAction, this::getFilledInfo);
        confirm.setEnabled(false);
        ColoredButton cancel = ColoredButton.form(I18N.getString("cancel"));
        cancel.addActionListener(this::GuestCancelButtonAction);
        form = new Form.FormBuilder(I18N.getString("checkIn"))
                .addComponent(name, I18N.getString("name"))
                .addComponent(idCard, I18N.getString("idCard"))
                .addComponent(generation, I18N.getString("age"))
                .addComponent(info, I18N.getString("info"))
                .build(confirm, cancel);
        form.getPanel().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                guestNum = 1;
                if (guestNum == numberToCheckIn || editing){
                    confirm.setText(I18N.getString("confirm"));
                } else {
                    confirm.setText(I18N.getString("addNext"));
                }
            }
            @Override
            public void componentHidden(ComponentEvent e) {
                id = null;
                checkInGuests = new ArrayList<>();
            }
        });
        form.addResetFields(fields);
        fields.forEach(field -> field.addUpdateAction(this::colorBasedOnFilled));
    }

    public void fillBasedOn(Guest guest) {
        name.setData(guest.getName());
        idCard.setData(guest.getIdCard());
        generation.setData(guest.getGeneration());
        info.setData(guest.getInfo());
        room = guest.getRoom();
        id = guest.getId();
        reservationId = guest.getReservationId();
    }

    private void GuestCancelButtonAction(ActionEvent actionEvent) {
        if (editing) {
            HotelSystemUI.getPanelController().switchPanel(ContentPanelNames.GUEST_TABLE);
            setEditing(false);
        } else {
            HotelSystemUI.getPanelController().switchPanel(ContentPanelNames.RESERVATION_TABLE);
        }
    }

    private void colorBasedOnFilled() {
        confirm.setEnabled(isAllFilled());
    }

    private boolean isAllFilled() {
        return name.hasData() && (generation.getData() == Guest.GuestGeneration.CHILD || idCard.hasData()) && info.getData().length() <= 1000;
    }

    private String getFilledInfo() {
        StringBuilder builder = new StringBuilder();
        if (!name.hasData()) {
            appendInfo(builder, "nameNotFilled");
        }
        if (generation.getData() == Guest.GuestGeneration.ADULT && !idCard.hasData()) {
            appendInfo(builder, "idCardMandatory");
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

    private void GuestAddNextButtonAction() {
        final Guest guest = new Guest(name.getData(), room, idCard.getData(), generation.getData(), info.getData(), reservationId);
        guest.setId(id);
        checkInGuests.add(guest);
        if (editing) {
            HotelSystemUI.getGuestTable().createGuests(checkInGuests);
            HotelSystemUI.getGuestTable().removeEditingRow();
            HotelSystemUI.getPanelController().switchPanelNow(ContentPanelNames.GUEST_TABLE);
            setEditing(false);
        } else if (guestNum == numberToCheckIn - 1) {
            confirm.setText(I18N.getString("confirm"));
            guestNum++;
        } else if (guestNum == numberToCheckIn){
            HotelSystemUI.getGuestTable().createGuests(checkInGuests);
            HotelSystemUI.getPanelController().switchPanelNow(ContentPanelNames.GUEST_TABLE);
            HotelSystemUI.getReservationsTable().checkInReservation();
        } else {
            guestNum++;
        }
        fields.forEach(Field::reset);
    }

    public JPanel getPanel() {
        return form.getPanel();
    }

    public void setNumberToCheckIn(int number) {
        numberToCheckIn = number;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }
}
