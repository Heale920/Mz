CREATE TABLE IF NOT EXISTS admin_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id BIGINT NOT NULL,
    admin_name VARCHAR(50) NOT NULL,
    admin_action_type VARCHAR(50) NOT NULL,
    detail TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_admin_id (admin_id),
    INDEX idx_action_type (admin_action_type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 