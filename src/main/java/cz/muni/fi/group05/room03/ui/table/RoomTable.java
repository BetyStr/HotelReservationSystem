package cz.muni.fi.group05.room03.ui.table;

import cz.muni.fi.group05.room03.data.HotelSystemDao;
import cz.muni.fi.group05.room03.model.Room;
import cz.muni.fi.group05.room03.model.Reservation;
import cz.muni.fi.group05.room03.data.RoomDao;
import cz.muni.fi.group05.room03.model.Guest;
import cz.muni.fi.group05.room03.ui.HotelSystemUI;
import cz.muni.fi.group05.room03.ui.I18N;
import cz.muni.fi.group05.room03.ui.Menu;
import cz.muni.fi.group05.room03.ui.Message;
import cz.muni.fi.group05.room03.ui.table.util.Column;
import cz.muni.fi.group05.room03.ui.table.util.Table;
import cz.muni.fi.group05.room03.ui.table.util.TableUI;

import javax.swing.JPanel;
import java.awt.event.KeyEvent;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoomTable {

    private static final I18N I18N = new I18N(RoomTable.class);
    private final RoomDao roomDao;
    private final Table table;
    private final TableUI tableUI;

    private final Column<String> key = Column.of(String.class, I18N.getString("key"));
    private final Column<Room.RoomType> type = Column.of(Room.RoomType.class, I18N.getString("type"));
    private final Column<Integer> numberOfBeds = Column.of(Integer.class, I18N.getString("bedNum"));
    private final Column<Room.RoomStatus> status = Column.of(Room.RoomStatus.class, I18N.getString("status"));
    private final Column<Double> price = Column.of(Double.class, I18N.getString("price"));

    public RoomTable() {
        table = new Table(key, type, numberOfBeds, status, price);
        roomDao = HotelSystemDao.getRoomDao();
        roomDao.findAll().forEach(room -> table.addRow(room.getKey(), room.getType(), room.getBeds(), room.getStatus(), room.getPrice()));
        Menu menu = new Menu.Builder()
                .addMenuItem(Menu.createMenuItem(I18N.getString("checkOut"), KeyEvent.VK_O, I18N.getString("checkOutDes"), 'o', this::checkOut), Menu.MenuCondition.MORE_THAN_ZERO)
                .addMenuItem(Menu.createMenuItem(I18N.getString("showInfo"), KeyEvent.VK_S, I18N.getString("showInfoDes"), 's', this::showPersonalizedInfo), Menu.MenuCondition.EXACTLY_ONE)
                .addMenuItem(Menu.createMenuItem(I18N.getString("edit"), KeyEvent.VK_E, I18N.getString("editDes"), 'e', this::roomEditAction), Menu.MenuCondition.EXACTLY_ONE)
                .build();
        tableUI = new TableUI(menu, table);
        tableUI.addDoubleClickAction(this::showPersonalizedInfo);
        tableUI.sortColumnsOnShow(List.of(key));
    }

    public void checkOut() {
        Map<String, Room> rooms = getSelectedRooms().stream().collect(Collectors.toMap(Room::getKey, room -> room));
        List<Guest> guests = HotelSystemDao.getGuestDao().findAll().stream().filter(guest -> rooms.containsKey(guest.getRoom())).collect(Collectors.toList());
        if (guests.size() == 0 || rooms.values().stream().anyMatch(room -> room.getStatus() == Room.RoomStatus.NOT_OCCUPIED)) {
            Message.showWarningDialog(I18N.getString("checkOutNotOccupiedWarn"));
            return;
        }
        long resId = guests.get(0).getReservationId();
        if (guests.stream().anyMatch(guest -> !guest.getReservationId().equals(resId))) {
            Message.showWarningDialog(I18N.getString("sameResRoomsWarn"));
            return;
        }
        Reservation reservation = HotelSystemDao.getReservationDao().findById(resId);
        long duration = ChronoUnit.DAYS.between(reservation.getDateFrom(), reservation.getDateTo());
        double totalPrice = guests.stream().mapToDouble(guest -> rooms.get(guest.getRoom()).getPrice() * duration).sum();
        HotelSystemUI.getGuestTable().deleteGuests(guests);
        int[] selectedRows = tableUI.getSelectedPositions();
        int i = 0;
        for (var room : rooms.values()) {
            room.setStatus(Room.RoomStatus.NOT_OCCUPIED);
            roomDao.update(room);
            table.setValueAt(Room.RoomStatus.NOT_OCCUPIED, selectedRows[i++], status);
        }
        if (HotelSystemDao.getGuestDao().findByResId(resId).isEmpty()) {
            HotelSystemUI.getReservationsTable().checkOutReservation(reservation);
        }
        HotelSystemUI.getFooter().calculate();
        showPrice(guests.size(), totalPrice);
    }

    public void changeStatus(String selectedRoomKey, Room.RoomStatus roomStatus) {
        for (int i = 0; i < table.getRowCount(); i++) {
            String key = table.getRowValue(i, this.key);
            if (key.equals(selectedRoomKey)) {
                Room room = roomDao.findByKey(selectedRoomKey);
                room.setStatus(roomStatus);
                roomDao.update(room);
                table.setValueAt(roomStatus, i, status);
                break;
            }
        }
    }

    public JPanel getPanel() {
        return tableUI.getPanel();
    }

    private void roomEditAction() {
        int selectedRow = tableUI.getSelectedPosition();
        int newNumberOfBeds = table.getRowValue(selectedRow, numberOfBeds);
        int n = Message.showOptions(I18N.getString("numOfBedsInRoom"), I18N.getString("addOrRemoveBedQuestion"),
                I18N.getString("addBed"), I18N.getString("removeBed"));
        if (n == 0) {
            newNumberOfBeds += 1;
        } else if (n == 1) {
            newNumberOfBeds -= 1;
        }
        if (newNumberOfBeds < 1) {
            Message.showWarningDialog(I18N.getString("negativeBedsWarn"));
            return;
        }
        if (newNumberOfBeds > 7) {
            Message.showWarningDialog(I18N.getString("tooManyBeds"));
            return;
        }
        Room room = roomDao.findByKey(table.getRowValue(selectedRow, key));
        if (newNumberOfBeds < room.getBeds() && newNumberOfBeds <
                HotelSystemDao.getGuestDao().findAll().stream()
                        .filter(guest -> room.getKey().equals(guest.getRoom())).count()) {
            Message.showWarningDialog(I18N.getString("full"));
            return;
        }
        table.setValueAt(newNumberOfBeds, selectedRow, numberOfBeds);
        room.setBeds(newNumberOfBeds);
        roomDao.update(room);
    }

    private String getStringAboutGuest(Guest guest) {
        StringBuilder stringBuilder = new StringBuilder().append("NAME = ")
                .append(guest.getName()).append(", AGE = ").append(guest.getGeneration());
        String idCard = guest.getIdCard();
        String info = guest.getInfo();
        if (!idCard.isBlank()) {
            stringBuilder.append(", ID CARD = ").append(idCard);
        }
        if (!info.isBlank()) {
            stringBuilder.append(", INFO = ").append(TableUI.truncatedString(info));
        }
        return stringBuilder.toString();
    }

    private void showPersonalizedInfo() {
        Room room = getSelectedRoom();
        String key = room.getKey();
        List<Guest> guests = HotelSystemDao.getGuestDao().findByRoomKey(key);
        StringBuilder info = new StringBuilder();
        if (guests.isEmpty()) {
            info.append(I18N.getString("noGuests")).append(" ").append(key);
        } else {
            info.append(I18N.getString("guestsInRoom")).append(" ").append(key).append(":").append(System.lineSeparator());
            for (Guest guest : guests) {
                info.append(getStringAboutGuest(guest)).append("\n");
            }
            long resId = guests.get(0).getReservationId();
            int tax = Integer.parseInt(HotelSystemDao.getImportantDataDao().findByKey("TAX"));
            Reservation reservation = HotelSystemDao.getReservationDao().findById(resId);
            double totalPrice = room.getPrice() * ChronoUnit.DAYS.between(reservation.getDateFrom(), reservation.getDateTo());
            info.append(I18N.getString("priceWithoutTax")).append(": ").append(String.format("%.2f", totalPrice)).append(System.lineSeparator());
            final double taxVal = (totalPrice / 100) * tax;
            totalPrice = totalPrice + taxVal;

            info.append(I18N.getString("tax")).append(" (").append(tax).append("%): ").append(String.format("%.2f", taxVal)).append(System.lineSeparator());
            info.append(I18N.getString("totalPrice")).append(": ").append(String.format("%.2f", totalPrice)).append(System.lineSeparator());
        }
        Message.showInformationDialog(I18N.getString("infoAboutRoom"), info.toString());
    }

    private Room getRowRoom(int row) {
        return new Room(
                table.getRowValue(row, key),
                table.getRowValue(row, type),
                table.getRowValue(row, numberOfBeds),
                table.getRowValue(row, status),
                table.getRowValue(row, price));
    }

    private Room getSelectedRoom() {
        return getRowRoom(tableUI.getSelectedPosition());
    }

    private List<Room> getSelectedRooms() {
        return Arrays.stream(tableUI.getSelectedPositions()).mapToObj(this::getRowRoom).collect(Collectors.toList());
    }

    private void showPrice(int people, double price) {
        int tax = Integer.parseInt(HotelSystemDao.getImportantDataDao().findByKey("TAX"));
        double taxVal = price * tax / 100;
        String info = I18N.getString("numOfPeople") + ":\t" +
                people + "\n" +
                I18N.getString("priceWithoutTax") + ":\t" +
                String.format("%.2f", price) + "\n" +
                I18N.getString("tax") + " (" + tax + "%):\t" +
                String.format("%.2f", taxVal) + "\n" +
                I18N.getString("totalPrice") + ":\t" +
                String.format("%.2f", price + taxVal) + "\n";
        Message.showInformationDialog(I18N.getString("checkOut"), info);
    }
}
