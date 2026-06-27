package com.example.demo.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void success_shouldReturnCode200() {
        Result<String> result = Result.success("data");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    void error_shouldReturnCode500() {
        Result<Void> result = Result.error("错误信息");
        assertEquals(500, result.getCode());
        assertEquals("错误信息", result.getMessage());
        assertNull(result.getData());
    }
}
