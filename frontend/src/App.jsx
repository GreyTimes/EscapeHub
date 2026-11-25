import { useEffect, useState } from 'react';

import { apiRequest } from './services/api';
import { getActiveSession } from './services/sessionApi';

import TeamsPage from './features/teams/TeamsPage.jsx';
import MissionCatalogue from './features/mission-catalogue/MissionCatalogue';
import PlayModeSelector from './features/mission-session/PlayModeSelector';
import ActiveSession from './features/session/ActiveSession';

const DEV_USER_ID = import.meta.env.VITE_DEV_USER_ID;

function App() {
    const [backendStatus, setBackendStatus] = useState('Checking...');

    const [selectedMission, setSelectedMission] = useState(null);
    const [startedSession, setStartedSession] = useState(null);

    const [inTeamSetup, setInTeamSetup] = useState(false);

    const [recoverableSession, setRecoverableSession] = useState(null);
    const [recoveryStatus, setRecoveryStatus] = useState('checking');
    const [recoveryError, setRecoveryError] = useState('');
    const [recoveryAttempt, setRecoveryAttempt] = useState(0);

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

    useEffect(() => {
        let cancelled = false;

        async function checkActiveSession() {
            setRecoveryStatus('checking');
            setRecoveryError('');

            try {
                const session = await getActiveSession();

                if (cancelled) {
                    return;
                }

                setRecoverableSession(session);
                setRecoveryStatus(session ? 'available' : 'none');
            } catch (error) {
                if (!cancelled) {
                    setRecoverableSession(null);
                    setRecoveryError(error.message);
                    setRecoveryStatus('error');
                }
            }
        }

        checkActiveSession();

        return () => {
            cancelled = true;
        };
    }, [recoveryAttempt]);

    function handleSessionAbandoned() {
        setStartedSession(null);
        setRecoverableSession(null);
        setRecoveryStatus('none');
        setInTeamSetup(false);
        setSelectedMission(null);
    }

    function handleContinueMission() {
        setStartedSession(recoverableSession);
        setRecoverableSession(null);
    }

    return (
        <main>
            <h1>EscapeHub</h1>

            <p>Collaborative Mystery and Escape Room Platform</p>

            <section>
                <h2>Development Environment</h2>
                <p>Backend status: {backendStatus}</p>
            </section>

            {startedSession ? (
                <ActiveSession
                    session={startedSession}
                    onSessionAbandoned={handleSessionAbandoned}
                />
            ) : recoveryStatus === 'checking' ? (
                <section>
                    <h2>Checking for an active mission...</h2>
                    <p>
                        Please wait while EscapeHub checks your saved progress.
                    </p>
                </section>
            ) : recoveryStatus === 'error' ? (
                <section>
                    <h2>Unable to check your active mission</h2>

                    <p>{recoveryError}</p>

                    <button
                        type="button"
                        onClick={() =>
                            setRecoveryAttempt((attempt) => attempt + 1)
                        }
                    >
                        Retry
                    </button>
                </section>
            ) : recoverableSession ? (
                <section>
                    <h2>Active Mission Found</h2>

                    <p>You already have an active mission saved.</p>

                    <p>
                        Mode: <strong>{recoverableSession.mode}</strong>
                    </p>

                    <p>
                        Started:{' '}
                        <strong>
                            {recoverableSession.startedAt
                                ? new Date(
                                    recoverableSession.startedAt
                                ).toLocaleString()
                                : 'Unknown'}
                        </strong>
                    </p>

                    <button
                        type="button"
                        onClick={handleContinueMission}
                    >
                        Continue Mission
                    </button>
                </section>
            ) : !selectedMission ? (
                <MissionCatalogue
                    onSelectMission={setSelectedMission}
                />
            ) : inTeamSetup ? (
                <TeamsPage
                    missionVersionId={selectedMission.missionVersionId}
                    currentUserId={DEV_USER_ID}
                    onSessionStarted={(session) => {
                        setStartedSession(session);
                        setInTeamSetup(false);
                    }}
                    onBack={() => setInTeamSetup(false)}
                />
            ) : (
                <div>
                    <p>
                        Selected mission:{' '}
                        <strong>{selectedMission.title}</strong>
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
                        onTeamSelected={() => setInTeamSetup(true)}
                    />
                </div>
            )}
        </main>
    );
}

export default App;