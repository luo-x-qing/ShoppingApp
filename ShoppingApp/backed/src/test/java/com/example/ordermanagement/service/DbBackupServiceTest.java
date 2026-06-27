package com.example.ordermanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbBackupServiceTest {

    private DbBackupService dbBackupService;

    @BeforeEach
    void setUp() {
        dbBackupService = new DbBackupService();
    }

    @Test
    void testInit_withStandardUrl() throws Exception {
        ReflectionTestUtils.setField(dbBackupService, "dbUser", "root");
        ReflectionTestUtils.setField(dbBackupService, "dbPass", "123456");
        ReflectionTestUtils.setField(dbBackupService, "dbUrl", "jdbc:mysql://localhost:3306/order_management?useSSL=false&serverTimezone=UTC");

        dbBackupService.init();

        assertEquals("localhost", getFieldString("host"));
        assertEquals("3306", getFieldString("port"));
        assertEquals("order_management", getFieldString("dbName"));
    }

    @Test
    void testInit_withDefaultPort() throws Exception {
        ReflectionTestUtils.setField(dbBackupService, "dbUser", "root");
        ReflectionTestUtils.setField(dbBackupService, "dbPass", "123456");
        ReflectionTestUtils.setField(dbBackupService, "dbUrl", "jdbc:mysql://localhost/order_management?useSSL=false");

        dbBackupService.init();

        assertEquals("localhost", getFieldString("host"));
        assertEquals("3306", getFieldString("port"));
        assertEquals("order_management", getFieldString("dbName"));
    }

    @Test
    void testInit_withoutQueryParams() throws Exception {
        ReflectionTestUtils.setField(dbBackupService, "dbUser", "root");
        ReflectionTestUtils.setField(dbBackupService, "dbPass", "123456");
        ReflectionTestUtils.setField(dbBackupService, "dbUrl", "jdbc:mysql://localhost:3306/order_management");

        dbBackupService.init();

        assertEquals("localhost", getFieldString("host"));
        assertEquals("3306", getFieldString("port"));
        assertEquals("order_management", getFieldString("dbName"));
    }

    @Test
    void testInit_withNonStandardPort() throws Exception {
        ReflectionTestUtils.setField(dbBackupService, "dbUser", "admin");
        ReflectionTestUtils.setField(dbBackupService, "dbPass", "pass");
        ReflectionTestUtils.setField(dbBackupService, "dbUrl", "jdbc:mysql://192.168.1.100:3307/mydb");

        dbBackupService.init();

        assertEquals("192.168.1.100", getFieldString("host"));
        assertEquals("3307", getFieldString("port"));
        assertEquals("mydb", getFieldString("dbName"));
    }

    @Test
    void testInit_withMalformedUrl() throws Exception {
        ReflectionTestUtils.setField(dbBackupService, "dbUser", "root");
        ReflectionTestUtils.setField(dbBackupService, "dbPass", "123456");
        ReflectionTestUtils.setField(dbBackupService, "dbUrl", "jdbc:mysql://");

        dbBackupService.init();

        assertNull(getFieldString("host"));
        assertNull(getFieldString("port"));
        assertNull(getFieldString("dbName"));
    }

    @Test
    void testCountTables_returnsCorrectCount() {
        String dump = "CREATE TABLE foo (\n" +
                      "  id INT\n" +
                      ");\n" +
                      "CREATE TABLE bar (\n" +
                      "  id INT\n" +
                      ");\n" +
                      "CREATE TABLE baz (\n" +
                      "  id INT\n" +
                      ");\n";
        assertEquals(3, invokeCountTables(dump));
    }

    @Test
    void testCountTables_withNoTables() {
        String dump = "INSERT INTO foo VALUES (1);\n" +
                      "ALTER TABLE bar ADD COLUMN x INT;\n";
        assertEquals(0, invokeCountTables(dump));
    }

    @Test
    void testCountTables_withCommentContainingKeyword() {
        String dump = "-- CREATE TABLE ignored (\n" +
                      "CREATE TABLE real_one (\n" +
                      "  id INT\n" +
                      ");\n";
        assertEquals(2, invokeCountTables(dump));
    }

    @Test
    void testCountTables_withEmptyDump() {
        assertEquals(0, invokeCountTables(""));
    }

    @Test
    void testScheduledBackup_callsBackupNow() {
        DbBackupService spy = spy(dbBackupService);
        doReturn(true).when(spy).backupNow();

        spy.scheduledBackup();

        verify(spy, times(1)).backupNow();
    }



    private String getFieldString(String name) throws Exception {
        Field f = DbBackupService.class.getDeclaredField(name);
        f.setAccessible(true);
        return (String) f.get(dbBackupService);
    }

    private int invokeCountTables(String dump) {
        try {
            Method m = DbBackupService.class.getDeclaredMethod("countTables", String.class);
            m.setAccessible(true);
            return (int) m.invoke(dbBackupService, dump);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
