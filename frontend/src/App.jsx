import { useEffect, useState } from 'react';

import { apiRequest } from './services/api';

import PlayModeSelector from './features/mission-session/PlayModeSelector';

// Development-only mission used to try the Solo / Team choice
// until the mission catalogue (#21) is connected.
const DEV_MISSION_ID = import.meta.env.VITE_DEV_MISSION_ID;

const DEV_MISSION_VERSION_ID = import.meta.env.VITE_DEV_MISSION_VERSION_ID;

function App() {

    const [backendStatus, setBackendStatus] = useState('Checking...');

    const [startedSession, setStartedSession] = useState(null);

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
                <section>
                    <h2>Mission started</h2>

                    <p>
                        Session {startedSession.id} ({startedSession.mode}, {startedSession.status})
                    </p>
                </section>
            ) : (
                <PlayModeSelector
                    missionId={DEV_MISSION_ID}
                    missionVersionId={DEV_MISSION_VERSION_ID}
                    onSoloStarted={setStartedSession}
                />
            )}
        </main>
    );
}

export default App;
