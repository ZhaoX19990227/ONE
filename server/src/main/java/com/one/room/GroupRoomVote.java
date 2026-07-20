package com.one.room;

import com.one.common.AuditedEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_room_vote")
public class GroupRoomVote extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "room_id") private GroupDecisionRoom room;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "candidate_id") private GroupRoomCandidate candidate;
    @Column(name = "user_id", nullable = false) private long userId;

    protected GroupRoomVote() {}
    public static GroupRoomVote cast(GroupDecisionRoom room, GroupRoomCandidate candidate, long userId) {
        GroupRoomVote vote = new GroupRoomVote(); vote.room = room; vote.candidate = candidate; vote.userId = userId; return vote;
    }
    public void changeTo(GroupRoomCandidate candidate) { this.candidate = candidate; }
    public GroupRoomCandidate getCandidate() { return candidate; }
}
