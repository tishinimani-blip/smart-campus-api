/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

/**
 *
 * @author TISHINI
 */

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Part 3 — Sensor Resource
 * Manages /api/v1/sensors
 * Delegates readings sub-resource to SensorReadingResource (Part 4).
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private static final List<String> VALID_STATUSES = List.of("ACTIVE", "MAINTENANCE", "OFFLINE");
    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * GET /api/v1/sensors?type=CO2
     * Optional @QueryParam "type" filters by sensor type.
     * Query param is correct REST design for filtering — a path segment (/sensors/type/CO2)
     * would wrongly imply that "type/CO2" is a named resource.
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> list = new ArrayList<>(dataStore.getSensors().values());
        if (type != null && !type.isBlank()) {
            list = list.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return Response.ok(list).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor.
     * Validates roomId exists → 422 if not (LinkedResourceNotFoundException).
     * @Consumes(APPLICATION_JSON): sending text/plain or application/xml → 415 Unsupported Media Type.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Request body is required.")).build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "'roomId' is required.")).build();
        }
        if (sensor.getType() == null || sensor.getType().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "'type' is required (e.g. Temperature, CO2, Occupancy).")).build();
        }

        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Cannot register sensor: room '" + sensor.getRoomId() + "' does not exist in the system."
            );
        }

        if (sensor.getId() == null || sensor.getId().isBlank()) {
            String prefix = sensor.getType().substring(0, Math.min(4, sensor.getType().length())).toUpperCase();
            sensor.setId(prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        if (dataStore.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Sensor ID '" + sensor.getId() + "' already exists.")).build();
        }
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        } else {
            String s = sensor.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(s)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Invalid status. Allowed: ACTIVE, MAINTENANCE, OFFLINE.")).build();
            }
            sensor.setStatus(s);
        }

        dataStore.getSensors().put(sensor.getId(), sensor);
        room.addSensorId(sensor.getId());
        dataStore.getSensorReadings().put(sensor.getId(), new ArrayList<>());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    /** GET /api/v1/sensors/{sensorId} — get single sensor */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Sensor '" + sensorId + "' not found.")).build();
        }
        return Response.ok(sensor).build();
    }

    /** PUT /api/v1/sensors/{sensorId} — update sensor status, type, or room */
    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updated) {
        Sensor existing = dataStore.getSensor(sensorId);
        if (existing == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Sensor '" + sensorId + "' not found.")).build();
        }
        if (updated == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Request body is required.")).build();
        }
        if (updated.getType() != null && !updated.getType().isBlank()) {
            existing.setType(updated.getType());
        }
        if (updated.getStatus() != null && !updated.getStatus().isBlank()) {
            String s = updated.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(s)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Invalid status. Allowed: ACTIVE, MAINTENANCE, OFFLINE.")).build();
            }
            existing.setStatus(s);
        }
        if (updated.getRoomId() != null && !updated.getRoomId().isBlank()
                && !updated.getRoomId().equals(existing.getRoomId())) {
            Room newRoom = dataStore.getRoom(updated.getRoomId());
            if (newRoom == null) {
                throw new LinkedResourceNotFoundException(
                        "Cannot update sensor: room '" + updated.getRoomId() + "' does not exist."
                );
            }
            Room oldRoom = dataStore.getRoom(existing.getRoomId());
            if (oldRoom != null) oldRoom.removeSensorId(sensorId);
            newRoom.addSensorId(sensorId);
            existing.setRoomId(updated.getRoomId());
        }
        if (updated.getCurrentValue() != 0) {
            existing.setCurrentValue(updated.getCurrentValue());
        }
        return Response.ok(existing).build();
    }

    /** DELETE /api/v1/sensors/{sensorId} — remove sensor and unlink from room */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Sensor '" + sensorId + "' not found.")).build();
        }
        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room != null) room.removeSensorId(sensorId);
        dataStore.getSensors().remove(sensorId);
        dataStore.getSensorReadings().remove(sensorId);
        return Response.noContent().build();
    }

    /**
     * Part 4 — Sub-Resource Locator
     * Delegates /api/v1/sensors/{sensorId}/readings to SensorReadingResource.
     * Returns the sub-resource instance; JAX-RS dispatches GET/POST/DELETE to it.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new javax.ws.rs.NotFoundException("Sensor '" + sensorId + "' not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
