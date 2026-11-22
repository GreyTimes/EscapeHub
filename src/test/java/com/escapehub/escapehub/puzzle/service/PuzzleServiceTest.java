package com.escapehub.escapehub.puzzle.service;

import com.escapehub.escapehub.mission.map.entity.MissionNode;
import com.escapehub.escapehub.mission.map.repository.MissionNodeRepository;
import com.escapehub.escapehub.mission.session.entity.MissionSession;
import com.escapehub.escapehub.mission.session.entity.MissionSessionStatus;
import com.escapehub.escapehub.mission.session.entity.SessionMember;
import com.escapehub.escapehub.mission.session.entity.SessionMode;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PuzzleServiceTest {
    @Mock
    private PuzzleRepository puzzleRepository;

    @Mock
    private SessionPuzzleProgressRepository progressRepository;

    @Mock
    private MissionSessionRepository sessionRepository;

    @Mock
    private SessionMemberRepository memberRepository;

    @Mock
    private MissionNodeRepository missionNodeRepository;

    @Mock
    private MissionSessionService missionSessionService;

    private PuzzleService service;

    private final UUID userId = UUID.randomUUID();
    private final UUID sessionId = UUID.randomUUID();
    private final UUID missionVersionId = UUID.randomUUID();
    private final UUID nodeId = UUID.randomUUID();
    private final UUID puzzleId = UUID.randomUUID();

    private MissionSession session;
    private SessionMember member;
    private MissionNode node;

    @BeforeEach
    void setUp() {
        service = new PuzzleService(
                puzzleRepository,
                progressRepository,
                sessionRepository,
                memberRepository,
                missionNodeRepository,
                missionSessionService
        );

        session = new MissionSession(missionVersionId, SessionMode.SOLO, null, userId);
        session.setId(sessionId);

        member = new SessionMember(sessionId, userId);
        member.setCurrentNodeId(nodeId);

        node = new MissionNode();
        node.setId(nodeId);
        node.setMissionVersionId(missionVersionId);
    }

    private Puzzle puzzle(boolean finalPuzzle) {
        Puzzle puzzle = new Puzzle(nodeId, "capital", "What is the capital of Greece?", "Athens", finalPuzzle);
        puzzle.setId(puzzleId);
        return puzzle;
    }

    private void givenPlayerInPuzzleRoom(Puzzle puzzle) {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, userId)).thenReturn(Optional.of(member));
        when(puzzleRepository.findById(puzzleId)).thenReturn(Optional.of(puzzle));
        when(missionNodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
    }

    @Test
    void correctAnswerSolvesPuzzleAndSavesProgressForSession() {
        givenPlayerInPuzzleRoom(puzzle(false));
        when(progressRepository.findBySessionIdAndPuzzleId(sessionId, puzzleId)).thenReturn(Optional.empty());

        PuzzleAnswerResponse response = service.submitAnswer(sessionId, puzzleId, "Athens", userId);

        assertTrue(response.correct());
        assertTrue(response.solved());
        assertFalse(response.missionCompleted());
        assertEquals(1, response.attemptsUsed());

        ArgumentCaptor<SessionPuzzleProgress> captor = ArgumentCaptor.forClass(SessionPuzzleProgress.class);
        verify(progressRepository).save(captor.capture());
        SessionPuzzleProgress saved = captor.getValue();
        assertEquals(sessionId, saved.getSessionId());
        assertEquals(puzzleId, saved.getPuzzleId());
        assertTrue(saved.isSolved());
        assertEquals(userId, saved.getSolvedByUserId());
        assertNotNull(saved.getSolvedAt());

        verify(missionSessionService, never()).completeSession(any());
    }

    @Test
    void wrongAnswerIsNotSolvedButCountsAsAttempt() {
        givenPlayerInPuzzleRoom(puzzle(false));
        when(progressRepository.findBySessionIdAndPuzzleId(sessionId, puzzleId)).thenReturn(Optional.empty());

        PuzzleAnswerResponse response = service.submitAnswer(sessionId, puzzleId, "Thessaloniki", userId);

        assertFalse(response.correct());
        assertFalse(response.solved());
        assertEquals(1, response.attemptsUsed());

        ArgumentCaptor<SessionPuzzleProgress> captor = ArgumentCaptor.forClass(SessionPuzzleProgress.class);
        verify(progressRepository).save(captor.capture());
        assertFalse(captor.getValue().isSolved());
        assertNull(captor.getValue().getSolvedByUserId());

        verify(missionSessionService, never()).completeSession(any());
    }

    @Test
    void answerIgnoresCaseAndExtraSpaces() {
        assertTrue(PuzzleService.isCorrect("  athens ", "Athens"));
        assertTrue(PuzzleService.isCorrect("New   York", "new york"));
        assertFalse(PuzzleService.isCorrect("Athen", "Athens"));
        assertFalse(PuzzleService.isCorrect(null, "Athens"));
    }

    @Test
    void solvingFinalPuzzleCompletesMission() {
        givenPlayerInPuzzleRoom(puzzle(true));
        when(progressRepository.findBySessionIdAndPuzzleId(sessionId, puzzleId)).thenReturn(Optional.empty());

        PuzzleAnswerResponse response = service.submitAnswer(sessionId, puzzleId, "athens", userId);

        assertTrue(response.correct());
        assertTrue(response.missionCompleted());
        verify(missionSessionService).completeSession(session);
    }

    @Test
    void wrongAnswerOnFinalPuzzleDoesNotCompleteMission() {
        givenPlayerInPuzzleRoom(puzzle(true));
        when(progressRepository.findBySessionIdAndPuzzleId(sessionId, puzzleId)).thenReturn(Optional.empty());

        PuzzleAnswerResponse response = service.submitAnswer(sessionId, puzzleId, "Sparta", userId);

        assertFalse(response.missionCompleted());
        verify(missionSessionService, never()).completeSession(any());
    }

    @Test
    void alreadySolvedPuzzleIsNotCountedAgain() {
        givenPlayerInPuzzleRoom(puzzle(true));
        SessionPuzzleProgress solved = new SessionPuzzleProgress(sessionId, puzzleId);
        solved.recordAttempt();
        solved.markSolved(userId);
        when(progressRepository.findBySessionIdAndPuzzleId(sessionId, puzzleId)).thenReturn(Optional.of(solved));

        PuzzleAnswerResponse response = service.submitAnswer(sessionId, puzzleId, "anything", userId);

        assertTrue(response.solved());
        assertEquals(1, response.attemptsUsed());
        verify(progressRepository, never()).save(any());
        verify(missionSessionService, never()).completeSession(any());
    }

    @Test
    void playerInAnotherRoomCannotAnswer() {
        member.setCurrentNodeId(UUID.randomUUID());
        givenPlayerInPuzzleRoom(puzzle(false));

        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.submitAnswer(sessionId, puzzleId, "Athens", userId)
        );

        assertEquals(SessionException.Reason.NOT_IN_PUZZLE_ROOM, exception.getReason());
        verify(progressRepository, never()).save(any());
    }

    @Test
    void cannotAnswerInInactiveSession() {
        session.setStatus(MissionSessionStatus.COMPLETED);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.submitAnswer(sessionId, puzzleId, "Athens", userId)
        );

        assertEquals(SessionException.Reason.SESSION_NOT_ACTIVE, exception.getReason());
        verifyNoInteractions(progressRepository, missionSessionService);
    }

    @Test
    void puzzleOfAnotherMissionIsNotFound() {
        MissionNode otherMissionNode = new MissionNode();
        otherMissionNode.setId(nodeId);
        otherMissionNode.setMissionVersionId(UUID.randomUUID());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, userId)).thenReturn(Optional.of(member));
        when(puzzleRepository.findById(puzzleId)).thenReturn(Optional.of(puzzle(false)));
        when(missionNodeRepository.findById(nodeId)).thenReturn(Optional.of(otherMissionNode));

        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.submitAnswer(sessionId, puzzleId, "Athens", userId)
        );

        assertEquals(SessionException.Reason.PUZZLE_NOT_FOUND, exception.getReason());
    }

    @Test
    void nonMemberCannotAnswer() {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, userId)).thenReturn(Optional.empty());

        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.submitAnswer(sessionId, puzzleId, "Athens", userId)
        );

        assertEquals(SessionException.Reason.NOT_A_SESSION_MEMBER, exception.getReason());
    }

    @Test
    void playerSeesPuzzlesOfCurrentRoomWithSessionProgress() {
        Puzzle puzzle = puzzle(false);
        SessionPuzzleProgress progress = new SessionPuzzleProgress(sessionId, puzzleId);
        progress.recordAttempt();
        progress.recordAttempt();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, userId)).thenReturn(Optional.of(member));
        when(missionNodeRepository.findById(nodeId)).thenReturn(Optional.of(node));
        when(puzzleRepository.findByNodeId(nodeId)).thenReturn(List.of(puzzle));
        when(progressRepository.findBySessionIdAndPuzzleIdIn(sessionId, List.of(puzzleId)))
                .thenReturn(List.of(progress));

        List<PuzzleResponse> puzzles = service.getPuzzlesForNode(sessionId, nodeId, userId);

        assertEquals(1, puzzles.size());
        PuzzleResponse response = puzzles.get(0);
        assertEquals(puzzleId, response.id());
        assertEquals("What is the capital of Greece?", response.question());
        assertFalse(response.solved());
        assertEquals(2, response.attemptsUsed());
    }

    @Test
    void playerCannotSeePuzzlesOfAnotherRoom() {
        member.setCurrentNodeId(UUID.randomUUID());

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(memberRepository.findBySessionIdAndUserId(sessionId, userId)).thenReturn(Optional.of(member));
        when(missionNodeRepository.findById(nodeId)).thenReturn(Optional.of(node));

        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.getPuzzlesForNode(sessionId, nodeId, userId)
        );

        assertEquals(SessionException.Reason.NOT_IN_PUZZLE_ROOM, exception.getReason());
        verifyNoInteractions(puzzleRepository);
    }

    @Test
    void missingUserIsRejected() {
        SessionException exception = assertThrows(
                SessionException.class,
                () -> service.submitAnswer(sessionId, puzzleId, "Athens", null)
        );

        assertEquals(SessionException.Reason.MISSING_CURRENT_USER, exception.getReason());
    }
}
