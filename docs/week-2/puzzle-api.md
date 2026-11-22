# Puzzle API (#37)

Both endpoints require a current user (X-Dev-User-Id in the dev profile)
who is a member of the session. Errors have the form {"message": "..."}.

A player sees and answers only the puzzles of the room (node) they are
currently in. The correct answer is never returned by the API.

## 1. Puzzles of a room

    GET /api/v1/sessions/{sessionId}/nodes/{nodeId}/puzzles

Response 200 (empty list if the room has no puzzle):

    [
      {
        "id": "<UUID>",
        "nodeId": "<UUID>",
        "question": "What is the capital of Greece?",
        "finalPuzzle": true,
        "solved": false,
        "attemptsUsed": 0
      }
    ]

solved and attemptsUsed are shared by all players of the session.

## 2. Submit an answer

    POST /api/v1/sessions/{sessionId}/puzzles/{puzzleId}/answer

Request:

    { "answer": "Athens" }

The answer is compared ignoring upper/lower case and extra spaces.

Response 200:

    {
      "puzzleId": "<UUID>",
      "correct": true,
      "solved": true,
      "attemptsUsed": 1,
      "missionCompleted": true,
      "message": "Correct! You completed the mission."
    }

- A wrong answer returns correct=false and increases attemptsUsed.
- Answering a puzzle that is already solved changes nothing.
- Solving a puzzle with finalPuzzle=true sets the session to COMPLETED
  (missionCompleted=true) and all its members to FINISHED.

## 3. Errors

| Status | When |
|---|---|
| 400 | answer is empty or longer than 255 characters |
| 401 | No current user |
| 403 | Not a member of the session, or not in the puzzle's room |
| 404 | Session, room or puzzle not found in this mission |
| 409 | The session is not ACTIVE (completed or abandoned) |

## 4. Test data (until mission import exists)

Puzzles are stored in the table puzzles. Example for a node of a seeded mission:

    INSERT INTO puzzles (id, node_id, puzzle_key, question, correct_answer, final_puzzle)
    VALUES (gen_random_uuid(), '<NODE_UUID>', 'capital',
            'What is the capital of Greece?', 'Athens', true);
