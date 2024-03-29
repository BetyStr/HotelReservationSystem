package cz.muni.fi.group05.room03.data;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImportantDataDaoTest {

    private static EmbeddedDataSource dataSource;
    private ImportantDataDao importantDataDao;

    @BeforeAll
    static void initTestDataSource() {
        dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("memory:importantdao-test");
        dataSource.setCreateDatabase("create");
    }

    @BeforeEach
    void createReservationDao() throws SQLException {
        importantDataDao = new ImportantDataDao(dataSource);
        try (var connection = dataSource.getConnection(); var st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM APP.DATA");
        }
    }

    @AfterEach
    void cleanUp() {
        importantDataDao.dropTable();
    }

    private ImportantDataDao createFailingDao(Throwable exceptionToBeThrown) {
        try {
            var dataSource = mock(DataSource.class);
            when(dataSource.getConnection()).thenAnswer(i -> ImportantDataDaoTest.dataSource.getConnection());
            var importantDataDao = new ImportantDataDao(dataSource);
            when(dataSource.getConnection()).thenThrow(exceptionToBeThrown);
            return importantDataDao;
        } catch (SQLException ex) {
            throw new RuntimeException("Mock configuration failed", ex);
        }
    }

    @Test
    void createData() {
        importantDataDao.create("TAX", "20");
        assert(importantDataDao.findByKey("TAX")).equals("20");
    }

    @Test
    void createDataWithException() {
        var sqlException = new SQLException();
        var failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.create("TAX", "20"))
                .withMessage("Failed to store data TAX")
                .withCause(sqlException);
    }

    @Test
    void update() {
        importantDataDao.create("TAX", "20");
        importantDataDao.create("SALE", "10");

        importantDataDao.update("TAX", "30");

        assert(importantDataDao.findByKey("TAX")).equals("30");
        assert(importantDataDao.findByKey("SALE")).equals("10");
    }

    @Test
    void updateNonExisting() {
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> importantDataDao.update("TAX", "20"))
                .withMessage("Failed to update non-existing data: TAX");
    }

    @Test
    void updateWithException() {
        var sqlException = new SQLException();
        ImportantDataDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.update("TAX", "20"))
                .withMessage("Failed to update data")
                .withCause(sqlException);
    }

    @Test
    void findByKeyEmpty() {
        assertThat(importantDataDao.findByKey("SALE")).isBlank();
    }

    @Test
    void findByKey() {
        importantDataDao.create("TAX", "20");
        importantDataDao.create("SALE", "5");
        importantDataDao.create("SUM", "1000");

        assert(importantDataDao.findByKey("SALE").equals("5"));
        assert(importantDataDao.findByKey("TAX").equals("20"));
        assert(importantDataDao.findByKey("SUM").equals("1000"));
    }

    @Test
    void findByResIdWithException() {
        var sqlException = new SQLException();
        ImportantDataDao failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> failingDao.findByKey("TAX"))
                .withMessage("Failed to load data with key TAX")
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
                .withMessage("Failed to create DATA table");
    }

    @Test
    void dropTableWithException() {
        var sqlException = new SQLException();
        var failingDao = createFailingDao(sqlException);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(failingDao::dropTable)
                .withMessage("Failed to drop DATA table");
    }
}
