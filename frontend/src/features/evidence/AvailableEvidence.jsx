import { useEffect, useState } from 'react';
import {
    getAvailableRoomEvidence,
    discoverEvidence
} from './evidenceApi';

function AvailableEvidence({ sessionId, roomId }) {
    const [evidence, setEvidence] = useState([]);
    const [loading, setLoading] = useState(true);
    const [discoveringId, setDiscoveringId] = useState(null);
    const [error, setError] = useState(null);
    const [message, setMessage] = useState(null);

    async function loadEvidence() {
        setLoading(true);
        setError(null);

        try {
            const result = await getAvailableRoomEvidence(
                sessionId,
                roomId
            );

            setEvidence(result ?? []);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (sessionId && roomId) {
            loadEvidence();
        }
    }, [sessionId, roomId]);

    async function handleExamine(evidenceId) {
        setDiscoveringId(evidenceId);
        setError(null);
        setMessage(null);

        try {
            const discovered = await discoverEvidence(
                sessionId,
                evidenceId
            );

            setEvidence((previous) =>
                previous.filter((item) => item.id !== evidenceId)
            );

            setMessage(`Discovered: ${discovered.title}`);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setDiscoveringId(null);
        }
    }

    return (
        <section style={{ marginTop: '16px' }}>
            <h4>Available Evidence</h4>

            <button
                type="button"
                onClick={loadEvidence}
                disabled={loading || discoveringId !== null}
            >
                {loading ? 'Loading...' : 'Refresh Evidence'}
            </button>

            {error && (
                <p role="alert" style={{ color: '#b91c1c' }}>
                    {error}
                </p>
            )}

            {message && (
                <p role="status" style={{ color: '#15803d' }}>
                    {message}
                </p>
            )}

            {loading ? (
                <p>Loading available evidence...</p>
            ) : evidence.length === 0 ? (
                <p>No undiscovered evidence in this room.</p>
            ) : (
                <ul>
                    {evidence.map((item) => (
                        <li
                            key={item.id}
                            style={{ marginBottom: '12px' }}
                        >
                            <strong>{item.title}</strong>

                            <p>{item.description}</p>

                            <button
                                type="button"
                                onClick={() => handleExamine(item.id)}
                                disabled={discoveringId !== null}
                            >
                                {discoveringId === item.id
                                    ? 'Examining...'
                                    : 'Examine'}
                            </button>
                        </li>
                    ))}
                </ul>
            )}
        </section>
    );
}

export default AvailableEvidence;