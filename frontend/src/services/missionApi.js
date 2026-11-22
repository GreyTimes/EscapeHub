import { apiRequest } from './api';

// GET /api/v1/missions
export function getAvailableMissions() {
    return apiRequest('/missions');
}