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

    public List<Flight> searchFlights(String departCity, String arriveCity,
                                       LocalDateTime startDate, LocalDateTime endDate) {
        return flightRepository.findByDepartCityAndArriveCityAndDepartTimeBetweenAndStatus(
            departCity, arriveCity, startDate, endDate, "有效"
        );
    }

    public List<Flight> getFlightsOrderByPriceAsc() {
        return flightRepository.findByStatusOrderByPriceAsc("有效");
    }

    public List<Flight> getFlightsOrderByPriceDesc() {
        return flightRepository.findByStatusOrderByPriceDesc("有效");
    }

    public List<Flight> getFlightsOrderByTime() {
        return flightRepository.findByStatusOrderByDepartTimeAsc("有效");
    }

    public List<Flight> getFlightsByDepartCity(String departCity) {
        return flightRepository.findByDepartCityAndStatusOrderByPriceAsc(departCity, "有效");
    }

    public List<Flight> getFlightsByArriveCity(String arriveCity) {
        return flightRepository.findByArriveCityAndStatusOrderByPriceAsc(arriveCity, "有效");
    }

    public Flight getFlightById(Long id) {
        return flightRepository.findById(id).orElse(null);
    }

    public Flight addFlight(Flight flight) {
        flight.setStatus("有效");
        return flightRepository.save(flight);
    }

    public Flight updateFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public void deleteFlight(Long id) {
        flightRepository.deleteById(id);
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public List<Flight> getValidFlights() {
        return flightRepository.findByStatusOrderByDepartTimeAsc("有效");
    }

    public boolean decreaseSeats(Long flightId) {
        int updated = flightRepository.decreaseSeats(flightId);
        return updated > 0;
    }

    public boolean increaseSeats(Long flightId) {
        int updated = flightRepository.increaseSeats(flightId);
        return updated > 0;
    }
}
