import { useEffect, useState } from 'react';
import { getAvailableMissions } from '../../services/missionApi';

function MissionCatalogue({ onSelectMission }) {
    const [missions, setMissions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let cancelled = false;

        async function loadMissions() {
            try {
                setLoading(true);
                setError(null);
                const data = await getAvailableMissions();
                if (!cancelled) {
                    setMissions(data);
                }
            } catch (err) {
                if (!cancelled) {
                    setError(err.message || 'Failed to load missions.');
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        loadMissions();

        return () => {
            cancelled = true;
        };
    }, []);

    if (loading) {
        return <p>Loading available missions...</p>;
    }

    if (error) {
        return <p>Error loading missions: {error}</p>;
    }

    if (missions.length === 0) {
        return <p>No missions available right now.</p>;
    }

    return (
        <section>
            <h2>Available Missions</h2>
            <ul>
                {missions.map((mission) => (
                    <li key={mission.missionId}>
                        <h3>{mission.title}</h3>
                        <p>{mission.description}</p>
                        <button
                            type="button"
                            onClick={() => onSelectMission(mission)}
                        >
                            Select Mission
                        </button>
                    </li>
                ))}
            </ul>
        </section>
    );
}

export default MissionCatalogue;