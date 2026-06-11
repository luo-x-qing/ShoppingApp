package com.example.ordermanagement.repository;

import com.example.ordermanagement.model.RouteScenic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteScenicRepository extends JpaRepository<RouteScenic, Long> {
    List<RouteScenic> findByTourRouteIdOrderByDayNumberAscSortOrderAsc(Long routeId);

    @Query("SELECT DISTINCT rs.tourRoute.id FROM RouteScenic rs WHERE rs.scenic.province = :province")
    List<Long> findRouteIdsByScenicProvince(@Param("province") String province);

    @Query("SELECT DISTINCT rs.tourRoute.id FROM RouteScenic rs WHERE rs.scenic.city = :city")
    List<Long> findRouteIdsByScenicCity(@Param("city") String city);

    @Query("SELECT DISTINCT rs.tourRoute.id FROM RouteScenic rs WHERE rs.scenic.province = :province AND rs.scenic.city = :city")
    List<Long> findRouteIdsByScenicProvinceAndCity(@Param("province") String province, @Param("city") String city);
}
