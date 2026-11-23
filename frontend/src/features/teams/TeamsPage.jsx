
import { useState } from 'react';

import CreateTeamForm from './CreateTeamForm.jsx';
import JoinTeamForm from './JoinTeamForm.jsx';
import TeamDetails from './TeamDetails.jsx';

function TeamsPage() {

    const [team, setTeam] = useState(null);

    return (
        <div>
            <h1>Team Management</h1>

            {!team && (
                <>
                    <CreateTeamForm onTeamCreated={setTeam} />

                    <JoinTeamForm onTeamJoined={setTeam} />
                </>
            )}

            {team && (
                <TeamDetails team={team} />
            )}
        </div>
    );
}

export default TeamsPage;
