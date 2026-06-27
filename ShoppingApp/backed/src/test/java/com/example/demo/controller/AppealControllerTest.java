package com.example.demo.controller;

import com.example.demo.model.Appeal;
import com.example.demo.model.AppealRequest;
import com.example.demo.model.Result;
import com.example.demo.model.User;
import com.example.demo.service.AppealService;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppealControllerTest {

    @Mock
    private AppealService appealService;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AppealController appealController;

    private Appeal createAppeal(Long id, String username, String replyStatus) {
        Appeal appeal = new Appeal();
        appeal.setId(id);
        appeal.setUsername(username);
        appeal.setShopName("店铺" + username);
        appeal.setStatus("BANNED");
        appeal.setType("误封申诉");
        appeal.setContent("申诉内容");
        appeal.setReplyStatus(replyStatus);
        appeal.setCreateTime(LocalDateTime.now());
        return appeal;
    }

    // ========== 用户端接口 ==========

    @Test
    void submitAppeal_shouldReturnSuccess() {
        AppealRequest request = new AppealRequest();
        request.setUsername("merchant1");
        request.setShopName("测试店铺");

        Appeal saved = createAppeal(1L, "merchant1", "PENDING");
        when(appealService.submitAppeal(any(AppealRequest.class))).thenReturn(saved);

        Result<Map<String, Object>> result = appealController.submitAppeal(request);

        assertEquals(200, result.getCode());
        assertEquals(1L, result.getData().get("appealId"));
    }

    @Test
    void submitAppeal_whenException_shouldReturnError() {
        when(appealService.submitAppeal(any(AppealRequest.class)))
            .thenThrow(new RuntimeException("提交失败"));

        Result<Map<String, Object>> result = appealController.submitAppeal(new AppealRequest());

        assertEquals(500, result.getCode());
    }

    @Test
    void getUserAppeals_shouldReturnUserAppeals() {
        List<Appeal> appeals = Arrays.asList(
            createAppeal(1L, "merchant1", "PENDING")
        );
        when(appealService.getUserAppeals("merchant1")).thenReturn(appeals);

        Result<List<Appeal>> result = appealController.getUserAppeals("merchant1");

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    // ========== 管理员接口 ==========

    @Test
    void getAllAppeals_shouldReturnFiltered() {
        List<Appeal> allAppeals = Arrays.asList(
            createAppeal(1L, "merchant1", "PENDING"),
            createAppeal(2L, "merchant2", "PROCESSED")
        );
        when(appealService.getAllAppeals()).thenReturn(allAppeals);
        when(appealService.countPending()).thenReturn(1L);
        when(appealService.countProcessed()).thenReturn(1L);
        when(appealService.countRejected()).thenReturn(0L);

        Result<Map<String, Object>> result = appealController.getAllAppeals(null, null, null);

        assertEquals(200, result.getCode());
        Map<String, Object> data = result.getData();
        assertEquals(2L, data.get("total"));
        assertEquals(1L, data.get("pendingCount"));
        assertEquals(1L, data.get("processedCount"));
    }

    @Test
    void getAllAppeals_withStatusFilter_shouldFilter() {
        List<Appeal> allAppeals = Arrays.asList(
            createAppeal(1L, "merchant1", "PENDING"),
            createAppeal(2L, "merchant2", "PROCESSED")
        );
        when(appealService.getAllAppeals()).thenReturn(allAppeals);
        when(appealService.countPending()).thenReturn(1L);
        when(appealService.countProcessed()).thenReturn(1L);
        when(appealService.countRejected()).thenReturn(0L);

        Result<Map<String, Object>> result = appealController.getAllAppeals("PENDING", null, null);

        assertEquals(200, result.getCode());
        List<Appeal> list = (List<Appeal>) result.getData().get("list");
        assertEquals(1, list.size());
        assertEquals("PENDING", list.get(0).getReplyStatus());
    }

    @Test
    void getAppealDetail_whenExists_shouldReturn() {
        Appeal appeal = createAppeal(1L, "merchant1", "PENDING");
        when(appealService.getAppealById(1L)).thenReturn(appeal);

        Result<Appeal> result = appealController.getAppealDetail(1L);

        assertEquals(200, result.getCode());
        assertEquals("merchant1", result.getData().getUsername());
    }

    @Test
    void getAppealDetail_whenNotExists_shouldReturnError() {
        when(appealService.getAppealById(999L)).thenReturn(null);

        Result<Appeal> result = appealController.getAppealDetail(999L);

        assertEquals(500, result.getCode());
        assertEquals("申诉不存在", result.getMessage());
    }

    @Test
    void getPendingAppeals_shouldReturnPending() {
        List<Appeal> pending = Arrays.asList(
            createAppeal(1L, "merchant1", "PENDING")
        );
        when(appealService.getPendingAppeals()).thenReturn(pending);

        Result<List<Appeal>> result = appealController.getPendingAppeals();

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void replyAppeal_withProcessed_shouldRecoverAccount() {
        Appeal appeal = createAppeal(1L, "merchant1", "PENDING");
        when(appealService.replyAppeal(1L, "已处理", "PROCESSED")).thenReturn(appeal);

        User merchant = new User();
        merchant.setId(1L);
        merchant.setUsername("merchant1");
        merchant.setRole("MERCHANT");
        merchant.setStatus("BANNED");
        when(userService.findByUsername("merchant1")).thenReturn(merchant);

        Map<String, String> request = new HashMap<>();
        request.put("reply", "已处理");
        request.put("replyStatus", "PROCESSED");

        Result<Map<String, Object>> result = appealController.replyAppeal(1L, request);

        assertEquals(200, result.getCode());
        assertEquals("NORMAL", merchant.getStatus());
        verify(userService).updateUser(1L, merchant);
        verify(notificationService).sendAppealReplyNotification(
            1L, "merchant1", "申诉已通过", "您的申诉已通过审核，账号已恢复正常。", "已处理");
    }

    @Test
    void replyAppeal_withRejected_shouldNotify() {
        Appeal appeal = createAppeal(1L, "merchant1", "PENDING");
        when(appealService.replyAppeal(1L, "证据不足", "REJECTED")).thenReturn(appeal);

        User merchant = new User();
        merchant.setId(1L);
        merchant.setUsername("merchant1");
        merchant.setRole("MERCHANT");
        when(userService.findByUsername("merchant1")).thenReturn(merchant);

        Map<String, String> request = new HashMap<>();
        request.put("reply", "证据不足");
        request.put("replyStatus", "REJECTED");

        Result<Map<String, Object>> result = appealController.replyAppeal(1L, request);

        assertEquals(200, result.getCode());
        verify(notificationService).sendAppealReplyNotification(
            1L, "merchant1", "申诉未通过", "您的申诉未被通过。", "证据不足");
    }

    @Test
    void replyAppeal_withEmptyReply_shouldReturnError() {
        Map<String, String> request = new HashMap<>();
        request.put("reply", "");
        request.put("replyStatus", "PROCESSED");

        Result<Map<String, Object>> result = appealController.replyAppeal(1L, request);

        assertEquals(500, result.getCode());
        assertEquals("请填写回复内容", result.getMessage());
    }

    @Test
    void replyAppeal_whenNotExists_shouldReturnError() {
        when(appealService.replyAppeal(999L, "回复", "PROCESSED")).thenReturn(null);

        Map<String, String> request = new HashMap<>();
        request.put("reply", "回复");
        request.put("replyStatus", "PROCESSED");

        Result<Map<String, Object>> result = appealController.replyAppeal(999L, request);

        assertEquals(500, result.getCode());
        assertEquals("申诉不存在", result.getMessage());
    }

    @Test
    void getStatistics_shouldReturnCounts() {
        List<Appeal> allAppeals = Arrays.asList(
            createAppeal(1L, "merchant1", "PENDING"),
            createAppeal(2L, "merchant2", "PROCESSED")
        );
        when(appealService.getAllAppeals()).thenReturn(allAppeals);
        when(appealService.countPending()).thenReturn(1L);
        when(appealService.countProcessed()).thenReturn(1L);
        when(appealService.countRejected()).thenReturn(0L);

        Result<Map<String, Long>> result = appealController.getStatistics();

        assertEquals(200, result.getCode());
        assertEquals(2L, result.getData().get("total"));
        assertEquals(1L, result.getData().get("pending"));
    }

    @Test
    void deleteAppeal_shouldDelete() {
        Result<Map<String, Object>> result = appealController.deleteAppeal(1L);

        assertEquals(200, result.getCode());
        verify(appealService).deleteAppeal(1L);
    }
}
