package com.one.recommendation;

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

@Entity
@Table(name = "decision_session")
public class DecisionSession extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id", nullable = false) private long userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Dimension dimension;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private DecisionMode mode;
    @Enumerated(EnumType.STRING) @Column(name = "time_slot", nullable = false, length = 20) private TimeSlot timeSlot;
    @Column(name = "budget_max_fen") private Integer budgetMaxFen;
    @Column(name = "context_json", columnDefinition = "json") private String contextJson;
    @Column(name = "winner_candidate_id") private Long winnerCandidateId;
    @Column(name = "chosen_candidate_id") private Long chosenCandidateId;
    @Column(name = "actual_record_id") private Long actualRecordId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private DecisionStatus status;
    @Version private long version;

    protected DecisionSession() {}

    public static DecisionSession presented(long userId, Dimension dimension, DecisionMode mode,
                                            TimeSlot timeSlot, Integer budgetMaxFen, String contextJson) {
        DecisionSession session = new DecisionSession();
        session.userId = userId; session.dimension = dimension; session.mode = mode;
        session.timeSlot = timeSlot; session.budgetMaxFen = budgetMaxFen;
        session.contextJson = contextJson; session.status = DecisionStatus.PRESENTED;
        return session;
    }

    public void choose(long candidateId) {
        if (status != DecisionStatus.PRESENTED && status != DecisionStatus.CHOSEN) {
            throw new BusinessException("DECISION_CLOSED", "这轮推荐已经结束", HttpStatus.CONFLICT);
        }
        this.chosenCandidateId = candidateId;
        this.status = DecisionStatus.CHOSEN;
    }

    public void presentWinner(long candidateId) {
        if (mode == DecisionMode.SPIN && status == DecisionStatus.PRESENTED) this.winnerCandidateId = candidateId;
    }

    public void record(long recordId) {
        if (status != DecisionStatus.CHOSEN || chosenCandidateId == null) {
            throw new BusinessException("DECISION_NOT_CHOSEN", "请先确认本轮推荐，再完成记录", HttpStatus.CONFLICT);
        }
        this.actualRecordId = recordId;
        this.status = DecisionStatus.RECORDED;
    }

    public Long getId() { return id; }
    public long getUserId() { return userId; }
    public Dimension getDimension() { return dimension; }
    public DecisionMode getMode() { return mode; }
    public TimeSlot getTimeSlot() { return timeSlot; }
    public Integer getBudgetMaxFen() { return budgetMaxFen; }
    public String getContextJson() { return contextJson; }
    public void updateContextJson(String contextJson) { this.contextJson = contextJson; }
    public Long getWinnerCandidateId() { return winnerCandidateId; }
    public Long getChosenCandidateId() { return chosenCandidateId; }
    public DecisionStatus getStatus() { return status; }
}
