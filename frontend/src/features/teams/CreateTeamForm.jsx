import { useState } from 'react';
import { createTeam } from './teamsApi.js';

function CreateTeamForm({ onTeamCreated }) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    async function handleSubmit(event) {
        event.preventDefault();
        setError('');
        setLoading(true);

        try {
            const team = await createTeam('EscapeHub Team');
            onTeamCreated(team);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    return (
        <form onSubmit={handleSubmit}>
            <p>
                Create a team and receive an invitation code to share
                with your teammates.
            </p>

            <button type="submit" disabled={loading}>
                {loading ? 'Creating Team...' : 'Create New Team'}
            </button>

            {error && <p role="alert">{error}</p>}
        </form>
    );
}

export default CreateTeamForm;