import { apiRequest } from '../../services/api';

export function fetchEvidenceJournal(sessionId) {
    return apiRequest(`/sessions/${sessionId}/evidence`);
}

export function discoverEvidence(sessionId, evidenceId) {
    return apiRequest(
        `/sessions/${sessionId}/evidence/${evidenceId}/discover`,
        {
            method: 'POST'
        }
    );
}