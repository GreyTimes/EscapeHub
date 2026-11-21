import { apiRequest } from '../../services/api';

// GET /api/v1/sessions/{sessionId}/map
export function getSessionMap(sessionId) {
    return apiRequest(`/sessions/${sessionId}/map`);
}

// PATCH /api/v1/sessions/{sessionId}/location
export function movePlayerLocation(sessionId, targetNodeId) {
    return apiRequest(`/sessions/${sessionId}/location`, {
        method: 'PATCH',
        body: { targetNodeId }
    });
}