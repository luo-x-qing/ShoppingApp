package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Attraction;
import com.example.ordermanagement.repository.AttractionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AttractionService {

    @Autowired
    private AttractionRepository attractionRepository;

    @Autowired
    private AmapService amapService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Attraction> getAllAttractions() {
        return attractionRepository.findAll();
    }

    public Attraction getAttractionById(Long id) {
        return attractionRepository.findById(id).orElse(null);
    }

    public List<Attraction> getAttractionsByProvince(String province) {
        return attractionRepository.findByProvince(province);
    }

    public List<Attraction> getAttractionsByCity(String city) {
        return attractionRepository.findByCity(city);
    }

    public List<Attraction> getAttractionsByProvinceAndCity(String province, String city) {
        return attractionRepository.findByProvinceAndCity(province, city);
    }

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

    public List<Map<String, Object>> getNearbyAttractions(Long id, double radiusKm) {
        Attraction current = getAttractionById(id);
        if (current == null || current.getLatitude() == null || current.getLongitude() == null) {
            return Collections.emptyList();
        }

        List<Attraction> candidates;
        if (current.getProvince() != null && !current.getProvince().isEmpty()) {
            candidates = attractionRepository.findByProvinceAndIdNot(current.getProvince(), id);
        } else {
            candidates = attractionRepository.findAll().stream()
                    .filter(a -> !a.getId().equals(id))
                    .collect(Collectors.toList());
        }

        double lat1 = current.getLatitude();
        double lon1 = current.getLongitude();

        return candidates.stream()
                .filter(a -> a.getLatitude() != null && a.getLongitude() != null)
                .map(a -> {
                    double dist = haversine(lat1, lon1, a.getLatitude(), a.getLongitude());
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("name", a.getName());
                    m.put("province", a.getProvince());
                    m.put("city", a.getCity());
                    m.put("score", a.getScore());
                    m.put("latitude", a.getLatitude());
                    m.put("longitude", a.getLongitude());
                    m.put("photo", a.getPhoto());
                    m.put("distance", Math.round(dist * 100.0) / 100.0);
                    return m;
                })
                .filter(m -> (double) m.get("distance") <= radiusKm)
                .sorted(Comparator.comparingDouble(m -> (double) m.get("distance")))
                .collect(Collectors.toList());
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public void deleteAttraction(Long id) {
        attractionRepository.deleteById(id);
    }

    public List<Attraction> searchByProvinceAndName(String province, String name){
        return attractionRepository.findByProvinceAndNameContaining(province, name);
    }

    public List<Attraction> searchByCityAndName(String city, String name){
        return attractionRepository.findByCityAndNameContaining(city, name);
    }

    @Transactional
    public int importScenicSpotsFromJson() {
        int added = 0;
        int failed = 0;

        try {
            InputStream inputStream = getClass().getResourceAsStream("/scenic_spots.json");
            if (inputStream == null) {
                System.err.println("错误：未找到 scenic_spots.json 文件");
                return 0;
            }

            List<Map<String, String>> spotList = objectMapper.readValue(
                inputStream,
                new TypeReference<List<Map<String, String>>>() {}
            );

            System.out.println("共读取到 " + spotList.size() + " 个景点");

            for (Map<String, String> spot : spotList) {
                String name = spot.get("name");
                String province = spot.get("province");
                String city = spot.get("city");

                if (attractionRepository.findByNameAndProvince(name, province) != null) {
                    System.out.println("跳过已存在: " + name);
                    continue;
                }

                String fullAddress = name;
                if (city != null && !city.isEmpty()) {
                    fullAddress = name + "," + city;
                } else if (province != null && !province.isEmpty()) {
                    fullAddress = name + "," + province;
                }
                String location = amapService.geocodeWithRetry(fullAddress, 5);

                Attraction attraction = new Attraction();
                attraction.setName(name);
                attraction.setProvince(province);
                attraction.setCity(city);

                if (location != null && location.contains(",")) {
                    String[] parts = location.split(",");
                    try {
                        attraction.setLongitude(Double.parseDouble(parts[0]));
                        attraction.setLatitude(Double.parseDouble(parts[1]));
                        System.out.println("✓ " + name + " -> " + location);
                    } catch (NumberFormatException e) {
                        System.err.println("✗ 坐标解析失败: " + name);
                        failed++;
                        continue;
                    }
                } else {
                    System.err.println("✗ 地理编码失败: " + name);
                    failed++;
                    continue;
                }

                attraction.setPhoto("");
                attraction.setScore(4.5);
                attraction.setTicketPrice(0.0);
                attraction.setOpenTime("");
                attraction.setType("风景名胜");
                attraction.setLevel("");

                attractionRepository.save(attraction);
                added++;

                Thread.sleep(3000);
            }

            System.out.println("导入完成：成功 " + added + " 个，失败 " + failed + " 个");
            return added;

        } catch (Exception e) {
            e.printStackTrace();
            return added;
        }
    }

    @Transactional
    public int importScenicSpotsDirect(String[][] spots) {
        int added = 0;
        for (String[] spot : spots) {
            String name = spot[0];
            String province = spot[1];
            String city = spot[2];

            if (attractionRepository.findByNameAndProvince(name, province) != null) continue;

            String fullAddress = name;
            if (city != null && !city.isEmpty()) {
                fullAddress = name + "," + city;
            } else if (province != null && !province.isEmpty()) {
                fullAddress = name + "," + province;
            }
            String location = amapService.geocodeWithRetry(fullAddress, 5);

            if (location == null || !location.contains(",")) {
                System.err.println("地理编码失败: " + name);
                continue;
            }

            String[] parts = location.split(",");
            Attraction a = new Attraction();
            a.setName(name);
            a.setProvince(province);
            a.setCity(city);
            a.setLongitude(Double.parseDouble(parts[0]));
            a.setLatitude(Double.parseDouble(parts[1]));
            a.setPhoto("");
            a.setScore(4.5);
            a.setTicketPrice(0.0);

            attractionRepository.save(a);
            added++;

            try { Thread.sleep(3000); } catch (InterruptedException e) {}
        }
        return added;
    }

    @Transactional
    public int reGeocodeAll() {
        List<Attraction> all = attractionRepository.findAll();
        int updated = 0;
        int failed = 0;
        for (Attraction a : all) {
            String fullAddress = a.getName();
            if (a.getCity() != null && !a.getCity().isEmpty()) {
                fullAddress = a.getName() + "," + a.getCity();
            } else if (a.getProvince() != null && !a.getProvince().isEmpty()) {
                fullAddress = a.getName() + "," + a.getProvince();
            }
            String location = amapService.geocodeWithRetry(fullAddress, 3);
            if (location != null && location.contains(",")) {
                String[] parts = location.split(",");
                try {
                    a.setLongitude(Double.parseDouble(parts[0]));
                    a.setLatitude(Double.parseDouble(parts[1]));
                    attractionRepository.save(a);
                    updated++;
                    System.out.println("✓ 更新 " + a.getName() + " -> " + location);
                } catch (NumberFormatException e) {
                    System.err.println("✗ 坐标解析失败: " + a.getName());
                    failed++;
                }
            } else {
                System.err.println("✗ 地理编码失败: " + a.getName());
                failed++;
            }
            try { Thread.sleep(3000); } catch (InterruptedException e) { }
        }
        System.out.println("重新编码完成：更新 " + updated + " 个，失败 " + failed + " 个");
        return updated;
    }

    @Transactional
    public int reGeocodePrecise() {
        List<Attraction> all = attractionRepository.findAll();
        int updated = 0;
        for (Attraction a : all) {
            String fullAddress = a.getName();
            if (a.getCity() != null && !a.getCity().isEmpty()) {
                fullAddress = a.getName() + "," + a.getCity();
            } else if (a.getProvince() != null && !a.getProvince().isEmpty()) {
                fullAddress = a.getName() + "," + a.getProvince();
            }
            Map<String, Double> bestCoord = amapService.geocodeWithPreference(fullAddress);
            if (bestCoord != null) {
                a.setLatitude(bestCoord.get("lat"));
                a.setLongitude(bestCoord.get("lng"));
                attractionRepository.save(a);
                updated++;
                System.out.println("✓ 精确更新 " + a.getName() + " -> "
                        + bestCoord.get("lat") + "," + bestCoord.get("lng"));
            } else {
                System.err.println("✗ 精确编码失败: " + a.getName());
            }
            try { Thread.sleep(1500); } catch (InterruptedException e) { }
        }
        System.out.println("精确重编码完成：" + updated + " 个成功");
        return updated;
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
