package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.repository.HotelOrderRepository;
import com.example.ordermanagement.repository.HotelRepository;
import com.example.ordermanagement.repository.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelOrderServiceTest {

    @Mock
    private HotelOrderRepository hotelOrderRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private SystemNoticeService systemNoticeService;

    @InjectMocks
    private HotelOrderService hotelOrderService;

    private HotelOrder sampleOrder;
    private RoomType sampleRoomType;
    private Hotel sampleHotel;

    @BeforeEach
    void setUp() {
        sampleOrder = new HotelOrder();
        sampleOrder.setId(1L);
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
        sampleOrder.setCreateTime(LocalDateTime.now());

        sampleRoomType = new RoomType();
        sampleRoomType.setId(200L);
        sampleRoomType.setTypeName("豪华大床房");
        sampleRoomType.setAvailableCount(10);
        sampleRoomType.setPrice(500.0);

        sampleHotel = new Hotel();
        sampleHotel.setId(100L);
        sampleHotel.setName("测试酒店");
        sampleHotel.setMerchantId(10L);
    }

    @Test
    void getAll_ShouldReturnAllOrders() {
        List<HotelOrder> expectedOrders = Arrays.asList(sampleOrder);
        when(hotelOrderRepository.findAllByOrderByIdDesc()).thenReturn(expectedOrders);

        List<HotelOrder> actualOrders = hotelOrderService.getAll();

        assertThat(actualOrders).isNotEmpty();
        assertThat(actualOrders.size()).isEqualTo(1);
        verify(hotelOrderRepository, times(1)).findAllByOrderByIdDesc();
    }

    @Test
    void getOrdersByUsername_ShouldReturnUserOrders() {
        when(hotelOrderRepository.findByUsernameOrderByIdDesc("testuser"))
                .thenReturn(Arrays.asList(sampleOrder));

        List<HotelOrder> orders = hotelOrderService.getOrdersByUsername("testuser");

        assertThat(orders).isNotEmpty();
        assertThat(orders.get(0).getUsername()).isEqualTo("testuser");
        verify(hotelOrderRepository, times(1)).findByUsernameOrderByIdDesc("testuser");
    }

    @Test
    void getById_WithValidId_ShouldReturnOrder() {
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        HotelOrder order = hotelOrderService.getById(1L);

        assertThat(order).isNotNull();
        assertThat(order.getId()).isEqualTo(1L);
        verify(hotelOrderRepository, times(1)).findById(1L);
    }

    @Test
    void createOrder_WithValidData_ShouldCreateOrder() {
        when(roomTypeRepository.findById(200L)).thenReturn(Optional.of(sampleRoomType));
        when(roomTypeRepository.decreaseAvailableCount(200L, 2)).thenReturn(1);
        when(hotelRepository.findById(100L)).thenReturn(Optional.of(sampleHotel));
        when(hotelOrderRepository.save(any(HotelOrder.class))).thenReturn(sampleOrder);

        HotelOrder createdOrder = hotelOrderService.createOrder(sampleOrder);

        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getMerchantId()).isEqualTo(10L);
        verify(roomTypeRepository, times(1)).findById(200L);
        verify(roomTypeRepository, times(1)).decreaseAvailableCount(200L, 2);
        verify(hotelOrderRepository, times(1)).save(any(HotelOrder.class));
    }

    @Test
    void createOrder_WithRoomTypeNotFound_ShouldThrowException() {
        when(roomTypeRepository.findById(200L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hotelOrderService.createOrder(sampleOrder))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("房间类型不存在");
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        sampleRoomType.setAvailableCount(1);
        when(roomTypeRepository.findById(200L)).thenReturn(Optional.of(sampleRoomType));

        assertThatThrownBy(() -> hotelOrderService.createOrder(sampleOrder))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("房间库存不足");
    }

    @Test
    void payOrder_WithValidOrder_ShouldSucceed() {
        sampleOrder.setStatus("待支付");
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(hotelOrderRepository.save(any(HotelOrder.class))).thenReturn(sampleOrder);

        boolean result = hotelOrderService.payOrder(1L);

        assertThat(result).isTrue();
        assertThat(sampleOrder.getStatus()).isEqualTo("待确认");
        verify(hotelOrderRepository, times(1)).save(any(HotelOrder.class));
    }

    @Test
    void payOrder_WithInvalidOrder_ShouldReturnFalse() {
        sampleOrder.setStatus("已确认");
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        boolean result = hotelOrderService.payOrder(1L);

        assertThat(result).isFalse();
        verify(hotelOrderRepository, never()).save(any(HotelOrder.class));
    }

    @Test
    void confirmOrder_WithValidOrder_ShouldSucceed() {
        sampleOrder.setStatus("待确认");
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(hotelOrderRepository.save(any(HotelOrder.class))).thenReturn(sampleOrder);

        boolean result = hotelOrderService.confirmOrder(1L);

        assertThat(result).isTrue();
        assertThat(sampleOrder.getStatus()).isEqualTo("已确认");
        assertThat(sampleOrder.getConfirmTime()).isNotNull();
        verify(hotelOrderRepository, times(1)).save(any(HotelOrder.class));
    }

    @Test
    void confirmCheckIn_WithValidOrder_ShouldSucceed() {
        sampleOrder.setStatus("已确认");
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(hotelOrderRepository.save(any(HotelOrder.class))).thenReturn(sampleOrder);

        boolean result = hotelOrderService.confirmCheckIn(1L);

        assertThat(result).isTrue();
        assertThat(sampleOrder.getStatus()).isEqualTo("已入住");
        verify(hotelOrderRepository, times(1)).save(any(HotelOrder.class));
    }

    @Test
    void cancelOrderRequest_WithValidOrder_ShouldSucceed() {
        sampleOrder.setStatus("已支付");
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(hotelOrderRepository.save(any(HotelOrder.class))).thenReturn(sampleOrder);

        boolean result = hotelOrderService.cancelOrderRequest(1L, "个人原因", "testuser");

        assertThat(result).isTrue();
        assertThat(sampleOrder.getStatus()).isEqualTo("取消申请中");
        assertThat(sampleOrder.getCancelReason()).isEqualTo("个人原因");
        verify(hotelOrderRepository, times(1)).save(any(HotelOrder.class));
    }

    @Test
    void cancelOrderRequest_WithWrongUsername_ShouldReturnFalse() {
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));

        boolean result = hotelOrderService.cancelOrderRequest(1L, "个人原因", "wronguser");

        assertThat(result).isFalse();
        verify(hotelOrderRepository, never()).save(any(HotelOrder.class));
    }

    @Test
    void approveCancel_WithValidOrder_ShouldSucceed() {
        sampleOrder.setStatus("取消申请中");
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(hotelOrderRepository.save(any(HotelOrder.class))).thenReturn(sampleOrder);
        when(roomTypeRepository.increaseAvailableCount(200L, 2)).thenReturn(1);

        boolean result = hotelOrderService.approveCancel(1L);

        assertThat(result).isTrue();
        assertThat(sampleOrder.getStatus()).isEqualTo("已取消");
        assertThat(sampleOrder.getCancelTime()).isNotNull();
        verify(hotelOrderRepository, times(1)).save(any(HotelOrder.class));
    }

    @Test
    void rejectCancel_WithValidOrder_ShouldSucceed() {
        sampleOrder.setStatus("取消申请中");
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(hotelOrderRepository.save(any(HotelOrder.class))).thenReturn(sampleOrder);

        boolean result = hotelOrderService.rejectCancel(1L, "已过取消期限");

        assertThat(result).isTrue();
        assertThat(sampleOrder.getStatus()).isEqualTo("已支付");
        assertThat(sampleOrder.getCancelReason()).contains("拒绝取消");
        verify(hotelOrderRepository, times(1)).save(any(HotelOrder.class));
    }

    @Test
    void getOrdersByMerchantId_ShouldReturnOrders() {
        when(hotelOrderRepository.findByMerchantId(10L)).thenReturn(Arrays.asList(sampleOrder));

        List<HotelOrder> orders = hotelOrderService.getOrdersByMerchantId(10L);

        assertThat(orders).isNotEmpty();
        assertThat(orders.get(0).getMerchantId()).isEqualTo(10L);
        verify(hotelOrderRepository, times(1)).findByMerchantId(10L);
    }

    @Test
    void addComment_WithValidData_ShouldSucceed() {
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(hotelOrderRepository.save(any(HotelOrder.class))).thenReturn(sampleOrder);

        HotelOrder updatedOrder = hotelOrderService.addComment(1L, "很好", 5);

        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getComment()).isEqualTo("很好");
        assertThat(updatedOrder.getRating()).isEqualTo(5);
        assertThat(updatedOrder.getStatus()).isEqualTo("已完成");
        verify(hotelOrderRepository, times(1)).save(any(HotelOrder.class));
    }

    @Test
    void confirmCheckOut_WithValidOrder_ShouldSucceed() {
        sampleOrder.setStatus("已入住");
        when(hotelOrderRepository.findById(1L)).thenReturn(Optional.of(sampleOrder));
        when(hotelOrderRepository.save(any(HotelOrder.class))).thenReturn(sampleOrder);

        boolean result = hotelOrderService.confirmCheckOut(1L);

        assertThat(result).isTrue();
        assertThat(sampleOrder.getStatus()).isEqualTo("已完成");
        verify(hotelOrderRepository, times(1)).save(any(HotelOrder.class));
    }

    @Test
    void countAll_ShouldReturnCount() {
        when(hotelOrderRepository.count()).thenReturn(10L);

        long count = hotelOrderService.countAll();

        assertThat(count).isEqualTo(10L);
        verify(hotelOrderRepository, times(1)).count();
    }

    @Test
    void countByStatus_ShouldReturnCount() {
        when(hotelOrderRepository.findByStatus("待支付")).thenReturn(Arrays.asList(sampleOrder));

        long count = hotelOrderService.countByStatus("待支付");

        assertThat(count).isEqualTo(1);
        verify(hotelOrderRepository, times(1)).findByStatus("待支付");
    }
}