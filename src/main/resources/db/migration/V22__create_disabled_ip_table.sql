-- 학교별 사용불가 IP 지정 (IP대장 페이지용)
CREATE TABLE IF NOT EXISTS disabled_ip (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    school_id BIGINT NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    reason TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_disabled_ip_school FOREIGN KEY (school_id) REFERENCES school(school_id) ON DELETE CASCADE,
    CONSTRAINT uq_disabled_ip_school_address UNIQUE (school_id, ip_address)
);

CREATE INDEX idx_disabled_ip_school_id ON disabled_ip(school_id);
