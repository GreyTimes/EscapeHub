import { useState } from 'react';

import {
    getLobby,
    startLobbyMission
} from '../../services/lobbyApi';

import LobbyMembers from './LobbyMembers';

function LobbyPage({
                       initialLobby,
                       currentUserId,
                       onSessionStarted
                   }) {
    const [lobby, setLobby] = useState(initialLobby);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    async function handleRefresh() {
        setError('');
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

    async function handleStartMission() {
        setError('');
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

    const members = lobby.members ?? [];
    const isLeader =
        lobby.createdByUserId === currentUserId;

    const hasEnoughParticipants = members.length >= 2;

    return (
        <section style={{ marginTop: '20px' }}>
            <h3>Team Lobby</h3>

            <p>
                Mission: The selected mission will begin when
                the team leader starts it.
            </p>

            <p>
                <strong>Status:</strong> {lobby.status}
            </p>

            <p>
                <strong>Players:</strong> {members.length} / 6
            </p>

            <LobbyMembers
                members={members}
                createdByUserId={lobby.createdByUserId}
            />

            <button
                type="button"
                onClick={handleRefresh}
                disabled={loading}
            >
                {loading ? 'Refreshing...' : 'Refresh Members'}
            </button>

            {lobby.status === 'WAITING' && isLeader && (
                <div style={{ marginTop: '16px' }}>
                    <button
                        type="button"
                        onClick={handleStartMission}
                        disabled={loading || !hasEnoughParticipants}
                    >
                        {loading ? 'Starting...' : 'Start Mission'}
                    </button>

                    {!hasEnoughParticipants && (
                        <p>
                            At least two players are required
                            before the mission can start.
                        </p>
                    )}
                </div>
            )}

            {lobby.status === 'WAITING' && !isLeader && (
                <p>
                    Waiting for the team leader to start the mission...
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

export default LobbyPage;