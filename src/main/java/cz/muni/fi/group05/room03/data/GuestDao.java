package cz.muni.fi.group05.room03.data;

import cz.muni.fi.group05.room03.model.Guest;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class GuestDao {

    private final DataSource dataSource;

    public GuestDao(DataSource dataSource) {
        this.dataSource = dataSource;
        initTable();
    }

    public void create(Guest guest) {
        if (guest.getId() != null) {
            update(guest);
            return;
        }
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                     "INSERT INTO GUEST (FULLNAME, ROOM, ID_CARD, AGE, INFO, RES_ID) VALUES (?, ?, ?, ?, ?, ?)",
                     RETURN_GENERATED_KEYS)) {
            st.setString(1, guest.getName());
            st.setString(2, guest.getRoom());
            st.setString(3, guest.getIdCard());
            st.setString(4, guest.getGeneration().name());
            st.setString(5, guest.getInfo());
            st.setLong(6, guest.getReservationId());
            st.executeUpdate();
            try (var rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    guest.setId(rs.getLong(1));
                } else {
                    throw new DataAccessException("Failed to fetch generated key: no key returned for guest: " + guest);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to store guest " + guest, ex);
        }
    }

    public void delete(Guest guest) {
        if (guest.getId() == null) {
            throw new IllegalArgumentException("Guest has null ID" + guest);
        }
        try {
            var connection = dataSource.getConnection();
            var st = connection.prepareStatement("DELETE FROM GUEST WHERE ID =" + guest.getId());
            int rowsChange = st.executeUpdate();
            if (rowsChange == 0) {
                throw new DataAccessException("Failed to delete non-existing guest: " + guest);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete guest: " + guest, e);
        }
    }

    public void update(Guest guest) {
        if (guest.getId() == null) {
            throw new IllegalArgumentException("Guest has null ID: " + guest);
        }
        try {
            var connection = dataSource.getConnection();
            String name = guest.getName();
            String room = guest.getRoom();
            String idCard = guest.getIdCard();
            String generation = guest.getGeneration().name();
            String info = guest.getInfo();
            Long resId = guest.getReservationId();
            Long guestId = guest.getId();
            var st = connection.prepareStatement("UPDATE GUEST SET " +
                    "FULLNAME = " + "'" + name + "'" +
                    ", ROOM = " + "'" + room + "'" +
                    ", ID_CARD = " + "'" + idCard + "'" +
                    ", AGE = " + "'" + generation + "'" +
                    ", INFO = " + "'" + info +  "'" +
                    ", RES_ID = " + resId +
                    " WHERE ID = " + guestId
            );
            int rowsChange = st.executeUpdate();
            if (rowsChange == 0) {
                throw new DataAccessException("Failed to update non-existing guest: " + guest);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update guest: " + guest, e);
        }
    }

    public List<Guest> findAll() {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT * FROM GUEST")) {
            return getGuests(st);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to load all guests", ex);
        }
    }

    public List<Guest> findByRoomKey(String roomKey) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT * FROM GUEST WHERE ROOM = '" + roomKey + "'")) {
            return getGuests(st);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to load all guests", ex);
        }
    }

    public List<Guest> findByResId(Long id) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT * FROM GUEST WHERE RES_ID = " + id)) {
            return getGuests(st);
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to load all guests", ex);
        }
    }

    private List<Guest> getGuests(PreparedStatement st) throws SQLException {
        List<Guest> guests = new ArrayList<>();
        try (var rs = st.executeQuery()) {
            while (rs.next()) {
                Guest guest = new Guest(
                        rs.getString("FULLNAME"),
                        rs.getString("ROOM"),
                        rs.getString("ID_CARD"),
                        Guest.GuestGeneration.valueOf(rs.getString("AGE")),
                        rs.getString("INFO"),
                        rs.getLong("RES_ID"));
                guest.setId(rs.getLong("ID"));
                guests.add(guest);
            }
        }
        return guests;
    }

    public int countAll() {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT COUNT(*) AS count FROM GUEST")) {
            int count = 0;
            try (var rs = st.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("count");
                }
            }
            return count;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to count all guests", ex);
        }
    }

    public int countAllWithRoom() {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                     "SELECT COUNT(*) AS count FROM GUEST WHERE ROOM <> ''")) {
            int count = 0;
            try (var rs = st.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("count");
                }
            }
            return count;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to count guests with room", ex);
        }
    }

    private void initTable() {
        if (!tableExits("APP", "GUEST")) {
            createTable();
        }
    }

    protected boolean tableExits(String schemaName, String tableName) {
        try (var connection = dataSource.getConnection();
             var rs = connection.getMetaData().getTables(null, schemaName, tableName, null)) {
            return rs.next();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to detect if the table " + schemaName + "." + tableName + " exist", ex);
        }
    }

    protected void createTable() {
        try (var connection = dataSource.getConnection();
             var st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE APP.GUEST (" +
                    "ID BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "FULLNAME VARCHAR(100) NOT NULL," +
                    "ROOM VARCHAR(10) NOT NULL," +
                    "ID_CARD VARCHAR(20) NOT NULL," +
                    "AGE VARCHAR(20) NOT NULL CONSTRAINT GENERATION_CHECK CHECK (AGE IN ('ADULT', 'CHILD'))," +
                    "INFO VARCHAR(1000)," +
                    "RES_ID BIGINT" +
                    ")");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create GUEST table", ex);
        }
    }

    public void dropTable() {
        try (var connection = dataSource.getConnection();
             var st = connection.createStatement()) {
            st.executeUpdate("DROP TABLE APP.GUEST");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to drop GUEST table", ex);
        }
    }
}
