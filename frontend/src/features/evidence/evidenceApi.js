const API_BASE = "/api/v1";

export async function fetchEvidenceJournal(sessionId) {
    const response = await fetch(`${API_BASE}/sessions/${sessionId}/evidence`);
    if (!response.ok) {
        throw new Error(`Failed to fetch evidence journal (${response.status})`);
    }
    return response.json();
}

export async function discoverEvidence(sessionId, evidenceId, payload) {
    const response = await fetch(
        `${API_BASE}/sessions/${sessionId}/evidence/${evidenceId}/discover`,
        {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(payload),
        }
    );

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `Failed to discover evidence (${response.status})`);
    }

    return response.json();
}