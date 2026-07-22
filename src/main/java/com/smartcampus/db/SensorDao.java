package com.smartcampus.db;

import com.smartcampus.model.Sensor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SensorDao {

    public List<Sensor> findAll(String type) throws SQLException {
        String sql = (type != null && !type.isBlank())
                ? "SELECT id, type, status, current_value, room_id FROM sensors WHERE LOWER(type) = LOWER(?)"
                : "SELECT id, type, status, current_value, room_id FROM sensors";

        List<Sensor> sensors = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (type != null && !type.isBlank()) ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) sensors.add(map(rs));
            }
        }
        return sensors;
    }

    public Sensor findById(String id) throws SQLException {
        String sql = "SELECT id, type, status, current_value, room_id FROM sensors WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(Sensor sensor) throws SQLException {
        String sql = "INSERT INTO sensors (id, type, status, current_value, room_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sensor.getId());
            ps.setString(2, sensor.getType());
            ps.setString(3, sensor.getStatus());
            ps.setDouble(4, sensor.getCurrentValue());
            ps.setString(5, sensor.getRoomId());
            ps.executeUpdate();
        }
    }

    public void updateCurrentValue(String id, double value) throws SQLException {
        String sql = "UPDATE sensors SET current_value = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, value);
            ps.setString(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM sensors WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public boolean existsById(String id) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sensors WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private Sensor map(ResultSet rs) throws SQLException {
        Sensor sensor = new Sensor();
        sensor.setId(rs.getString("id"));
        sensor.setType(rs.getString("type"));
        sensor.setStatus(rs.getString("status"));
        sensor.setCurrentValue(rs.getDouble("current_value"));
        sensor.setRoomId(rs.getString("room_id"));
        return sensor;
    }
}