package com.example.demo.service;

import com.example.demo.model.Appeal;
import com.example.demo.model.AppealRequest;
import com.example.demo.repository.AppealRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppealService {

    @Autowired
    private AppealRepository appealRepository;

    @Transactional
    public Appeal submitAppeal(AppealRequest request) {
        Appeal appeal = new Appeal();
        appeal.setUsername(request.getUsername());
        appeal.setShopName(request.getShopName());
        appeal.setStatus(request.getStatus());
        appeal.setType(request.getType());
        appeal.setContent(request.getContent());
        appeal.setContact(request.getContact());
        appeal.setCreateTime(LocalDateTime.now());
        appeal.setReplyStatus("PENDING");
        return appealRepository.save(appeal);
    }

    public List<Appeal> getAllAppeals() {
        return appealRepository.findAll();
    }

    public Appeal getAppealById(Long id) {
        return appealRepository.findById(id).orElse(null);
    }

    public List<Appeal> getPendingAppeals() {
        return appealRepository.findByReplyStatus("PENDING");
    }

    public List<Appeal> getProcessedAppeals() {
        return appealRepository.findByReplyStatus("PROCESSED");
    }

    public List<Appeal> getRejectedAppeals() {
        return appealRepository.findByReplyStatus("REJECTED");
    }

    public List<Appeal> getUserAppeals(String username) {
        return appealRepository.findByUsername(username);
    }

    public List<Appeal> getUserPendingAppeals(String username) {
        return appealRepository.findByUsernameAndReplyStatus(username, "PENDING");
    }

    @Transactional
    public Appeal replyAppeal(Long appealId, String reply, String replyStatus) {
        Appeal appeal = appealRepository.findById(appealId).orElse(null);
        if (appeal == null) return null;
        appeal.setReply(reply);
        appeal.setReplyStatus(replyStatus);
        appeal.setReplyTime(LocalDateTime.now());
        return appealRepository.save(appeal);
    }

    public long countPending() {
        return appealRepository.countPending();
    }

    public long countProcessed() {
        return appealRepository.countProcessed();
    }

    public long countRejected() {
        return appealRepository.countRejected();
    }

    @Transactional
    public void deleteAppeal(Long id) {
        appealRepository.deleteById(id);
    }
}