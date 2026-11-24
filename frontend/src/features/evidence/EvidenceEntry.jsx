import React from "react";

export default function EvidenceEntry({ entry }) {
    const formattedTime = entry.discoveredAt
        ? new Date(entry.discoveredAt).toLocaleTimeString([], {
            hour: "2-digit",
            minute: "2-digit",
        })
        : "";

    return (
        <article className="evidence-entry">
            <div className="evidence-entry__header">
                <strong>{entry.title}</strong>
                {formattedTime && <span>{formattedTime}</span>}
            </div>
            {entry.description && <p>{entry.description}</p>}
            <small>Discovered by player: {entry.discoveredByUserId}</small>
        </article>
    );
}