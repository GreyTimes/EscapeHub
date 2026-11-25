import { useState } from 'react';

import CreateTeamForm from './CreateTeamForm.jsx';
import JoinTeamForm from './JoinTeamForm.jsx';
import TeamDetails from './TeamDetails.jsx';
import LobbyPage from '../lobby/LobbyPage.jsx';

import {
    createLobby,
    getWaitingLobbyForTeam,
    joinLobby
} from '../../services/lobbyApi';

function TeamsPage({
                       missionVersionId,
                       currentUserId,
                       onSessionStarted,
                       onBack
                   }) {
    const [team, setTeam] = useState(null);
    const [lobby, setLobby] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    async function resolveTeamLobby(selectedTeam) {
        let waitingLobby;

        if (selectedTeam.leaderUserId === currentUserId) {
            // The leader creates the lobby for the selected mission.
            try {
                waitingLobby = await createLobby(
                    selectedTeam.teamId,
                    missionVersionId
                );
            } catch (createError) {
                // A waiting lobby may already exist from a previous attempt.
                try {
                    waitingLobby = await getWaitingLobbyForTeam(
                        selectedTeam.teamId
                    );
                } catch {
                    throw createError;
                }
            }
        } else {
            // Other team members retrieve the leader's waiting lobby.
            waitingLobby = await getWaitingLobbyForTeam(
                selectedTeam.teamId
            );
        }

        const alreadyJoined = waitingLobby.members?.some(
            (member) => member.userId === currentUserId
        );

        if (alreadyJoined) {
            return waitingLobby;
        }

        return joinLobby(waitingLobby.id);
    }

    async function handleTeamCreated(createdTeam) {
        setTeam(createdTeam);
        setError('');
        setLoading(true);

        try {
            const readyLobby = await resolveTeamLobby(createdTeam);
            setLobby(readyLobby);
        } catch (requestError) {
            setError(
                `Your team was created, but its mission lobby could not be opened: ${requestError.message}`
            );
        } finally {
            setLoading(false);
        }
    }

    async function handleTeamJoined(joinedTeam) {
        setTeam(joinedTeam);
        setError('');
        setLoading(true);

        try {
            const readyLobby = await resolveTeamLobby(joinedTeam);
            setLobby(readyLobby);
        } catch (requestError) {
            setError(
                `You joined the team, but could not enter its mission lobby: ${requestError.message}`
            );
        } finally {
            setLoading(false);
        }
    }

    async function handleRetryLobby() {
        if (!team) {
            return;
        }

        setError('');
        setLoading(true);

        try {
            const readyLobby = await resolveTeamLobby(team);
            setLobby(readyLobby);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <section>
            <h2>Team Setup</h2>

            {!team ? (
                <div
                    style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
                        gap: '20px',
                        marginTop: '16px'
                    }}
                >
                    <div
                        style={{
                            padding: '16px',
                            border: '1px solid #d1d5db',
                            borderRadius: '8px'
                        }}
                    >
                        <h3>Create a New Team</h3>

                        <CreateTeamForm
                            onTeamCreated={handleTeamCreated}
                        />
                    </div>

                    <div
                        style={{
                            padding: '16px',
                            border: '1px solid #d1d5db',
                            borderRadius: '8px'
                        }}
                    >
                        <h3>Join an Existing Team</h3>

                        <JoinTeamForm
                            onTeamJoined={handleTeamJoined}
                        />
                    </div>
                </div>
            ) : (
                <>
                    <TeamDetails team={team} />

                    {loading && (
                        <p>
                            Preparing your team lobby...
                        </p>
                    )}

                    {!loading && lobby && (
                        <LobbyPage
                            initialLobby={lobby}
                            currentUserId={currentUserId}
                            onSessionStarted={onSessionStarted}
                        />
                    )}

                    {!loading && !lobby && (
                        <div>
                            <p>
                                The team exists, but the mission lobby
                                is not available yet.
                            </p>

                            <button
                                type="button"
                                onClick={handleRetryLobby}
                            >
                                Retry Lobby Connection
                            </button>
                        </div>
                    )}
                </>
            )}

            {error && (
                <p role="alert">
                    {error}
                </p>
            )}

            {!team && (
                <div style={{ marginTop: '20px' }}>
                    <button
                        type="button"
                        onClick={onBack}
                        disabled={loading}
                    >
                        Back to Play Mode
                    </button>
                </div>
            )}
        </section>
    );
}

export default TeamsPage;