package com.one.room;

import com.one.catalog.CatalogItem;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "group_room_candidate")
public class GroupRoomCandidate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "room_id") private GroupDecisionRoom room;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "item_id") private CatalogItem item;
    @Column(name = "position_no", nullable = false) private int positionNo;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    protected GroupRoomCandidate() {}
    public static GroupRoomCandidate of(GroupDecisionRoom room, CatalogItem item, int position) {
        GroupRoomCandidate value = new GroupRoomCandidate();
        value.room = room; value.item = item; value.positionNo = position; return value;
    }
    @PrePersist void beforeCreate() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public GroupDecisionRoom getRoom() { return room; }
    public CatalogItem getItem() { return item; }
    public int getPositionNo() { return positionNo; }
}
