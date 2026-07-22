CREATE TABLE IF NOT EXISTS rooms (
    id          VARCHAR(20)  PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    capacity    INT          NOT NULL
);

CREATE TABLE IF NOT EXISTS sensors (
    id            VARCHAR(20)  PRIMARY KEY,
    type          VARCHAR(100) NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    current_value DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    room_id       VARCHAR(20)  NOT NULL REFERENCES rooms(id)
);

CREATE TABLE IF NOT EXISTS sensor_readings (
    id          VARCHAR(36)      PRIMARY KEY,
    sensor_id   VARCHAR(20)      NOT NULL REFERENCES sensors(id),
    value       DOUBLE PRECISION NOT NULL,
    timestamp   BIGINT           NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sensors_room_id ON sensors(room_id);
CREATE INDEX IF NOT EXISTS idx_readings_sensor_id ON sensor_readings(sensor_id);