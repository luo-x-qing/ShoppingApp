package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserByToken(String token) {
        return userRepository.findByToken(token);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getAllMerchants() {
        return userRepository.findByRole("MERCHANT");
    }

    public List<User> getAllNormalUsers() {
        return userRepository.findByRole("USER");
    }

    public User getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            User safeUser = new User();
            safeUser.setId(user.getId());
            safeUser.setUsername(user.getUsername());
            safeUser.setRole(user.getRole());
            safeUser.setStatus(user.getStatus());
            safeUser.setShopName(user.getShopName());
            safeUser.setPhone(user.getPhone());
            safeUser.setEmail(user.getEmail());
            safeUser.setAvatar(user.getAvatar());
            safeUser.setGender(user.getGender());
            safeUser.setAge(user.getAge());
            safeUser.setCreateTime(user.getCreateTime());
            safeUser.setUpdateTime(user.getUpdateTime());
            safeUser.setBanReason(user.getBanReason());
            safeUser.setBanExpireTime(user.getBanExpireTime());
            return safeUser;
        }
        return null;
    }

    public User getUserEntity(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User getUserFullInfo(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setToken(null);
            user.setPassword(null);
        }
        return user;
    }

    public User registerUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User registerUser(String username, String password, String phone, String email) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setPhone(phone);
        user.setEmail(email);
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User registerMerchant(String username, String password, String shopName, String phone) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("MERCHANT");
        user.setShopName(shopName);
        user.setPhone(phone);
        user.setStatus("PENDING");
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User registerMerchant(String username, String password, String shopName, String phone,
                                 String email, String shopAddress, String shopDescription) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("MERCHANT");
        user.setShopName(shopName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setShopAddress(shopAddress);
        user.setShopDescription(shopDescription);
        user.setStatus("PENDING");
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null) return null;
        if (!password.equals(user.getPassword())) {
            return null;
        }
        String token = UUID.randomUUID().toString();
        user.setToken(token);
        userRepository.save(user);
        return user;
    }

    public User updateUser(Long id, User newUser) {
        User oldUser = userRepository.findById(id).orElse(null);
        if (oldUser == null) return null;

        if (newUser.getUsername() != null) {
            oldUser.setUsername(newUser.getUsername());
        }
        if (newUser.getPassword() != null && !newUser.getPassword().isEmpty()) {
            oldUser.setPassword(newUser.getPassword());
        }
        if (newUser.getPhone() != null) {
            oldUser.setPhone(newUser.getPhone());
        }
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getGender() != null) {
            oldUser.setGender(newUser.getGender());
        }
        if (newUser.getAge() != null) {
            oldUser.setAge(newUser.getAge());
        }
        if (newUser.getAvatar() != null) {
            oldUser.setAvatar(newUser.getAvatar());
        }
        if (newUser.getShopName() != null) {
            oldUser.setShopName(newUser.getShopName());
        }
        if (newUser.getShopDescription() != null) {
            oldUser.setShopDescription(newUser.getShopDescription());
        }
        if (newUser.getShopAddress() != null) {
            oldUser.setShopAddress(newUser.getShopAddress());
        }
        if (newUser.getShopPhone() != null) {
            oldUser.setShopPhone(newUser.getShopPhone());
        }
        oldUser.setUpdateTime(LocalDateTime.now());
        return userRepository.save(oldUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public boolean banUser(Long userId, String reason, int durationDays) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        user.setStatus("BANNED");
        user.setBanReason(reason);

        if (durationDays > 0) {
            user.setBanExpireTime(LocalDateTime.now().plusDays(durationDays));
        } else {
            user.setBanExpireTime(null);
        }
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        notificationService.sendUserBannedNotification(userId, user.getUsername(), reason, durationDays);

        return true;
    }

    public boolean unbanUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        user.setStatus("NORMAL");
        user.setBanExpireTime(null);
        user.setBanReason(null);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        notificationService.sendUserUnbannedNotification(userId, user.getUsername());

        return true;
    }

    public boolean isBanned(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        return "BANNED".equals(user.getStatus());
    }

    @Transactional
    public int autoUnbanExpiredUsers() {
        List<User> expiredUsers = userRepository.findByStatusAndBanExpireTimeBefore("BANNED", LocalDateTime.now());
        int count = 0;
        for (User user : expiredUsers) {
            user.setStatus("NORMAL");
            user.setBanExpireTime(null);
            user.setBanReason(null);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            count++;
            System.out.println("自动解禁用户: " + user.getUsername());
        }
        return count;
    }

    public User updateMerchantInfo(Long merchantId, User newInfo) {
        User merchant = userRepository.findById(merchantId).orElse(null);
        if (merchant == null || !"MERCHANT".equals(merchant.getRole())) return null;

        if (newInfo.getShopName() != null) {
            merchant.setShopName(newInfo.getShopName());
        }
        if (newInfo.getShopDescription() != null) {
            merchant.setShopDescription(newInfo.getShopDescription());
        }
        if (newInfo.getShopAddress() != null) {
            merchant.setShopAddress(newInfo.getShopAddress());
        }
        if (newInfo.getShopPhone() != null) {
            merchant.setShopPhone(newInfo.getShopPhone());
        }
        if (newInfo.getPhone() != null) {
            merchant.setPhone(newInfo.getPhone());
        }
        if (newInfo.getEmail() != null) {
            merchant.setEmail(newInfo.getEmail());
        }
        if (newInfo.getAvatar() != null) {
            merchant.setAvatar(newInfo.getAvatar());
        }
        merchant.setUpdateTime(LocalDateTime.now());
        return userRepository.save(merchant);
    }

    public boolean approveMerchant(Long merchantId) {
        User merchant = userRepository.findById(merchantId).orElse(null);
        if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
            return false;
        }
        merchant.setStatus("NORMAL");
        merchant.setUpdateTime(LocalDateTime.now());
        userRepository.save(merchant);
        return true;
    }

    public boolean rejectMerchant(Long merchantId, String reason) {
        User merchant = userRepository.findById(merchantId).orElse(null);
        if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
            return false;
        }
        merchant.setStatus("REJECTED");
        merchant.setUpdateTime(LocalDateTime.now());
        userRepository.save(merchant);
        return true;
    }

    public boolean disableMerchant(Long merchantId) {
        User merchant = userRepository.findById(merchantId).orElse(null);
        if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
            return false;
        }
        merchant.setStatus("BANNED");
        merchant.setUpdateTime(LocalDateTime.now());
        userRepository.save(merchant);
        return true;
    }

    public boolean enableMerchant(Long merchantId) {
        User merchant = userRepository.findById(merchantId).orElse(null);
        if (merchant == null || !"MERCHANT".equals(merchant.getRole())) {
            return false;
        }
        merchant.setStatus("NORMAL");
        merchant.setUpdateTime(LocalDateTime.now());
        userRepository.save(merchant);
        return true;
    }

    public Map<String, String> getMerchantBasicInfo(Long merchantId) {
        User merchant = userRepository.findById(merchantId).orElse(null);
        if (merchant == null) {
            return null;
        }
        Map<String, String> info = new HashMap<>();
        info.put("id", String.valueOf(merchant.getId()));
        info.put("username", merchant.getUsername());
        info.put("shopName", merchant.getShopName());
        info.put("status", merchant.getStatus());
        return info;
    }

    public long getNormalUserTotalCount() {
        return userRepository.countByRoleAndStatus("USER", null);
    }

    public long getNormalUserCount() {
        return userRepository.countByRoleAndStatus("USER", "NORMAL");
    }

    public long getBannedUserCount() {
        return userRepository.countByRoleAndStatus("USER", "BANNED");
    }
}
