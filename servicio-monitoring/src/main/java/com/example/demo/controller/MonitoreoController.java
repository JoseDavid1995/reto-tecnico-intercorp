package com.example.demo.controller;

import com.example.demo.entity.RegistroEntity;
import com.example.demo.repository.RegistroRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoreo")
public class MonitoreoController {

    private final RegistroRepository registroRepository;

    public MonitoreoController(RegistroRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    // 1. Listar todos los registros
    @GetMapping("/registros")
    public ResponseEntity<List<RegistroEntity>> obtenerTodos() {
        return ResponseEntity.ok(registroRepository.findAll());
    }

    // 2. Dashboard de estadísticas (Pendientes, Procesados, Totales)
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        long total = registroRepository.count();
        long pendientes = registroRepository.countByEstado("PENDING");
        long completados = registroRepository.countByEstado("COMPLETED");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRegistros", total);
        stats.put("registrosPendientes", pendientes);
        stats.put("registrosCompletados", completados);

        return ResponseEntity.ok(stats);
    }
}