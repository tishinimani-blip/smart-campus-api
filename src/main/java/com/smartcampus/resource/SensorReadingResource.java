/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

/**
 *
 * @author TISHINI
 */

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Part 4 — Sub-Resource: Sensor Readings
 * Handles /api/v1/sensors/{sensorId}/readings
 * Returned by SensorResource via the Sub-Resource Locator pattern.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /** GET /api/v1/sensors/{sensorId}/readings — full history */
    @GET
    public Response getReadings() {
        List<SensorReading> readings = dataStore.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    /** GET /api/v1/sensors/{sensorId}/readings/latest — most recent reading */
    @GET
    @Path("/latest")
    public Response getLatestReading() {
        List<SensorReading> readings = dataStore.getReadingsForSensor(sensorId);
        if (readings.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "No readings found for sensor '" + sensorId + "'."))
                    .build();
        }
        Optional<SensorReading> latest = readings.stream()
                .max(Comparator.comparingLong(SensorReading::getTimestamp));
        return Response.ok(latest.get()).build();
    }

    /** GET /api/v1/sensors/{sensorId}/readings/{readingId} — single reading */
    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        List<SensorReading> readings = dataStore.getReadingsForSensor(sensorId);
        return readings.stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Reading '" + readingId + "' not found for sensor '" + sensorId + "'."))
                        .build());
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings — add a reading.
     * BLOCKED if sensor is MAINTENANCE or OFFLINE → 403 Forbidden.
     * Side Effect: updates parent Sensor.currentValue to the new value.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Sensor '" + sensorId + "' not found.")).build();
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is under MAINTENANCE and cannot accept new readings. " +
                    "Set status to ACTIVE first."
            );
        }
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is OFFLINE and cannot accept new readings."
            );
        }
        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Request body with a 'value' field is required.")).build();
        }
        SensorReading newReading = new SensorReading(reading.getValue());
        dataStore.getReadingsForSensor(sensorId).add(newReading);
        sensor.setCurrentValue(newReading.getValue());
        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }

    /** DELETE /api/v1/sensors/{sensorId}/readings/{readingId} — remove a reading */
    @DELETE
    @Path("/{readingId}")
    public Response deleteReading(@PathParam("readingId") String readingId) {
        List<SensorReading> readings = dataStore.getReadingsForSensor(sensorId);
        boolean removed = readings.removeIf(r -> r.getId().equals(readingId));
        if (!removed) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Reading '" + readingId + "' not found for sensor '" + sensorId + "'."))
                    .build();
        }
        return Response.noContent().build();
    }
}
