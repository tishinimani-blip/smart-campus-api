# Smart Campus Sensor & Room Management API

---

## 📌 Module Information

* **Module:** 5COSC022W — Client-Server Architectures
* **University:** University of Westminster
* **Technology Stack:** Java 11, JAX-RS (Jersey), Apache Tomcat 9, Maven
* **Storage:** In-memory (ConcurrentHashMap)

---

## 📖 API Overview

This project implements a RESTful API for managing Smart Campus infrastructure.

The system allows:

* Managing **Rooms**
* Managing **Sensors**
* Storing **Sensor Readings (historical data)**

**Base URL:**
http://localhost:8080/smart-campus-api/api/v1

All responses are returned in **JSON format**.

---

## 📦 Data Models

### 🏫 Room

```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50,
  "sensorIds": ["TEMP-001", "CO2-001"]
}
```

### 🌡️ Sensor

```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 22.5,
  "roomId": "LIB-301"
}
```

**Valid Status Values:**
ACTIVE, MAINTENANCE, OFFLINE

### 📊 Sensor Reading

```json
{
  "id": "uuid",
  "timestamp": 1714000000000,
  "value": 22.5
}
```

---

## 🔗 API Endpoints

### 🔹 Discovery

* GET /api/v1

---

### 🔹 Room Management

* GET /api/v1/rooms
* POST /api/v1/rooms
* GET /api/v1/rooms/{roomId}
* PUT /api/v1/rooms/{roomId}
* DELETE /api/v1/rooms/{roomId}
* GET /api/v1/rooms/{roomId}/sensors

---

### 🔹 Sensor Management

* GET /api/v1/sensors
* GET /api/v1/sensors?type=Temperature
* POST /api/v1/sensors
* GET /api/v1/sensors/{sensorId}
* PUT /api/v1/sensors/{sensorId}
* DELETE /api/v1/sensors/{sensorId}

---

### 🔹 Sensor Readings

* GET /api/v1/sensors/{sensorId}/readings
* POST /api/v1/sensors/{sensorId}/readings
* GET /api/v1/sensors/{sensorId}/readings/{readingId}
* DELETE /api/v1/sensors/{sensorId}/readings/{readingId}

---

## ⚙️ Build & Run Instructions

### 1. Clone Repository

```
git clone https://github.com/<your-username>/smart-campus-api.git
cd smart-campus-api
```

### 2. Build Project

```
mvn clean package
```

### 3. Deploy to Tomcat

Copy the generated WAR file into:

```
Tomcat/webapps/
```

### 4. Start Server

```
startup.bat
```

### 5. Test API

Open in browser:

```
http://localhost:8080/smart-campus-api/api/v1
```

---

## 🧪 Sample API Requests

### ✔ Create Room

```json
POST /rooms
{
  "id": "CONF-202",
  "name": "Conference Room",
  "capacity": 20
}
```

### ✔ Create Sensor

```json
POST /sensors
{
  "id": "TEMP-003",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 25,
  "roomId": "CONF-202"
}
```

### ✔ Add Reading

```json
POST /sensors/TEMP-003/readings
{
  "value": 26.5
}
```

---

## ⚠️ Error Handling

| Status Code | Description            |
| ----------- | ---------------------- |
| 409         | Room has sensors       |
| 422         | Invalid room reference |
| 403         | Sensor not active      |
| 500         | Internal server error  |

---

## 📚 Conceptual Answers

### ✔ JAX-RS Lifecycle

JAX-RS creates a new instance per request.
Therefore shared data is managed using a singleton (ConcurrentHashMap).

---

### ✔ HATEOAS

The API provides navigation links in responses, allowing clients to discover resources dynamically.

---

### ✔ DELETE Idempotency

Deleting the same resource multiple times results in the same final state.

---

### ✔ Query Parameters

Filtering using query parameters (?type=) is flexible and follows REST standards.

---

### ✔ Sub-Resource Pattern

Separates logic into smaller classes, improving maintainability and scalability.

---

### ✔ HTTP 422

Used when request is valid but contains invalid data (e.g., non-existent room).

---

### ✔ Security

Stack traces are hidden to prevent exposing internal system details.

---

### ✔ Logging

Logging is implemented using filters to track all requests and responses centrally.


