package com.one.room;

import com.one.common.AuditedEntity;
import com.one.common.BusinessException;
import com.one.common.Dimension;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Entity
@Table(name = "group_decision_room")
public class GroupDecisionRoom extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "share_code", nullable = false, unique = true, length = 12) private String shareCode;
    @Column(name = "owner_user_id", nullable = false) private long ownerUserId;
    @Column(nullable = false, length = 60) private String title;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Dimension dimension;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private GroupRoomStatus status;
    @Column(name = "winner_candidate_id") private Long winnerCandidateId;
    @Column(name = "expires_at", nullable = false) private Instant expiresAt;
    @Version private long version;

    protected GroupDecisionRoom() {}

    public static GroupDecisionRoom open(String code, long ownerUserId, String title, Dimension dimension) {
        GroupDecisionRoom room = new GroupDecisionRoom();
        room.shareCode = code; room.ownerUserId = ownerUserId; room.title = title.strip();
        room.dimension = dimension; room.status = GroupRoomStatus.OPEN;
        room.expiresAt = Instant.now().plusSeconds(24 * 60 * 60);
        return room;
    }

    public void ensureOpen() {
        if (status == GroupRoomStatus.OPEN && expiresAt.isBefore(Instant.now())) status = GroupRoomStatus.EXPIRED;
        if (status != GroupRoomStatus.OPEN) {
            throw new BusinessException("ROOM_CLOSED", "这次选择已经结束", HttpStatus.CONFLICT);
        }
    }

    public void close(Long winnerCandidateId) {
        ensureOpen(); this.winnerCandidateId = winnerCandidateId; this.status = GroupRoomStatus.CLOSED;
    }

    public Long getId() { return id; }
    public String getShareCode() { return shareCode; }
    public long getOwnerUserId() { return ownerUserId; }
    public String getTitle() { return title; }
    public Dimension getDimension() { return dimension; }
    public GroupRoomStatus getStatus() { return status; }
    public Long getWinnerCandidateId() { return winnerCandidateId; }
    public Instant getExpiresAt() { return expiresAt; }
}
