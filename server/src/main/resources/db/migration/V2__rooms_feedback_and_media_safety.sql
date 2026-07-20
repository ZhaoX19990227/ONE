ALTER TABLE decision_session
    ADD COLUMN winner_candidate_id BIGINT NULL AFTER context_json,
    ADD KEY idx_decision_winner (winner_candidate_id);

CREATE TABLE recommendation_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    dimension VARCHAR(20) NOT NULL,
    item_id BIGINT NOT NULL,
    source_session_id BIGINT NULL,
    feedback_type VARCHAR(20) NOT NULL,
    reason VARCHAR(40) NULL,
    expires_at DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_rec_feedback_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    CONSTRAINT fk_rec_feedback_item FOREIGN KEY (item_id) REFERENCES catalog_item (id),
    CONSTRAINT fk_rec_feedback_session FOREIGN KEY (source_session_id) REFERENCES decision_session (id),
    KEY idx_rec_feedback_active (user_id, dimension, expires_at),
    KEY idx_rec_feedback_item (user_id, item_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE group_decision_room (
    id BIGINT NOT NULL AUTO_INCREMENT,
    share_code VARCHAR(12) NOT NULL,
    owner_user_id BIGINT NOT NULL,
    title VARCHAR(60) NOT NULL,
    dimension VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    winner_candidate_id BIGINT NULL,
    expires_at DATETIME(3) NOT NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_room_share_code (share_code),
    CONSTRAINT fk_room_owner FOREIGN KEY (owner_user_id) REFERENCES user_account (id),
    KEY idx_room_owner_created (owner_user_id, created_at),
    KEY idx_room_status_expires (status, expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE group_room_candidate (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    item_id BIGINT NOT NULL,
    position_no INT NOT NULL,
    created_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_room_candidate_room FOREIGN KEY (room_id) REFERENCES group_decision_room (id) ON DELETE CASCADE,
    CONSTRAINT fk_room_candidate_item FOREIGN KEY (item_id) REFERENCES catalog_item (id),
    UNIQUE KEY uk_room_item (room_id, item_id),
    UNIQUE KEY uk_room_position (room_id, position_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE group_decision_room
    ADD CONSTRAINT fk_room_winner FOREIGN KEY (winner_candidate_id) REFERENCES group_room_candidate (id) ON DELETE SET NULL;

CREATE TABLE group_room_vote (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    candidate_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME(3) NOT NULL,
    updated_at DATETIME(3) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_room_vote_room FOREIGN KEY (room_id) REFERENCES group_decision_room (id) ON DELETE CASCADE,
    CONSTRAINT fk_room_vote_candidate FOREIGN KEY (candidate_id) REFERENCES group_room_candidate (id) ON DELETE CASCADE,
    CONSTRAINT fk_room_vote_user FOREIGN KEY (user_id) REFERENCES user_account (id),
    UNIQUE KEY uk_room_user_vote (room_id, user_id),
    KEY idx_room_vote_candidate (candidate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE media_asset
    ADD COLUMN safety_status VARCHAR(20) NOT NULL DEFAULT 'NOT_CHECKED' AFTER status,
    ADD COLUMN safety_label VARCHAR(80) NULL AFTER safety_status;
