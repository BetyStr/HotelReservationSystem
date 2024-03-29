package cz.muni.fi.group05.room03.ui;

import cz.muni.fi.group05.room03.data.HotelSystemDao;
import cz.muni.fi.group05.room03.model.Room;

import javax.swing.JLabel;

public class Footer {

    private static final I18N I18N = new I18N(Footer.class);
    private final JLabel label;

    public Footer(JLabel label) {
        this.label = label;
        calculate();
    }

    public JLabel getLabel() {
        return label;
    }

    public void calculate() {
        label.setText((I18N.getString("allGuests") + " ") +
                HotelSystemDao.getGuestDao().countAllWithRoom() +
                "    |    " +
                (I18N.getString("allRooms") + " ") +
                HotelSystemDao.getRoomDao().countWithStatus(Room.RoomStatus.OCCUPIED) +
                " / " + HotelSystemDao.getRoomDao().countAll());
    }
}
