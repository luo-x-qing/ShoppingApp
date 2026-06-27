package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.FlightOrder;
import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.service.FlightOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightOrderControllerTest {

    @Mock
    private FlightOrderService flightOrderService;

    @InjectMocks
    private FlightOrderController flightOrderController;

    private FlightOrder createValidOrder() {
        FlightOrder order = new FlightOrder();
        order.setId(1L);
        order.setFlightNumber("CA1234");
        order.setDepartCity("北京");
        order.setArriveCity("上海");
        order.setPrice(1200.0);
        order.setUsername("test_user");
        order.setPassengerName("张三");
        order.setContactPhone("13800138000");
        order.setStatus("待支付");
        return order;
    }

    @Test
    void createOrder_whenValid_shouldReturnSuccess() {
        FlightOrder input = new FlightOrder();
        input.setUsername("test_user");
        input.setPassengerName("张三");
        input.setContactPhone("13800138000");
        input.setFlightNumber("CA1234");

        FlightOrder saved = createValidOrder();
        when(flightOrderService.createOrder(any(FlightOrder.class))).thenReturn(saved);

        Result<FlightOrder> result = flightOrderController.createOrder(input);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals(1L, result.getData().getId());
        verify(flightOrderService).createOrder(any(FlightOrder.class));
    }

    @Test
    void createOrder_whenUsernameEmpty_shouldReturnError() {
        FlightOrder input = new FlightOrder();
        input.setUsername("");
        input.setPassengerName("张三");
        input.setContactPhone("13800138000");
        input.setFlightNumber("CA1234");

        Result<FlightOrder> result = flightOrderController.createOrder(input);

        assertEquals(500, result.getCode());
        assertEquals("用户未登录", result.getMessage());
        verifyNoInteractions(flightOrderService);
    }

    @Test
    void createOrder_whenPassengerNameEmpty_shouldReturnError() {
        FlightOrder input = new FlightOrder();
        input.setUsername("test_user");
        input.setPassengerName("");
        input.setContactPhone("13800138000");
        input.setFlightNumber("CA1234");

        Result<FlightOrder> result = flightOrderController.createOrder(input);

        assertEquals(500, result.getCode());
        assertEquals("乘客姓名不能为空", result.getMessage());
        verifyNoInteractions(flightOrderService);
    }

    @Test
    void createOrder_whenContactPhoneEmpty_shouldReturnError() {
        FlightOrder input = new FlightOrder();
        input.setUsername("test_user");
        input.setPassengerName("张三");
        input.setContactPhone("");
        input.setFlightNumber("CA1234");

        Result<FlightOrder> result = flightOrderController.createOrder(input);

        assertEquals(500, result.getCode());
        assertEquals("联系电话不能为空", result.getMessage());
        verifyNoInteractions(flightOrderService);
    }

    @Test
    void createOrder_whenFlightNumberEmpty_shouldReturnError() {
        FlightOrder input = new FlightOrder();
        input.setUsername("test_user");
        input.setPassengerName("张三");
        input.setContactPhone("13800138000");
        input.setFlightNumber("");

        Result<FlightOrder> result = flightOrderController.createOrder(input);

        assertEquals(500, result.getCode());
        assertEquals("航班信息不完整", result.getMessage());
        verifyNoInteractions(flightOrderService);
    }

    @Test
    void createOrder_whenServiceThrows_shouldReturnError() {
        FlightOrder input = new FlightOrder();
        input.setUsername("test_user");
        input.setPassengerName("张三");
        input.setContactPhone("13800138000");
        input.setFlightNumber("CA1234");

        when(flightOrderService.createOrder(any(FlightOrder.class)))
            .thenThrow(new RuntimeException("DB错误"));

        Result<FlightOrder> result = flightOrderController.createOrder(input);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("创建订单失败"));
    }

    @Test
    void getOrdersByUsername_withUsername_shouldReturnUserOrders() {
        List<FlightOrder> orders = Arrays.asList(createValidOrder());
        when(flightOrderService.getOrdersByUsername("test_user")).thenReturn(orders);

        Result<List<FlightOrder>> result = flightOrderController.getOrdersByUsername("test_user");

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void getOrdersByUsername_withoutUsername_shouldReturnAllOrders() {
        List<FlightOrder> orders = Arrays.asList(createValidOrder());
        when(flightOrderService.getAllOrders()).thenReturn(orders);

        Result<List<FlightOrder>> result = flightOrderController.getOrdersByUsername(null);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void getOrdersByUsername_whenEmptyUsername_shouldReturnAllOrders() {
        List<FlightOrder> orders = Arrays.asList(createValidOrder());
        when(flightOrderService.getAllOrders()).thenReturn(orders);

        Result<List<FlightOrder>> result = flightOrderController.getOrdersByUsername("");

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
    }

    @Test
    void getOrderById_whenExists_shouldReturnOrder() {
        FlightOrder order = createValidOrder();
        when(flightOrderService.getOrderById(1L)).thenReturn(Optional.of(order));

        Result<FlightOrder> result = flightOrderController.getOrderById(1L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
    }

    @Test
    void getOrderById_whenNotExists_shouldReturnError() {
        when(flightOrderService.getOrderById(999L)).thenReturn(Optional.empty());

        Result<FlightOrder> result = flightOrderController.getOrderById(999L);

        assertEquals(500, result.getCode());
        assertEquals("订单不存在", result.getMessage());
    }

    @Test
    void payOrder_whenSuccessful_shouldReturnSuccess() {
        FlightOrder paid = createValidOrder();
        paid.setStatus("已支付");
        when(flightOrderService.updateOrderStatus(1L, "已支付")).thenReturn(paid);

        Result<FlightOrder> result = flightOrderController.payOrder(1L);

        assertEquals(200, result.getCode());
        assertEquals("已支付", result.getData().getStatus());
    }

    @Test
    void payOrder_whenOrderNotFound_shouldReturnError() {
        when(flightOrderService.updateOrderStatus(999L, "已支付")).thenReturn(null);

        Result<FlightOrder> result = flightOrderController.payOrder(999L);

        assertEquals(500, result.getCode());
        assertEquals("订单不存在", result.getMessage());
    }

    @Test
    void payOrder_whenServiceThrows_shouldReturnError() {
        when(flightOrderService.updateOrderStatus(1L, "已支付"))
            .thenThrow(new RuntimeException("支付失败"));

        Result<FlightOrder> result = flightOrderController.payOrder(1L);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("支付失败"));
    }

    @Test
    void cancelOrder_whenSuccessful_shouldReturnSuccess() {
        FlightOrder cancelled = createValidOrder();
        cancelled.setStatus("已取消");
        when(flightOrderService.cancelOrder(1L)).thenReturn(cancelled);

        Result<FlightOrder> result = flightOrderController.cancelOrder(1L);

        assertEquals(200, result.getCode());
        assertEquals("已取消", result.getData().getStatus());
    }

    @Test
    void cancelOrder_whenNotFound_shouldReturnError() {
        when(flightOrderService.cancelOrder(999L)).thenReturn(null);

        Result<FlightOrder> result = flightOrderController.cancelOrder(999L);

        assertEquals(500, result.getCode());
        assertEquals("订单不存在或无法取消", result.getMessage());
    }

    @Test
    void cancelOrder_whenNotPending_shouldReturnError() {
        when(flightOrderService.cancelOrder(1L))
            .thenThrow(new RuntimeException("只有待支付的订单才能取消"));

        Result<FlightOrder> result = flightOrderController.cancelOrder(1L);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("取消失败"));
    }

    @Test
    void updateOrderStatus_whenSuccessful_shouldReturnSuccess() {
        FlightOrder updated = createValidOrder();
        updated.setStatus("已出票");
        when(flightOrderService.updateOrderStatus(1L, "已出票")).thenReturn(updated);

        Result<FlightOrder> result = flightOrderController.updateOrderStatus(1L, "已出票");

        assertEquals(200, result.getCode());
        assertEquals("已出票", result.getData().getStatus());
    }

    @Test
    void updateOrderStatus_whenNotFound_shouldReturnError() {
        when(flightOrderService.updateOrderStatus(999L, "已出票")).thenReturn(null);

        Result<FlightOrder> result = flightOrderController.updateOrderStatus(999L, "已出票");

        assertEquals(500, result.getCode());
        assertEquals("订单不存在", result.getMessage());
    }

    @Test
    void deleteOrder_whenSuccessful_shouldReturnSuccess() {
        when(flightOrderService.deleteOrder(1L)).thenReturn(true);

        Result<String> result = flightOrderController.deleteOrder(1L);

        assertEquals(200, result.getCode());
        assertEquals("删除成功", result.getData());
    }

    @Test
    void deleteOrder_whenNotFound_shouldReturnError() {
        when(flightOrderService.deleteOrder(999L)).thenReturn(false);

        Result<String> result = flightOrderController.deleteOrder(999L);

        assertEquals(500, result.getCode());
        assertEquals("订单不存在", result.getMessage());
    }

    @Test
    void deleteOrder_whenServiceThrows_shouldReturnError() {
        when(flightOrderService.deleteOrder(1L))
            .thenThrow(new RuntimeException("删除失败"));

        Result<String> result = flightOrderController.deleteOrder(1L);

        assertEquals(500, result.getCode());
        assertTrue(result.getMessage().contains("删除失败"));
    }
}
