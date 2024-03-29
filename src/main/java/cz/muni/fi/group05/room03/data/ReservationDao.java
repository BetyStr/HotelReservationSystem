package cz.muni.fi.group05.room03.data;

import cz.muni.fi.group05.room03.model.Reservation;

import javax.sql.DataSource;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class ReservationDao {

    private final DataSource dataSource;

    public ReservationDao(DataSource dataSource) {
        this.dataSource = dataSource;
        initTable();
    }

    public void create(Reservation reservation) {
        if (reservation.getId() != null) {
            update(reservation);
            return;
        }
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                "INSERT INTO RESERVATION (NAME, DATE_FROM, DATE_TO, TELEPHONE, EMAIL, PERSONS, INFO, STATE) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", RETURN_GENERATED_KEYS)) {
            st.setString(1, reservation.getName());
            st.setDate(2, Date.valueOf(reservation.getDateFrom()));
            st.setDate(3, Date.valueOf(reservation.getDateTo()));
            st.setString(4, reservation.getTelephone());
            st.setString(5, reservation.getEmail());
            st.setInt(6, reservation.getPeople());
            st.setString(7, reservation.getInfo());
            st.setString(8, reservation.getState().name());
            st.executeUpdate();
            try (var rs = st.getGeneratedKeys()) {
                if (rs.next()) {
                    reservation.setId(rs.getLong(1));
                } else {
                    throw new DataAccessException(
                            "Failed to fetch generated key: no key returned for reservation: " + reservation);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to store reservation " + reservation, ex);
        }
    }

    public void delete(Reservation reservation) {
        if (reservation.getId() == null) {
            throw new IllegalArgumentException("Reservation has null ID" + reservation);
        }
        try {
            var connection = dataSource.getConnection();
            var st = connection.prepareStatement("DELETE FROM RESERVATION WHERE ID =" +
                    reservation.getId());
            int rowsChange = st.executeUpdate();
            if (rowsChange == 0) {
                throw new DataAccessException("Failed to delete non-existing reservation: " + reservation);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete reservation: " + reservation, e);
        }
    }

    public void update(Reservation reservation) {
        if (reservation.getId() == null) {
            throw new IllegalArgumentException("Reservation has null ID: " + reservation);
        }
        try {
            var connection = dataSource.getConnection();
            String name = reservation.getName();
            Date dateFrom = Date.valueOf(reservation.getDateFrom());
            Date dateTo = Date.valueOf(reservation.getDateTo());
            String phoneNumber = reservation.getTelephone();
            String mail = reservation.getEmail();
            Integer numberOfPersons = reservation.getPeople();
            String info = reservation.getInfo();
            String state = reservation.getState().name();
            Long id = reservation.getId();
            var st = connection.prepareStatement("UPDATE RESERVATION SET " +
                            "NAME = " + "'" + name + "'" +
                            ", DATE_FROM = " + "'" + dateFrom + "'" +
                            ", DATE_TO = " + "'" + dateTo + "'" +
                            ", TELEPHONE = " + "'" + phoneNumber + "'" +
                            ", EMAIL = " + "'" + mail +  "'" +
                            ", PERSONS = " + numberOfPersons +
                            ", INFO = " + "'" + info + "'" +
                            ", STATE = " + "'" + state + "'" +
                            " WHERE ID = " +  id
                    );
            int rowsChange = st.executeUpdate();
            if (rowsChange == 0) {
                throw new DataAccessException("Failed to update non-existing reservation: " + reservation);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update reservation " + reservation, e);
        }
    }

    public List<Reservation> findAll() {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT * FROM RESERVATION")) {
            List<Reservation> reservations = new ArrayList<>();
            try (var rs = st.executeQuery()) {
                while (rs.next()) {
                    Reservation reservation = new Reservation(
                            rs.getString("NAME"),
                            rs.getDate("DATE_FROM").toLocalDate(),
                            rs.getDate("DATE_TO").toLocalDate(),
                            rs.getString("TELEPHONE"),
                            rs.getString("EMAIL"),
                            rs.getInt("PERSONS"),
                            rs.getString("INFO"),
                            Reservation.ReservationState.valueOf(rs.getString("STATE")));
                    reservation.setId(rs.getLong("ID"));
                    reservations.add(reservation);
                }
            }
            return reservations;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to load all reservations", ex);
        }
    }

    public Reservation findById(Long id) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement("SELECT * FROM RESERVATION WHERE ID = " + id)) {
            Reservation reservation = null;
            try (var rs = st.executeQuery()) {
                if (rs.next()) {
                    reservation = new Reservation(
                            rs.getString("NAME"),
                            rs.getDate("DATE_FROM").toLocalDate(),
                            rs.getDate("DATE_TO").toLocalDate(),
                            rs.getString("TELEPHONE"),
                            rs.getString("EMAIL"),
                            rs.getInt("PERSONS"),
                            rs.getString("INFO"),
                            Reservation.ReservationState.valueOf(rs.getString("STATE")));
                    reservation.setId(rs.getLong("ID"));
                }
            }
            return reservation;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to load reservation with id: " + id, ex);
        }
    }

    private void initTable() {
        if (!tableExits("APP", "RESERVATION")) {
            createTable();
        }
    }

    protected boolean tableExits(String schemaName, String tableName) {
        try (var connection = dataSource.getConnection();
             var rs = connection.getMetaData().getTables(null, schemaName, tableName, null)) {
            return rs.next();
        } catch (SQLException ex) {
            throw new DataAccessException(
                    "Failed to detect if the table " + schemaName + "." + tableName + " exist", ex);
        }
    }

    protected void createTable() {
        try (var connection = dataSource.getConnection();
             var st = connection.createStatement()) {
            st.executeUpdate("CREATE TABLE APP.RESERVATION (" +
                    "ID BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "NAME VARCHAR(100) NOT NULL," +
                    "DATE_FROM DATE NOT NULL," +
                    "DATE_TO DATE NOT NULL," +
                    "TELEPHONE VARCHAR(20) NOT NULL," +
                    "EMAIL VARCHAR(50)," +
                    "PERSONS INT NOT NULL," +
                    "INFO VARCHAR(1000)," +
                    "STATE VARCHAR(20) NOT NULL CONSTRAINT " +
                    "STATE_CHECK CHECK (STATE IN ('UPCOMING', 'DOING', 'CANCELED', 'ENDED'))" +
                    ")");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create RESERVATION table", ex);
        }
    }

    public void dropTable() {
        try (var connection = dataSource.getConnection();
             var st = connection.createStatement()) {
            st.executeUpdate("DROP TABLE APP.RESERVATION");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to drop RESERVATION table", ex);
        }
    }
}
