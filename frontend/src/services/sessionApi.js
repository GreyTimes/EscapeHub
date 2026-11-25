
import { apiRequest } from './api';

export const PLAY_MODES = Object.freeze({
    SOLO: 'SOLO',
    TEAM: 'TEAM'
});

// POST /api/v1/missions/{missionId}/sessions
export function startSession(missionId, missionVersionId, mode) {
    return apiRequest(`/missions/${missionId}/sessions`, {
        method: 'POST',
        body: { mode, missionVersionId }
    });
}

// GET /api/v1/sessions/{sessionId}
export function getSession(sessionId) {
    return apiRequest(`/sessions/${sessionId}`);
}

// GET /api/v1/sessions/active
export function getActiveSession() {
    return apiRequest('/sessions/active');
}

// POST /api/v1/sessions/{sessionId}/abandon
export function abandonSession(sessionId) {
    return apiRequest(`/sessions/${sessionId}/abandon`, {
        method: 'POST'
    });
}
