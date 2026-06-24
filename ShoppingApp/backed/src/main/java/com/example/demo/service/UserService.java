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

    // ========== 基础查询方法 ==========

    // 按用户名查找
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 根据token获取用户
    public User getUserByToken(String token) {
        return userRepository.findByToken(token);
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 获取所有商家
    public List<User> getAllMerchants() {
        return userRepository.findByRole("MERCHANT");
    }

    // 获取所有普通用户
    public List<User> getAllNormalUsers() {
        return userRepository.findByRole("USER");
    }

    // 根据ID获取用户（基础信息，隐藏密码和token）- 返回副本，不修改原始实体
    public User getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            // 创建副本返回，不修改原始实体对象
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
            // 不设置 password 和 token
            return safeUser;
        }
        return null;
    }

    // 获取用户实体用于内部更新操作（不清空任何字段）
    // 注意：这个方法返回的是持久化实体，修改后会自动同步到数据库
    public User getUserEntity(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // ✅ 新增：获取用户完整信息（管理员用，保留所有字段用于显示）
    public User getUserFullInfo(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setToken(null); // 只隐藏token
            user.setPassword(null); // 隐藏密码
        }
        return user;
    }

    // ========== 注册方法 ==========

    // 普通用户注册
    public User registerUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole("USER");
        user.setStatus("NORMAL");
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    // ✅ 新增：带完整信息的用户注册
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

    // 商家注册（初始状态为待审核）
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

    // ✅ 新增：带完整信息的商家注册
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

    // ========== 登录方法 ==========

    // 登录
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

    // ========== 更新用户信息 ==========

    // 更新用户信息（保留token不变）
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

    // 删除用户
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    // ========== 禁言功能 ==========

    // ✅ 禁言用户（支持时长）- 保存禁言原因
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

        // 发送通知
        notificationService.sendUserBannedNotification(userId, user.getUsername(), reason, durationDays);

        return true;
    }

    // ✅ 解禁用户 - 清除禁言原因
    public boolean unbanUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        user.setStatus("NORMAL");
        user.setBanExpireTime(null);
        user.setBanReason(null);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        // 发送通知
        notificationService.sendUserUnbannedNotification(userId, user.getUsername());

        return true;
    }

    public boolean isBanned(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;
        return "BANNED".equals(user.getStatus());
    }

    // 自动解禁过期的用户
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

    // ========== 商家专用方法 ==========

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

    // ========== 管理员操作商家状态 ==========

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

        // 添加调试日志
        System.out.println("========== 禁用商家 ==========");
        System.out.println("商家ID: " + merchantId);
        System.out.println("商家名: " + merchant.getUsername());
        System.out.println("当前密码: " + (merchant.getPassword() != null ? "存在(长度=" + merchant.getPassword().length() + ")" : "null"));

        merchant.setStatus("BANNED");
        merchant.setUpdateTime(LocalDateTime.now());
        User saved = userRepository.save(merchant);

        System.out.println("保存后密码: " + (saved.getPassword() != null ? "存在(长度=" + saved.getPassword().length() + ")" : "null"));

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

    // 获取商家基本信息（用于通知，不修改实体）
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

    // ========== ✅ 新增：统计方法（用于管理员页面显示统计卡片） ==========

    // 获取普通用户总数
    public long getNormalUserTotalCount() {
        return userRepository.countByRoleAndStatus("USER", null);
    }

    // 获取正常状态的普通用户数量
    public long getNormalUserCount() {
        return userRepository.countByRoleAndStatus("USER", "NORMAL");
    }

    // 获取已禁言的普通用户数量
    public long getBannedUserCount() {
        return userRepository.countByRoleAndStatus("USER", "BANNED");
    }
}