import { apiRequest } from '../../services/api';

export function getRoomPuzzles(sessionId, nodeId) {
    return apiRequest(
        `/sessions/${sessionId}/nodes/${nodeId}/puzzles`
    );
}

export function submitPuzzleAnswer(sessionId, puzzleId, answer) {
    return apiRequest(
        `/sessions/${sessionId}/puzzles/${puzzleId}/answer`,
        {
            method: 'POST',
            body: { answer }
        }
    );
}