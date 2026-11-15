import { useEffect, useState } from 'react';

import { apiRequest } from './services/api';

function App() {

    const [backendStatus, setBackendStatus] = useState('Checking...');

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
        </main>
    );
}

export default App;
