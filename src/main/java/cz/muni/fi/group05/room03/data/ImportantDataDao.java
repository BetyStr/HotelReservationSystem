package cz.muni.fi.group05.room03.data;

import javax.sql.DataSource;
import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;

public class ImportantDataDao {

    private final DataSource dataSource;

    public ImportantDataDao(DataSource dataSource) {
        this.dataSource = dataSource;
        initTable();
    }

    public void create(String key, String value) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                     "INSERT INTO DATA (NAME, VALUE) VALUES (?, ?)",
                     RETURN_GENERATED_KEYS)) {
            st.setString(1, key);
            st.setString(2, value);
            st.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to store data " + key, ex);
        }
    }


    public void update(String key, String value) {
        try {
            var connection = dataSource.getConnection();
            String format = String.format("UPDATE DATA SET VALUE = '%s' WHERE NAME = '%s'", value, key);
            var st = connection.prepareStatement(format);
            int rowsChange = st.executeUpdate();
            if (rowsChange == 0) {
                throw new DataAccessException("Failed to update non-existing data: " + key);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update data", e);
        }
    }

    public String findByKey(String key) {
        try (var connection = dataSource.getConnection();
             var st = connection.prepareStatement(
                     "SELECT VALUE FROM DATA WHERE NAME = " + "'"  + key + "'")) {
            String value = null;
            try (var rs = st.executeQuery()) {
                if (rs.next()) {
                    value = rs.getString("VALUE");
                }
            }
            return value;
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to load data with key " + key, ex);
        }
    }

    private void initTable() {
        if (!tableExits("APP", "DATA")) {
            createTable();
            create("TAX", "2");
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
            st.executeUpdate("CREATE TABLE APP.DATA (" +
                    "ID BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY," +
                    "NAME VARCHAR(20) NOT NULL," +
                    "VALUE VARCHAR(20) NOT NULL" +
                    ")");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to create DATA table", ex);
        }
    }

    public void dropTable() {
        try (var connection = dataSource.getConnection();
             var st = connection.createStatement()) {
            st.executeUpdate("DROP TABLE APP.DATA");
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to drop DATA table", ex);
        }
    }
}
