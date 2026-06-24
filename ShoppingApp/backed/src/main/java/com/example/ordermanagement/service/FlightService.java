package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Flight;
import com.example.ordermanagement.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    // ========== 用户端查询 ==========
    
    // 搜索航班
    public List<Flight> searchFlights(String departCity, String arriveCity, 
                                       LocalDateTime startDate, LocalDateTime endDate) {
        return flightRepository.findByDepartCityAndArriveCityAndDepartTimeBetweenAndStatus(
            departCity, arriveCity, startDate, endDate, "有效"
        );
    }
    
    // 按价格排序（升序）
    public List<Flight> getFlightsOrderByPriceAsc() {
        return flightRepository.findByStatusOrderByPriceAsc("有效");
    }
    
    // 按价格排序（降序）
    public List<Flight> getFlightsOrderByPriceDesc() {
        return flightRepository.findByStatusOrderByPriceDesc("有效");
    }
    
    // 按出发时间排序
    public List<Flight> getFlightsOrderByTime() {
        return flightRepository.findByStatusOrderByDepartTimeAsc("有效");
    }
    
    // 按出发城市查询
    public List<Flight> getFlightsByDepartCity(String departCity) {
        return flightRepository.findByDepartCityAndStatusOrderByPriceAsc(departCity, "有效");
    }
    
    // 按到达城市查询
    public List<Flight> getFlightsByArriveCity(String arriveCity) {
        return flightRepository.findByArriveCityAndStatusOrderByPriceAsc(arriveCity, "有效");
    }
    
    // 获取航班详情
    public Flight getFlightById(Long id) {
        return flightRepository.findById(id).orElse(null);
    }
    
    // ========== 管理员端操作 ==========
    
    // 添加航班
    public Flight addFlight(Flight flight) {
        flight.setStatus("有效");
        return flightRepository.save(flight);
    }
    
    // 修改航班
    public Flight updateFlight(Flight flight) {
        return flightRepository.save(flight);
    }
    
    // 删除航班
    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }
    
    // 获取所有航班（包括失效的）
    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }
    
    // 获取有效航班
    public List<Flight> getValidFlights() {
        return flightRepository.findByStatusOrderByDepartTimeAsc("有效");
    }
    
    // 扣减座位（供订单服务调用）
    public boolean decreaseSeats(Long flightId) {
        int updated = flightRepository.decreaseSeats(flightId);
        return updated > 0;
    }
    
    // 恢复座位（供订单服务调用）
    public boolean increaseSeats(Long flightId) {
        int updated = flightRepository.increaseSeats(flightId);
        return updated > 0;
    }
}