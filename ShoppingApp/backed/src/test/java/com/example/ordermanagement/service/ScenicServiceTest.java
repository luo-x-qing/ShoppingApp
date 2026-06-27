package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Scenic;
import com.example.ordermanagement.repository.ScenicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScenicServiceTest {

    @Mock
    private ScenicRepository scenicRepository;

    @Mock
    private AmapService amapService;

    @InjectMocks
    private ScenicService scenicService;

    private Scenic createScenic(Long id, String name, String province, String city) {
        Scenic s = new Scenic();
        s.setId(id);
        s.setName(name);
        s.setProvince(province);
        s.setCity(city);
        s.setCreateTime(LocalDateTime.now());
        s.setUpdateTime(LocalDateTime.now());
        return s;
    }

    @Test
    void testList() {
        List<Scenic> expected = Arrays.asList(createScenic(1L, "景点A", "四川省", "成都市"));
        when(scenicRepository.findAll()).thenReturn(expected);
        List<Scenic> result = scenicService.list();
        assertEquals(1, result.size());
        assertEquals("景点A", result.get(0).getName());
    }

    @Test
    void testGetByCity() {
        List<Scenic> expected = Arrays.asList(createScenic(1L, "景点A", "四川省", "成都市"));
        when(scenicRepository.findByCity("成都市")).thenReturn(expected);
        List<Scenic> result = scenicService.getByCity("成都市");
        assertEquals(1, result.size());
    }

    @Test
    void testGetByProvinceAndCity() {
        List<Scenic> expected = Arrays.asList(createScenic(1L, "景点A", "四川省", "成都市"));
        when(scenicRepository.findByProvinceAndCity("四川省", "成都市")).thenReturn(expected);
        List<Scenic> result = scenicService.getByProvinceAndCity("四川省", "成都市");
        assertEquals(1, result.size());
    }

    @Test
    void testSaveNew() {
        Scenic scenic = createScenic(null, "新景点", "四川省", null);
        when(amapService.geoDetail(anyString())).thenReturn(Collections.singletonMap("city", "成都市"));
        when(scenicRepository.save(any(Scenic.class))).thenAnswer(inv -> inv.getArgument(0));
        Scenic result = scenicService.save(scenic);
        assertNotNull(result);
        assertNotNull(result.getCreateTime());
        assertNotNull(result.getUpdateTime());
        assertEquals("成都市", result.getCity());
    }

    @Test
    void testSaveUpdateExisting() {
        Scenic existing = createScenic(1L, "旧景点", "四川省", "成都市");
        when(scenicRepository.save(any(Scenic.class))).thenAnswer(inv -> inv.getArgument(0));
        Scenic result = scenicService.save(existing);
        assertEquals("成都市", result.getCity());
    }

    @Test
    void testGetByIdFound() {
        Scenic scenic = createScenic(1L, "景点A", "四川省", "成都市");
        when(scenicRepository.findById(1L)).thenReturn(Optional.of(scenic));
        Scenic result = scenicService.getById(1L);
        assertNotNull(result);
        assertEquals("景点A", result.getName());
    }

    @Test
    void testGetByIdNotFound() {
        when(scenicRepository.findById(99L)).thenReturn(Optional.empty());
        Scenic result = scenicService.getById(99L);
        assertNull(result);
    }

    @Test
    void testUpdateExisting() {
        Scenic existing = createScenic(1L, "旧景点", "四川省", "成都市");
        Scenic update = createScenic(null, "新名字", "四川省", null);
        when(scenicRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(amapService.geoDetail(anyString())).thenReturn(Collections.singletonMap("city", "成都市"));
        when(scenicRepository.save(any(Scenic.class))).thenAnswer(inv -> inv.getArgument(0));
        Scenic result = scenicService.update(1L, update);
        assertNotNull(result);
        assertEquals(1L, result.getId().longValue());
        assertEquals("新名字", result.getName());
    }

    @Test
    void testUpdateNonExisting() {
        Scenic update = createScenic(null, "新名字", "四川省", "成都市");
        when(scenicRepository.findById(99L)).thenReturn(Optional.empty());
        Scenic result = scenicService.update(99L, update);
        assertNull(result);
    }

    @Test
    void testDelete() {
        scenicService.delete(1L);
        verify(scenicRepository, times(1)).deleteById(1L);
    }

    @Test
    void testSearchByNameOnly() {
        when(scenicRepository.findByNameContaining("长城")).thenReturn(Arrays.asList(createScenic(1L, "长城", "北京市", "北京市")));
        List<Scenic> result = scenicService.search(null, null, "长城");
        assertEquals(1, result.size());
    }

    @Test
    void testSearchByCityAndName() {
        when(scenicRepository.findByCityAndNameContaining("成都市", "熊猫")).thenReturn(Arrays.asList(createScenic(1L, "熊猫基地", "四川省", "成都市")));
        List<Scenic> result = scenicService.search(null, "成都市", "熊猫");
        assertEquals(1, result.size());
    }

    @Test
    void testSearchByProvinceAndName() {
        when(scenicRepository.findByProvinceAndNameContaining("四川省", "熊猫")).thenReturn(Arrays.asList(createScenic(1L, "熊猫基地", "四川省", "成都市")));
        List<Scenic> result = scenicService.search("四川省", null, "熊猫");
        assertEquals(1, result.size());
    }

    @Test
    void testSearchByCityOnly() {
        when(scenicRepository.findByCity("成都市")).thenReturn(Arrays.asList(createScenic(1L, "景点A", "四川省", "成都市")));
        List<Scenic> result = scenicService.search(null, "成都市", null);
        assertEquals(1, result.size());
    }

    @Test
    void testSearchByProvinceOnly() {
        when(scenicRepository.findByProvince("四川省")).thenReturn(Arrays.asList(createScenic(1L, "景点A", "四川省", "成都市")));
        List<Scenic> result = scenicService.search("四川省", null, null);
        assertEquals(1, result.size());
    }

    @Test
    void testSearchEmptyParams() {
        when(scenicRepository.findAll()).thenReturn(Arrays.asList(createScenic(1L, "景点A", "四川省", "成都市")));
        List<Scenic> result = scenicService.search(null, null, null);
        assertEquals(1, result.size());
    }

    @Test
    void testAutoFillCityWhenCityIsNull() {
        Scenic scenic = createScenic(null, "宽窄巷子", "四川省", null);
        when(amapService.geoDetail("宽窄巷子,四川省")).thenReturn(Collections.singletonMap("city", "成都市"));
        scenicService.save(scenic);
        assertEquals("成都市", scenic.getCity());
    }

    @Test
    void testAutoFillCityWhenCityIsEmpty() {
        Scenic scenic = createScenic(null, "宽窄巷子", "四川省", "");
        when(amapService.geoDetail("宽窄巷子,四川省")).thenReturn(Collections.singletonMap("city", "成都市"));
        scenicService.save(scenic);
        assertEquals("成都市", scenic.getCity());
    }

    @Test
    void testAutoFillCityWhenNameIsNull() {
        Scenic scenic = createScenic(null, null, "四川省", null);
        when(scenicRepository.save(any(Scenic.class))).thenAnswer(inv -> inv.getArgument(0));
        Scenic result = scenicService.save(scenic);
        assertNull(result.getCity());
    }

    @Test
    void testAutoFillCityWithoutProvince() {
        Scenic scenic = createScenic(null, "东方明珠", null, null);
        when(amapService.geoDetail("东方明珠")).thenReturn(Collections.singletonMap("city", "上海市"));
        when(scenicRepository.save(any(Scenic.class))).thenAnswer(inv -> inv.getArgument(0));
        Scenic result = scenicService.save(scenic);
        assertEquals("上海市", result.getCity());
    }

    @Test
    void testAutoFillCityGeoReturnsEmptyCity() {
        Scenic scenic = createScenic(null, "未知景点", "四川省", null);
        when(amapService.geoDetail("未知景点,四川省")).thenReturn(Collections.singletonMap("city", ""));
        when(scenicRepository.save(any(Scenic.class))).thenAnswer(inv -> inv.getArgument(0));
        Scenic result = scenicService.save(scenic);
        assertNull(result.getCity());
    }
}
