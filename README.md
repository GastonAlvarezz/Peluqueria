Peinados Cristy – Sistema Web de Reservas para Peluquería

Peinados Cristy es un sistema web desarrollado para digitalizar la operación de una peluquería real.
El objetivo principal del proyecto es automatizar la gestión de turnos, mejorar la organización interna y ofrecer una experiencia más profesional y moderna a los clientes.

El sistema permite que los usuarios reserven servicios de manera online, visualicen horarios disponibles, reciban un precio estimado según el tipo de pelo y confirmen su turno sin necesidad de enviar mensajes por WhatsApp.

 # Objetivo del Proyecto

El proyecto busca resolver problemas comunes en peluquerías tradicionales:
Demoras en respuestas y saturación de mensajes.
Conflictos por falta de organización en la agenda.
Doble reserva o superposición de turnos.
Falta de control sobre duración y precio de cada servicio.
Ausencia de automatización.
Con este sistema, la peluquería obtiene una plataforma 100% digital, fácil de usar, escalable y pensada para mejorar su eficiencia operativa.

# Características Principales
 Para Clientes

* Selección de servicios (corte, uñas, color, tratamientos).
* Elección del tipo de pelo: corto, medio o largo.
* Cálculo dinámico del precio y duración según el servicio.
* Visualización de horarios disponibles.
* Confirmación instantánea del turno.

# Para Administradores

* Panel de gestión de turnos.
* Listado por fecha y filtrado por servicio.
* Evita colisiones y turnos duplicados.
* Control del flujo operativo de la peluquería.

# Tecnologías Utilizadas

* Backend: Java 17, Spring Boot, Spring Data JPA
* Frontend: HTML5, CSS3, Thymeleaf
* Base de Datos: MySQL
* Gestión de Dependencias: Maven
* Concurrencia: synchronized, Runnable, servicios protegidos contra condiciones de carrera

# Lógica Destacada
✔ Cálculo de horarios disponibles

El sistema evalúa:

* Apertura y cierre del local.
* Duración del servicio elegido.
* Turnos ya reservados ese día.
* Evita solapamientos automáticamente.

✔ Manejo de concurrencia

* Operaciones críticas (como guardar turnos) se protegen para evitar condiciones de carrera usando un gestor de sincronización.

✔ Precio dinámico

* El precio final depende del tipo de pelo, aplicando un multiplicador automático.

# Arquitectura

* El proyecto utiliza una arquitectura clásica MVC:
* Modelo: Entidades JPA (Turno, Servicio).
* Vista: Thymeleaf + HTML + CSS.
* Controlador: REST + controladores web para renderizado.
* Servicio: Contiene la lógica de negocio central.

# Estado del Proyecto

* Proyecto funcional
* Arquitectura sólida
* Listo para ampliarse a producción
