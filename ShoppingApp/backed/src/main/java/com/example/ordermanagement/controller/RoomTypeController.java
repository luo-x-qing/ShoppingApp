package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.model.Result;
import com.example.ordermanagement.service.RoomTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/room-types")
@CrossOrigin(origins = "*")
public class RoomTypeController {

    private static final Logger log = LoggerFactory.getLogger(RoomTypeController.class);

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping("/hotel/{hotelId}")
    public Result<List<RoomType>> getByHotelId(@PathVariable Long hotelId) {
        log.info("获取酒店房型，酒店ID：{}", hotelId);
        try {
            List<RoomType> roomTypes = roomTypeService.findByHotelId(hotelId);
            return Result.success(roomTypes);
        } catch (Exception e) {
            log.error("获取房型失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping
    public Result<List<RoomType>> getAll() {
        try {
            List<RoomType> roomTypes = roomTypeService.findAll();
            return Result.success(roomTypes);
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<RoomType> getById(@PathVariable Long id) {
        try {
            RoomType roomType = roomTypeService.findById(id);
            if (roomType != null) {
                return Result.success(roomType);
            } else {
                return Result.error("房型不存在");
            }
        } catch (Exception e) {
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 添加房型 - 使用 Map 接收参数
     */
    @PostMapping
    public Result<RoomType> create(@RequestBody Map<String, Object> params) {
        log.info("添加房型参数：{}", params);
        try {
            // 提取参数
            String typeName = (String) params.get("typeName");
            Double price = Double.valueOf(params.get("price").toString());
            Integer totalCount = Integer.valueOf(params.get("totalCount").toString());
            Integer availableCount = Integer.valueOf(params.get("availableCount").toString());
            String size = (String) params.get("size");
            String bedType = (String) params.get("bedType");
            String windowStatus = (String) params.get("windowStatus");
            String breakfast = (String) params.get("breakfast");

            // 参数校验
            if (typeName == null || typeName.isEmpty()) {
                return Result.error("房型名称不能为空");
            }
            if (price == null || price <= 0) {
                return Result.error("价格必须大于0");
            }
            if (totalCount == null || totalCount <= 0) {
                return Result.error("总数量必须大于0");
            }
            if (availableCount == null || availableCount <= 0) {
                return Result.error("可预订数量必须大于0");
            }

            // 处理酒店信息
            Object hotelObj = params.get("hotel");
            Long hotelId = null;
            if (hotelObj instanceof Map) {
                Map<String, Object> hotelMap = (Map<String, Object>) hotelObj;
                Object idObj = hotelMap.get("id");
                if (idObj != null) {
                    hotelId = Long.valueOf(idObj.toString());
                }
            }

            if (hotelId == null) {
                log.error("酒店ID为空，参数：{}", params);
                return Result.error("请指定所属酒店");
            }

            // 构建 RoomType 对象
            RoomType roomType = new RoomType();
            roomType.setTypeName(typeName);
            roomType.setPrice(price);
            roomType.setTotalCount(totalCount);
            roomType.setAvailableCount(availableCount);
            roomType.setSize(size);
            roomType.setBedType(bedType);
            roomType.setWindowStatus(windowStatus);
            roomType.setBreakfast(breakfast);

            Hotel hotel = new Hotel();
            hotel.setId(hotelId);
            roomType.setHotel(hotel);

            RoomType saved = roomTypeService.save(roomType);
            log.info("房型添加成功，ID：{}", saved.getId());
            return Result.success(saved);

        } catch (Exception e) {
            log.error("添加房型失败", e);
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 更新房型
     */
    @PutMapping("/{id}")
    public Result<RoomType> update(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        log.info("更新房型，ID：{}，参数：{}", id, params);
        try {
            RoomType existing = roomTypeService.findById(id);
            if (existing == null) {
                return Result.error("房型不存在");
            }

            // 更新字段
            if (params.containsKey("typeName")) {
                existing.setTypeName((String) params.get("typeName"));
            }
            if (params.containsKey("price")) {
                existing.setPrice(Double.valueOf(params.get("price").toString()));
            }
            if (params.containsKey("totalCount")) {
                existing.setTotalCount(Integer.valueOf(params.get("totalCount").toString()));
            }
            if (params.containsKey("availableCount")) {
                existing.setAvailableCount(Integer.valueOf(params.get("availableCount").toString()));
            }
            if (params.containsKey("size")) {
                existing.setSize((String) params.get("size"));
            }
            if (params.containsKey("bedType")) {
                existing.setBedType((String) params.get("bedType"));
            }
            if (params.containsKey("windowStatus")) {
                existing.setWindowStatus((String) params.get("windowStatus"));
            }
            if (params.containsKey("breakfast")) {
                existing.setBreakfast((String) params.get("breakfast"));
            }

            RoomType updated = roomTypeService.update(existing);
            return Result.success(updated);

        } catch (Exception e) {
            log.error("更新房型失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            boolean deleted = roomTypeService.deleteById(id);
            if (deleted) {
                return Result.success("删除成功");
            } else {
                return Result.error("房型不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }
}