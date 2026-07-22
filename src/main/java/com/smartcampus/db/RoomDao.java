package com.smartcampus.db;

import com.smartcampus.model.Room;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {

    public List<Room> findAll() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT id, name, capacity FROM rooms";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(map(rs));
            }
        }
        return rooms;
    }

    public Room findById(String id) throws SQLException {
        String sql = "SELECT id, name, capacity FROM rooms WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (id, name, capacity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getId());
            ps.setString(2, room.getName());
            ps.setInt(3, room.getCapacity());
            ps.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM rooms WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
    }

    public boolean hasSensors(String roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM sensors WHERE room_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private Room map(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setId(rs.getString("id"));
        room.setName(rs.getString("name"));
        room.setCapacity(rs.getInt("capacity"));
        return room;
    }
}