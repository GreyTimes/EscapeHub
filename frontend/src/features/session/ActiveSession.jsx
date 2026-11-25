import { useState } from 'react';

import { abandonSession } from '../../services/sessionApi';
import InvestigationMap from '../exploration/InvestigationMap';
import EvidenceJournal from '../evidence/EvidenceJournal';

function ActiveSession({ session, onSessionAbandoned }) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    async function handleAbandon() {
        setError(null);
        setLoading(true);

        try {
            const abandoned = await abandonSession(session.id);
            onSessionAbandoned?.(abandoned);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <section>
            <h2>Active Mission Session</h2>

            <p><strong>Session ID:</strong> {session.id}</p>
            <p><strong>Mode:</strong> {session.mode}</p>
            <p><strong>Status:</strong> {session.status}</p>
            {session.teamId && (
                <p><strong>Team ID:</strong> {session.teamId}</p>
            )}

            {session.status === 'ACTIVE' && (
                <>
                    <button
                        type="button"
                        onClick={handleAbandon}
                        disabled={loading}
                    >
                        {loading ? 'Abandoning...' : 'Abandon Session'}
                    </button>

                    <InvestigationMap sessionId={session.id} />
                </>
            )}

            {error && (
                <p role="alert">
                    {error}
                </p>
            )}

            <EvidenceJournal sessionId={session.id} />
        </section>
    );
}

export default ActiveSession;