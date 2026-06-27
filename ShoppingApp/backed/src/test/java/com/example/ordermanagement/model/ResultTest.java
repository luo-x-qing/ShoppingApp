package com.example.ordermanagement.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void success_shouldReturnCode200() {
        Result<String> result = Result.success("test data");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("test data", result.getData());
    }

    @Test
    void successWithMessage_shouldReturnCustomMessage() {
        Result<Integer> result = Result.success("操作成功", 42);
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals(42, result.getData());
    }

    @Test
    void error_shouldReturnCode500() {
        Result<Void> result = Result.error("系统错误");
        assertEquals(500, result.getCode());
        assertEquals("系统错误", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void errorWithCode_shouldReturnCustomCode() {
        Result<Void> result = Result.error(401, "未授权");
        assertEquals(401, result.getCode());
        assertEquals("未授权", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void setterAndGetter_shouldWorkCorrectly() {
        Result<String> result = new Result<>();
        result.setCode(200);
        result.setMessage("msg");
        result.setData("data");
        assertEquals(200, result.getCode());
        assertEquals("msg", result.getMessage());
        assertEquals("data", result.getData());
    }
}
