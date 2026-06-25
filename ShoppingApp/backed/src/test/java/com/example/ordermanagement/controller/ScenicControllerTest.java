package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Scenic;
import com.example.ordermanagement.service.ScenicService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ScenicControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ScenicService scenicService;

    @InjectMocks
    private ScenicController scenicController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(scenicController).build();
    }

    private Scenic createScenic(Long id, String name, String province, String city) {
        Scenic s = new Scenic();
        s.setId(id);
        s.setName(name);
        s.setProvince(province);
        s.setCity(city);
        return s;
    }

    @Test
    void testList() throws Exception {
        List<Scenic> scenics = Arrays.asList(createScenic(1L, "景点A", "四川省", "成都市"));
        when(scenicService.list()).thenReturn(scenics);

        mockMvc.perform(get("/scenic/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("景点A")));
    }

    @Test
    void testGetByIdFound() throws Exception {
        Scenic scenic = createScenic(1L, "景点A", "四川省", "成都市");
        when(scenicService.getById(1L)).thenReturn(scenic);

        mockMvc.perform(get("/scenic/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("景点A")));
    }

    @Test
    void testGetByIdNotFound() throws Exception {
        when(scenicService.getById(99L)).thenReturn(null);

        mockMvc.perform(get("/scenic/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAdd() throws Exception {
        Scenic input = createScenic(null, "新景点", "四川省", "成都市");
        Scenic saved = createScenic(1L, "新景点", "四川省", "成都市");
        saved.setAuditStatus(0);

        when(scenicService.save(any(Scenic.class))).thenReturn(saved);

        mockMvc.perform(post("/scenic/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.auditStatus", is(0)));
    }

    @Test
    void testUpdateFound() throws Exception {
        Scenic update = createScenic(null, "更新名", "四川省", "成都市");
        Scenic updated = createScenic(1L, "更新名", "四川省", "成都市");
        when(scenicService.update(eq(1L), any(Scenic.class))).thenReturn(updated);

        mockMvc.perform(put("/scenic/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("更新名")));
    }

    @Test
    void testUpdateNotFound() throws Exception {
        Scenic update = createScenic(null, "更新名", "四川省", "成都市");
        when(scenicService.update(eq(99L), any(Scenic.class))).thenReturn(null);

        mockMvc.perform(put("/scenic/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDelete() throws Exception {
        doNothing().when(scenicService).delete(1L);

        mockMvc.perform(delete("/scenic/1"))
                .andExpect(status().isNoContent());

        verify(scenicService, times(1)).delete(1L);
    }

    @Test
    void testSearch() throws Exception {
        List<Scenic> result = Arrays.asList(createScenic(1L, "熊猫基地", "四川省", "成都市"));
        when(scenicService.search(eq("四川省"), eq("成都市"), eq("熊猫"))).thenReturn(result);

        mockMvc.perform(get("/scenic/search")
                        .param("province", "四川省")
                        .param("city", "成都市")
                        .param("name", "熊猫"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("熊猫基地")));
    }
}
