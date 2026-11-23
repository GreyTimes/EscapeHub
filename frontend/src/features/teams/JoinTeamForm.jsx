
import { useState } from 'react';
import { joinTeam } from './teamsApi.js';

function JoinTeamForm({ onTeamJoined }) {

    const [joinCode, setJoinCode] = useState('');
    const [error, setError] = useState('');

    async function handleSubmit(event) {
        event.preventDefault();
        setError('');

        try {
            const team = await joinTeam(joinCode);
            setJoinCode('');
            onTeamJoined(team);
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <form onSubmit={handleSubmit}>
            <h2>Join a Team</h2>

            <input
                type="text"
                placeholder="Enter join code"
                value={joinCode}
                onChange={(event) => setJoinCode(event.target.value)}
            />

            <button type="submit">
                Join Team
            </button>

            {error && <p>{error}</p>}
        </form>
    );
}

export default JoinTeamForm;
