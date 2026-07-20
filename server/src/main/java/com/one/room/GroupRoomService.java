package com.one.room;

import com.one.catalog.CatalogBrand;
import com.one.catalog.CatalogItem;
import com.one.catalog.CatalogItemRepository;
import com.one.common.BusinessException;
import com.one.identity.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroupRoomService {
    private static final char[] CODE_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private final SecureRandom random = new SecureRandom();
    private final GroupDecisionRoomRepository rooms;
    private final GroupRoomCandidateRepository candidates;
    private final GroupRoomVoteRepository votes;
    private final CatalogItemRepository items;
    private final UserAccountRepository users;

    public GroupRoomService(GroupDecisionRoomRepository rooms, GroupRoomCandidateRepository candidates,
                            GroupRoomVoteRepository votes, CatalogItemRepository items,
                            UserAccountRepository users) {
        this.rooms = rooms; this.candidates = candidates; this.votes = votes; this.items = items; this.users = users;
    }

    @Transactional
    public GroupRoomDtos.View create(long userId, GroupRoomDtos.CreateRequest request) {
        if (!request.dimension().isRecommendable()) {
            throw new BusinessException("INVALID_ROOM_DIMENSION", "这个分类不支持群投票", HttpStatus.BAD_REQUEST);
        }
        List<Long> distinctIds = request.itemIds().stream().distinct().toList();
        if (distinctIds.size() < 2 || distinctIds.size() > 8) {
            throw new BusinessException("INVALID_ROOM_CANDIDATES", "请选择 2 到 8 个不重复候选", HttpStatus.BAD_REQUEST);
        }
        List<CatalogItem> selected = distinctIds.stream().map(id -> items.findById(id)
                .filter(item -> item.isActive() && item.getDimension() == request.dimension())
                .orElseThrow(() -> new BusinessException("CATALOG_ITEM_NOT_FOUND", "候选内容不存在", HttpStatus.NOT_FOUND))).toList();
        String title = request.title() == null || request.title().isBlank() ? "今晚吃什么？" : request.title().strip();
        GroupDecisionRoom room = rooms.save(GroupDecisionRoom.open(nextCode(), userId, title, request.dimension()));
        for (int index = 0; index < selected.size(); index++) {
            candidates.save(GroupRoomCandidate.of(room, selected.get(index), index + 1));
        }
        return view(userId, room);
    }

    @Transactional
    public GroupRoomDtos.View get(long userId, String code) { return view(userId, room(code)); }

    @Transactional
    public GroupRoomDtos.View vote(long userId, String code, long candidateId) {
        GroupDecisionRoom room = lockedRoom(code); room.ensureOpen();
        GroupRoomCandidate candidate = candidates.findByIdAndRoomId(candidateId, room.getId())
                .orElseThrow(() -> new BusinessException("ROOM_CANDIDATE_NOT_FOUND", "这个候选不在房间里", HttpStatus.NOT_FOUND));
        GroupRoomVote vote = votes.findByRoomIdAndUserId(room.getId(), userId).orElse(null);
        if (vote == null) votes.save(GroupRoomVote.cast(room, candidate, userId)); else vote.changeTo(candidate);
        return view(userId, room);
    }

    @Transactional
    public GroupRoomDtos.View close(long userId, String code) {
        GroupDecisionRoom room = lockedRoom(code);
        if (room.getOwnerUserId() != userId) {
            throw new BusinessException("ROOM_OWNER_REQUIRED", "只有发起人可以结束选择", HttpStatus.FORBIDDEN);
        }
        List<GroupRoomCandidate> roomCandidates = candidates.findByRoomIdOrderByPositionNoAsc(room.getId());
        Map<Long, Integer> counts = voteCounts(room.getId());
        Long winner = roomCandidates.stream().max((left, right) -> {
            int compared = Integer.compare(counts.getOrDefault(left.getId(), 0), counts.getOrDefault(right.getId(), 0));
            return compared != 0 ? compared : Integer.compare(right.getPositionNo(), left.getPositionNo());
        }).map(GroupRoomCandidate::getId).orElse(null);
        room.close(winner); return view(userId, room);
    }

    private GroupRoomDtos.View view(long userId, GroupDecisionRoom room) {
        if (room.getStatus() == GroupRoomStatus.OPEN && room.getExpiresAt().isBefore(java.time.Instant.now())) {
            try { room.ensureOpen(); } catch (BusinessException ignored) { /* 状态已转为过期 */ }
        }
        List<GroupRoomCandidate> roomCandidates = candidates.findByRoomIdOrderByPositionNoAsc(room.getId());
        List<GroupRoomVote> roomVotes = votes.findByRoomId(room.getId());
        Map<Long, Integer> counts = new LinkedHashMap<>();
        roomVotes.forEach(vote -> counts.merge(vote.getCandidate().getId(), 1, Integer::sum));
        // 只返回当前用户的选择和聚合票数，绝不向客户端暴露投票者身份。
        Long myVote = votes.findByRoomIdAndUserId(room.getId(), userId)
                .map(value -> value.getCandidate().getId()).orElse(null);
        Long finalMyVote = myVote;
        List<GroupRoomDtos.Candidate> candidateViews = roomCandidates.stream().map(value -> {
            CatalogItem item = value.getItem(); CatalogBrand brand = item.getBrand();
            return new GroupRoomDtos.Candidate(value.getId(), item.getId(), item.getName(),
                    brand == null ? null : brand.getName(), brand == null ? null : brand.getShortName(),
                    brand == null ? null : brand.getBrandColor(), item.getDefaultPriceFen(),
                    counts.getOrDefault(value.getId(), 0), value.getId().equals(finalMyVote),
                    value.getId().equals(room.getWinnerCandidateId()));
        }).toList();
        String owner = users.findById(room.getOwnerUserId()).map(value -> value.getNickname()).orElse("群友");
        return new GroupRoomDtos.View(room.getShareCode(), room.getTitle(), room.getDimension(), room.getStatus(),
                owner, room.getOwnerUserId() == userId, roomVotes.size(), room.getExpiresAt(),
                room.getWinnerCandidateId(), candidateViews);
    }

    private Map<Long, Integer> voteCounts(long roomId) {
        Map<Long, Integer> counts = new LinkedHashMap<>();
        votes.findByRoomId(roomId).forEach(vote -> counts.merge(vote.getCandidate().getId(), 1, Integer::sum));
        return counts;
    }

    private GroupDecisionRoom room(String code) {
        return rooms.findByShareCode(code.toUpperCase())
                .orElseThrow(() -> new BusinessException("ROOM_NOT_FOUND", "没有找到这次群选择", HttpStatus.NOT_FOUND));
    }

    private GroupDecisionRoom lockedRoom(String code) {
        return rooms.findByShareCodeForUpdate(code.toUpperCase())
                .orElseThrow(() -> new BusinessException("ROOM_NOT_FOUND", "没有找到这次群选择", HttpStatus.NOT_FOUND));
    }

    private String nextCode() {
        for (int attempt = 0; attempt < 8; attempt++) {
            StringBuilder code = new StringBuilder(8);
            for (int index = 0; index < 8; index++) code.append(CODE_ALPHABET[random.nextInt(CODE_ALPHABET.length)]);
            if (!rooms.existsByShareCode(code.toString())) return code.toString();
        }
        throw new BusinessException("ROOM_CODE_EXHAUSTED", "暂时无法创建房间，请稍后再试", HttpStatus.SERVICE_UNAVAILABLE);
    }
}
