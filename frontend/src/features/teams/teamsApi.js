
import { apiRequest } from '../../services/api';

// POST /api/v1/teams
export function createTeam(name) {
    return apiRequest('/teams', {
        method: 'POST',
        body: { name }
    });
}

// POST /api/v1/teams/join
export function joinTeam(joinCode) {
    return apiRequest('/teams/join', {
        method: 'POST',
        body: { joinCode }
    });
}

// GET /api/v1/teams/{teamId}
export function getTeam(teamId) {
    return apiRequest(`/teams/${teamId}`);
}
