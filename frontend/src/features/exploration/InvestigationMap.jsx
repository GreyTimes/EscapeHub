import { useEffect, useState } from 'react';

import { getSessionMap, movePlayerLocation } from './explorationApi';
import MapRoom from './MapRoom';
import RoomDetails from './RoomDetails';

const MAP_WIDTH = 640;
const MAP_HEIGHT = 400;
const MAP_PADDING = 75;

function computeNodePositions(nodes) {
    const positions = new Map();
    if (!nodes || nodes.length === 0) {
        return positions;
    }

    const xs = nodes.map((n) => n.mapX);
    const ys = nodes.map((n) => n.mapY);
    const minX = Math.min(...xs);
    const maxX = Math.max(...xs);
    const minY = Math.min(...ys);
    const maxY = Math.max(...ys);

    const usePercentageScale = minX >= 0 && maxX <= 100 && minY >= 0 && maxY <= 100;
    const rangeMinX = usePercentageScale ? 0 : minX;
    const rangeMaxX = usePercentageScale ? 100 : maxX;
    const rangeMinY = usePercentageScale ? 0 : minY;
    const rangeMaxY = usePercentageScale ? 100 : maxY;

    const usableWidth = MAP_WIDTH - MAP_PADDING * 2;
    const usableHeight = MAP_HEIGHT - MAP_PADDING * 2;

    for (const node of nodes) {
        const x = rangeMaxX === rangeMinX
            ? MAP_WIDTH / 2
            : MAP_PADDING + ((node.mapX - rangeMinX) / (rangeMaxX - rangeMinX)) * usableWidth;
        const y = rangeMaxY === rangeMinY
            ? MAP_HEIGHT / 2
            : MAP_PADDING + ((node.mapY - rangeMinY) / (rangeMaxY - rangeMinY)) * usableHeight;

        positions.set(node.id, { x, y });
    }

    return positions;
}

function InvestigationMap({ sessionId }) {
    const [mapData, setMapData] = useState(null);
    const [selectedNodeId, setSelectedNodeId] = useState(null);
    const [loading, setLoading] = useState(true);
    const [moving, setMoving] = useState(false);
    const [error, setError] = useState(null);

    async function loadMap() {
        setError(null);
        setLoading(true);

        try {
            const data = await getSessionMap(sessionId);
            setMapData(data);
            setSelectedNodeId((prev) => prev ?? data.currentNodeId ?? data.nodes[0]?.id ?? null);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        if (!sessionId) {
            return;
        }
        loadMap();
    }, [sessionId]);

    async function handleMove(targetNodeId) {
        setError(null);
        setMoving(true);

        try {
            const updated = await movePlayerLocation(sessionId, targetNodeId);
            setMapData(updated);
            setSelectedNodeId(updated.currentNodeId);
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setMoving(false);
        }
    }

    if (loading && !mapData) {
        return <p>Loading investigation map...</p>;
    }

    if (!mapData) {
        return (
            <div>
                {error && <p role="alert">{error}</p>}
                <button type="button" onClick={loadMap}>
                    Retry
                </button>
            </div>
        );
    }

    const nodes = mapData.nodes ?? [];
    const connections = mapData.connections ?? [];
    const positions = computeNodePositions(nodes);

    const currentNode = nodes.find((n) => n.id === mapData.currentNodeId) ?? null;
    const connectedIds = new Set(currentNode?.connectedNodeIds ?? []);
    const selectedNode = nodes.find((n) => n.id === selectedNodeId) ?? currentNode ?? nodes[0] ?? null;
    const availableMoves = nodes.filter((n) => connectedIds.has(n.id));

    return (
        <div style={{ marginTop: '20px' }}>
            <h3>Investigation Map</h3>

            <p>
                <strong>Current Room:</strong>{' '}
                {currentNode ? currentNode.title : 'Not placed in a room yet'}
                {' '}
                <button type="button" onClick={loadMap} disabled={loading || moving}>
                    Refresh Map
                </button>
            </p>

            {nodes.length === 0 ? (
                <p>No rooms found for this mission version.</p>
            ) : (
                <>
                    <div
                        style={{
                            position: 'relative',
                            width: `${MAP_WIDTH}px`,
                            height: `${MAP_HEIGHT}px`,
                            border: '1px solid #9ca3af',
                            borderRadius: '8px',
                            backgroundColor: '#fafafa',
                            overflow: 'hidden'
                        }}
                    >
                        <svg
                            width={MAP_WIDTH}
                            height={MAP_HEIGHT}
                            style={{
                                position: 'absolute',
                                inset: 0,
                                pointerEvents: 'none'
                            }}
                        >
                            {connections.map((conn) => {
                                const from = positions.get(conn.sourceNodeId);
                                const to = positions.get(conn.targetNodeId);
                                if (!from || !to) {
                                    return null;
                                }

                                const isActiveEdge =
                                    conn.sourceNodeId === mapData.currentNodeId ||
                                    conn.targetNodeId === mapData.currentNodeId;

                                return (
                                    <line
                                        key={`${conn.sourceNodeId}-${conn.targetNodeId}`}
                                        x1={from.x}
                                        y1={from.y}
                                        x2={to.x}
                                        y2={to.y}
                                        stroke={isActiveEdge ? '#15803d' : '#9ca3af'}
                                        strokeWidth={isActiveEdge ? 3 : 2}
                                    />
                                );
                            })}
                        </svg>

                        {nodes.map((node) => (
                            <MapRoom
                                key={node.id}
                                node={node}
                                position={positions.get(node.id)}
                                isCurrent={node.id === mapData.currentNodeId}
                                isConnected={connectedIds.has(node.id)}
                                isSelected={selectedNode?.id === node.id}
                                disabled={moving}
                                onSelect={setSelectedNodeId}
                            />
                        ))}
                    </div>

                    <RoomDetails
                        room={selectedNode}
                        isCurrent={selectedNode?.id === mapData.currentNodeId}
                        isConnected={selectedNode ? connectedIds.has(selectedNode.id) : false}
                        availableMoves={availableMoves}
                        moving={moving}
                        onMove={handleMove}
                    />
                </>
            )}

            {error && (
                <p role="alert">
                    {error}
                </p>
            )}
        </div>
    );
}

export default InvestigationMap;