package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Attraction;
import com.example.ordermanagement.repository.AttractionRepository;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

@Service
public class AttractionService {

    private static final Map<String, double[]> PROVINCE_CENTERS = new HashMap<>();
    static {
        PROVINCE_CENTERS.put("北京市", new double[]{39.9042, 116.4074});
        PROVINCE_CENTERS.put("上海市", new double[]{31.2304, 121.4737});
        PROVINCE_CENTERS.put("天津市", new double[]{39.1252, 117.1908});
        PROVINCE_CENTERS.put("重庆市", new double[]{29.5330, 106.5048});
        PROVINCE_CENTERS.put("河北省", new double[]{38.0455, 114.5020});
        PROVINCE_CENTERS.put("山西省", new double[]{37.8570, 112.5624});
        PROVINCE_CENTERS.put("辽宁省", new double[]{41.8057, 123.4315});
        PROVINCE_CENTERS.put("吉林省", new double[]{43.8960, 125.3265});
        PROVINCE_CENTERS.put("黑龙江省", new double[]{45.7420, 126.6425});
        PROVINCE_CENTERS.put("江苏省", new double[]{32.0617, 118.7969});
        PROVINCE_CENTERS.put("浙江省", new double[]{30.2741, 120.1551});
        PROVINCE_CENTERS.put("安徽省", new double[]{31.8206, 117.2272});
        PROVINCE_CENTERS.put("福建省", new double[]{26.0745, 119.2965});
        PROVINCE_CENTERS.put("江西省", new double[]{28.6763, 115.8955});
        PROVINCE_CENTERS.put("山东省", new double[]{36.6516, 117.0204});
        PROVINCE_CENTERS.put("河南省", new double[]{34.7656, 113.7532});
        PROVINCE_CENTERS.put("湖北省", new double[]{30.5465, 114.3423});
        PROVINCE_CENTERS.put("湖南省", new double[]{28.2569, 112.9410});
        PROVINCE_CENTERS.put("广东省", new double[]{23.1292, 113.2644});
        PROVINCE_CENTERS.put("海南省", new double[]{20.0200, 110.3486});
        PROVINCE_CENTERS.put("四川省", new double[]{30.5728, 104.0668});
        PROVINCE_CENTERS.put("贵州省", new double[]{26.6470, 106.6302});
        PROVINCE_CENTERS.put("云南省", new double[]{25.0453, 102.7100});
        PROVINCE_CENTERS.put("陕西省", new double[]{34.2655, 108.9542});
        PROVINCE_CENTERS.put("甘肃省", new double[]{36.0642, 103.8264});
        PROVINCE_CENTERS.put("青海省", new double[]{36.6232, 101.7805});
        PROVINCE_CENTERS.put("台湾省", new double[]{25.0330, 121.5654});
        PROVINCE_CENTERS.put("内蒙古自治区", new double[]{40.8174, 111.7663});
        PROVINCE_CENTERS.put("广西壮族自治区", new double[]{22.8170, 108.3665});
        PROVINCE_CENTERS.put("西藏自治区", new double[]{29.6500, 91.1170});
        PROVINCE_CENTERS.put("宁夏回族自治区", new double[]{38.4713, 106.2588});
        PROVINCE_CENTERS.put("新疆维吾尔自治区", new double[]{43.7930, 87.6270});
        PROVINCE_CENTERS.put("香港特别行政区", new double[]{22.3072, 114.1759});
        PROVINCE_CENTERS.put("澳门特别行政区", new double[]{22.1987, 113.5492});
    }

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private AmapService amapService;

    // 查询所有景点
    public List<Attraction> getAllAttractions() {
        return attractionRepository.findAll();
    }

    // 根据ID查询景点（自动补全坐标）
    public Attraction getAttractionById(Long id) {
        Attraction a = attractionRepository.findById(id).orElse(null);
        if (a != null) fillCoordinates(a);
        return a;
    }

    // 自动补全坐标（高德 API → 省份中心兜底）
    private void fillCoordinates(Attraction a) {
        if (a.getLatitude() != null && a.getLongitude() != null) return;
        String address = a.getName();
        if (a.getCity() != null && !a.getCity().isEmpty()) {
            address = a.getCity() + a.getName();
        } else if (a.getProvince() != null && !a.getProvince().isEmpty()) {
            address = a.getProvince() + a.getName();
        }

        // 1. 高德 API 地理编码
        String location = amapService.geo(address);
        if (location != null && location.contains(",")) {
            String[] parts = location.split(",");
            try {
                a.setLongitude(Double.parseDouble(parts[0]));
                a.setLatitude(Double.parseDouble(parts[1]));
                attractionRepository.save(a);
                return;
            } catch (NumberFormatException ignored) {}
        }

        // 2. 高德 API 没返回，用省份中心坐标兜底
        if (a.getProvince() != null) {
            double[] center = PROVINCE_CENTERS.get(a.getProvince());
            if (center != null) {
                a.setLatitude(center[0]);
                a.setLongitude(center[1]);
                attractionRepository.save(a);
            }
        }
    }

