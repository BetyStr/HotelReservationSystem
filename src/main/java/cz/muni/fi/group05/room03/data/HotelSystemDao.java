package cz.muni.fi.group05.room03.data;

import org.apache.derby.jdbc.EmbeddedDataSource;
import javax.sql.DataSource;

public class HotelSystemDao {

    private static HotelSystemDao instance;
    private final GuestDao guestDao;
    private final ReservationDao reservationDao;
    private final RoomDao roomDao;
    private final ImportantDataDao importantDataDao;

    private HotelSystemDao() {
        final DataSource dataSource = createDataSource();
        importantDataDao = new ImportantDataDao(dataSource);
        roomDao = new RoomDao(dataSource);
        guestDao = new GuestDao(dataSource);
        reservationDao = new ReservationDao(dataSource);
    }

    public static void create() {
        if (instance != null)
            throw new UnsupportedOperationException("HotelSystemDao Error: HotelSystemDao is already created!");
        instance = new HotelSystemDao();
    }

    public static ImportantDataDao getImportantDataDao() {
        return getAssertedInstance().importantDataDao;
    }

    public static GuestDao getGuestDao() {
        return getAssertedInstance().guestDao;
    }

    public static ReservationDao getReservationDao() {
        return getAssertedInstance().reservationDao;
    }

    public static RoomDao getRoomDao() {
        return getAssertedInstance().roomDao;
    }

    private static HotelSystemDao getAssertedInstance() {
        if (instance == null)
            throw new UnsupportedOperationException("HotelSystemDao Error: HotelSystemDao is not yet created, call create() method first!");
        return instance;
    }

    private DataSource createDataSource() {
        String dbPath = System.getProperty("user.home") + "/hotel-evidence";
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName(dbPath);
        dataSource.setCreateDatabase("create");
        return dataSource;
    }
}
