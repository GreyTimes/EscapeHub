import { useState } from 'react';

import { PLAY_MODES, startSession } from '../../services/sessionApi';

/**
 * Lets the player choose how to play a mission: Solo or Team.
 *
 * Solo: starts a session right away through the backend.
 * Team: team missions start from the team lobby, so this component
 *       only reports the choice through onTeamSelected.
 *
 * Props:
 *   missionId, missionVersionId  - the mission to play (from the catalogue)
 *   missionTitle                 - optional, shown in the heading
 *   onSoloStarted(session)       - called with the created session
 *   onTeamSelected()             - called when the player picks Team
 */
function PlayModeSelector({
    missionId,
    missionVersionId,
    missionTitle,
    onSoloStarted,
    onTeamSelected
}) {

    const [selectedMode, setSelectedMode] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    async function handleSolo() {

        setSelectedMode(PLAY_MODES.SOLO);
        setError(null);
        setLoading(true);

        try {
            const session = await startSession(
                missionId,
                missionVersionId,
                PLAY_MODES.SOLO
            );

            onSoloStarted?.(session);

        } catch (requestError) {
            setError(requestError.message);

        } finally {
            setLoading(false);
        }
    }

    function handleTeam() {
        setSelectedMode(PLAY_MODES.TEAM);
        setError(null);

        onTeamSelected?.();
    }

    if (!missionId || !missionVersionId) {
        return <p>Select a mission first.</p>;
    }

    return (
        <section>
            <h2>
                How do you want to play{missionTitle ? ` "${missionTitle}"` : ''}?
            </h2>

            <button
                type="button"
                onClick={handleSolo}
                disabled={loading}
            >
                {loading ? 'Starting...' : 'Solo'}
            </button>

            {' '}

            <button
                type="button"
                onClick={handleTeam}
                disabled={loading}
            >
                Team
            </button>

            {selectedMode === PLAY_MODES.TEAM && !onTeamSelected && (
                <p>
                    Team missions are started by your team leader from the team lobby.
                </p>
            )}

            {error && (
                <p role="alert">
                    {error}
                </p>
            )}
        </section>
    );
}

export default PlayModeSelector;
