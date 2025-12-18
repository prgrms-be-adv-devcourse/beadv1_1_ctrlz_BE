CREATE TABLE IF NOT EXISTS user_behavior (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(255),
    behavior_value VARCHAR(255),
    behavior_type VARCHAR(50),
    created_at TIMESTAMP
);
