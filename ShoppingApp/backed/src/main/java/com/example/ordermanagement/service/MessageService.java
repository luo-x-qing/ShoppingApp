package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Message;
import com.example.ordermanagement.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Transactional
    public Message sendMessage(Message message) {
        message.setCreateTime(null);
        return messageRepository.save(message);
    }

    public List<Message> getChatMessages(String username, String merchantId, Long hotelId) {
        return messageRepository.findChatMessages(username, merchantId, hotelId);
    }

    public Integer getUserUnreadCount(String username, String merchantId) {
        return messageRepository.countUnreadForUser(username, merchantId);
    }

    public Integer getMerchantUnreadCount(String merchantId, String username) {
        return messageRepository.countUnreadForMerchant(merchantId, username);
    }

    @Transactional
    public void markAsReadForUser(String username, String merchantId) {
        messageRepository.markAsReadForUser(username, merchantId);
    }

    @Transactional
    public void markAsReadForMerchant(String merchantId, String username) {
        messageRepository.markAsReadForMerchant(merchantId, username);
    }

    public List<Object[]> findMerchantSessions(String merchantId) {
        return messageRepository.findMerchantSessions(merchantId);
    }

    public Integer getMerchantTotalUnreadCount(String merchantId) {
        return messageRepository.countTotalUnreadForMerchant(merchantId);
    }
}
