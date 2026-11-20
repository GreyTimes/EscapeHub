import { useEffect, useState } from 'react';

import { apiRequest } from './services/api';

import PlayModeSelector from './features/mission-session/PlayModeSelector';

import LobbyPage from './features/lobby/LobbyPage';
import ActiveSession from './features/session/ActiveSession';

// Development-only mission used to try the Solo / Team choice
// until the mission catalogue (#21) is connected.
const DEV_MISSION_ID = import.meta.env.VITE_DEV_MISSION_ID;

const DEV_MISSION_VERSION_ID = import.meta.env.VITE_DEV_MISSION_VERSION_ID;

const DEV_USER_ID = import.meta.env.VITE_DEV_USER_ID;

function App() {

    const [backendStatus, setBackendStatus] = useState('Checking...');

    const [startedSession, setStartedSession] = useState(null);

    const [inLobby, setInLobby] = useState(false);

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

                <p>
                    Backend status: {backendStatus}
                </p>
            </section>

            {startedSession ? (
                         <ActiveSession
                             session={startedSession}
                             onSessionAbandoned={() => {
                                 setStartedSession(null);
                                 setInLobby(false);
                             }}
                         />
                     ) : inLobby ? (
                         <LobbyPage
                             missionVersionId={DEV_MISSION_VERSION_ID}
                             currentUserId={DEV_USER_ID}
                             onSessionStarted={(session) => {
                                 setStartedSession(session);
                                 setInLobby(false);
                             }}
                             onBack={() => setInLobby(false)}
                         />
                     ) : (
                         <PlayModeSelector
                             missionId={DEV_MISSION_ID}
                             missionVersionId={DEV_MISSION_VERSION_ID}
                             onSoloStarted={setStartedSession}
                             onTeamSelected={() => setInLobby(true)}
                         />
                     )}
        </main>
    );
}

export default App;
