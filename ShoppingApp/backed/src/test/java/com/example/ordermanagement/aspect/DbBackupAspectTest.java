package com.example.ordermanagement.aspect;

import com.example.ordermanagement.service.DbBackupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DbBackupAspectTest {

    @Mock
    private DbBackupService dbBackupService;

    @InjectMocks
    private DbBackupAspect dbBackupAspect;

    @Test
    void testAfterWrite_callsBackupNowAsync() {
        dbBackupAspect.afterWrite();

        verify(dbBackupService, timeout(1000).times(1)).backupNow();
    }

    @Test
    void testAfterWrite_whenBackupFails_doesNotThrow() {
        doThrow(new RuntimeException("backup failed")).when(dbBackupService).backupNow();

        dbBackupAspect.afterWrite();

        verify(dbBackupService, timeout(1000).times(1)).backupNow();
    }
}
