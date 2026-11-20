import { useState } from 'react';

import {
    createLobby,
    getLobby,
    joinLobby,
    startLobbyMission
} from '../../services/lobbyApi';
import LobbyMembers from './LobbyMembers';

function LobbyPage({
    missionVersionId,
    currentUserId,
    onSessionStarted,
    onBack
}) {
    const [teamIdInput, setTeamIdInput] = useState('');
    const [lobbyIdInput, setLobbyIdInput] = useState('');
    const [lobby, setLobby] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    async function handleCreateLobby(event) {
        event.preventDefault();

        const teamId = teamIdInput.trim();
        if (!teamId) {
            setError('Team ID is required.');
            return;
        }

        setError(null);
        setLoading(true);

        try {
            const created = await createLobby(teamId, missionVersionId);
            setLobby(created);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    async function handleOpenLobby(event) {
        event.preventDefault();

        const lobbyId = lobbyIdInput.trim();
        if (!lobbyId) {
            setError('Lobby ID is required.');
            return;
        }

        setError(null);
        setLoading(true);

        try {
            const loaded = await getLobby(lobbyId);
            setLobby(loaded);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    async function handleRefresh() {
        if (!lobby) {
            return;
        }

        setError(null);
        setLoading(true);

        try {
            const updated = await getLobby(lobby.id);
            setLobby(updated);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    async function handleJoinLobby() {
        if (!lobby) {
            return;
        }

        setError(null);
        setLoading(true);

        try {
            const updated = await joinLobby(lobby.id);
            setLobby(updated);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    async function handleStartMission() {
        if (!lobby) {
            return;
        }

        setError(null);
        setLoading(true);

        try {
            const session = await startLobbyMission(lobby.id);
            onSessionStarted?.(session);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    const isLeader = lobby && currentUserId && lobby.createdByUserId === currentUserId;
    const isMember = lobby && currentUserId && lobby.members.some((m) => m.userId === currentUserId);
    const hasEnoughParticipants = lobby && lobby.members.length >= 2;

    return (
        <section>
            <h2>Team Lobby</h2>

            {!lobby ? (
                <div>
                    <form onSubmit={handleCreateLobby}>
                        <h3>Create a new lobby (Team Leader)</h3>
                        <label>
                            Team ID:{' '}
                            <input
                                type="text"
                                value={teamIdInput}
                                onChange={(event) => setTeamIdInput(event.target.value)}
                                placeholder="Team UUID"
                                disabled={loading}
                            />
                        </label>{' '}
                        <button type="submit" disabled={loading}>
                            {loading ? 'Creating...' : 'Create Lobby'}
                        </button>
                    </form>

                    <form onSubmit={handleOpenLobby}>
                        <h3>Open an existing lobby</h3>
                        <label>
                            Lobby ID:{' '}
                            <input
                                type="text"
                                value={lobbyIdInput}
                                onChange={(event) => setLobbyIdInput(event.target.value)}
                                placeholder="Lobby UUID"
                                disabled={loading}
                            />
                        </label>{' '}
                        <button type="submit" disabled={loading}>
                            {loading ? 'Loading...' : 'Open Lobby'}
                        </button>
                    </form>
                </div>
            ) : (
                <div>
                    <p><strong>Lobby ID:</strong> {lobby.id}</p>
                    <p><strong>Team ID:</strong> {lobby.teamId}</p>
                    <p><strong>Status:</strong> {lobby.status}</p>

                    <LobbyMembers
                        members={lobby.members}
                        createdByUserId={lobby.createdByUserId}
                    />

                    <div>
                        <button
                            type="button"
                            onClick={handleRefresh}
                            disabled={loading}
                        >
                            Refresh
                        </button>

                        {lobby.status === 'WAITING' && !isMember && (
                            <>
                                {' '}
                                <button
                                    type="button"
                                    onClick={handleJoinLobby}
                                    disabled={loading}
                                >
                                    Join Lobby
                                </button>
                            </>
                        )}

                        {lobby.status === 'WAITING' && isLeader && (
                            <>
                                {' '}
                                <button
                                    type="button"
                                    onClick={handleStartMission}
                                    disabled={loading || !hasEnoughParticipants}
                                >
                                    {loading ? 'Starting...' : 'Start Mission'}
                                </button>
                            </>
                        )}
                    </div>

                    {lobby.status === 'WAITING' && isLeader && !hasEnoughParticipants && (
                        <p>At least 2 participants are required to start the mission.</p>
                    )}
                </div>
            )}

            {error && (
                <p role="alert">
                    {error}
                </p>
            )}

            <p>
                <button type="button" onClick={onBack} disabled={loading}>
                    Back to Play Mode
                </button>
            </p>
        </section>
    );
}

export default LobbyPage;