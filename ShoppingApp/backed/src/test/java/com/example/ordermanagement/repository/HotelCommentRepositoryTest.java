package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.HotelComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class HotelCommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HotelCommentRepository hotelCommentRepository;

    private HotelComment sampleComment;

    @BeforeEach
    void setUp() {
        sampleComment = new HotelComment();
        sampleComment.setHotelId(1L);
        sampleComment.setOrderId(100L);
        sampleComment.setUsername("testuser");
        sampleComment.setContent("酒店环境很好，服务态度也不错！");
        sampleComment.setScore(5);
        sampleComment.setStatus("正常");
        sampleComment.setCreateTime(LocalDateTime.now());
    }

    @Test
    void save_ShouldPersistComment() {
        HotelComment savedComment = entityManager.persistAndFlush(sampleComment);

        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getUsername()).isEqualTo("testuser");
        assertThat(savedComment.getStatus()).isEqualTo("正常");
        assertThat(savedComment.getScore()).isEqualTo(5);
    }

    @Test
    void findByHotelId_ShouldReturnHotelComments() {
        entityManager.persistAndFlush(sampleComment);

        HotelComment comment2 = new HotelComment();
        comment2.setHotelId(1L);
        comment2.setOrderId(101L);
        comment2.setUsername("testuser2");
        comment2.setContent("位置很好，交通便利。");
        comment2.setScore(4);
        comment2.setStatus("正常");
        comment2.setCreateTime(LocalDateTime.now());
        entityManager.persistAndFlush(comment2);

        List<HotelComment> comments = hotelCommentRepository.findByHotelId(1L);

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getHotelId()).isEqualTo(1L);
    }

    @Test
    void existsByOrderId_ShouldReturnTrueIfExists() {
        entityManager.persistAndFlush(sampleComment);

        boolean exists = hotelCommentRepository.existsByOrderId(100L);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByOrderId_ShouldReturnFalseIfNotExists() {
        boolean exists = hotelCommentRepository.existsByOrderId(999L);

        assertThat(exists).isFalse();
    }

    @Test
    void findByStatus_ShouldReturnCommentsWithStatus() {
        entityManager.persistAndFlush(sampleComment);

        HotelComment violationComment = new HotelComment();
        violationComment.setHotelId(2L);
        violationComment.setOrderId(102L);
        violationComment.setUsername("testuser3");
        violationComment.setContent("违规内容");
        violationComment.setScore(3);
        violationComment.setStatus("违规");
        violationComment.setCreateTime(LocalDateTime.now());
        entityManager.persistAndFlush(violationComment);

        List<HotelComment> normalComments = hotelCommentRepository.findByStatus("正常");
        List<HotelComment> violationComments = hotelCommentRepository.findByStatus("违规");

        assertThat(normalComments).hasSize(1);
        assertThat(violationComments).hasSize(1);
        assertThat(violationComments.get(0).getStatus()).isEqualTo("违规");
    }

    @Test
    void getAvgScoreByHotelId_ShouldReturnAverageScore() {
        entityManager.persistAndFlush(sampleComment);

        HotelComment comment2 = new HotelComment();
        comment2.setHotelId(1L);
        comment2.setOrderId(101L);
        comment2.setUsername("testuser2");
        comment2.setContent("位置很好，交通便利。");
        comment2.setScore(4);
        comment2.setStatus("正常");
        comment2.setCreateTime(LocalDateTime.now());
        entityManager.persistAndFlush(comment2);

        Double avgScore = hotelCommentRepository.getAvgScoreByHotelId(1L);

        assertThat(avgScore).isEqualTo(4.5);
    }

    @Test
    void getCommentCountByHotelId_ShouldReturnCount() {
        entityManager.persistAndFlush(sampleComment);

        HotelComment comment2 = new HotelComment();
        comment2.setHotelId(1L);
        comment2.setOrderId(101L);
        comment2.setUsername("testuser2");
        comment2.setContent("位置很好，交通便利。");
        comment2.setScore(4);
        comment2.setStatus("正常");
        comment2.setCreateTime(LocalDateTime.now());
        entityManager.persistAndFlush(comment2);

        Integer count = hotelCommentRepository.getCommentCountByHotelId(1L);

        assertThat(count).isEqualTo(2);
    }

    @Test
    void findByHotelIds_ShouldReturnCommentsForHotels() {
        entityManager.persistAndFlush(sampleComment);

        HotelComment comment2 = new HotelComment();
        comment2.setHotelId(2L);
        comment2.setOrderId(101L);
        comment2.setUsername("testuser2");
        comment2.setContent("第二个酒店的评价");
        comment2.setScore(4);
        comment2.setStatus("正常");
        comment2.setCreateTime(LocalDateTime.now());
        entityManager.persistAndFlush(comment2);

        List<HotelComment> comments = hotelCommentRepository.findByHotelIds(Arrays.asList(1L, 2L));

        assertThat(comments).hasSize(2);
    }

    @Test
    void findByHotelIdsAndRating_ShouldReturnFilteredComments() {
        entityManager.persistAndFlush(sampleComment);

        HotelComment comment2 = new HotelComment();
        comment2.setHotelId(1L);
        comment2.setOrderId(101L);
        comment2.setUsername("testuser2");
        comment2.setContent("位置很好，交通便利。");
        comment2.setScore(4);
        comment2.setStatus("正常");
        comment2.setCreateTime(LocalDateTime.now());
        entityManager.persistAndFlush(comment2);

        List<HotelComment> comments = hotelCommentRepository.findByHotelIdsAndRating(Arrays.asList(1L), 5);

        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).getScore()).isEqualTo(5);
    }

    @Test
    @Transactional
    void updateStatus_ShouldUpdateCommentStatus() {
        // 保存实体
        HotelComment saved = entityManager.persistAndFlush(sampleComment);
        Long savedId = saved.getId();
        
        // 执行更新操作
        int updated = hotelCommentRepository.updateStatus(savedId, "违规");
        
        // 验证更新结果
        assertThat(updated).isEqualTo(1);

        // 刷新并清除持久化上下文，强制从数据库重新查询
        entityManager.flush();
        entityManager.clear();

        HotelComment updatedComment = hotelCommentRepository.findById(savedId).orElse(null);
        assertThat(updatedComment).isNotNull();
        assertThat(updatedComment.getStatus()).isEqualTo("违规");
    }

    @Test
    @Transactional
    void updateReply_ShouldUpdateCommentReply() {
        // 保存实体
        HotelComment saved = entityManager.persistAndFlush(sampleComment);
        Long savedId = saved.getId();
        
        // 执行更新操作
        int updated = hotelCommentRepository.updateReply(savedId, "感谢您的评价！");
        
        // 验证更新结果
        assertThat(updated).isEqualTo(1);

        // 刷新并清除持久化上下文，强制从数据库重新查询
        entityManager.flush();
        entityManager.clear();

        HotelComment updatedComment = hotelCommentRepository.findById(savedId).orElse(null);
        assertThat(updatedComment).isNotNull();
        assertThat(updatedComment.getReply()).isEqualTo("感谢您的评价！");
        assertThat(updatedComment.getReplyTime()).isNotNull();
    }
}