/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

/**
 *
 * @author TISHINI
 */

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Part 1 — Discovery Endpoint
 * GET /api/v1 — returns API metadata and HATEOAS resource links.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        Map<String, Object> response = new HashMap<>();
        response.put("apiName", "Smart Campus Sensor & Room Management API");
        response.put("version", "v1.0.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors.");
        response.put("contact", Map.of(
                "name",  "Campus Facilities Admin",
                "email", "facilities@university.ac.uk"
        ));
        response.put("links", Map.of(
                "rooms",   "/api/v1/rooms",
                "sensors", "/api/v1/sensors"
        ));
        return Response.ok(response).build();
    }
}
