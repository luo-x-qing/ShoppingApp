package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.model.Message;
import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.service.HotelOrderService;
import com.example.ordermanagement.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private HotelOrderService hotelOrderService;

    @PostMapping("/send")
    public Result<Message> sendMessage(@RequestBody Message message) {
        try {
            Message saved = messageService.sendMessage(message);
            return Result.success(saved);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发送失败：" + e.getMessage());
        }
    }

    @GetMapping("/chat")
    public Result<List<Message>> getChatMessages(
            @RequestParam String username,
            @RequestParam String merchantId,
            @RequestParam(required = false) Long hotelId) {
        try {
            List<Message> messages = messageService.getChatMessages(username, merchantId, hotelId);
            return Result.success(messages);
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/merchant/conversations")
    public Result<List<Map<String, Object>>> getMerchantConversations(@RequestParam String merchantId) {
        try {
            List<Object[]> sessions = messageService.findMerchantSessions(merchantId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (Object[] session : sessions) {
                String username = (String) session[0];
                Long orderId = (Long) session[1];

                HotelOrder order = hotelOrderService.getById(orderId);
                String hotelName = order != null && order.getName() != null ? order.getName() : "未知酒店";
                Long hotelId = order != null ? order.getHotelId() : null;
                String contactName = order != null && order.getName() != null ? order.getName() : username;

                List<Message> messages = messageService.getChatMessages(username, merchantId, hotelId);

                Map<String, Object> conv = new HashMap<>();
                conv.put("userId", username);
                conv.put("userName", contactName);
                conv.put("hotelName", hotelName);
                conv.put("hotelId", hotelId);
                conv.put("orderId", orderId);
                conv.put("merchantId", merchantId);

                if (!messages.isEmpty()) {
                    Message lastMsg = messages.get(messages.size() - 1);
                    conv.put("lastMessage", lastMsg.getContent());
                    conv.put("lastSender", lastMsg.getSenderRole());
                    conv.put("lastTime", lastMsg.getCreateTime());
                } else {
                    conv.put("lastMessage", "暂无消息");
                    conv.put("lastSender", "");
                    conv.put("lastTime", new Date());
                }

                Integer unreadCount = messageService.getMerchantUnreadCount(merchantId, username);
                conv.put("unreadCount", unreadCount != null ? unreadCount : 0);

                result.add(conv);
            }

            result.sort((a, b) -> {
                Date timeA = (Date) a.get("lastTime");
                Date timeB = (Date) b.get("lastTime");
                return timeB.compareTo(timeA);
            });

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/unread")
    public Result<Map<String, Integer>> getUnreadCount(
            @RequestParam String role,
            @RequestParam String identifier,
            @RequestParam String otherParty) {
        try {
            Map<String, Integer> result = new HashMap<>();
            Integer count;
            if ("user".equals(role)) {
                count = messageService.getUserUnreadCount(identifier, otherParty);
            } else {
                count = messageService.getMerchantUnreadCount(identifier, otherParty);
            }
            result.put("unreadCount", count != null ? count : 0);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @PostMapping("/read")
    public Result<Map<String, Object>> markAsRead(
            @RequestParam String role,
            @RequestParam String identifier,
            @RequestParam String otherParty) {
        try {
            if ("user".equals(role)) {
                messageService.markAsReadForUser(identifier, otherParty);
            } else {
                messageService.markAsReadForMerchant(identifier, otherParty);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    @PostMapping("/merchant/read-all")
    public Result<Map<String, Object>> markAllAsRead(@RequestParam String merchantId) {
        try {
            List<Object[]> sessions = messageService.findMerchantSessions(merchantId);
            for (Object[] session : sessions) {
                String username = (String) session[0];
                messageService.markAsReadForMerchant(merchantId, username);
            }
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("操作失败：" + e.getMessage());
        }
    }
}
