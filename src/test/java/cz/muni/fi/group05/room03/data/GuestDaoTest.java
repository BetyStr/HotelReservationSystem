package cz.muni.fi.group05.room03.data;

import cz.muni.fi.group05.room03.model.Guest;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GuestDaoTest {

    private static EmbeddedDataSource dataSource;
    private GuestDao guestDao;

    @BeforeAll
    static void initTestDataSource() {
        dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:guest-test");
        dataSource.setCreateDatabase("create");
    }

    @BeforeEach
    void createReservationDao() throws SQLException {
        guestDao = new GuestDao(dataSource);
        try (var connection = dataSource.getConnection(); var st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM APP.GUEST");
        }
    }

    @AfterEach
    void cleanUp() {
        guestDao.dropTable();
    }

    private GuestDao createFailingDao(Throwable exceptionToBeThrown) {
        try {
            var dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenAnswer(i -> GuestDaoTest.dataSource.getConnection());
            var guestDao = new GuestDao(dataSource);
            when(dataSource.getConnection()).thenThrow(exceptionToBeThrown);
            return guestDao;
        } catch (SQLException ex) {
            throw new RuntimeException("Mock configuration failed", ex);
        }
    }

    private GuestDao createDaoWithBadGeneratedKeys() {
        try {
            var resultSet = mock(ResultSet.class);
            when(resultSet.next()).thenReturn(false);

            var preparedStatement = mock(PreparedStatement.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);

            var connection = mock(Connection.class);
            when(connection.prepareStatement(
                    "INSERT INTO GUEST (FULLNAME, ROOM, ID_CARD, AGE, INFO, RES_ID) VALUES (?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS)).thenReturn(preparedStatement);

            var dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenAnswer(i -> GuestDaoTest.dataSource.getConnection());

            var guestDao = new GuestDao(dataSource);
            when(dataSource.getConnection()).thenReturn(connection);
            return guestDao;
        } catch (SQLException ex) {
            throw new RuntimeException("Mock configuration failed", ex);
        }
    }

    @Test
    void createGuest() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        guestDao.create(guest);

        assertThat(guest.getId()).isNotNull();
        assertThat(guestDao.findAll()).usingFieldByFieldElementComparator()
                .containsExactly(guest);
    }

    @Test
    void createGuestWithException() {
        var sqlException = new SQLException();
        var failingDao = createFailingDao(sqlException);

        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.create(guest))
                .withMessage("Failed to store guest " + guest)
                .withCause(sqlException);
    }

    @Test
    void createWithBadGeneratedKeys() {
        var guestDaoWithBadKeys = createDaoWithBadGeneratedKeys();
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> guestDaoWithBadKeys.create(guest))
                .withMessage("Failed to fetch generated key: no key returned for guest: " + guest);
    }

    @Test
    void createWithExistingId() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        guestDao.create(guest);
        var guest2 = new Guest(guest.getName(), guest.getRoom(), "1589", guest.getGeneration(), guest.getInfo(), guest.getReservationId(), guest.getId());
        guestDao.create(guest2);

        assertThat(guestDao.findAll()).usingFieldByFieldElementComparator()
                .containsExactly(guest2);
    }

    @Test
    void deleteGuest() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        guestDao.create(guest);
        guestDao.delete(guest);
        assertThat(guestDao.findAll()).isEmpty();
    }

    @Test
    void deleteNonExistingGuest() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        guest.setId(123L);
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> guestDao.delete(guest))
                .withMessage("Failed to delete non-existing guest: " + guest);
    }

    @Test
    void deleteGuestWithNoId() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> guestDao.delete(guest))
                .withMessage("Guest has null ID" + guest);
    }

    @Test
    void deleteGuestWithException() {
        var sqlException = new SQLException();
        GuestDao failingDao = createFailingDao(sqlException);

        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        guest.setId(123L);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.delete(guest))
                .withMessage("Failed to delete guest: " + guest)
                .withCause(sqlException);
    }

    @Test
    void updateGuest() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        var guest2 = new Guest("Malina Malinova", "102", "1235", Guest.GuestGeneration.CHILD, "nieco ine", 2L);

        guestDao.create(guest);
        guestDao.create(guest2);

        var guest3 = new Guest(guest.getName(), "103", guest.getIdCard(), guest.getGeneration(), guest.getInfo(), guest.getReservationId(), guest.getId());
        guestDao.update(guest3);

        assertThat(guestDao.findAll()).usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(guest2, guest3);
    }

    @Test
    void updateGuestWithNullId() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> guestDao.update(guest))
                .withMessage("Guest has null ID: " + guest);
    }

    @Test
    void updateNonExistingGuest() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        guest.setId(123L);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> guestDao.update(guest))
                .withMessage("Failed to update non-existing guest: " + guest);
    }

    @Test
    void updateGuestWithException() {
        var sqlException = new SQLException();
        GuestDao failingDao = createFailingDao(sqlException);

        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        guest.setId(123L);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.update(guest))
                .withMessage("Failed to update guest: " + guest)
                .withCause(sqlException);
    }

    @Test
    void findAllGuestsEmpty() {
        assertThat(guestDao.findAll()).isEmpty();
    }

    @Test
    void findAllGuests() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        var guest2 = new Guest("Malina Malinova", "102", "1235", Guest.GuestGeneration.CHILD, "nieco ine", 2L);
        var guest3 = new Guest("Jablko Jablkove", "103", "1236", Guest.GuestGeneration.ADULT, "este nieco ine", 3L);

        guestDao.create(guest);
        guestDao.create(guest2);
        guestDao.create(guest3);

        assertThat(guestDao.findAll())
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(guest, guest2, guest3);
    }

    @Test
    void findAllGuestsWithException() {
        var sqlException = new SQLException();
        GuestDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::findAll)
                .withMessage("Failed to load all guests")
                .withCause(sqlException);
    }

    @Test
    void findByRoomKeyEmpty() {
        assertThat(guestDao.findByRoomKey("101")).isEmpty();
    }

    @Test
    void findByRoomKey() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        var guest2 = new Guest("Malina Malinova", "101", "1235", Guest.GuestGeneration.CHILD, "nieco ine", 2L);
        var guest3 = new Guest("Jablko Jablkove", "103", "1236", Guest.GuestGeneration.ADULT, "este nieco ine", 3L);

        guestDao.create(guest);
        guestDao.create(guest2);
        guestDao.create(guest3);

        assertThat(guestDao.findByRoomKey("101"))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(guest, guest2);
        assertThat(guestDao.findByRoomKey("103"))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(guest3);
    }

    @Test
    void findByRoomKeyWithException() {
        var sqlException = new SQLException();
        GuestDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.findByRoomKey("101"))
                .withMessage("Failed to load all guests")
                .withCause(sqlException);
    }

    @Test
    void findByResIdEmpty() {
        assertThat(guestDao.findByResId(1L)).isEmpty();
    }

    @Test
    void findByResId() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        var guest2 = new Guest("Malina Malinova", "101", "1235", Guest.GuestGeneration.CHILD, "nieco ine", 1L);
        var guest3 = new Guest("Jablko Jablkove", "103", "1236", Guest.GuestGeneration.ADULT, "este nieco ine", 3L);

        guestDao.create(guest);
        guestDao.create(guest2);
        guestDao.create(guest3);

        assertThat(guestDao.findByResId(1L))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(guest, guest2);
        assertThat(guestDao.findByResId(3L))
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(guest3);
    }

    @Test
    void findByResIdWithException() {
        var sqlException = new SQLException();
        GuestDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.findByResId(1L))
                .withMessage("Failed to load all guests")
                .withCause(sqlException);
    }

    @Test
    void countAll() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        var guest2 = new Guest("Malina Malinova", "101", "1235", Guest.GuestGeneration.CHILD, "nieco ine", 1L);
        var guest3 = new Guest("Jablko Jablkove", "103", "1236", Guest.GuestGeneration.ADULT, "este nieco ine", 3L);

        guestDao.create(guest);
        guestDao.create(guest2);
        guestDao.create(guest3);

        assertThat(guestDao.countAll()).isEqualTo(3);
    }

    @Test
    void countAllWithException() {
        var sqlException = new SQLException();
        GuestDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::countAll)
                .withMessage("Failed to count all guests")
                .withCause(sqlException);
    }

    @Test
    void countAllWithRoom() {
        var guest = new Guest("Hruska Hruskova", "101", "1234", Guest.GuestGeneration.ADULT, "nieco", 1L);
        var guest2 = new Guest("Malina Malinova", "", "1235", Guest.GuestGeneration.CHILD, "nieco ine", 1L);
        var guest3 = new Guest("Jablko Jablkove", "103", "1236", Guest.GuestGeneration.ADULT, "este nieco ine", 3L);

        guestDao.create(guest);
        guestDao.create(guest2);
        guestDao.create(guest3);

        assertThat(guestDao.countAllWithRoom()).isEqualTo(2);
    }

    @Test
    void countAllWithRoomWithException() {
        var sqlException = new SQLException();
        GuestDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::countAllWithRoom)
                .withMessage("Failed to count guests with room")
                .withCause(sqlException);
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
                .withMessage("Failed to create GUEST table");
    }

    @Test
    void dropTableWithException() {
        var sqlException = new SQLException();
        var failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::dropTable)
                .withMessage("Failed to drop GUEST table");
    }
}
