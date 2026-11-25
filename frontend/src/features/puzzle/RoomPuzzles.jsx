import { useEffect, useState } from 'react';

import {
    getRoomPuzzles,
    submitPuzzleAnswer
} from './puzzleApi';

function RoomPuzzles({ sessionId, roomId }) {
    const [puzzles, setPuzzles] = useState([]);
    const [answers, setAnswers] = useState({});
    const [results, setResults] = useState({});
    const [loading, setLoading] = useState(true);
    const [submittingId, setSubmittingId] = useState(null);
    const [error, setError] = useState(null);

    useEffect(() => {
        let cancelled = false;

        async function loadPuzzles() {
            setLoading(true);
            setError(null);
            setPuzzles([]);
            setAnswers({});
            setResults({});

            try {
                const data = await getRoomPuzzles(sessionId, roomId);

                if (!cancelled) {
                    setPuzzles(data ?? []);
                }
            } catch (requestError) {
                if (!cancelled) {
                    setError(requestError.message);
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        if (sessionId && roomId) {
            loadPuzzles();
        }

        return () => {
            cancelled = true;
        };
    }, [sessionId, roomId]);

    function handleAnswerChange(puzzleId, value) {
        setAnswers((previous) => ({
            ...previous,
            [puzzleId]: value
        }));
    }

    async function handleSubmit(puzzleId) {
        const answer = answers[puzzleId]?.trim();

        if (!answer || submittingId) {
            return;
        }

        setError(null);
        setSubmittingId(puzzleId);

        try {
            const result = await submitPuzzleAnswer(
                sessionId,
                puzzleId,
                answer
            );

            setResults((previous) => ({
                ...previous,
                [puzzleId]: result
            }));

            setPuzzles((previous) =>
                previous.map((puzzle) =>
                    puzzle.id === puzzleId
                        ? {
                            ...puzzle,
                            solved: result.solved,
                            attemptsUsed: result.attemptsUsed
                        }
                        : puzzle
                )
            );

            if (result.solved) {
                setAnswers((previous) => ({
                    ...previous,
                    [puzzleId]: ''
                }));
            }
        } catch (requestError) {
            setError(requestError.message);
        } finally {
            setSubmittingId(null);
        }
    }

    if (loading) {
        return <p>Loading room puzzles...</p>;
    }

    if (error && puzzles.length === 0) {
        return (
            <div>
                <p role="alert">{error}</p>
            </div>
        );
    }

    if (puzzles.length === 0) {
        return (
            <div>
                <h4>Puzzles</h4>
                <p>There are no puzzles in this room.</p>
            </div>
        );
    }

    return (
        <div style={{ marginTop: '16px' }}>
            <h4>Room Puzzles</h4>

            {puzzles.map((puzzle) => {
                const result = results[puzzle.id];
                const isSolved = puzzle.solved;
                const isSubmitting = submittingId === puzzle.id;

                return (
                    <div
                        key={puzzle.id}
                        style={{
                            border: '1px solid #d1d5db',
                            borderRadius: '8px',
                            padding: '12px',
                            marginBottom: '12px'
                        }}
                    >
                        <h5>
                            {puzzle.finalPuzzle
                                ? 'Final Puzzle'
                                : 'Investigation Puzzle'}
                        </h5>

                        <p>{puzzle.question}</p>

                        {isSolved ? (
                            <p style={{ color: 'green' }}>
                                Puzzle solved.
                            </p>
                        ) : (
                            <>
                                <input
                                    type="text"
                                    value={answers[puzzle.id] ?? ''}
                                    onChange={(event) =>
                                        handleAnswerChange(
                                            puzzle.id,
                                            event.target.value
                                        )
                                    }
                                    placeholder="Enter your answer..."
                                    disabled={isSubmitting}
                                />

                                <button
                                    type="button"
                                    onClick={() => handleSubmit(puzzle.id)}
                                    disabled={
                                        isSubmitting ||
                                        !answers[puzzle.id]?.trim()
                                    }
                                    style={{ marginLeft: '8px' }}
                                >
                                    {isSubmitting
                                        ? 'Checking...'
                                        : 'Submit Answer'}
                                </button>
                            </>
                        )}

                        <p>
                            <small>
                                Attempts: {puzzle.attemptsUsed ?? 0}
                            </small>
                        </p>

                        {result && (
                            <p
                                role="status"
                                style={{
                                    color: result.correct
                                        ? 'green'
                                        : 'crimson'
                                }}
                            >
                                {result.message}
                            </p>
                        )}

                        {result?.missionCompleted && (
                            <h4>Mission completed!</h4>
                        )}
                    </div>
                );
            })}

            {error && (
                <p role="alert">{error}</p>
            )}
        </div>
    );
}

export default RoomPuzzles;