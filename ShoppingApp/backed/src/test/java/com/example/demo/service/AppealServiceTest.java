package com.example.demo.service;

import com.example.demo.model.Appeal;
import com.example.demo.model.AppealRequest;
import com.example.demo.repository.AppealRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppealServiceTest {

    @Mock
    private AppealRepository appealRepository;

    @InjectMocks
    private AppealService appealService;

    private Appeal createMockAppeal(Long id, String username, String replyStatus) {
        Appeal appeal = new Appeal();
        appeal.setId(id);
        appeal.setUsername(username);
        appeal.setShopName("店铺" + username);
        appeal.setStatus("BANNED");
        appeal.setType("误封申诉");
        appeal.setContent("申诉内容");
        appeal.setContact("13800138000");
        appeal.setReplyStatus(replyStatus);
        appeal.setCreateTime(LocalDateTime.now());
        return appeal;
    }

    @Test
    void submitAppeal_shouldSaveAndReturn() {
        AppealRequest request = new AppealRequest();
        request.setUsername("merchant1");
        request.setShopName("测试店铺");
        request.setStatus("BANNED");
        request.setType("误封申诉");
        request.setContent("我没有违规");
        request.setContact("13800138000");

        Appeal saved = createMockAppeal(1L, "merchant1", "PENDING");
        when(appealRepository.save(any(Appeal.class))).thenReturn(saved);

        Appeal result = appealService.submitAppeal(request);

        assertNotNull(result);
        assertEquals("PENDING", result.getReplyStatus());
        assertEquals("merchant1", result.getUsername());
        verify(appealRepository).save(any(Appeal.class));
    }

    @Test
    void getAllAppeals_shouldReturnAll() {
        List<Appeal> expected = Arrays.asList(
            createMockAppeal(1L, "merchant1", "PENDING"),
            createMockAppeal(2L, "merchant2", "PROCESSED")
        );
        when(appealRepository.findAll()).thenReturn(expected);

        List<Appeal> result = appealService.getAllAppeals();

        assertEquals(2, result.size());
    }

    @Test
    void getAppealById_whenExists_shouldReturn() {
        Appeal expected = createMockAppeal(1L, "merchant1", "PENDING");
        when(appealRepository.findById(1L)).thenReturn(Optional.of(expected));

        Appeal result = appealService.getAppealById(1L);

        assertNotNull(result);
        assertEquals("merchant1", result.getUsername());
    }

    @Test
    void getAppealById_whenNotExists_shouldReturnNull() {
        when(appealRepository.findById(999L)).thenReturn(Optional.empty());

        Appeal result = appealService.getAppealById(999L);

        assertNull(result);
    }

    @Test
    void getPendingAppeals_shouldReturnPending() {
        List<Appeal> expected = Arrays.asList(
            createMockAppeal(1L, "merchant1", "PENDING")
        );
        when(appealRepository.findByReplyStatus("PENDING")).thenReturn(expected);

        List<Appeal> result = appealService.getPendingAppeals();

        assertEquals(1, result.size());
        assertEquals("PENDING", result.get(0).getReplyStatus());
    }

    @Test
    void replyAppeal_whenExists_shouldUpdate() {
        Appeal appeal = createMockAppeal(1L, "merchant1", "PENDING");
        when(appealRepository.findById(1L)).thenReturn(Optional.of(appeal));
        when(appealRepository.save(any(Appeal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appeal result = appealService.replyAppeal(1L, "已处理", "PROCESSED");

        assertNotNull(result);
        assertEquals("PROCESSED", result.getReplyStatus());
        assertEquals("已处理", result.getReply());
        assertNotNull(result.getReplyTime());
    }

    @Test
    void replyAppeal_whenNotExists_shouldReturnNull() {
        when(appealRepository.findById(999L)).thenReturn(Optional.empty());

        Appeal result = appealService.replyAppeal(999L, "已处理", "PROCESSED");

        assertNull(result);
    }

    @Test
    void countPending_shouldDelegateToRepository() {
        when(appealRepository.countPending()).thenReturn(5L);

        long count = appealService.countPending();

        assertEquals(5L, count);
    }

    @Test
    void deleteAppeal_shouldDeleteById() {
        appealService.deleteAppeal(1L);

        verify(appealRepository).deleteById(1L);
    }
}
