# Reto Técnico - Arquitectura Orientada a Eventos con Spring Boot y Kafka

Sistema distribuido compuesto por tres microservicios orientados a procesar archivos masivos de manera asíncrona mediante lotes (Batch), mensajería orientada a eventos con Apache Kafka y un API de monitoreo en tiempo real.

---

## Arquitectura del Sistema

El sistema se compone de los siguientes microservicios desacoplados:

1. **Servicio A (`servicio-batch-producer` - Puerto 8081)**:  
   - Implementado con **Spring Batch**. Lee archivos CSV/Excel en bloques (*chunks*) para optimizar el uso de memoria.
   - Persiste los registros en la base de datos con un estado inicial `PENDING` utilizando JPA.
   - Publica un evento asíncrono en un tópico de Apache Kafka indicando que hay nueva data lista para ser procesada.

2. **Servicio B (`servicio-consumidor` - Puerto 8082)**:
   - Microservicio suscriptor al tópico de Kafka mediante `@KafkaListener`.
   - Simula un procesamiento de negocio asíncrono y pesado.
   - Actualiza de forma automática el estado del registro en la base de datos compartida a `COMPLETED`.

3. **Servicio C (`servicio-monitoreo` - Puerto 8083)**:
   - Expone una API REST ligera para clientes y frontends.
   - Consulta de manera eficiente el estado general del sistema (conteo de registros totales, pendientes y completados) mediante repositorios JPA, sin acoplarse al broker de mensajería.

---

## Justificación Técnica de Kafka (Rendimiento y Latencia)

Tal como se solicita en los requerimientos de optimización de latencia y throughput, la configuración implementada en los productores y consumidores responde a los siguientes criterios:

* **Estrategia de Particionamiento y Consumo**: Se utiliza el ID del registro como clave (*key*) al enviar el mensaje a Kafka. Esto garantiza que todos los eventos relacionados con un mismo registro mantengan el orden estricto de procesamiento y se distribuyan equitativamente entre las particiones del tópico.
* **Propiedad `acks` (Durabilidad vs. Throughput)**: Se configuró el nivel de confirmación del productor para equilibrar la consistencia de los datos masivos y la velocidad. Al procesar lotes grandes de datos (*batch*), un nivel de acuse óptimo asegura que el productor no sufra cuellos de botella bloqueantes, manteniendo un alto *throughput* sin sacrificar la trazabilidad en la base de datos.
* **Procesamiento Asíncrono Desacoplado**: El uso de Kafka desacopla por completo al Servicio A del Servicio B. El Servicio Batch no espera una respuesta sincrónica del consumidor, evitando bloqueos y permitiendo escalar horizontalmente el procesamiento en segundo plano según la carga operativa.

---

## Guía de Despliegue Inmediato (Docker Compose)

Todo el ecosistema (infraestructura + los 3 microservicios) se encuentra dockerizado para un despliegue transparente.

### Prerrequisitos
* Tener instalado **Docker** y **Docker Compose**.

### Instrucciones de Ejecución

1. Clona o ubícate en la raíz del repositorio donde se encuentra el archivo `docker-compose.yml`.
2. Asegúrate de que las carpetas de tus microservicios estén en el mismo directorio raíz con los nombres correspondientes (`servicio-batch-producer`, `servicio-consumidor`, `servicio-monitoreo`).
3. Ejecuta el siguiente comando para compilar las imágenes y levantar todo el entorno:
   ```bash
   docker-compose up --build -d
   
1- Los servicios se inicializarán en el siguiente orden de dependencias:

Base de datos MySQL (3306)

Zookeeper y Apache Kafka (9092)

Servicio A (8081)

Servicio B (8082)

Servicio C (8083)


Endpoints de Pruebas y Monitoreo

0. Limpiar BD antes de prueba E2E
docker exec -it intercorp_mysql mysql -uroot -ppassword intercorp_db -e "TRUNCATE TABLE registros;"

1. Ejecutar el Proceso Batch (Servicio A)
POST: http://localhost:8081/api/batch/run (o el endpoint que hayas configurado en tu controlador para disparar el lector CSV)


2. Consultar Estadísticas del Sistema (Servicio C)
GET: http://localhost:8083/api/monitoreo/estadisticas

Respuesta esperada:

{
  "totalRegistros": 5,
  "registrosPendientes": 0,
  "registrosCompletados": 5
}

3. Listar Todos los Registros y su Estado (Servicio C)
GET: http://localhost:8083/api/monitoreo/registros

Pruebas Unitarias
El proyecto cuenta con pruebas unitarias implementadas con JUnit 5 y Mockito para aislar las dependencias externas (repositorios JPA y KafkaTemplate)

Para ejecutar las pruebas de manera local en cualquier microservicio mediante Maven:
mvn test