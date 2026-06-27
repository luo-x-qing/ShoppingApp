package com.example.demo.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AppealTest {

    @Test
    void constructor_shouldSetDefaults() {
        Appeal appeal = new Appeal();
        assertEquals("PENDING", appeal.getReplyStatus());
        assertNotNull(appeal.getCreateTime());
    }

    @Test
    void setterAndGetter_shouldWorkCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        Appeal appeal = new Appeal();
        appeal.setId(1L);
        appeal.setUsername("merchant1");
        appeal.setShopName("测试店铺");
        appeal.setStatus("BANNED");
        appeal.setType("误封申诉");
        appeal.setContent("我没有违规");
        appeal.setContact("13800138000");
        appeal.setReply("已处理");
        appeal.setReplyStatus("PROCESSED");
        appeal.setCreateTime(now);
        appeal.setReplyTime(now.plusHours(1));

        assertEquals(1L, appeal.getId());
        assertEquals("merchant1", appeal.getUsername());
        assertEquals("测试店铺", appeal.getShopName());
        assertEquals("BANNED", appeal.getStatus());
        assertEquals("误封申诉", appeal.getType());
        assertEquals("我没有违规", appeal.getContent());
        assertEquals("13800138000", appeal.getContact());
        assertEquals("已处理", appeal.getReply());
        assertEquals("PROCESSED", appeal.getReplyStatus());
        assertEquals(now, appeal.getCreateTime());
        assertEquals(now.plusHours(1), appeal.getReplyTime());
    }
}
