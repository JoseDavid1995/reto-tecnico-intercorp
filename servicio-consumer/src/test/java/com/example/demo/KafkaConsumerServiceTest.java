package com.example.demo;

import com.example.demo.entity.RegistroEntity;
import com.example.demo.repository.RegistroRepository;
import com.example.demo.service.KafkaConsumerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private RegistroRepository registroRepository;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @Test
    void testConsumirRegistroExitoso() {
        String mensajePayload = "REG-001-VALOR";
        String idKey = "10";

        RegistroEntity registroExistente = new RegistroEntity();
        registroExistente.setId(10L);
        registroExistente.setCodigoUnico("REG-001-VALOR");
        registroExistente.setEstado("PENDING");

        when(registroRepository.findById(10L)).thenReturn(Optional.of(registroExistente));
        when(registroRepository.save(any(RegistroEntity.class))).thenReturn(registroExistente);

        kafkaConsumerService.consumirRegistro(mensajePayload, idKey);

        assertEquals("COMPLETED", registroExistente.getEstado());
        
        verify(registroRepository, times(1)).findById(10L);
        verify(registroRepository, times(1)).save(registroExistente);
    }
}