import React, { useCallback, useEffect, useState } from "react";
import EvidenceEntry from "./EvidenceEntry";
import { fetchEvidenceJournal } from "./evidenceApi";

export default function EvidenceJournal({ sessionId, refreshTrigger = 0 }) {
    const [entries, setEntries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const loadJournal = useCallback(async () => {
        if (!sessionId) return;
        setLoading(true);
        setError(null);
        try {
            const data = await fetchEvidenceJournal(sessionId);
            setEntries(Array.isArray(data) ? data : []);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [sessionId]);

    useEffect(() => {
        loadJournal();
    }, [loadJournal, refreshTrigger]);

    return (
        <section className="evidence-journal">
            <div className="evidence-journal__header">
                <h3>Evidence Journal</h3>
                <button type="button" onClick={loadJournal}>
                    Refresh
                </button>
            </div>

            {loading && <p>Loading discovered evidence...</p>}
            {error && <p role="alert">{error}</p>}

            {!loading && !error && entries.length === 0 && (
                <p>No evidence discovered yet in this session.</p>
            )}

            {!loading && !error && entries.length > 0 && (
                <div className="evidence-journal__list">
                    {entries.map((entry) => (
                        <EvidenceEntry
                            key={entry.discoveryId || `${entry.evidenceId}-${entry.discoveredAt}`}
                            entry={entry}
                        />
                    ))}
                </div>
            )}
        </section>
    );
}