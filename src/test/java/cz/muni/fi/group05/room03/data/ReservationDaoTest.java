package cz.muni.fi.group05.room03.data;

import cz.muni.fi.group05.room03.model.Reservation;
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
import java.time.LocalDate;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReservationDaoTest {

    private static EmbeddedDataSource dataSource;
    private ReservationDao reservationDao;

    @BeforeAll
    static void initTestDataSource() {
        dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:reservation-test");
        dataSource.setCreateDatabase("create");
    }

    @BeforeEach
    void createReservationDao() throws SQLException {
        reservationDao = new ReservationDao(dataSource);
        try (var connection = dataSource.getConnection(); var st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM APP.RESERVATION");
        }
    }

    @AfterEach
    void cleanUp() {
        reservationDao.dropTable();
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
                .withMessage("Failed to create RESERVATION table");
    }

    @Test
    void dropTableWithException() {
        var sqlException = new SQLException();
        var failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::dropTable)
                .withMessage("Failed to drop RESERVATION table");
    }

    @Test
    void createReservation() {
        var res = new Reservation("Hruska Hruskova", LocalDate.now(), LocalDate.of(2021,9,17), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.UPCOMING);
        reservationDao.create(res);

        assertThat(res.getId()).isNotNull();
        assertThat(reservationDao.findById(res.getId())).isNotSameAs(res).isEqualToComparingFieldByField(res);
    }
    @Test
    void createReservationWithExistingId() {
        var res = new Reservation("Hruska Hruskova", LocalDate.now(), LocalDate.of(2021,9,17), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.UPCOMING);
        reservationDao.create(res);
        var res2 = new Reservation(res.getName(), res.getDateFrom(), res.getDateTo(), res.getTelephone(), "tada@gmail.com", res.getPeople(), res.getInfo(), res.getState(), res.getId());
        reservationDao.create(res2);

        assertThat(reservationDao.findById(res.getId()))
                .isEqualToComparingFieldByField(res2);
    }

    @Test
    void createReservationWithException() {
        var sqlException = new SQLException();
        var failingDao = createFailingDao(sqlException);

        var res = new Reservation("Hruska Hruskova", LocalDate.now(), LocalDate.of(2021,9,17), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.UPCOMING);
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.create(res))
                .withMessage("Failed to store reservation " + res)
                .withCause(sqlException);
    }

    @Test
    void createWithBadGeneratedKeys() {
        var reservationDaoWithBadKeys = createDaoWithBadGeneratedKeys();
        var res = new Reservation("Hruska Hruskova", LocalDate.now(), LocalDate.of(2021,9,17), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.UPCOMING);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> reservationDaoWithBadKeys.create(res))
                .withMessage("Failed to fetch generated key: no key returned for reservation: " + res);
    }

    @Test
    void findAllEmpty() {
        assertThat(reservationDao.findAll()).isEmpty();
    }

    @Test
    void findAll() {
        var res1 = new Reservation("Kiwi Kiwiove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.DOING);
        var res2 = new Reservation("Mango Mangove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 4, "", Reservation.ReservationState.DOING);
        var res3 = new Reservation("Mandarinka Mandarinkova", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 2, "", Reservation.ReservationState.DOING);

        reservationDao.create(res1);
        reservationDao.create(res2);
        reservationDao.create(res3);

        assertThat(reservationDao.findAll())
                .usingFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(res1, res2, res3);
    }

    @Test
    void findAllWithException() {
        var sqlException = new SQLException();
        ReservationDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::findAll)
                .withMessage("Failed to load all reservations")
                .withCause(sqlException);
    }

    @Test
    void findByIdWithException() {
        var sqlException = new SQLException();
        ReservationDao failingDao = createFailingDao(sqlException);

        Long id = 123L;
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.findById(id))
                .withMessage("Failed to load reservation with id: " + id)
                .withCause(sqlException);
    }

    @Test
    void update() {
        var res1 = new Reservation("Kiwi Kiwiove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.DOING);
        var res2 = new Reservation("Mango Mangove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 4, "", Reservation.ReservationState.DOING);

        reservationDao.create(res1);
        reservationDao.create(res2);

        var res1b = new Reservation(res1.getName(), res1.getDateFrom(), res1.getDateTo(), res1.getTelephone(), "tada@gmail.com", res1.getPeople(), res1.getInfo(), Reservation.ReservationState.ENDED, res1.getId());

        reservationDao.update(res1b);

        assertThat(reservationDao.findById(res1.getId()))
                .isEqualToComparingFieldByField(res1b);
        assertThat(reservationDao.findById(res2.getId()))
                .isEqualToComparingFieldByField(res2);
    }

    @Test
    void updateWithNullId() {
        var res = new Reservation("Kiwi Kiwiove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.DOING);

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> reservationDao.update(res))
                .withMessage("Reservation has null ID: " + res);
    }

    @Test
    void updateNonExisting() {
        var res = new Reservation("Kiwi Kiwiove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.DOING);
        res.setId(123L);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> reservationDao.update(res))
                .withMessage("Failed to update non-existing reservation: " + res);
    }

    @Test
    void updateWithException() {
        var sqlException = new SQLException();
        ReservationDao failingDao = createFailingDao(sqlException);

        var res = new Reservation("Kiwi Kiwiove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.DOING);
        res.setId(123L);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.update(res))
                .withMessage("Failed to update reservation " + res)
                .withCause(sqlException);
    }

    @Test
    void deleteReservation() {
        var res = new Reservation("Kiwi Kiwiove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.DOING);
        reservationDao.create(res);
        reservationDao.delete(res);
        assertThat(reservationDao.findAll()).isEmpty();
    }

    @Test
    void deleteNonExistingRes() {
        var res = new Reservation("Kiwi Kiwiove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.DOING);
        res.setId(123L);
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> reservationDao.delete(res))
                .withMessage("Failed to delete non-existing reservation: " + res);
    }

    @Test
    void deleteResWithNoId() {
        var res = new Reservation("Kiwi Kiwiove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.DOING);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> reservationDao.delete(res))
                .withMessage("Reservation has null ID" + res);
    }

    @Test
    void deleteWithException() {
        var sqlException = new SQLException();
        ReservationDao failingDao = createFailingDao(sqlException);

        var res = new Reservation("Kiwi Kiwiove", LocalDate.now(), LocalDate.now(), "783920484", "mail1@mail.com", 1, "", Reservation.ReservationState.DOING);
        res.setId(123L);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.delete(res))
                .withMessage("Failed to delete reservation: " + res)
                .withCause(sqlException);
    }

    private ReservationDao createDaoWithBadGeneratedKeys() {
        try {
            var resultSet = mock(ResultSet.class);
            when(resultSet.next()).thenReturn(false);

            var preparedStatement = mock(PreparedStatement.class);
            when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);

            var connection = mock(Connection.class);
            when(connection.prepareStatement(
                    "INSERT INTO RESERVATION (NAME, DATE_FROM, DATE_TO, TELEPHONE, EMAIL, PERSONS, INFO, STATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    RETURN_GENERATED_KEYS)).thenReturn(preparedStatement);

            var dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenAnswer(i -> ReservationDaoTest.dataSource.getConnection());

            var reservationDao = new ReservationDao(dataSource);
            when(dataSource.getConnection()).thenReturn(connection);
            return reservationDao;
        } catch (SQLException ex) {
            throw new RuntimeException("Mock configuration failed", ex);
        }
    }

    private ReservationDao createFailingDao(Throwable exceptionToBeThrown) {
        try {
            var dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenAnswer(i -> ReservationDaoTest.dataSource.getConnection());
            var reservationDao = new ReservationDao(dataSource);
            when(dataSource.getConnection()).thenThrow(exceptionToBeThrown);
            return reservationDao;
        } catch (SQLException ex) {
            throw new RuntimeException("Mock configuration failed", ex);
        }
    }
}
