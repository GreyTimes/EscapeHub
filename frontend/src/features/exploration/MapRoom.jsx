function MapRoom({
    node,
    position,
    isCurrent,
    isConnected,
    isSelected,
    disabled,
    onSelect
}) {
    let backgroundColor = '#f3f4f6';
    let borderColor = '#6b7280';

    if (isCurrent) {
        backgroundColor = '#dbeafe';
        borderColor = '#1d4ed8';
    } else if (isConnected) {
        backgroundColor = '#dcfce7';
        borderColor = '#15803d';
    }

    return (
        <button
            type="button"
            onClick={() => onSelect(node.id)}
            disabled={disabled}
            style={{
                position: 'absolute',
                left: `${position.x}px`,
                top: `${position.y}px`,
                transform: 'translate(-50%, -50%)',
                minWidth: '120px',
                padding: '8px 10px',
                borderRadius: '8px',
                border: `${isSelected ? 3 : 2}px solid ${borderColor}`,
                backgroundColor,
                cursor: disabled ? 'not-allowed' : 'pointer',
                textAlign: 'center',
                boxShadow: isSelected ? '0 0 0 2px #111827' : 'none'
            }}
        >
            <div><strong>{node.title}</strong></div>
            {isCurrent && <div><small>Current location</small></div>}
            {!isCurrent && isConnected && <div><small>Reachable</small></div>}
        </button>
    );
}

export default MapRoom;