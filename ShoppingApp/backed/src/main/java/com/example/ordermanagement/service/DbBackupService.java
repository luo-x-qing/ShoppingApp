package com.example.ordermanagement.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DbBackupService {

    private static final Logger log = LoggerFactory.getLogger(DbBackupService.class);

    private static final String BACKUP_FILE = "D:\\luo-x-qing\\ShoppingApp\\ShoppingApp\\db_backup.sql";

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPass;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    private String dbName;
    private String host;
    private String port;

    @PostConstruct
    public void init() {
        try {
            String url = dbUrl.substring(5);
            int slash = url.indexOf("//");
            int colon = url.indexOf(":", slash + 2);
            int question = url.indexOf("?", colon + 1);
            host = url.substring(slash + 2, colon);
            int nextColon = url.indexOf(":", colon + 1);
            int slash2 = url.indexOf("/", colon + 1);
            if (nextColon > 0 && nextColon < slash2) {
                port = url.substring(colon + 1, nextColon);
                dbName = url.substring(nextColon + 1, question > 0 ? question : url.length());
            } else {
                port = "3306";
                dbName = url.substring(colon + 1, question > 0 ? question : url.length());
            }
            log.info("DB Backup config: host={}, port={}, db={}, user={}", host, port, dbName, dbUser);
        } catch (Exception e) {
            log.error("解析数据库连接URL失败", e);
        }
    }

    public synchronized boolean backupNow() {
        try {
            log.info("开始全库备份...");

            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-u" + dbUser,
                    "-p" + dbPass,
                    "-h" + host,
                    "-P" + port,
                    dbName,
                    "--complete-insert"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = p.waitFor();

            if (exitCode != 0) {
                log.error("mysqldump 失败，退出码: {}\n{}", exitCode, output);
                return false;
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(BACKUP_FILE), "UTF-8")) {
                writer.write(output.toString());
            }

            log.info("全库备份成功: {} ({} bytes, {} 张表)", BACKUP_FILE, output.length(), countTables(output.toString()));
            return true;

        } catch (Exception e) {
            log.error("数据库备份失败", e);
            return false;
        }
    }

    private int countTables(String dump) {
        int count = 0;
        int idx = 0;
        while ((idx = dump.indexOf("CREATE TABLE ", idx)) != -1) {
            count++;
            idx += 12;
        }
        return count;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void scheduledBackup() {
        log.info("开始定时全库备份...");
        backupNow();
    }
}
