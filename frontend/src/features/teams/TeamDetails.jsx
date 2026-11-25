import { useState } from 'react';

function TeamDetails({ team }) {
    const [copyMessage, setCopyMessage] = useState('');

    if (!team) {
        return null;
    }

    async function handleCopyCode() {
        try {
            await navigator.clipboard.writeText(team.joinCode);
            setCopyMessage('Invitation code copied!');
        } catch {
            setCopyMessage(
                'Unable to copy automatically. Select and copy the code.'
            );
        }
    }

    return (
        <section>
            <h3>{team.name}</h3>

            <p>Share this invitation code with your teammates:</p>

            <div
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    margin: '12px 0'
                }}
            >
                <code
                    style={{
                        fontSize: '24px',
                        fontWeight: 'bold',
                        letterSpacing: '3px'
                    }}
                >
                    {team.joinCode}
                </code>

                <button type="button" onClick={handleCopyCode}>
                    Copy Code
                </button>
            </div>

            {copyMessage && (
                <p role="status">{copyMessage}</p>
            )}

            <h4>Team Members</h4>

            <ul>
                {team.members?.map((member) => (
                    <li key={member.userId}>
                        {member.displayName}
                        {member.role === 'LEADER' && ' (Leader)'}
                    </li>
                ))}
            </ul>
        </section>
    );
}

export default TeamDetails;