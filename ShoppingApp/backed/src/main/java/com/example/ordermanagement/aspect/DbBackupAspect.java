package com.example.ordermanagement.aspect;

import com.example.ordermanagement.service.DbBackupService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DbBackupAspect {

    private static final Logger log = LoggerFactory.getLogger(DbBackupAspect.class);

    @Autowired
    private DbBackupService dbBackupService;

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void inRestController() {}

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) " +
              "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
              "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) " +
              "|| @annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void writeOperation() {}

    @AfterReturning("inRestController() && writeOperation()")
    public void afterWrite() {
        new Thread(() -> {
            try {
                dbBackupService.backupNow();
            } catch (Exception e) {
                log.warn("自动备份失败", e);
            }
        }).start();
    }
}