    // 查询附近景点（同一省份，按距离排序）
    public List<Map<String, Object>> getNearbyAttractions(Long id, double radiusKm) {
        Attraction current = getAttractionById(id);
        if (current == null || current.getLatitude() == null) return Collections.emptyList();

        List<Attraction> candidates;
        if (current.getProvince() != null && !current.getProvince().isEmpty()) {
            candidates = attractionRepository.findByProvinceAndIdNot(current.getProvince(), id);
        } else {
            candidates = attractionRepository.findAll();
            candidates.removeIf(a -> a.getId().equals(id));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Attraction a : candidates) {
            fillCoordinates(a);
            if (a.getLatitude() == null) continue;
            double dist = calculateDistance(
                current.getLatitude(), current.getLongitude(),
                a.getLatitude(), a.getLongitude()
            );
            if (dist <= radiusKm) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", a.getId());
                item.put("name", a.getName());
                item.put("province", a.getProvince());
                item.put("city", a.getCity());
                item.put("latitude", a.getLatitude());
                item.put("longitude", a.getLongitude());
                item.put("photo", a.getPhoto());
                item.put("score", a.getScore());
                item.put("type", a.getType());
                item.put("ticketPrice", a.getTicketPrice());
                item.put("distance", Math.round(dist * 10) / 10.0);
                result.add(item);
            }
        }
        result.sort(Comparator.comparingDouble(m -> (Double) m.get("distance")));
        return result;
    }

    // Haversine 距离计算（单位：km）
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // 按省份查询景点
    public List<Attraction> getAttractionsByProvince(String province) {
        return attractionRepository.findByProvince(province);
    }

    // 按城市查询景点
    public List<Attraction> getAttractionsByCity(String city) {
        return attractionRepository.findByCity(city);
    }

    // 按省份和城市查询景点
    public List<Attraction> getAttractionsByProvinceAndCity(String province, String city) {
        return attractionRepository.findByProvinceAndCity(province, city);
    }

    // 新增/更新景点（自动补全城市）
    public Attraction saveAttraction(Attraction attraction) {
        if ((attraction.getCity() == null || attraction.getCity().isEmpty())
                && attraction.getName() != null && !attraction.getName().isEmpty()) {
            String address = attraction.getName();
            if (attraction.getProvince() != null && !attraction.getProvince().isEmpty()) {
                address = attraction.getName() + "," + attraction.getProvince();
            }
            Map<String, String> detail = amapService.geoDetail(address);
            if (detail.containsKey("city") && !detail.get("city").isEmpty()) {
                attraction.setCity(detail.get("city"));
            }
        }
        return attractionRepository.save(attraction);
    }

    // 批量自动补全城市（通过高德API）
    public int autoFillCity() {
        List<Attraction> all = attractionRepository.findAll();
        int count = 0;
        for (Attraction a : all) {
            if (a.getCity() == null || a.getCity().isEmpty()) {
                String address = a.getName();
                if (a.getProvince() != null && !a.getProvince().isEmpty()) {
                    address = a.getName() + "," + a.getProvince();
                }
                Map<String, String> detail = amapService.geoDetail(address);
                if (detail.containsKey("city") && !detail.get("city").isEmpty()) {
                    a.setCity(detail.get("city"));
                    attractionRepository.save(a);
                    count++;
                }
            }
        }
        return count;
    }

    // 删除景点
    public void deleteAttraction(Long id) {
        attractionRepository.deleteById(id);
    }

    // 按省份搜索景点
    public List<Attraction> searchByProvinceAndName(String province, String name){
        return attractionRepository.findByProvinceAndNameContaining(province, name);
    }

    // 按城市搜索景点
    public List<Attraction> searchByCityAndName(String city, String name){
        return attractionRepository.findByCityAndNameContaining(city, name);
    }

    public int importAll() {
        String[] provinces = {
            "北京市", "上海市", "天津市", "重庆市",
            "河北省", "山西省", "辽宁省", "吉林省", "黑龙江省",
            "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省",
            "河南省", "湖北省", "湖南省", "广东省", "海南省",
            "四川省", "贵州省", "云南省", "陕西省", "甘肃省", "青海省",
            "台湾省", "内蒙古自治区", "广西壮族自治区", "西藏自治区", "宁夏回族自治区",
            "新疆维吾尔自治区", "香港特别行政区", "澳门特别行政区"
        };

        int added = 0;
        for (String province : provinces) {
            for (int page = 1; page <= 5; page++) {
                List<Map<String, Object>> pois = amapService.searchPoi("060000", province, page, 50);
                if (pois.isEmpty()) break;
                for (Map<String, Object> poi : pois) {
                    try {
                        String name = (String) poi.get("name");
                        String pname = (String) poi.getOrDefault("pname", province);
                        if (attractionRepository.findByNameAndProvince(name, pname) != null) continue;

                        Attraction a = new Attraction();
                        a.setName(name);
                        a.setProvince(pname);

                        Object cityObj = poi.get("cityname");
                        a.setCity(cityObj instanceof String ? (String) cityObj : "");

                        String typeStr = (String) poi.getOrDefault("type", "");
                        if (!typeStr.isEmpty()) {
                            String[] parts = typeStr.split(";");
                            a.setType(parts.length > 1 ? parts[1] : parts[0]);
                        }

                        Object ratingObj = poi.get("rating");
                        if (ratingObj != null) {
                            try { a.setScore(Double.parseDouble(ratingObj.toString())); }
                            catch (NumberFormatException e) { a.setScore(4.0); }
                        } else { a.setScore(4.0); }

                        Object costObj = poi.get("cost");
                        if (costObj != null) {
                            try { a.setTicketPrice(Double.parseDouble(costObj.toString())); }
                            catch (NumberFormatException e) { a.setTicketPrice(0.0); }
                        } else { a.setTicketPrice(0.0); }

                        a.setOpenTime((String) poi.getOrDefault("opentime", ""));

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> photos = (List<Map<String, Object>>) poi.get("photos");
                        if (photos != null && !photos.isEmpty()) {
                            String photoUrl = (String) photos.get(0).get("url");
                            a.setPhoto(photoUrl);
                        }

                        a.setLevel("");

                        // 保存坐标信息
                        Object locationObj = poi.get("location");
                        if (locationObj instanceof String && !((String) locationObj).isEmpty()) {
                            String[] parts = ((String) locationObj).split(",");
                            if (parts.length == 2) {
                                try {
                                    a.setLongitude(Double.parseDouble(parts[0]));
                                    a.setLatitude(Double.parseDouble(parts[1]));
                                } catch (NumberFormatException ignored) {}
                            }
                        }

                        attractionRepository.save(a);
                        added++;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        return added;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void resetAndRenumber() {
        long count = attractionRepository.count();
        if (count == 0) return;

        Session session = entityManager.unwrap(Session.class);
        session.doWork((Connection conn) -> {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");

                stmt.execute("DROP TEMPORARY TABLE IF EXISTS _id_map");
                stmt.execute("CREATE TEMPORARY TABLE _id_map (" +
                        "  old_id BIGINT NOT NULL, new_id BIGINT NOT NULL, PRIMARY KEY (old_id))");

                stmt.execute("INSERT INTO _id_map (old_id, new_id) " +
                        "SELECT t.id, @rownum := @rownum + 1 " +
                        "FROM attraction t, (SELECT @rownum := 0) r " +
                        "ORDER BY IFNULL(t.province,''), IFNULL(t.city,''), t.name");

                stmt.execute("UPDATE comment c " +
                        "INNER JOIN _id_map m ON c.attraction_id = m.old_id " +
                        "SET c.attraction_id = m.new_id");

                stmt.execute("UPDATE score s " +
                        "INNER JOIN _id_map m ON s.attraction_id = m.old_id " +
                        "SET s.attraction_id = m.new_id");

                stmt.execute("CREATE TABLE _attraction_new LIKE attraction");
                stmt.execute("INSERT INTO _attraction_new (id, name, province, city, photo, score, ticket_price, `type`, `level`, open_time, description) " +
                        "SELECT m.new_id, a.name, a.province, a.city, a.photo, a.score, a.ticket_price, a.type, a.level, a.open_time, a.description " +
                        "FROM attraction a INNER JOIN _id_map m ON a.id = m.old_id");
                stmt.execute("DROP TABLE attraction");
                stmt.execute("RENAME TABLE _attraction_new TO attraction");

                stmt.execute("DROP TEMPORARY TABLE IF EXISTS _id_map");
                stmt.execute("ALTER TABLE attraction AUTO_INCREMENT = 1");

                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            }
        });
    }
}