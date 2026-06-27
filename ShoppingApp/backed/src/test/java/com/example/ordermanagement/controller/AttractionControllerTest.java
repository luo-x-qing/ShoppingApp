package com.example.ordermanagement.controller;

import com.example.ordermanagement.service.AmapService;
import com.example.ordermanagement.service.AttractionService;
import com.example.ordermanagement.service.DbBackupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AttractionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AttractionService attractionService;

    @Mock
    private AmapService amapService;

    @Mock
    private DbBackupService dbBackupService;

    @InjectMocks
    private AttractionController attractionController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(attractionController).build();
    }

    @Test
    void testBackup_success() throws Exception {
        when(dbBackupService.backupNow()).thenReturn(true);

        mockMvc.perform(post("/api/attractions/backup")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("备份成功")));
    }

    @Test
    void testBackup_failure() throws Exception {
        when(dbBackupService.backupNow()).thenReturn(false);

        mockMvc.perform(post("/api/attractions/backup")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("备份失败")));
    }
}
