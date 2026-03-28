import React, { useState } from "react";
import Avatar from "./Avatar";

function formatDate(dateStr) {
  if (!dateStr) return "";
  const date = new Date(dateStr + "T00:00:00");
  const now = new Date();
  const diff = Math.floor((now - date) / (1000 * 60 * 60 * 24));

  if (diff === 0) return "hoje";
  if (diff === 1) return "ontem";
  if (diff < 7) return `${diff}d atrás`;

  return date.toLocaleDateString("pt-BR", {
    day: "numeric",
    month: "short",
  });
}

function formatTime(dateTimeStr) {
  if (!dateTimeStr) return "";
  const t = dateTimeStr.split("T")[1];
  return t ? t.slice(0, 5) : "";
}

const TYPE_COLORS = {
  "Reunião Deliberativa": "#0d9488",
  "Reunião Deliberativa Extraordinária": "#0d9488",
  "Sessão Deliberativa": "#0f766e",
  "Sessão Deliberativa Extraordinária": "#0f766e",
  "Audiência Pública": "#0d9488",
  "Audiência Pública e Deliberação": "#0f766e",
};

function EventFeedCard({ item }) {
  const [expanded, setExpanded] = useState(false);

  const e = item.event;
  const eventType = e.eventType;
  const description = e.description;
  const agendaSummary = e.agendaSummary;
  const situation = e.situation;
  const organName = e.organName;
  const location = e.location;
  const videoUrl = e.videoUrl;
  const startTime = formatTime(e.startTime);
  const endTime = formatTime(e.endTime);
  const deputies = e.deputies || [];
  const eventDate = item.date;

  const typeColor = TYPE_COLORS[eventType] || "#0d9488";

  const timeRange =
    startTime && endTime ? `${startTime} — ${endTime}` : startTime;

  const displayText = agendaSummary || description;

  return (
    <article className="feed-card card-event" onClick={() => setExpanded(!expanded)}>
      <div className="event-card-header">
        <span
          className="event-type-badge"
          style={{ backgroundColor: typeColor }}
        >
          {eventType}
        </span>
        <span className="event-card-date">{formatDate(eventDate)}</span>
      </div>

      {organName && <p className="event-organ">{organName}</p>}

      {timeRange && <p className="event-time">{timeRange}</p>}

      {displayText && (
        <p className={`feed-card-summary ${expanded ? "expanded" : ""}`}>
          {displayText}
        </p>
      )}

      {!expanded && displayText && displayText.length > 100 && (
        <button
          className="feed-card-read-more"
          onClick={(ev) => {
            ev.stopPropagation();
            setExpanded(true);
          }}
        >
          ler mais
        </button>
      )}

      {situation && (
        <div className="event-situation">
          <span
            className={`event-situation-badge ${situation.toLowerCase().includes("encerrad") ? "ended" : situation.toLowerCase().includes("cancelad") ? "cancelled" : "upcoming"}`}
          >
            {situation}
          </span>
        </div>
      )}

      {deputies.length > 0 && (
        <div className="event-deputies-section">
          <span className="event-deputies-label">
            {deputies.length}{" "}
            {deputies.length === 1 ? "deputado presente" : "deputados presentes"}
          </span>
          <div className="event-deputies-list">
            {deputies.map((d) => (
              <div key={d.deputyId} className="event-deputy-row">
                <Avatar name={d.name || "??"} size={28} photo={d.photo} />
                <div className="event-deputy-info">
                  <span className="event-deputy-name">{d.name}</span>
                  <span className="event-deputy-party">
                    {d.party} · {d.state}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      <div className="event-card-footer">
        {location && <span className="event-location">{location}</span>}
        {videoUrl && (
          <a
            className="event-video-link"
            href={videoUrl}
            target="_blank"
            rel="noopener noreferrer"
            onClick={(ev) => ev.stopPropagation()}
          >
            Assistir
          </a>
        )}
      </div>
    </article>
  );
}

export default EventFeedCard;
