import { apiRequest } from './api';

// POST /api/v1/teams/{teamId}/lobbies
export function createLobby(teamId, missionVersionId) {
    return apiRequest(`/teams/${teamId}/lobbies`, {
        method: 'POST',
        body: { missionVersionId }
    });
}

// GET /api/v1/teams/{teamId}/lobbies/waiting
export function getWaitingLobbyForTeam(teamId) {
    return apiRequest(`/teams/${teamId}/lobbies/waiting`);
}

// GET /api/v1/lobbies/{lobbyId}
export function getLobby(lobbyId) {
    return apiRequest(`/lobbies/${lobbyId}`);
}

// POST /api/v1/lobbies/{lobbyId}/join
export function joinLobby(lobbyId) {
    return apiRequest(`/lobbies/${lobbyId}/join`, {
        method: 'POST'
    });
}

// POST /api/v1/lobbies/{lobbyId}/start
export function startLobbyMission(lobbyId) {
    return apiRequest(`/lobbies/${lobbyId}/start`, {
        method: 'POST'
    });
}