
import { useState } from 'react';
import { createTeam } from './teamsApi.js';

function CreateTeamForm({ onTeamCreated }) {

    const [name, setName] = useState('');
    const [error, setError] = useState('');

    async function handleSubmit(event) {
        event.preventDefault();
        setError('');

        try {
            const team = await createTeam(name);
            setName('');
            onTeamCreated(team);
        } catch (err) {
            setError(err.message);
        }
    }

    return (
        <form onSubmit={handleSubmit}>
            <h2>Create a Team</h2>

            <input
                type="text"
                placeholder="Team name"
                value={name}
                onChange={(event) => setName(event.target.value)}
            />

            <button type="submit">
                Create Team
            </button>

            {error && <p>{error}</p>}
        </form>
    );
}

export default CreateTeamForm;
