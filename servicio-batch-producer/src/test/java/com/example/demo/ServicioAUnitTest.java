package com.example.demo;

import com.example.demo.entity.RegistroEntity;
import com.example.demo.repository.RegistroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioAUnitTest {

    @Mock
    private RegistroRepository registroRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;


    @Test
    void testGuardarYEnviarKafkaExitoso() {
        // 1. Arrange (Preparar datos de prueba)
        RegistroEntity registroInput = new RegistroEntity();
        registroInput.setCodigoUnico("REG-001-VALOR");
        registroInput.setEstado("PENDING");
        registroInput.setDetalle("Procesado por Batch");

        RegistroEntity registroGuardado = new RegistroEntity();
        registroGuardado.setId(10L);
        registroGuardado.setCodigoUnico("REG-001-VALOR");
        registroGuardado.setEstado("PENDING");
        registroGuardado.setDetalle("Procesado por Batch");

        when(registroRepository.save(any(RegistroEntity.class))).thenReturn(registroGuardado);

        RegistroEntity resultado = registroRepository.save(registroInput);
        
        kafkaTemplate.send("registros-topic", String.valueOf(resultado.getId()), resultado.getCodigoUnico());

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("PENDING", resultado.getEstado());

        verify(registroRepository, times(1)).save(any(RegistroEntity.class));
        
        verify(kafkaTemplate, times(1)).send(
                eq("registros-topic"), 
                eq("10"), 
                eq("REG-001-VALOR")
        );
    }
}