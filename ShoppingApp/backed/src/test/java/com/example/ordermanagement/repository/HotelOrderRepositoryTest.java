package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.HotelOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class HotelOrderRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HotelOrderRepository hotelOrderRepository;

    private HotelOrder sampleOrder;

    @BeforeEach
    void setUp() {
        sampleOrder = new HotelOrder();
        sampleOrder.setUsername("testuser");
        sampleOrder.setHotelId(100L);
        sampleOrder.setMerchantId(10L);
        sampleOrder.setRoomTypeId(200L);
        sampleOrder.setRoomTypeName("豪华大床房");
        sampleOrder.setRoomCount(2);
        sampleOrder.setCheckIn(LocalDate.now().toString());
        sampleOrder.setCheckOut(LocalDate.now().plusDays(2).toString());
        sampleOrder.setStatus("待支付");
        sampleOrder.setPrice(500.0);
        sampleOrder.setName("测试酒店");
        sampleOrder.setContactPhone("13800138000");
        sampleOrder.setCreateTime(LocalDateTime.now());
    }

    @Test
    void save_ShouldPersistOrder() {
        HotelOrder savedOrder = entityManager.persistAndFlush(sampleOrder);

        assertThat(savedOrder.getId()).isNotNull();
        assertThat(savedOrder.getUsername()).isEqualTo("testuser");
        assertThat(savedOrder.getStatus()).isEqualTo("待支付");
    }

    @Test
    void findAllByOrderByIdDesc_ShouldReturnOrdersInDescOrder() {
        entityManager.persistAndFlush(sampleOrder);
        
        HotelOrder order2 = new HotelOrder();
        order2.setUsername("testuser2");
        order2.setHotelId(101L);
        order2.setMerchantId(10L);
        order2.setRoomTypeId(201L);
        order2.setRoomTypeName("标准双床房");
        order2.setRoomCount(1);
        order2.setCheckIn(LocalDate.now().toString());
        order2.setCheckOut(LocalDate.now().plusDays(3).toString());
        order2.setStatus("待支付");
        order2.setPrice(350.0);
        order2.setName("测试酒店2");
        order2.setCreateTime(LocalDateTime.now());
        entityManager.persistAndFlush(order2);

        List<HotelOrder> orders = hotelOrderRepository.findAllByOrderByIdDesc();

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getId()).isGreaterThan(orders.get(1).getId());
    }

    @Test
    void findByUsernameOrderByIdDesc_ShouldReturnUserOrders() {
        entityManager.persistAndFlush(sampleOrder);

        HotelOrder order2 = new HotelOrder();
        order2.setUsername("testuser");
        order2.setHotelId(101L);
        order2.setMerchantId(10L);
        order2.setRoomTypeId(201L);
        order2.setRoomTypeName("标准双床房");
        order2.setRoomCount(1);
        order2.setCheckIn(LocalDate.now().toString());
        order2.setCheckOut(LocalDate.now().plusDays(3).toString());
        order2.setStatus("已确认");
        order2.setPrice(350.0);
        order2.setName("测试酒店2");
        order2.setCreateTime(LocalDateTime.now());
        entityManager.persistAndFlush(order2);

        List<HotelOrder> orders = hotelOrderRepository.findByUsernameOrderByIdDesc("testuser");

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void findByHotelId_ShouldReturnOrdersForHotel() {
        entityManager.persistAndFlush(sampleOrder);

        List<HotelOrder> orders = hotelOrderRepository.findByHotelId(100L);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getHotelId()).isEqualTo(100L);
    }

    @Test
    void findByStatus_ShouldReturnOrdersWithStatus() {
        entityManager.persistAndFlush(sampleOrder);

        List<HotelOrder> orders = hotelOrderRepository.findByStatus("待支付");

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo("待支付");
    }

    @Test
    void findByMerchantId_ShouldReturnOrdersForMerchant() {
        entityManager.persistAndFlush(sampleOrder);

        List<HotelOrder> orders = hotelOrderRepository.findByMerchantId(10L);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getMerchantId()).isEqualTo(10L);
    }

    @Test
    void findByHotelIds_ShouldReturnOrdersForHotelIds() {
        entityManager.persistAndFlush(sampleOrder);

        // 修改这里：使用 Arrays.asList() 替代 List.of()
        List<HotelOrder> orders = hotelOrderRepository.findByHotelIds(Arrays.asList(100L, 101L));

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getHotelId()).isIn(100L, 101L);
    }
}