
function TeamDetails({ team }) {

    if (!team) {
        return null;
    }

    return (
        <div>
            <h2>{team.name}</h2>

            <p>
                Join Code: <strong>{team.joinCode}</strong>
            </p>

            <h3>Members</h3>

            <ul>
                {team.members?.map((member) => (
                    <li key={member.userId}>
                        {member.displayName}
                        {' - '}
                        {member.role}
                        {member.role === 'LEADER' && ' (Leader)'}
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default TeamDetails;
