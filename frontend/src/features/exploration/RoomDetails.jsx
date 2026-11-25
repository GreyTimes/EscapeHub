import RoomPuzzles from '../puzzle/RoomPuzzles';
import AvailableEvidence from '../evidence/AvailableEvidence';

function RoomDetails({
                         sessionId,
                         room,
                         isCurrent,
                         isConnected,
                         availableMoves,
                         moving,
                         onMove
                     }) {
    if (!room) {
        return <p>Select a room on the map to view its details.</p>;
    }

    return (
        <div
            style={{
                marginTop: '16px',
                padding: '12px',
                border: '1px solid #d1d5db',
                borderRadius: '8px',
                maxWidth: '640px'
            }}
        >
            <h4>
                {room.title}
                {isCurrent ? ' (You are here)' : ''}
            </h4>

            <p>
                {room.description ||
                    'No description available for this room.'}
            </p>

            <p>
                <small>
                    Coordinates: ({room.mapX}, {room.mapY})
                </small>
            </p>

            {!isCurrent && (
                <div>
                    {isConnected ? (
                        <button
                            type="button"
                            onClick={() => onMove(room.id)}
                            disabled={moving}
                        >
                            {moving
                                ? 'Moving...'
                                : `Move to ${room.title}`}
                        </button>
                    ) : (
                        <p>
                            <em>
                                Not directly connected to your current room.
                            </em>
                        </p>
                    )}
                </div>
            )}

            <div style={{ marginTop: '12px' }}>
                <strong>
                    Available moves from your current room:
                </strong>

                {availableMoves.length === 0 ? (
                    <p>No connected rooms available.</p>
                ) : (
                    <ul>
                        {availableMoves.map((targetRoom) => (
                            <li
                                key={targetRoom.id}
                                style={{ marginBottom: '4px' }}
                            >
                                {targetRoom.title}{' '}

                                <button
                                    type="button"
                                    onClick={() => onMove(targetRoom.id)}
                                    disabled={moving}
                                >
                                    {moving ? 'Moving...' : 'Move'}
                                </button>
                            </li>
                        ))}
                    </ul>
                )}
            </div>

            {isCurrent && (
                <>
                    <AvailableEvidence
                        sessionId={sessionId}
                        roomId={room.id}
                    />

                    <RoomPuzzles
                        sessionId={sessionId}
                        roomId={room.id}
                    />
                </>
            )}
        </div>
    );
}

export default RoomDetails;