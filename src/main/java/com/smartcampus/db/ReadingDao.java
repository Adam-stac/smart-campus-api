package com.smartcampus.db;

import com.smartcampus.model.SensorReading;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReadingDao {

    public List<SensorReading> findBySensorId(String sensorId) throws SQLException {
        String sql = "SELECT id, sensor_id, value, timestamp FROM sensor_readings WHERE sensor_id = ? ORDER BY timestamp ASC";
        List<SensorReading> readings = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sensorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) readings.add(map(rs));
            }
        }
        return readings;
    }

    public void insert(String sensorId, SensorReading reading) throws SQLException {
        String sql = "INSERT INTO sensor_readings (id, sensor_id, value, timestamp) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reading.getId());
            ps.setString(2, sensorId);
            ps.setDouble(3, reading.getValue());
            ps.setLong(4, reading.getTimestamp());
            ps.executeUpdate();
        }
    }

    private SensorReading map(ResultSet rs) throws SQLException {
        SensorReading reading = new SensorReading();
        reading.setId(rs.getString("id"));
        reading.setValue(rs.getDouble("value"));
        reading.setTimestamp(rs.getLong("timestamp"));
        return reading;
    }
}