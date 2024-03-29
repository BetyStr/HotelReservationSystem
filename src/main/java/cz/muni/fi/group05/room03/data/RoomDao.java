package cz.muni.fi.group05.room03.data;

import cz.muni.fi.group05.room03.model.Room;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class RoomDao {

    private final DataSource dataSource;

    public RoomDao(DataSource dataSource) {
        this.dataSource = dataSource;
        initTable();
    }

    public void create(Room room) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                     "INSERT INTO ROOM (NUMBER, TYPE, NUMBER_OF_BEDS, STATUS, PRICE) VALUES (?, ?, ?, ?, ?)",
                     RETURN_GENERATED_KEYS)) {
            st.setString(1, room.getKey());
            st.setString(2, room.getType().name());
            st.setInt(3, room.getBeds());
            st.setString(4, room.getStatus().name());
            st.setString(5, room.getPrice().toString());
            st.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to store room " + room, ex);
        }
    }

    public void update(Room room) {
        try {
            var connection = dataSource.getConnection();
            String format = String.format("UPDATE ROOM SET NUMBER = '%s', TYPE = '%s'" +
                            ", NUMBER_OF_BEDS = %d, STATUS = '%s', PRICE = '%s' WHERE NUMBER = '%s'",
                    room.getKey(), room.getType().name(), room.getBeds(), room.getStatus().name(), room.getPrice().toString(), room.getKey());
            var st = connection.prepareStatement(format);
            int rowsChange = st.executeUpdate();
            if (rowsChange == 0) {
                throw new DataAccessException("Failed to update non-existing room: " + room);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update room", e);
        }
    }

    public List<Room> findAll() {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT * FROM ROOM")) {
            List<Room> rooms = new ArrayList<>();
            try (var rs = st.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room(
                            rs.getString("NUMBER"),
                            Room.RoomType.valueOf(rs.getString("TYPE")),
                            rs.getInt("NUMBER_OF_BEDS"),
                            Room.RoomStatus.valueOf(rs.getString("STATUS")),
                            rs.getDouble("PRICE")
                            );
                    rooms.add(room);
                }
            }
            return rooms;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to load all rooms", ex);
        }
    }

    public Room findByKey(String key) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT * FROM ROOM WHERE NUMBER = '"  + key + "'")) {
            Room room = null;
            try (var rs = st.executeQuery()) {
                if (rs.next()) {
                    room = new Room(
                            rs.getString("NUMBER"),
                            Room.RoomType.valueOf(rs.getString("TYPE")),
                            rs.getInt("NUMBER_OF_BEDS"),
                            Room.RoomStatus.valueOf(rs.getString("STATUS")),
                            rs.getDouble("PRICE"));
                }
            }
            return room;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to load all rooms", ex);
        }
    }

    public int countWithStatus(Room.RoomStatus status) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT COUNT(*) AS count FROM ROOM WHERE STATUS = " + "'"  + status.name() + "'")) {
            int count = 0;
            try (var rs = st.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("count");
                }
            }
            return count;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to count rooms with status " + status, ex);
        }
    }

    public int countAll() {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT COUNT(*) AS count FROM ROOM")) {
            int count = 0;
            try (var rs = st.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("count");
                }
            }
            return count;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to count all rooms", ex);
        }
    }

    private void initTable() {
        if (!tableExits("APP", "ROOM")) {
            createTable();
            create(new Room("101A", Room.RoomType.FAMILY, 6, Room.RoomStatus.NOT_OCCUPIED, 120.0));
            create(new Room("101B", Room.RoomType.FAMILY, 6, Room.RoomStatus.NOT_OCCUPIED, 120.0));
            create(new Room("102B", Room.RoomType.DOUBLE, 5, Room.RoomStatus.NOT_OCCUPIED, 100.50));
            create(new Room("103", Room.RoomType.DOUBLE, 5, Room.RoomStatus.NOT_OCCUPIED, 100.50));
            create(new Room("104", Room.RoomType.SINGLE, 2, Room.RoomStatus.NOT_OCCUPIED, 70.0));
            create(new Room("105", Room.RoomType.SINGLE, 2, Room.RoomStatus.NOT_OCCUPIED, 70.0));
            create(new Room("201", Room.RoomType.SINGLE, 2, Room.RoomStatus.NOT_OCCUPIED, 70.0));
            create(new Room("202", Room.RoomType.FAMILY, 5, Room.RoomStatus.NOT_OCCUPIED, 110.0));
            create(new Room("203", Room.RoomType.FAMILY, 4, Room.RoomStatus.NOT_OCCUPIED, 105.50));
            create(new Room("204", Room.RoomType.FAMILY, 4, Room.RoomStatus.NOT_OCCUPIED, 105.50));
            create(new Room("205", Room.RoomType.DOUBLE, 3, Room.RoomStatus.NOT_OCCUPIED, 90.0));
            create(new Room("301", Room.RoomType.SINGLE, 1, Room.RoomStatus.NOT_OCCUPIED, 50.0));
            create(new Room("302", Room.RoomType.FAMILY, 3, Room.RoomStatus.NOT_OCCUPIED, 90.0));
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
            st.executeUpdate("CREATE TABLE APP.ROOM (" +
                    "NUMBER VARCHAR(7) PRIMARY KEY NOT NULL," +
                    "TYPE VARCHAR(20) NOT NULL CONSTRAINT TYPE_CHECK CHECK (TYPE IN ('FAMILY', 'SINGLE', 'DOUBLE'))," +
                    "NUMBER_OF_BEDS INT NOT NULL," +
                    "STATUS VARCHAR(20) NOT NULL CONSTRAINT STATUS_CHECK CHECK (STATUS IN ('NOT_OCCUPIED', 'OCCUPIED'))," +
                    "PRICE VARCHAR(10) NOT NULL" +
                    ")");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create ROOM table", ex);
        }
    }

    public void dropTable() {
        try (var connection = dataSource.getConnection();
             var st = connection.createStatement()) {
            st.executeUpdate("DROP TABLE APP.ROOM");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to drop ROOM table", ex);
        }
    }
}
