
import { useEffect, useState } from 'react';

import { apiRequest } from './services/api';
import TeamsPage from './features/teams/TeamsPage.jsx';
import MissionCatalogue from './features/mission-catalogue/MissionCatalogue';
import PlayModeSelector from './features/mission-session/PlayModeSelector';
import LobbyPage from './features/lobby/LobbyPage';
import ActiveSession from './features/session/ActiveSession';

const DEV_USER_ID = import.meta.env.VITE_DEV_USER_ID;

function App() {

    const [backendStatus, setBackendStatus] = useState('Checking...');
    const [selectedMission, setSelectedMission] = useState(null);
    const [startedSession, setStartedSession] = useState(null);
    const [inLobby, setInLobby] = useState(false);
    const [showTeams, setShowTeams] = useState(false);

    useEffect(() => {

        let cancelled = false;

        async function checkBackend() {

            try {
                const response = await apiRequest('/health');

                if (!cancelled) {
                    setBackendStatus(
                        response.status === 'ok'
                            ? 'Connected'
                            : 'Unexpected response'
                    );
                }

            } catch (error) {

                if (!cancelled) {
                    setBackendStatus(`Unavailable: ${error.message}`);
                }
            }
        }

        checkBackend();

        return () => {
            cancelled = true;
        };

    }, []);

    return (
        <main>
            <h1>EscapeHub</h1>

            <p>Collaborative Mystery and Escape Room Platform</p>

            <section>
                <h2>Development Environment</h2>

                <button
                    type="button"
                    onClick={() => setShowTeams(!showTeams)}
                >
                    {showTeams ? 'Back to Missions' : 'Team Management'}
                </button>

                <p>
                    Backend status: {backendStatus}
                </p>
            </section>

            {showTeams ? (
                <TeamsPage />
            ) : startedSession ? (
                <ActiveSession
                    session={startedSession}
                    onSessionAbandoned={() => {
                        setStartedSession(null);
                        setInLobby(false);
                        setSelectedMission(null);
                    }}
                />
            ) : !selectedMission ? (
                <MissionCatalogue
                    onSelectMission={(mission) => setSelectedMission(mission)}
                />
            ) : inLobby ? (
                <LobbyPage
                    missionVersionId={selectedMission.missionVersionId}
                    currentUserId={DEV_USER_ID}
                    onSessionStarted={(session) => {
                        setStartedSession(session);
                        setInLobby(false);
                    }}
                    onBack={() => setInLobby(false)}
                />
            ) : (
                <div>
                    <p>
                        Selected mission: <strong>{selectedMission.title}</strong>
                    </p>

                    <button
                        type="button"
                        onClick={() => setSelectedMission(null)}
                    >
                        Back to Missions
                    </button>

                    <PlayModeSelector
                        missionId={selectedMission.missionId}
                        missionVersionId={selectedMission.missionVersionId}
                        onSoloStarted={setStartedSession}
                        onTeamSelected={() => setInLobby(true)}
                    />
                </div>
            )}
        </main>
    );
}

export default App;
