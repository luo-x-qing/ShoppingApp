package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.service.HotelOrderService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HotelOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HotelOrderService hotelOrderService;

    @InjectMocks
    private HotelOrderController hotelOrderController;

    private ObjectMapper objectMapper;
    private HotelOrder sampleOrder;
    private List<HotelOrder> sampleOrders;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hotelOrderController).build();
        
        // 配置 ObjectMapper 支持 Java 8 时间类型
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        // 可选：禁用将日期写为时间戳，使用 ISO-8601 格式
        // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 准备测试数据
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
        sampleOrder.setContactPhone("13800138000");
        sampleOrder.setCreateTime(LocalDateTime.now());
        
        HotelOrder order2 = new HotelOrder();
        order2.setId(2L);
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
        
        sampleOrders = Arrays.asList(sampleOrder, order2);
    }

    @Test
    void list_ShouldReturnAllOrders() throws Exception {
        when(hotelOrderService.getAll()).thenReturn(sampleOrders);

        mockMvc.perform(get("/api/hotel-orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void create_WithValidOrder_ShouldReturnSuccess() throws Exception {
        when(hotelOrderService.createOrder(any(HotelOrder.class))).thenReturn(sampleOrder);

        mockMvc.perform(post("/api/hotel-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void create_WithMissingUsername_ShouldReturnError() throws Exception {
        sampleOrder.setUsername(null);

        mockMvc.perform(post("/api/hotel-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("用户未登录"));

        verify(hotelOrderService, never()).createOrder(any(HotelOrder.class));
    }

    @Test
    void create_WithMissingHotelId_ShouldReturnError() throws Exception {
        sampleOrder.setHotelId(null);

        mockMvc.perform(post("/api/hotel-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("酒店信息不完整"));

        verify(hotelOrderService, never()).createOrder(any(HotelOrder.class));
    }

    @Test
    void create_WithMissingRoomTypeId_ShouldReturnError() throws Exception {
        sampleOrder.setRoomTypeId(null);

        mockMvc.perform(post("/api/hotel-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("请选择房型"));

        verify(hotelOrderService, never()).createOrder(any(HotelOrder.class));
    }

    @Test
    void create_WithMissingCheckIn_ShouldReturnError() throws Exception {
        sampleOrder.setCheckIn(null);

        mockMvc.perform(post("/api/hotel-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("入住日期不能为空"));

        verify(hotelOrderService, never()).createOrder(any(HotelOrder.class));
    }

    @Test
    void create_WithMissingCheckOut_ShouldReturnError() throws Exception {
        sampleOrder.setCheckOut(null);

        mockMvc.perform(post("/api/hotel-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("退房日期不能为空"));

        verify(hotelOrderService, never()).createOrder(any(HotelOrder.class));
    }

    @Test
    void create_WhenServiceThrowsRuntimeException_ShouldReturnErrorMessage() throws Exception {
        when(hotelOrderService.createOrder(any(HotelOrder.class)))
                .thenThrow(new RuntimeException("房间库存不足"));

        mockMvc.perform(post("/api/hotel-orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleOrder)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("房间库存不足"));

        verify(hotelOrderService, times(1)).createOrder(any(HotelOrder.class));
    }

    @Test
    void getByUser_ShouldReturnUserOrders() throws Exception {
        when(hotelOrderService.getOrdersByUsername("testuser")).thenReturn(sampleOrders);

        mockMvc.perform(get("/api/hotel-orders/user")
                .param("username", "testuser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(hotelOrderService, times(1)).getOrdersByUsername("testuser");
    }

    @Test
    void payOrder_WithValidOrder_ShouldReturnSuccess() throws Exception {
        when(hotelOrderService.payOrder(1L)).thenReturn(true);

        mockMvc.perform(post("/api/hotel-orders/1/pay")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));

        verify(hotelOrderService, times(1)).payOrder(1L);
    }

    @Test
    void payOrder_WithInvalidOrder_ShouldReturnError() throws Exception {
        when(hotelOrderService.payOrder(999L)).thenReturn(false);

        mockMvc.perform(post("/api/hotel-orders/999/pay")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("支付失败，订单不存在或状态不正确"));

        verify(hotelOrderService, times(1)).payOrder(999L);
    }

    @Test
    void confirmOrder_WithValidOrder_ShouldReturnSuccess() throws Exception {
        when(hotelOrderService.confirmOrder(1L)).thenReturn(true);

        mockMvc.perform(post("/api/hotel-orders/1/confirm")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void confirmCheckIn_WithValidOrder_ShouldReturnSuccess() throws Exception {
        when(hotelOrderService.confirmCheckIn(1L)).thenReturn(true);

        mockMvc.perform(post("/api/hotel-orders/1/checkin")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void cancelRequest_WithValidRequest_ShouldReturnSuccess() throws Exception {
        when(hotelOrderService.cancelOrderRequest(eq(1L), eq("个人原因"), eq("testuser")))
                .thenReturn(true);

        mockMvc.perform(post("/api/hotel-orders/1/cancel-request")
                .param("username", "testuser")
                .param("reason", "个人原因")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void approveCancel_WithValidOrder_ShouldReturnSuccess() throws Exception {
        when(hotelOrderService.approveCancel(1L)).thenReturn(true);

        mockMvc.perform(post("/api/hotel-orders/1/cancel-approve")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void rejectCancel_WithValidRequest_ShouldReturnSuccess() throws Exception {
        when(hotelOrderService.rejectCancel(eq(1L), eq("已过取消期限")))
                .thenReturn(true);

        mockMvc.perform(post("/api/hotel-orders/1/cancel-reject")
                .param("reason", "已过取消期限")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void getOrdersByMerchantId_ShouldReturnOrders() throws Exception {
        when(hotelOrderService.getOrdersByMerchantId(10L)).thenReturn(sampleOrders);

        mockMvc.perform(get("/api/hotel-orders/merchant/orders")
                .param("merchantId", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void comment_WithValidData_ShouldReturnSuccess() throws Exception {
        when(hotelOrderService.addComment(eq(1L), eq("很好"), eq(5)))
                .thenReturn(sampleOrder);

        mockMvc.perform(post("/api/hotel-orders/comment/1")
                .param("comment", "很好")
                .param("rating", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void comment_WithInvalidOrder_ShouldReturnError() throws Exception {
        when(hotelOrderService.addComment(eq(999L), eq("很好"), eq(5)))
                .thenReturn(null);

        mockMvc.perform(post("/api/hotel-orders/comment/999")
                .param("comment", "很好")
                .param("rating", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("评价失败"));
    }

    @Test
    void confirmCheckOut_WithValidOrder_ShouldReturnSuccess() throws Exception {
        when(hotelOrderService.confirmCheckOut(1L)).thenReturn(true);

        mockMvc.perform(post("/api/hotel-orders/1/checkout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));
    }
}