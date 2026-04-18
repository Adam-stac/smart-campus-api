package com.smartcampus;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    public static Map<String, Room> getRooms() {
        return rooms;
    }
    public static Map<String, Sensor> getSensors() {
        return sensors;
    }
    public static Map<String, List<SensorReading>> getReadings() {
        return readings;
    }
}