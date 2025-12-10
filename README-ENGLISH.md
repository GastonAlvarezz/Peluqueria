# Peinados Cristy – Web Reservation System for Hair Salon

Peinados Cristy is a web-based system designed to digitalize the operations of a real hair salon.
The main goal of this project is to automate appointment scheduling, improve internal organization, and provide clients with a modern, professional experience.

The system allows users to book services online, view available time slots, receive dynamic price estimates based on hair type, and confirm their appointments instantly — eliminating the need for WhatsApp message coordination.

# Project Purpose

* This project seeks to solve common problems found in traditional salons:
* Delays in responses and message overload.
* Lack of calendar organization.
* Double bookings or overlapping appointments.
* No control over service duration and pricing.
* Zero automation.
* With this platform, the salon gains a fully digital, easy-to-use, scalable system designed to enhance operational efficiency.

# Key Features
# For Clients

* Service selection (haircut, nails, color, treatments).
* Hair type selection: short, medium, or long.
* Dynamic calculation of service price and duration.
* Viewing available time slots.
* Instant confirmation of the appointment.

# For Administrators

* Appointment management dashboard.
* List of appointments by date and service.
* Automatic collision and conflict prevention.
* Operational control over the salon’s daily schedule.

⚙️ Technologies Used

* Backend: Java 17, Spring Boot, Spring Data JPA
* Frontend: HTML5, CSS3, Thymeleaf
* Database: MySQL
* Dependency Management: Maven
* Concurrency: synchronized, Runnable, protected critical sections to avoid race conditions

# Highlighted Logic
✔ Available Time Calculation

* The system evaluates:
* Opening and closing hours.
* Selected service duration.
* Existing appointments for the day.
* Automatically prevents overlapping bookings.

✔ Concurrency Management

* Critical operations (such as saving appointments) are protected using a custom synchronization manager, preventing race conditions.

✔ Dynamic Pricing

* Final service price is automatically calculated based on hair type via adjustable multipliers.

# Architecture

* The application follows a traditional MVC architecture:
* Model: JPA Entities (Turno, Servicio).
* View: Thymeleaf + HTML + CSS.
* Controller: REST controllers + web controllers for UI rendering.
* Service Layer: Handles core business logic.

# Project Status

* Fully functional
* Strong architecture
* Ready to scale and expand
