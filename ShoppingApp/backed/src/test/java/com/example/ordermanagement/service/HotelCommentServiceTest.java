package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelComment;
import com.example.ordermanagement.repository.HotelCommentRepository;
import com.example.ordermanagement.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelCommentServiceTest {

    @Mock
    private HotelCommentRepository hotelCommentRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private HotelCommentService hotelCommentService;

    private HotelComment sampleComment;
    private Hotel sampleHotel;
    private List<HotelComment> sampleComments;

    @BeforeEach
    void setUp() {
        sampleComment = new HotelComment();
        sampleComment.setId(1L);
        sampleComment.setHotelId(1L);
        sampleComment.setOrderId(100L);
        sampleComment.setUsername("testuser");
        sampleComment.setContent("酒店环境很好，服务态度也不错！");
        sampleComment.setScore(5);
        sampleComment.setStatus("正常");
        sampleComment.setCreateTime(LocalDateTime.now());

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
        sampleHotel.setAvgRating(0.0);
    }

    @Test
    void getByHotelId_ShouldReturnHotelComments() {
        when(hotelCommentRepository.findByHotelId(1L)).thenReturn(sampleComments);

        List<HotelComment> comments = hotelCommentService.getByHotelId(1L);

        assertThat(comments).isNotEmpty();
        assertThat(comments.size()).isEqualTo(2);
        assertThat(comments.get(0).getHotelId()).isEqualTo(1L);
        verify(hotelCommentRepository, times(1)).findByHotelId(1L);
    }

    @Test
    void existsByOrderId_ShouldReturnTrueIfExists() {
        when(hotelCommentRepository.existsByOrderId(100L)).thenReturn(true);

        boolean exists = hotelCommentService.existsByOrderId(100L);

        assertThat(exists).isTrue();
        verify(hotelCommentRepository, times(1)).existsByOrderId(100L);
    }

    @Test
    void existsByOrderId_ShouldReturnFalseIfNotExists() {
        when(hotelCommentRepository.existsByOrderId(999L)).thenReturn(false);

        boolean exists = hotelCommentService.existsByOrderId(999L);

        assertThat(exists).isFalse();
        verify(hotelCommentRepository, times(1)).existsByOrderId(999L);
    }

    @Test
    void save_WithValidComment_ShouldSaveAndUpdateRating() {
        when(hotelCommentRepository.save(any(HotelComment.class))).thenReturn(sampleComment);
        when(hotelCommentRepository.findByHotelId(1L)).thenReturn(sampleComments);
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(sampleHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(sampleHotel);

        HotelComment savedComment = hotelCommentService.save(sampleComment);

        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getId()).isEqualTo(1L);
        assertThat(savedComment.getStatus()).isEqualTo("正常");
        verify(hotelCommentRepository, times(1)).save(any(HotelComment.class));
        verify(hotelRepository, times(1)).findById(1L);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void save_WithSensitiveWords_ShouldMarkAsViolation() {
        HotelComment sensitiveComment = new HotelComment();
        sensitiveComment.setHotelId(1L);
        sensitiveComment.setOrderId(102L);
        sensitiveComment.setUsername("testuser3");
        sensitiveComment.setContent("这个酒店包含敏感词1，需要过滤");
        sensitiveComment.setScore(3);

        when(hotelCommentRepository.save(any(HotelComment.class))).thenReturn(sensitiveComment);
        when(hotelCommentRepository.findByHotelId(1L)).thenReturn(sampleComments);
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(sampleHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(sampleHotel);

        HotelComment savedComment = hotelCommentService.save(sensitiveComment);

        assertThat(savedComment.getStatus()).isEqualTo("违规");
        verify(hotelCommentRepository, times(1)).save(any(HotelComment.class));
    }

    @Test
    void getAllComments_ShouldReturnAllComments() {
        when(hotelCommentRepository.findAll()).thenReturn(sampleComments);

        List<HotelComment> comments = hotelCommentService.getAllComments();

        assertThat(comments).isNotEmpty();
        assertThat(comments.size()).isEqualTo(2);
        verify(hotelCommentRepository, times(1)).findAll();
    }

    @Test
    void getMerchantComments_WithHotelIds_ShouldReturnComments() {
        List<Long> hotelIds = Arrays.asList(1L, 2L);
        when(hotelCommentRepository.findByHotelIds(hotelIds)).thenReturn(sampleComments);

        List<HotelComment> comments = hotelCommentService.getMerchantComments(hotelIds, null);

        assertThat(comments).isNotEmpty();
        assertThat(comments.size()).isEqualTo(2);
        verify(hotelCommentRepository, times(1)).findByHotelIds(hotelIds);
    }

    @Test
    void getMerchantComments_WithRatingFilter_ShouldReturnFilteredComments() {
        List<Long> hotelIds = Arrays.asList(1L, 2L);
        when(hotelCommentRepository.findByHotelIdsAndRating(hotelIds, 5))
                .thenReturn(Arrays.asList(sampleComment));

        List<HotelComment> comments = hotelCommentService.getMerchantComments(hotelIds, 5);

        assertThat(comments).isNotEmpty();
        assertThat(comments.size()).isEqualTo(1);
        assertThat(comments.get(0).getScore()).isEqualTo(5);
        verify(hotelCommentRepository, times(1)).findByHotelIdsAndRating(hotelIds, 5);
    }

    @Test
    void getMerchantComments_WithEmptyHotelIds_ShouldReturnEmptyList() {
        List<HotelComment> comments = hotelCommentService.getMerchantComments(Arrays.asList(), null);

        assertThat(comments).isEmpty();
        verify(hotelCommentRepository, never()).findByHotelIds(anyList());
    }

    @Test
    void replyComment_WithValidReply_ShouldSucceed() {
        when(hotelCommentRepository.findById(1L)).thenReturn(Optional.of(sampleComment));
        when(hotelCommentRepository.updateReply(eq(1L), eq("感谢您的评价")))
                .thenReturn(1);

        boolean result = hotelCommentService.replyComment(1L, "感谢您的评价");

        assertThat(result).isTrue();
        verify(hotelCommentRepository, times(1)).findById(1L);
        verify(hotelCommentRepository, times(1)).updateReply(eq(1L), eq("感谢您的评价"));
    }

    @Test
    void replyComment_WithEmptyReply_ShouldReturnFalse() {
        boolean result = hotelCommentService.replyComment(1L, "");

        assertThat(result).isFalse();
        verify(hotelCommentRepository, never()).findById(anyLong());
        verify(hotelCommentRepository, never()).updateReply(anyLong(), anyString());
    }

    @Test
    void replyComment_WithNullReply_ShouldReturnFalse() {
        boolean result = hotelCommentService.replyComment(1L, null);

        assertThat(result).isFalse();
        verify(hotelCommentRepository, never()).findById(anyLong());
        verify(hotelCommentRepository, never()).updateReply(anyLong(), anyString());
    }

    @Test
    void replyComment_WithNonExistentComment_ShouldReturnFalse() {
        when(hotelCommentRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = hotelCommentService.replyComment(999L, "感谢您的评价");

        assertThat(result).isFalse();
        verify(hotelCommentRepository, times(1)).findById(999L);
        verify(hotelCommentRepository, never()).updateReply(anyLong(), anyString());
    }

    @Test
    void replyComment_WithSensitiveWords_ShouldReturnFalse() {
        when(hotelCommentRepository.findById(1L)).thenReturn(Optional.of(sampleComment));

        boolean result = hotelCommentService.replyComment(1L, "包含敏感词1的回复");

        assertThat(result).isFalse();
        verify(hotelCommentRepository, times(1)).findById(1L);
        verify(hotelCommentRepository, never()).updateReply(anyLong(), anyString());
    }

    @Test
    void updateReply_WithValidReply_ShouldSucceed() {
        sampleComment.setReply("旧的回复");
        when(hotelCommentRepository.findById(1L)).thenReturn(Optional.of(sampleComment));
        when(hotelCommentRepository.updateReply(eq(1L), eq("更新后的回复")))
                .thenReturn(1);

        boolean result = hotelCommentService.updateReply(1L, "更新后的回复");

        assertThat(result).isTrue();
        verify(hotelCommentRepository, times(1)).findById(1L);
        verify(hotelCommentRepository, times(1)).updateReply(eq(1L), eq("更新后的回复"));
    }

    @Test
    void updateReply_WithNoExistingReply_ShouldReturnFalse() {
        when(hotelCommentRepository.findById(1L)).thenReturn(Optional.of(sampleComment));

        boolean result = hotelCommentService.updateReply(1L, "更新后的回复");

        assertThat(result).isFalse();
        verify(hotelCommentRepository, times(1)).findById(1L);
        verify(hotelCommentRepository, never()).updateReply(anyLong(), anyString());
    }

    @Test
    void getViolationComments_ShouldReturnViolationComments() {
        HotelComment violationComment = new HotelComment();
        violationComment.setId(3L);
        violationComment.setStatus("违规");
        List<HotelComment> violationComments = Arrays.asList(violationComment);

        when(hotelCommentRepository.findByStatus("违规")).thenReturn(violationComments);

        List<HotelComment> comments = hotelCommentService.getViolationComments();

        assertThat(comments).isNotEmpty();
        assertThat(comments.get(0).getStatus()).isEqualTo("违规");
        verify(hotelCommentRepository, times(1)).findByStatus("违规");
    }

    @Test
    void markViolation_ShouldMarkCommentAsViolation() {
        when(hotelCommentRepository.updateStatus(eq(1L), eq("违规")))
                .thenReturn(1);

        boolean result = hotelCommentService.markViolation(1L);

        assertThat(result).isTrue();
        verify(hotelCommentRepository, times(1)).updateStatus(eq(1L), eq("违规"));
    }

    @Test
    void markViolation_WhenUpdateFails_ShouldReturnFalse() {
        when(hotelCommentRepository.updateStatus(eq(999L), eq("违规")))
                .thenReturn(0);

        boolean result = hotelCommentService.markViolation(999L);

        assertThat(result).isFalse();
        verify(hotelCommentRepository, times(1)).updateStatus(eq(999L), eq("违规"));
    }

    @Test
    void deleteComment_ShouldDeleteComment() {
        doNothing().when(hotelCommentRepository).deleteById(1L);

        hotelCommentService.deleteComment(1L);

        verify(hotelCommentRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateHotelAverageRating_ShouldUpdateHotelRating() {
        when(hotelCommentRepository.findByHotelId(1L)).thenReturn(sampleComments);
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(sampleHotel));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(sampleHotel);

        hotelCommentService.save(sampleComment);

        verify(hotelRepository, times(1)).findById(1L);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }
}