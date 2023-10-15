package ru.practicum;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.dto.StatsHitDto;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@AutoConfigureMockMvc
@WebMvcTest(controllers = StatController.class)
public class StatControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StatServiceImpl statService;

    @Mock
    StatRepository statRepository;

    @Autowired
    private MockMvc mockMvc;

    @InjectMocks
    private StatController statController;

    @Test
    public void testSaveStatsHit() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        StatsHitDto statsHitDto = StatsHitDto.builder()
                .app("ewm-main-service")
                .uri("/events/2")
                .ip("192.163.0.1")
                .timestamp(Timestamp.valueOf(now).toLocalDateTime())
                .build();
        mockMvc.perform(MockMvcRequestBuilders.post("/hit")
                        .contentType("application/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statsHitDto)))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(statService, times(1)).saveStat(any(StatsHitDto.class));
    }
}
