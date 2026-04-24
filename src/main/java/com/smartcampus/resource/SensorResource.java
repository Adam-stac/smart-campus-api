package com.smartcampus.resource;

import com.smartcampus.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/sensors")
public class SensorResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(DataStore.getSensors().values());
        if (type != null && !type.isBlank()) {
            sensors.removeIf(s -> !s.getType().equalsIgnoreCase(type));
        }
        return Response.ok(sensors).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (!DataStore.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room with ID " + sensor.getRoomId() + " does not exist.");
        }
        String id = "SENS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        sensor.setId(id);
        DataStore.getSensors().put(id, sensor);
        DataStore.getRooms().get(sensor.getRoomId()).getSensorIds().add(id);
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\"}")
                    .build();
        }
        return Response.ok(sensor).build();
    }

    @DELETE
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Sensor not found\"}")
                    .build();
        }
        DataStore.getSensors().remove(sensorId);
        DataStore.getRooms().get(sensor.getRoomId()).getSensorIds().remove(sensorId);
        return Response.noContent().build();
    }

    @Path("{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}