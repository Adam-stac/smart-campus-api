package com.smartcampus.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class Sensor {

    private String id;

    @NotBlank(message = "Sensor type must not be blank")
    private String type;

    @NotBlank(message = "Sensor status must not be blank")
    @Pattern(regexp = "ACTIVE|INACTIVE|MAINTENANCE", message = "Status must be ACTIVE, INACTIVE or MAINTENANCE")
    private String status;

    private double currentValue;

    @NotBlank(message = "Room ID must not be blank")
    private String roomId;

    public Sensor() {
    }

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}