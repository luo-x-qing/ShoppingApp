package com.example.ordermanagement.service;

import com.example.ordermanagement.model.FlightOrder;
import com.example.ordermanagement.repository.FlightOrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightOrderServiceTest {

    @Mock
    private FlightOrderRepository flightOrderRepository;

    @Mock
    private SystemNoticeService systemNoticeService;

    @InjectMocks
    private FlightOrderService flightOrderService;

    private FlightOrder createMockOrder(Long id, String username, String status) {
        FlightOrder order = new FlightOrder();
        order.setId(id);
        order.setFlightId(10L);
        order.setFlightNumber("CA1234");
        order.setDepartCity("北京");
        order.setArriveCity("上海");
        order.setDepartTime(LocalDateTime.now().plusDays(1));
        order.setArriveTime(LocalDateTime.now().plusDays(1).plusHours(2));
        order.setPrice(1200.0);
        order.setUsername(username);
        order.setPassengerName("张三");
        order.setPassengerIdCard("110101199001011234");
        order.setContactPhone("13800138000");
        order.setStatus(status);
        order.setCreateTime(LocalDateTime.now());
        return order;
    }

    @Test
    void createOrder_shouldSetDefaultsAndSave() {
        FlightOrder input = new FlightOrder();
        input.setFlightId(10L);
        input.setUsername("test_user");
        input.setPassengerName("张三");
        input.setContactPhone("13800138000");

        FlightOrder saved = createMockOrder(1L, "test_user", "待支付");
        when(flightOrderRepository.save(any(FlightOrder.class))).thenReturn(saved);

        FlightOrder result = flightOrderService.createOrder(input);

        assertNotNull(result);
        assertEquals("待支付", result.getStatus());
        verify(flightOrderRepository).save(any(FlightOrder.class));
    }

    @Test
    void getOrderById_whenExists_shouldReturnOrder() {
        FlightOrder expected = createMockOrder(1L, "test_user", "待支付");
        when(flightOrderRepository.findById(1L)).thenReturn(Optional.of(expected));

        Optional<FlightOrder> result = flightOrderService.getOrderById(1L);

        assertTrue(result.isPresent());
        assertEquals("test_user", result.get().getUsername());
    }

    @Test
    void getOrderById_whenNotExists_shouldReturnEmpty() {
        when(flightOrderRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<FlightOrder> result = flightOrderService.getOrderById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void getOrdersByUsername_shouldReturnOrders() {
        List<FlightOrder> expected = Arrays.asList(
            createMockOrder(1L, "test_user", "待支付"),
            createMockOrder(2L, "test_user", "已支付")
        );
        when(flightOrderRepository.findByUsernameOrderByCreateTimeDesc("test_user")).thenReturn(expected);

        List<FlightOrder> result = flightOrderService.getOrdersByUsername("test_user");

        assertEquals(2, result.size());
    }

    @Test
    void getAllOrders_shouldReturnAllOrders() {
        List<FlightOrder> expected = Arrays.asList(
            createMockOrder(1L, "user1", "待支付"),
            createMockOrder(2L, "user2", "已支付")
        );
        when(flightOrderRepository.findAll()).thenReturn(expected);

        List<FlightOrder> result = flightOrderService.getAllOrders();

        assertEquals(2, result.size());
    }

    @Test
    void updateOrderStatus_toPaid_shouldSetPayTimeAndCreateNotice() {
        FlightOrder order = createMockOrder(1L, "test_user", "待支付");
        when(flightOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(flightOrderRepository.save(any(FlightOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightOrder result = flightOrderService.updateOrderStatus(1L, "已支付");

        assertNotNull(result);
        assertEquals("已支付", result.getStatus());
        assertNotNull(result.getPayTime());
        verify(systemNoticeService).createFlightOrderNotice(
            eq("test_user"), eq(1L), eq("北京"), eq("上海"),
            eq("CA1234"), anyString(), eq(1200.0)
        );
    }

    @Test
    void updateOrderStatus_toCancelled_whenPending_shouldSucceed() {
        FlightOrder order = createMockOrder(1L, "test_user", "待支付");
        when(flightOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(flightOrderRepository.save(any(FlightOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightOrder result = flightOrderService.updateOrderStatus(1L, "已取消");

        assertNotNull(result);
        assertEquals("已取消", result.getStatus());
        assertNotNull(result.getCancelTime());
    }

    @Test
    void updateOrderStatus_toCancelled_whenNotPending_shouldThrow() {
        FlightOrder order = createMockOrder(1L, "test_user", "已支付");
        when(flightOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> flightOrderService.updateOrderStatus(1L, "已取消"));
        assertEquals("只有待支付的订单才能取消", exception.getMessage());
    }

    @Test
    void updateOrderStatus_whenOrderNotFound_shouldReturnNull() {
        when(flightOrderRepository.findById(999L)).thenReturn(Optional.empty());

        FlightOrder result = flightOrderService.updateOrderStatus(999L, "已支付");

        assertNull(result);
    }

    @Test
    void cancelOrder_whenPending_shouldSucceed() {
        FlightOrder order = createMockOrder(1L, "test_user", "待支付");
        when(flightOrderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(flightOrderRepository.save(any(FlightOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FlightOrder result = flightOrderService.cancelOrder(1L);

        assertNotNull(result);
        assertEquals("已取消", result.getStatus());
        assertNotNull(result.getCancelTime());
    }

    @Test
    void cancelOrder_whenNotPending_shouldThrow() {
        FlightOrder order = createMockOrder(1L, "test_user", "已支付");
        when(flightOrderRepository.findById(1L)).thenReturn(Optional.of(order));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> flightOrderService.cancelOrder(1L));
        assertEquals("只有待支付的订单才能取消", exception.getMessage());
    }

    @Test
    void cancelOrder_whenNotFound_shouldReturnNull() {
        when(flightOrderRepository.findById(999L)).thenReturn(Optional.empty());

        FlightOrder result = flightOrderService.cancelOrder(999L);

        assertNull(result);
    }

    @Test
    void deleteOrder_whenExists_shouldReturnTrue() {
        when(flightOrderRepository.existsById(1L)).thenReturn(true);

        boolean result = flightOrderService.deleteOrder(1L);

        assertTrue(result);
        verify(flightOrderRepository).deleteById(1L);
    }

    @Test
    void deleteOrder_whenNotExists_shouldReturnFalse() {
        when(flightOrderRepository.existsById(999L)).thenReturn(false);

        boolean result = flightOrderService.deleteOrder(999L);

        assertFalse(result);
        verify(flightOrderRepository, never()).deleteById(anyLong());
    }
}
