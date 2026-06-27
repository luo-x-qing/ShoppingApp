package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Hotel;
import com.example.ordermanagement.model.HotelOrder;
import com.example.ordermanagement.model.RoomType;
import com.example.ordermanagement.repository.HotelCommentRepository;
import com.example.ordermanagement.repository.HotelOrderRepository;
import com.example.ordermanagement.repository.HotelRepository;
import com.example.ordermanagement.repository.RoomTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private HotelOrderRepository hotelOrderRepository;

    @Mock
    private BaiduMapService baiduMapService;

    @Mock
    private HotelCommentRepository hotelCommentRepository;

    @InjectMocks
    private HotelService hotelService;

    private Hotel sampleHotel;
    private RoomType sampleRoomType;
    private HotelOrder sampleOrder;

    @BeforeEach
    void setUp() {
        sampleHotel = new Hotel();
        sampleHotel.setId(1L);
        sampleHotel.setName("测试酒店");
        sampleHotel.setAddress("北京市朝阳区测试路123号");
        sampleHotel.setCategory("豪华酒店");
        sampleHotel.setStarLevel(5);
        sampleHotel.setPrice(800.0);
        sampleHotel.setMerchantId(10L);
        sampleHotel.setLatitude(39.9042);
        sampleHotel.setLongitude(116.4074);
        sampleHotel.setAvgRating(4.5);
        sampleHotel.setTotalRooms(100);
        sampleHotel.setStatus("营业中");
        sampleHotel.setUpdateTime(LocalDateTime.now());

        sampleRoomType = new RoomType();
        sampleRoomType.setId(1L);
        sampleRoomType.setTypeName("豪华大床房");
        sampleRoomType.setPrice(800.0);
        sampleRoomType.setAvailableCount(10);
        sampleRoomType.setTotalCount(10);
        sampleRoomType.setHotel(sampleHotel);

        sampleOrder = new HotelOrder();
        sampleOrder.setId(1L);
        sampleOrder.setUsername("testuser");
        sampleOrder.setHotelId(1L);
        sampleOrder.setPrice(800.0);
        sampleOrder.setStatus("已完成");
    }

    @Test
    void getAllHotels_ShouldReturnAllHotels() {
        when(hotelRepository.findAll()).thenReturn(Arrays.asList(sampleHotel));
        when(hotelCommentRepository.getAvgScoreByHotelId(1L)).thenReturn(4.5);

        List<Hotel> hotels = hotelService.getAllHotels();

        assertThat(hotels).isNotEmpty();
        assertThat(hotels.get(0).getId()).isEqualTo(1L);
        assertThat(hotels.get(0).getAvgRating()).isEqualTo(4.5);
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void getHotelById_WithValidId_ShouldReturnHotel() {
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(sampleHotel));
        when(hotelCommentRepository.getAvgScoreByHotelId(1L)).thenReturn(4.5);

        Hotel hotel = hotelService.getHotelById(1L);

        assertThat(hotel).isNotNull();
        assertThat(hotel.getId()).isEqualTo(1L);
        assertThat(hotel.getAvgRating()).isEqualTo(4.5);
        verify(hotelRepository, times(1)).findById(1L);
    }

    @Test
    void getHotelById_WithInvalidId_ShouldReturnNull() {
        when(hotelRepository.findById(999L)).thenReturn(Optional.empty());

        Hotel hotel = hotelService.getHotelById(999L);

        assertThat(hotel).isNull();
        verify(hotelRepository, times(1)).findById(999L);
    }

    @Test
    void saveHotel_ShouldSaveHotel() {
        when(hotelRepository.save(any(Hotel.class))).thenReturn(sampleHotel);

        Hotel savedHotel = hotelService.saveHotel(sampleHotel);

        assertThat(savedHotel).isNotNull();
        assertThat(savedHotel.getId()).isEqualTo(1L);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void deleteHotel_ShouldDeleteHotel() {
        doNothing().when(hotelRepository).deleteById(1L);

        hotelService.deleteHotel(1L);

        verify(hotelRepository, times(1)).deleteById(1L);
    }

    @Test
    void getHotelsByCategory_ShouldReturnHotels() {
        when(hotelRepository.findByCategory("豪华酒店")).thenReturn(Arrays.asList(sampleHotel));
        when(hotelCommentRepository.getAvgScoreByHotelId(1L)).thenReturn(4.5);

        List<Hotel> hotels = hotelService.getHotelsByCategory("豪华酒店");

        assertThat(hotels).isNotEmpty();
        assertThat(hotels.get(0).getCategory()).isEqualTo("豪华酒店");
        verify(hotelRepository, times(1)).findByCategory("豪华酒店");
    }

    @Test
    void getHotelsOrderByPrice_ShouldReturnSortedHotels() {
        when(hotelRepository.findAllOrderByPriceAsc()).thenReturn(Arrays.asList(sampleHotel));
        when(hotelCommentRepository.getAvgScoreByHotelId(1L)).thenReturn(4.5);

        List<Hotel> hotels = hotelService.getHotelsOrderByPrice();

        assertThat(hotels).isNotEmpty();
        verify(hotelRepository, times(1)).findAllOrderByPriceAsc();
    }

    @Test
    void getHotelsOrderByRating_ShouldReturnSortedHotels() {
        when(hotelRepository.findAllOrderByRatingDesc()).thenReturn(Arrays.asList(sampleHotel));
        when(hotelCommentRepository.getAvgScoreByHotelId(1L)).thenReturn(4.5);

        List<Hotel> hotels = hotelService.getHotelsOrderByRating();

        assertThat(hotels).isNotEmpty();
        verify(hotelRepository, times(1)).findAllOrderByRatingDesc();
    }

    @Test
    void getHotelsOrderByDistance_ShouldReturnSortedHotels() {
        when(hotelRepository.findAllOrderByDistance(39.9042, 116.4074)).thenReturn(Arrays.asList(sampleHotel));
        when(hotelCommentRepository.getAvgScoreByHotelId(1L)).thenReturn(4.5);

        List<Hotel> hotels = hotelService.getHotelsOrderByDistance(39.9042, 116.4074);

        assertThat(hotels).isNotEmpty();
        verify(hotelRepository, times(1)).findAllOrderByDistance(39.9042, 116.4074);
    }

    @Test
    void getHotelsByMerchant_ShouldReturnMerchantHotels() {
        when(hotelRepository.findByMerchantId(10L)).thenReturn(Arrays.asList(sampleHotel));
        when(hotelCommentRepository.getAvgScoreByHotelId(1L)).thenReturn(4.5);

        List<Hotel> hotels = hotelService.getHotelsByMerchant(10L);

        assertThat(hotels).isNotEmpty();
        assertThat(hotels.get(0).getMerchantId()).isEqualTo(10L);
        verify(hotelRepository, times(1)).findByMerchantId(10L);
    }

    @Test
    void getPersonalizedRecommendations_WithUserOrders_ShouldReturnRecommendations() {
        when(hotelOrderRepository.findByUsernameOrderByIdDesc("testuser")).thenReturn(Arrays.asList(sampleOrder));
        // 直接mock HotelRepository.findById 而不是调用 hotelService.getHotelById
        when(hotelRepository.findById(1L)).thenReturn(Optional.of(sampleHotel));
        when(hotelRepository.findRecommendedHotels(anyList(), anyDouble(), anyDouble()))
                .thenReturn(Arrays.asList(sampleHotel));
        when(hotelCommentRepository.getAvgScoreByHotelId(1L)).thenReturn(4.5);

        List<Hotel> hotels = hotelService.getPersonalizedRecommendations("testuser");

        assertThat(hotels).isNotEmpty();
        verify(hotelOrderRepository, times(1)).findByUsernameOrderByIdDesc("testuser");
        verify(hotelRepository, times(1)).findRecommendedHotels(anyList(), anyDouble(), anyDouble());
    }

    @Test
    void getPersonalizedRecommendations_WithoutUserOrders_ShouldReturnDefaultRecommendations() {
        when(hotelOrderRepository.findByUsernameOrderByIdDesc("newuser")).thenReturn(Arrays.asList());
        when(hotelRepository.findAllOrderByRatingDesc()).thenReturn(Arrays.asList(sampleHotel));

        List<Hotel> hotels = hotelService.getPersonalizedRecommendations("newuser");

        assertThat(hotels).isNotEmpty();
        verify(hotelOrderRepository, times(1)).findByUsernameOrderByIdDesc("newuser");
        verify(hotelRepository, times(1)).findAllOrderByRatingDesc();
    }

    @Test
    void addRoomType_ShouldCreateRoomType() {
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(sampleRoomType);

        RoomType savedRoomType = hotelService.addRoomType(sampleRoomType);

        assertThat(savedRoomType).isNotNull();
        assertThat(savedRoomType.getId()).isEqualTo(1L);
        assertThat(savedRoomType.getTypeName()).isEqualTo("豪华大床房");
        verify(roomTypeRepository, times(1)).save(any(RoomType.class));
    }

    @Test
    void updateRoomType_ShouldUpdateRoomType() {
        when(roomTypeRepository.save(any(RoomType.class))).thenReturn(sampleRoomType);

        RoomType updatedRoomType = hotelService.updateRoomType(sampleRoomType);

        assertThat(updatedRoomType).isNotNull();
        verify(roomTypeRepository, times(1)).save(any(RoomType.class));
    }

    @Test
    void deleteRoomType_ShouldDeleteRoomType() {
        doNothing().when(roomTypeRepository).deleteById(1L);

        hotelService.deleteRoomType(1L);

        verify(roomTypeRepository, times(1)).deleteById(1L);
    }

    @Test
    void getRoomTypesByHotel_ShouldReturnRoomTypes() {
        when(roomTypeRepository.findByHotelId(1L)).thenReturn(Arrays.asList(sampleRoomType));

        List<RoomType> roomTypes = hotelService.getRoomTypesByHotel(1L);

        assertThat(roomTypes).isNotEmpty();
        assertThat(roomTypes.get(0).getHotel().getId()).isEqualTo(1L);
        verify(roomTypeRepository, times(1)).findByHotelId(1L);
    }

    @Test
    void searchHotels_ShouldReturnMatchingHotels() {
        when(hotelRepository.searchByKeyword("测试", null)).thenReturn(Arrays.asList(sampleHotel));
        when(hotelCommentRepository.getAvgScoreByHotelId(1L)).thenReturn(4.5);

        List<Hotel> hotels = hotelService.searchHotels("测试", null);

        assertThat(hotels).isNotEmpty();
        assertThat(hotels.get(0).getName()).contains("测试");
        verify(hotelRepository, times(1)).searchByKeyword("测试", null);
    }

    @Test
    void countAll_ShouldReturnCount() {
        when(hotelRepository.count()).thenReturn(10L);

        long count = hotelService.countAll();

        assertThat(count).isEqualTo(10L);
        verify(hotelRepository, times(1)).count();
    }

    @Test
    void countByStatus_ShouldReturnCount() {
        when(hotelRepository.findAll()).thenReturn(Arrays.asList(sampleHotel));

        long count = hotelService.countByStatus("营业中");

        assertThat(count).isEqualTo(1);
        verify(hotelRepository, times(1)).findAll();
    }
}