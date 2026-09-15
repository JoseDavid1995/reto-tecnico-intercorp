package com.example.demo.service;

import com.example.demo.entity.RegistroEntity;
import com.example.demo.repository.RegistroRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class KafkaConsumerService {

    private final RegistroRepository registroRepository;

    public KafkaConsumerService(RegistroRepository registroRepository) {
        this.registroRepository = registroRepository;
    }

    @KafkaListener(topics = "registros-topic", groupId = "intercorp-group")
    public void consumirRegistro(String mensaje, @Header(KafkaHeaders.RECEIVED_KEY) String idKey) {
        try {
            Long id = Long.valueOf(idKey);
            Optional<RegistroEntity> optionalRegistro = registroRepository.findById(id);

            if (optionalRegistro.isPresent()) {
                RegistroEntity registro = optionalRegistro.get();
                registro.setEstado("COMPLETED");
                registro.setDetalle("Procesado exitosamente por el Consumer");
                registroRepository.save(registro);
                System.out.println(">>> [Servicio B] Registro ID " + id + " actualizado a COMPLETED.");
            }
        } catch (Exception e) {
            System.err.println("Error procesando mensaje de Kafka en Servicio B: " + e.getMessage());
        }
    }
}