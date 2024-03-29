package cz.muni.fi.group05.room03.ui.table;

import cz.muni.fi.group05.room03.data.HotelSystemDao;
import cz.muni.fi.group05.room03.model.Room;
import cz.muni.fi.group05.room03.ui.I18N;
import cz.muni.fi.group05.room03.ui.Menu;
import cz.muni.fi.group05.room03.ui.Message;
import cz.muni.fi.group05.room03.ui.form.GuestForm;
import cz.muni.fi.group05.room03.model.Guest;
import cz.muni.fi.group05.room03.ui.ContentPanelController.ContentPanelNames;
import cz.muni.fi.group05.room03.ui.HotelSystemUI;
import cz.muni.fi.group05.room03.data.GuestDao;
import cz.muni.fi.group05.room03.ui.table.util.Column;
import cz.muni.fi.group05.room03.ui.table.util.Table;
import cz.muni.fi.group05.room03.ui.table.util.TableUI;

import javax.swing.JPanel;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GuestTable {

    private static final I18N I18N = new I18N(GuestTable.class);

    private final GuestDao guestDao;
    private final Table table;
    private final TableUI tableUI;

    private final Column<String> name = Column.of(String.class, I18N.getString("name"));
    private final Column<String> room = Column.of(String.class, I18N.getString("room"));
    private final Column<String> idCard = Column.of(String.class, I18N.getString("idCard"));
    private final Column<Guest.GuestGeneration> age = Column.of(Guest.GuestGeneration.class, I18N.getString("age"));
    private final Column<String> info = Column.of(String.class, I18N.getString("info"));
    private final Column<Long> id = Column.of(Long.class, I18N.getString("id"));
    private final Column<Long> idReservation = Column.of(Long.class, I18N.getString("idRes"));

    public GuestTable() {
        table = new Table(name, room, idCard, age, info, id, idReservation);
        guestDao = HotelSystemDao.getGuestDao();
        guestDao.findAll().forEach(this::addGuest);
        Menu menu = new Menu.Builder()
                .addMenuItem(Menu.createMenuItem(I18N.getString("assignRoom"), KeyEvent.VK_O, I18N.getString("assignRoomDes"), 'o', this::assignRoomAction), Menu.MenuCondition.MORE_THAN_ZERO)
                .addMenuItem(Menu.createMenuItem(I18N.getString("showInfo"), KeyEvent.VK_I, I18N.getString("showInfoDes"), 'i', this::showPersonalizedInfo), Menu.MenuCondition.EXACTLY_ONE)
                .addMenuItem(Menu.createMenuItem(I18N.getString("edit"), KeyEvent.VK_E, I18N.getString("editDes"), 'e', this::guestEditAction), Menu.MenuCondition.EXACTLY_ONE)
                .build();
        tableUI = new TableUI(menu, table);
        tableUI.hideColumns(List.of(idCard, age, info, id, idReservation));
        tableUI.addDoubleClickAction(this::showPersonalizedInfo);
        tableUI.sortColumnsOnShow(List.of(room));
    }

    public void assignRoomAction() {
        List<Guest> selected = getSelectedGuests();
        var resId = selected.get(0).getReservationId();
        for (int i = 1; i < selected.size(); i++) {
            if (!selected.get(i).getReservationId().equals(resId)) {
                Message.showWarningDialog(I18N.getString("sameResWarn"));
                return;
            }
        }
        Object[] allRooms = getAvailableRooms(selected, resId);
        if (allRooms.length == 0) {
            Message.showWarningDialog(I18N.getString("full"));
            return;
        }
        String selection = Message.showAllOptions(I18N.getString("assignRoom"), I18N.getString("chooseRoom"), allRooms);
        if (selection != null && selection.length() > 0) {
            HotelSystemUI.getRoomTable().changeStatus(selection, Room.RoomStatus.OCCUPIED);
            Arrays.stream(tableUI.getSelectedPositions()).forEach(i -> table.setValueAt(selection, i, room));
            moveGuestsBetweenRooms(selected);
            selected.forEach(guest -> { guest.setRoom(selection); guestDao.update(guest); });
            HotelSystemUI.getFooter().calculate();
        }
    }

    public void createGuests(List<Guest> guests){
        for (Guest guest : guests) {
            guestDao.create(guest);
            addGuest(guest);
        }
    }

    public void deleteGuests(List<Guest> guests) {
        for (Guest guest: guests) {
            guestDao.delete(guest);
            for (int i = 0; i < table.getRowCount(); i++) {
                if (table.getRowValue(i, id).equals(guest.getId())) {
                    table.removeRow(i);
                    break;
                }
            }
        }
    }

    public JPanel getPanel() {
        return tableUI.getPanel();
    }

    public void removeEditingRow() {
        table.removeRow(tableUI.getSelectedPosition());
    }

    private void showPersonalizedInfo() {
        tableUI.showPersonalizedInfo(List.of(name, idCard, age, info, room));
    }

    private Object[] getAvailableRooms(List<Guest> selected, Long resId) {
        List<Guest> guests = HotelSystemDao.getGuestDao().findAll();
        Map<String, Integer> occupied = new HashMap<>();
        Set<String> present = new HashSet<>();
        for (var guest : guests) {
            var room = guest.getRoom();
            if (room != null) {
                occupied.put(room, occupied.getOrDefault(guest.getRoom(), 0) + 1);
            }
            if (resId.equals(guest.getReservationId())) {
                present.add(room);
            }
        }
        List<String> allRooms = new ArrayList<>();
        for (var r : HotelSystemDao.getRoomDao().findAll()) {
            if ((Room.RoomStatus.NOT_OCCUPIED.equals(r.getStatus()) || present.contains(r.getKey()))
                    && selected.size() + occupied.getOrDefault(r.getKey(), 0) <= r.getBeds()) {
                allRooms.add(r.getKey());
            }
        }
        return allRooms.toArray();
    }

    private void moveGuestsBetweenRooms(List<Guest> guests) {
        Map<String, Integer> rooms = new HashMap<>();
        for (Guest guest : guests) {
            String checkingRoom = guest.getRoom();
            if(!checkingRoom.equals("")) {
                rooms.put(checkingRoom, rooms.getOrDefault(checkingRoom, 0) + 1);
            }
        }
        for (String roomNumber : rooms.keySet()) {
            if(HotelSystemDao.getGuestDao().findByRoomKey(roomNumber).size() == rooms.get(roomNumber)) {
                HotelSystemUI.getRoomTable().changeStatus(roomNumber, Room.RoomStatus.NOT_OCCUPIED);
            }
        }
    }

    private void guestEditAction() {
        HotelSystemUI.getPanelController().switchPanel(ContentPanelNames.GUEST_FORM);
        Guest guest = getSelectedGuest();
        GuestForm guestForm = HotelSystemUI.getGuestForm();
        guestForm.fillBasedOn(guest);
        guestForm.setEditing(true);
        guestForm.setReservationId(guest.getReservationId());
    }

    private Guest getRowGuest(int row) {
        return new Guest(
                table.getRowValue(row, name),
                table.getRowValue(row, room),
                table.getRowValue(row, idCard),
                table.getRowValue(row, age),
                table.getRowValue(row, info),
                table.getRowValue(row, idReservation),
                table.getRowValue(row, id)
        );
    }

    private Guest getSelectedGuest() {
        return getRowGuest(tableUI.getSelectedPosition());
    }

    private List<Guest> getSelectedGuests() {
        return Arrays.stream(tableUI.getSelectedPositions()).mapToObj(this::getRowGuest).collect(Collectors.toList());
    }

    private void addGuest(Guest guest) {
        table.addRow(guest.getName(),
                guest.getRoom(),
                guest.getIdCard(),
                guest.getGeneration(),
                guest.getInfo(),
                guest.getId(),
                guest.getReservationId());
    }
}
