package com.escapehub.escapehub.puzzle.service;

import com.escapehub.escapehub.mission.map.entity.MissionNode;
import com.escapehub.escapehub.mission.map.repository.MissionNodeRepository;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import com.escapehub.escapehub.mission.session.repository.MissionSessionRepository;
import com.escapehub.escapehub.mission.session.repository.SessionMemberRepository;
import com.escapehub.escapehub.mission.session.service.MissionSessionService;
import com.escapehub.escapehub.mission.session.service.SessionException;
import com.escapehub.escapehub.puzzle.dto.PuzzleAnswerResponse;
import com.escapehub.escapehub.puzzle.dto.PuzzleResponse;
import com.escapehub.escapehub.puzzle.entity.Puzzle;
import com.escapehub.escapehub.puzzle.entity.SessionPuzzleProgress;
import com.escapehub.escapehub.puzzle.repository.PuzzleRepository;
import com.escapehub.escapehub.puzzle.repository.SessionPuzzleProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PuzzleService {
    private final PuzzleRepository puzzleRepository;
    private final SessionPuzzleProgressRepository progressRepository;
    private final MissionSessionRepository sessionRepository;
    private final SessionMemberRepository memberRepository;
    private final MissionNodeRepository missionNodeRepository;
    private final MissionSessionService missionSessionService;

    public PuzzleService(
            PuzzleRepository puzzleRepository,
            SessionPuzzleProgressRepository progressRepository,
            MissionSessionRepository sessionRepository,
            SessionMemberRepository memberRepository,
            MissionNodeRepository missionNodeRepository,
            MissionSessionService missionSessionService
    ) {
        this.puzzleRepository = puzzleRepository;
        this.progressRepository = progressRepository;
        this.sessionRepository = sessionRepository;
        this.memberRepository = memberRepository;
        this.missionNodeRepository = missionNodeRepository;
        this.missionSessionService = missionSessionService;
    }

    @Transactional(readOnly = true)
    public List<PuzzleResponse> getPuzzlesForNode(UUID sessionId, UUID nodeId, UUID userId) {
        requireUser(userId);

        MissionSession session = findSessionOrThrow(sessionId);
        SessionMember member = findMemberOrThrow(sessionId, userId);
        findNodeOfSessionOrThrow(session, nodeId);
        requirePlayerInRoom(member, nodeId);

        List<Puzzle> puzzles = puzzleRepository.findByNodeId(nodeId);
        if (puzzles.isEmpty()) {
            return List.of();
        }

        List<UUID> puzzleIds = puzzles.stream().map(Puzzle::getId).toList();
        Map<UUID, SessionPuzzleProgress> progressByPuzzle = progressRepository
                .findBySessionIdAndPuzzleIdIn(sessionId, puzzleIds).stream()
                .collect(Collectors.toMap(SessionPuzzleProgress::getPuzzleId, Function.identity()));

        return puzzles.stream()
                .map(puzzle -> PuzzleResponse.from(puzzle, progressByPuzzle.get(puzzle.getId())))
                .toList();
    }

    @Transactional
    public PuzzleAnswerResponse submitAnswer(UUID sessionId, UUID puzzleId, String answer, UUID userId) {
        requireUser(userId);

        MissionSession session = findSessionOrThrow(sessionId);
        if (!session.isActive()) {
            throw new SessionException(
                    SessionException.Reason.SESSION_NOT_ACTIVE,
                    "This mission is no longer active."
            );
        }

        SessionMember member = findMemberOrThrow(sessionId, userId);

        Puzzle puzzle = puzzleRepository.findById(puzzleId)
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.PUZZLE_NOT_FOUND,
                        "Puzzle not found."
                ));

        MissionNode node = missionNodeRepository.findById(puzzle.getNodeId())
                .filter(found -> found.getMissionVersionId().equals(session.getMissionVersionId()))
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.PUZZLE_NOT_FOUND,
                        "Puzzle not found in this mission."
                ));
        requirePlayerInRoom(member, node.getId());

        SessionPuzzleProgress progress = progressRepository
                .findBySessionIdAndPuzzleId(sessionId, puzzleId)
                .orElseGet(() -> new SessionPuzzleProgress(sessionId, puzzleId));

        if (progress.isSolved()) {
            return new PuzzleAnswerResponse(
                    puzzleId, true, true, progress.getAttemptsUsed(), false,
                    "This puzzle is already solved."
            );
        }

        progress.recordAttempt();
        boolean correct = isCorrect(answer, puzzle.getCorrectAnswer());

        if (!correct) {
            progressRepository.save(progress);
            return new PuzzleAnswerResponse(
                    puzzleId, false, false, progress.getAttemptsUsed(), false,
                    "Wrong answer. Try again."
            );
        }

        progress.markSolved(userId);
        progressRepository.save(progress);

        boolean missionCompleted = false;
        if (puzzle.isFinalPuzzle()) {
            missionSessionService.completeSession(session);
            missionCompleted = true;
        }

        return new PuzzleAnswerResponse(
                puzzleId, true, true, progress.getAttemptsUsed(), missionCompleted,
                missionCompleted ? "Correct! You completed the mission." : "Correct!"
        );
    }

    static boolean isCorrect(String given, String expected) {
        if (given == null || expected == null) {
            return false;
        }
        return normalize(given).equals(normalize(expected));
    }

    private static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private MissionSession findSessionOrThrow(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.SESSION_NOT_FOUND,
                        "Session not found."
                ));
    }

    private SessionMember findMemberOrThrow(UUID sessionId, UUID userId) {
        return memberRepository.findBySessionIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.NOT_A_SESSION_MEMBER,
                        "You are not a member of this session."
                ));
    }

    private MissionNode findNodeOfSessionOrThrow(MissionSession session, UUID nodeId) {
        return missionNodeRepository.findById(nodeId)
                .filter(node -> node.getMissionVersionId().equals(session.getMissionVersionId()))
                .orElseThrow(() -> new SessionException(
                        SessionException.Reason.NODE_NOT_FOUND,
                        "Room not found in this mission."
                ));
    }

    private void requirePlayerInRoom(SessionMember member, UUID nodeId) {
        if (!nodeId.equals(member.getCurrentNodeId())) {
            throw new SessionException(
                    SessionException.Reason.NOT_IN_PUZZLE_ROOM,
                    "You must be in this room to see or answer its puzzles."
            );
        }
    }

    private void requireUser(UUID userId) {
        if (userId == null) {
            throw new SessionException(
                    SessionException.Reason.MISSING_CURRENT_USER,
                    "A current user is required."
            );
        }
    }
}
