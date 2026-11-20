function LobbyMembers({ members, createdByUserId }) {
    if (!members || members.length === 0) {
        return <p>No participants in this lobby yet.</p>;
    }

    return (
        <div>
            <h3>Members ({members.length}/6)</h3>
            <ul>
                {members.map((member) => (
                    <li key={member.id}>
                        {member.userId}
                        {member.userId === createdByUserId ? ' (Leader)' : ''}
                    </li>
                ))}
            </ul>
        </div>
    );
}

export default LobbyMembers;