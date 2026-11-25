import { useState } from 'react';
import { joinTeam } from './teamsApi.js';

function JoinTeamForm({ onTeamJoined }) {
    const [joinCode, setJoinCode] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    async function handleSubmit(event) {
        event.preventDefault();
        setError('');

        const code = joinCode.trim().toUpperCase();

        if (!code) {
            setError('Please enter a team invitation code.');
            return;
        }

        setLoading(true);

        try {
            const team = await joinTeam(code);
            setJoinCode('');
            onTeamJoined(team);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <form onSubmit={handleSubmit}>
            <p>
                Enter the invitation code provided by your team leader.
            </p>

            <input
                type="text"
                placeholder="Enter team code"
                value={joinCode}
                onChange={(event) => setJoinCode(event.target.value)}
                maxLength={16}
                disabled={loading}
            />

            <button type="submit" disabled={loading}>
                {loading ? 'Joining Team...' : 'Join Existing Team'}
            </button>

            {error && <p role="alert">{error}</p>}
        </form>
    );
}

export default JoinTeamForm;