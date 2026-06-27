package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelComment;
import com.example.ordermanagement.service.HotelCommentService;
import com.example.ordermanagement.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HotelCommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HotelCommentService hotelCommentService;

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private HotelCommentController hotelCommentController;

    private ObjectMapper objectMapper;
    private HotelComment sampleComment;
    private List<HotelComment> sampleComments;
    private Hotel sampleHotel;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hotelCommentController).build();
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleComment = new HotelComment();
        sampleComment.setId(1L);
        sampleComment.setHotelId(1L);
        sampleComment.setOrderId(100L);
        sampleComment.setUsername("testuser");
        sampleComment.setContent("酒店环境很好，服务态度也不错！");
        sampleComment.setScore(5);
        sampleComment.setStatus("正常");
        sampleComment.setCreateTime(LocalDateTime.now());
        sampleComment.setImages("[\"http://example.com/img1.jpg\", \"http://example.com/img2.jpg\"]");
        sampleComment.setReply("感谢您的评价，欢迎再次光临！");
        sampleComment.setReplyTime(LocalDateTime.now());

        HotelComment comment2 = new HotelComment();
        comment2.setId(2L);
        comment2.setHotelId(1L);
        comment2.setOrderId(101L);
        comment2.setUsername("testuser2");
        comment2.setContent("位置很好，交通便利。");
        comment2.setScore(4);
        comment2.setStatus("正常");
        comment2.setCreateTime(LocalDateTime.now());

        sampleComments = Arrays.asList(sampleComment, comment2);

        sampleHotel = new Hotel();
        sampleHotel.setId(1L);
        sampleHotel.setName("测试酒店");
        sampleHotel.setMerchantId(10L);
    }

    @Test
    void getByHotel_ShouldReturnHotelComments() throws Exception {
        when(hotelCommentService.getByHotelId(1L)).thenReturn(sampleComments);

        mockMvc.perform(get("/api/hotel-comments/hotel/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].content").value("酒店环境很好，服务态度也不错！"));

        verify(hotelCommentService, times(1)).getByHotelId(1L);
    }

    @Test
    void getByHotel_ShouldFilterViolationComments() throws Exception {
        HotelComment violationComment = new HotelComment();
        violationComment.setId(3L);
        violationComment.setHotelId(1L);
        violationComment.setOrderId(102L);
        violationComment.setUsername("testuser3");
        violationComment.setContent("包含敏感词的评价");
        violationComment.setScore(3);
        violationComment.setStatus("违规");
        violationComment.setCreateTime(LocalDateTime.now());

        List<HotelComment> allComments = Arrays.asList(sampleComment, violationComment);
        when(hotelCommentService.getByHotelId(1L)).thenReturn(allComments);

        mockMvc.perform(get("/api/hotel-comments/hotel/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1L));

        verify(hotelCommentService, times(1)).getByHotelId(1L);
    }

    @Test
    void getByHotel_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        when(hotelCommentService.getByHotelId(1L)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/hotel-comments/hotel/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("查询失败：Database error"));
    }

    @Test
    void create_WithValidComment_ShouldReturnSuccess() throws Exception {
        when(hotelCommentService.existsByOrderId(100L)).thenReturn(false);
        when(hotelCommentService.save(any(HotelComment.class))).thenReturn(sampleComment);

        mockMvc.perform(post("/api/hotel-comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.message").value("评价成功"));

        verify(hotelCommentService, times(1)).existsByOrderId(100L);
        verify(hotelCommentService, times(1)).save(any(HotelComment.class));
    }

    @Test
    void create_WithDuplicateOrder_ShouldReturnError() throws Exception {
        when(hotelCommentService.existsByOrderId(100L)).thenReturn(true);

        mockMvc.perform(post("/api/hotel-comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("已评价"));

        verify(hotelCommentService, never()).save(any(HotelComment.class));
    }

    @Test
    void create_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        when(hotelCommentService.existsByOrderId(100L)).thenReturn(false);
        when(hotelCommentService.save(any(HotelComment.class)))
                .thenThrow(new RuntimeException("Save failed"));

        mockMvc.perform(post("/api/hotel-comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("评价失败：Save failed"));
    }

    @Test
    void getAllComments_ShouldReturnAllComments() throws Exception {
        when(hotelCommentService.getAllComments()).thenReturn(sampleComments);

        mockMvc.perform(get("/api/hotel-comments/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(hotelCommentService, times(1)).getAllComments();
    }

    @Test
    void getMerchantComments_ShouldReturnMerchantComments() throws Exception {
        List<Hotel> hotels = Arrays.asList(sampleHotel);
        when(hotelService.getHotelsByMerchant(10L)).thenReturn(hotels);
        when(hotelCommentService.getMerchantComments(anyList(), isNull())).thenReturn(sampleComments);

        mockMvc.perform(get("/api/hotel-comments/merchant")
                .param("merchantId", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(hotelService, times(1)).getHotelsByMerchant(10L);
        verify(hotelCommentService, times(1)).getMerchantComments(anyList(), isNull());
    }

    @Test
    void getMerchantComments_WithHotelIdFilter_ShouldReturnFilteredComments() throws Exception {
        List<Hotel> hotels = Arrays.asList(sampleHotel);
        when(hotelService.getHotelsByMerchant(10L)).thenReturn(hotels);
        when(hotelCommentService.getMerchantComments(anyList(), eq(5))).thenReturn(Arrays.asList(sampleComment));

        mockMvc.perform(get("/api/hotel-comments/merchant")
                .param("merchantId", "10")
                .param("hotelId", "1")
                .param("rating", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].score").value(5));

        verify(hotelService, times(1)).getHotelsByMerchant(10L);
        verify(hotelCommentService, times(1)).getMerchantComments(anyList(), eq(5));
    }

    @Test
    void getMerchantComments_WithNoHotels_ShouldReturnEmptyList() throws Exception {
        when(hotelService.getHotelsByMerchant(999L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/hotel-comments/merchant")
                .param("merchantId", "999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(hotelService, times(1)).getHotelsByMerchant(999L);
        verify(hotelCommentService, never()).getMerchantComments(anyList(), any());
    }

    @Test
    void replyComment_WithValidReply_ShouldReturnSuccess() throws Exception {
        when(hotelCommentService.replyComment(eq(1L), eq("感谢您的评价，欢迎再次光临！")))
                .thenReturn(true);

        Map<String, String> request = new HashMap<>();
        request.put("reply", "感谢您的评价，欢迎再次光临！");

        mockMvc.perform(post("/api/hotel-comments/1/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));

        verify(hotelCommentService, times(1))
                .replyComment(eq(1L), eq("感谢您的评价，欢迎再次光临！"));
    }

    @Test
    void replyComment_WithInvalidReply_ShouldReturnError() throws Exception {
        when(hotelCommentService.replyComment(eq(1L), eq("")))
                .thenReturn(false);

        Map<String, String> request = new HashMap<>();
        request.put("reply", "");

        mockMvc.perform(post("/api/hotel-comments/1/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("回复失败"));

        verify(hotelCommentService, times(1)).replyComment(eq(1L), eq(""));
    }

    @Test
    void updateReply_WithValidReply_ShouldReturnSuccess() throws Exception {
        when(hotelCommentService.updateReply(eq(1L), eq("感谢您的评价，欢迎再次光临！")))
                .thenReturn(true);

        Map<String, String> request = new HashMap<>();
        request.put("reply", "感谢您的评价，欢迎再次光临！");

        mockMvc.perform(put("/api/hotel-comments/1/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));

        verify(hotelCommentService, times(1))
                .updateReply(eq(1L), eq("感谢您的评价，欢迎再次光临！"));
    }

    @Test
    void updateReply_WithInvalidReply_ShouldReturnError() throws Exception {
        when(hotelCommentService.updateReply(eq(1L), eq("")))
                .thenReturn(false);

        Map<String, String> request = new HashMap<>();
        request.put("reply", "");

        mockMvc.perform(put("/api/hotel-comments/1/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("修改失败"));
    }

    @Test
    void getViolations_ShouldReturnViolationComments() throws Exception {
        HotelComment violationComment = new HotelComment();
        violationComment.setId(3L);
        violationComment.setStatus("违规");
        List<HotelComment> violationComments = Arrays.asList(violationComment);

        when(hotelCommentService.getViolationComments()).thenReturn(violationComments);

        mockMvc.perform(get("/api/hotel-comments/violations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("违规"));

        verify(hotelCommentService, times(1)).getViolationComments();
    }

    @Test
    void markViolation_ShouldMarkCommentAsViolation() throws Exception {
        when(hotelCommentService.markViolation(1L)).thenReturn(true);

        mockMvc.perform(post("/api/hotel-comments/1/violation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));

        verify(hotelCommentService, times(1)).markViolation(1L);
    }

    @Test
    void markViolation_WhenFails_ShouldReturnError() throws Exception {
        when(hotelCommentService.markViolation(999L)).thenReturn(false);

        mockMvc.perform(post("/api/hotel-comments/999/violation")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("标记失败"));
    }

    @Test
    void deleteComment_ShouldDeleteComment() throws Exception {
        doNothing().when(hotelCommentService).deleteComment(1L);

        mockMvc.perform(delete("/api/hotel-comments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));

        verify(hotelCommentService, times(1)).deleteComment(1L);
    }

    @Test
    void deleteComment_WhenServiceThrowsException_ShouldReturnError() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(hotelCommentService).deleteComment(1L);

        mockMvc.perform(delete("/api/hotel-comments/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("删除失败：Delete failed"));
    }
}