package com.example.demo;

import com.example.demo.controller.MonitoreoController;
import com.example.demo.entity.RegistroEntity;
import com.example.demo.repository.RegistroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoreoControllerTest {

    @Mock
    private RegistroRepository registroRepository;

    @InjectMocks
    private MonitoreoController monitoreoController;

    @Test
    void testObtenerEstadisticas() {
        when(registroRepository.count()).thenReturn(10L);
        when(registroRepository.countByEstado("PENDING")).thenReturn(3L);
        when(registroRepository.countByEstado("COMPLETED")).thenReturn(7L);

        ResponseEntity<Map<String, Object>> response = monitoreoController.obtenerEstadisticas();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        
        Map<String, Object> stats = response.getBody();
        assertNotNull(stats);
        assertEquals(10L, stats.get("totalRegistros"));
        assertEquals(3L, stats.get("registrosPendientes"));
        assertEquals(7L, stats.get("registrosCompletados"));

        verify(registroRepository, times(1)).count();
        verify(registroRepository, times(1)).countByEstado("PENDING");
        verify(registroRepository, times(1)).countByEstado("COMPLETED");
    }

    @Test
    void testObtenerTodosLosRegistros() {
        RegistroEntity r1 = new RegistroEntity();
        r1.setId(1L);
        r1.setEstado("COMPLETED");

        List<RegistroEntity> listaSimulada = Arrays.asList(r1);
        when(registroRepository.findAll()).thenReturn(listaSimulada);

        ResponseEntity<List<RegistroEntity>> response = monitoreoController.obtenerTodos();

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        verify(registroRepository, times(1)).findAll();
    }
}