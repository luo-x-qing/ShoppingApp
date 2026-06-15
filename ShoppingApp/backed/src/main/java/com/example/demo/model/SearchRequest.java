package com.example.demo.model;

public class SearchRequest {
    private String fromCity;
    private String toCity;
    private String fromDate;
    private String returnDate;
    private Integer adultCount;

    public String getFromCity() { return fromCity; }
    public void setFromCity(String fromCity) { this.fromCity = fromCity; }
    public String getToCity() { return toCity; }
    public void setToCity(String toCity) { this.toCity = toCity; }
    public String getFromDate() { return fromDate; }
    public void setFromDate(String fromDate) { this.fromDate = fromDate; }
    public String getReturnDate() { return returnDate; }
    public void setReturnDate(String returnDate) { this.returnDate = returnDate; }
    public Integer getAdultCount() { return adultCount; }
    public void setAdultCount(Integer adultCount) { this.adultCount = adultCount; }
}
