package cz.muni.fi.group05.room03.data;

import cz.muni.fi.group05.room03.model.Room;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoomDaoTest {

    private static EmbeddedDataSource dataSource;
    private RoomDao roomDao;

    @BeforeAll
    static void initTestDataSource() {
        dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:room-test");
        dataSource.setCreateDatabase("create");
    }

    @BeforeEach
    void createRoomDao() throws SQLException {
        roomDao = new RoomDao(dataSource);
        try (var connection = dataSource.getConnection(); var st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM APP.ROOM");
        }
    }

    @AfterEach
    void cleanUp() {
        roomDao.dropTable();
    }

    @Test
    void tableExistsWithException() {
        var sqlException = new SQLException();
        var failingDao = createFailingDao(sqlException);

        String any = "any";
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.tableExits(any, any))
                .withMessage("Failed to detect if the table " + any + "." + any + " exist");
    }

    @Test
    void createTableWithException() {
        var sqlException = new SQLException();
        var failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::createTable)
                .withMessage("Failed to create ROOM table");
    }

    @Test
    void dropTableWithException() {
        var sqlException = new SQLException();
        var failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::dropTable)
                .withMessage("Failed to drop ROOM table");
    }

    @Test
    void createRoom() {
        var room = new Room("007", Room.RoomType.DOUBLE, 2, Room.RoomStatus.NOT_OCCUPIED, 73.6);
        roomDao.create(room);
        assertThat(roomDao.findByKey(room.getKey())).isNotSameAs(room).isEqualToComparingFieldByField(room);
    }

    @Test
    void createRoomWithException() {
        var sqlException = new SQLException();
        var failingDao = createFailingDao(sqlException);

        var room = new Room("007", Room.RoomType.DOUBLE, 2, Room.RoomStatus.NOT_OCCUPIED, 73.6);
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.create(room))
                .withMessage("Failed to store room " + room)
                .withCause(sqlException);
    }

    @Test
    void findAllEmpty() {
        assertThat(roomDao.findAll()).isEmpty();
    }

    @Test
    void findAll() {
        var room1 = new Room("007", Room.RoomType.DOUBLE, 2, Room.RoomStatus.NOT_OCCUPIED, 73.6);
        var room2 = new Room("073", Room.RoomType.DOUBLE, 3, Room.RoomStatus.NOT_OCCUPIED, 122.3);
        var room3 = new Room("122", Room.RoomType.FAMILY, 5, Room.RoomStatus.NOT_OCCUPIED, 713.2);

        roomDao.create(room1);
        roomDao.create(room2);
        roomDao.create(room3);

        assertThat(roomDao.findAll())
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(room1, room2, room3);
    }

    @Test
    void findAllWithException() {
        var sqlException = new SQLException();
        RoomDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::findAll)
                .withMessage("Failed to load all rooms")
                .withCause(sqlException);
    }

    @Test
    void findByKeyWithException() {
        var sqlException = new SQLException();
        RoomDao failingDao = createFailingDao(sqlException);

        String key = "777";
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.findByKey(key))
                .withMessage("Failed to load all rooms")
                .withCause(sqlException);
    }

    @Test
    void findByKey() {
        var room1 = new Room("007", Room.RoomType.DOUBLE, 2, Room.RoomStatus.NOT_OCCUPIED, 73.6);
        var room2 = new Room("073", Room.RoomType.DOUBLE, 3, Room.RoomStatus.NOT_OCCUPIED, 122.3);

        roomDao.create(room1);
        roomDao.create(room2);

        var id = room1.getKey();
        assertThat(roomDao.findByKey(id))
                .isNotSameAs(room1).isEqualToComparingFieldByField(room1);
    }

    @Test
    void update() {
        var room1 = new Room("007", Room.RoomType.SINGLE, 2, Room.RoomStatus.NOT_OCCUPIED, 73.6);
        var room2 = new Room("777", Room.RoomType.DOUBLE, 5, Room.RoomStatus.NOT_OCCUPIED, 373.6);

        roomDao.create(room1);
        roomDao.create(room2);
        room1.setStatus(Room.RoomStatus.OCCUPIED);
        room1.setBeds(3);
        roomDao.update(room1);

        assertThat(roomDao.findByKey(room1.getKey()))
                .isEqualToComparingFieldByField(room1);
        assertThat(roomDao.findByKey(room2.getKey()))
                .isEqualToComparingFieldByField(room2);
    }

    @Test
    void updateNonExisting() {
        var room = new Room("007", Room.RoomType.DOUBLE, 2, Room.RoomStatus.NOT_OCCUPIED, 73.6);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> roomDao.update(room))
                .withMessage("Failed to update non-existing room: " + room);
    }

    @Test
    void updateWithException() {
        var sqlException = new SQLException();
        RoomDao failingDao = createFailingDao(sqlException);

        var room = new Room("007", Room.RoomType.DOUBLE, 2, Room.RoomStatus.NOT_OCCUPIED, 73.6);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.update(room))
                .withMessage("Failed to update room")
                .withCause(sqlException);
    }

    @Test
    void countWithStatus() {
        var room1 = new Room("007", Room.RoomType.DOUBLE, 2, Room.RoomStatus.NOT_OCCUPIED, 73.6);
        var room2 = new Room("009", Room.RoomType.DOUBLE, 2, Room.RoomStatus.NOT_OCCUPIED, 75.6);
        roomDao.create(room1);
        roomDao.create(room2);
        MatcherAssert.assertThat(roomDao.countWithStatus(Room.RoomStatus.NOT_OCCUPIED), equalTo(2));
        MatcherAssert.assertThat(roomDao.countWithStatus(Room.RoomStatus.OCCUPIED), equalTo(0));
        room1.setBeds(3);
        roomDao.update(room1);
        MatcherAssert.assertThat(roomDao.countWithStatus(Room.RoomStatus.NOT_OCCUPIED), equalTo(2));
        MatcherAssert.assertThat(roomDao.countWithStatus(Room.RoomStatus.OCCUPIED), equalTo(0));
        room2.setStatus(Room.RoomStatus.OCCUPIED);
        roomDao.update(room2);
        MatcherAssert.assertThat(roomDao.countWithStatus(Room.RoomStatus.NOT_OCCUPIED), equalTo(1));
        MatcherAssert.assertThat(roomDao.countWithStatus(Room.RoomStatus.OCCUPIED), equalTo(1));
    }

    @Test
    void countWithStatusWithException() {
        var sqlException = new SQLException();
        RoomDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.countWithStatus(Room.RoomStatus.NOT_OCCUPIED))
                .withMessage("Failed to count rooms with status " + Room.RoomStatus.NOT_OCCUPIED)
                .withCause(sqlException);
    }

    @Test
    void countAll() {
        var room1 = new Room("007", Room.RoomType.DOUBLE, 2, Room.RoomStatus.NOT_OCCUPIED, 73.6);
        var room2 = new Room("009", Room.RoomType.DOUBLE, 2, Room.RoomStatus.NOT_OCCUPIED, 75.6);
        roomDao.create(room1);
        roomDao.create(room2);
        MatcherAssert.assertThat(roomDao.countAll(), equalTo(2));
        room1.setBeds(3);
        roomDao.update(room1);
        MatcherAssert.assertThat(roomDao.countAll(), equalTo(2));
        MatcherAssert.assertThat(roomDao.countWithStatus(Room.RoomStatus.OCCUPIED), equalTo(0));
        var room3 = new Room("019", Room.RoomType.FAMILY, 5, Room.RoomStatus.NOT_OCCUPIED, 175.6);
        roomDao.create(room3);
        MatcherAssert.assertThat(roomDao.countAll(), equalTo(3));
    }

    @Test
    void countAllWithException() {
        var sqlException = new SQLException();
        RoomDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::countAll)
                .withMessage("Failed to count all rooms")
                .withCause(sqlException);
    }

    private RoomDao createFailingDao(Throwable exceptionToBeThrown) {
        try {
            var dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenAnswer(i -> RoomDaoTest.dataSource.getConnection());
            var roomDao = new RoomDao(dataSource);
            when(dataSource.getConnection()).thenThrow(exceptionToBeThrown);
            return roomDao;
        } catch (SQLException ex) {
            throw new RuntimeException("Mock configuration failed", ex);
        }
    }
}
