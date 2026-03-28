import React, { useState } from "react";
import Avatar from "./Avatar";
import VoteBadge from "./VoteBadge";

const VOTE_STYLES = {
  SIM: { emoji: "👍", color: "#10b981" },
  "NÃO": { emoji: "👎", color: "#ef4444" },
  "ABSTENÇÃO": { emoji: "🤷", color: "#f59e0b" },
  AUSENTE: { emoji: "👻", color: "#9ca3af" },
};

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

function ActivityFeedCard({ item }) {
  const [expanded, setExpanded] = useState(false);

  const a = item.activity;
  const title = a.title;
  const subtitle = a.subtitle;
  const voteRound = a.voteRound;
  const summary = a.summary;
  const author = a.author;
  const voteDate = item.date;
  const votes = a.votes || [];

  return (
    <article className="feed-card card-voting" onClick={() => setExpanded(!expanded)}>
      <div className="activity-card-header">
        <span className="activity-type-badge">Votacao</span>
        <span className="activity-card-date">{formatDate(voteDate)}</span>
      </div>
      {voteRound && <p className="activity-vote-round">{voteRound}</p>}

      <span className="activity-card-bill-number">{title}</span>
      {subtitle && <h3 className="feed-card-title">{subtitle}</h3>}

      {summary && (
        <p className={`feed-card-summary ${expanded ? "expanded" : ""}`}>
          {summary}
        </p>
      )}

      {!expanded && summary && summary.length > 100 && (
        <button
          className="feed-card-read-more"
          onClick={(e) => {
            e.stopPropagation();
            setExpanded(true);
          }}
        >
          ler mais
        </button>
      )}

      <div className="activity-votes-section">
        <span className="activity-votes-label">
          {votes.length} {votes.length === 1 ? "voto" : "votos"}
        </span>
        <div className="activity-votes-list">
          {votes.map((v) => {
            const style = VOTE_STYLES[v.vote] || VOTE_STYLES["AUSENTE"];
            return (
              <div
                key={v.deputyId}
                className="activity-vote-row"
                style={{
                  backgroundColor: `${style.color}08`,
                  borderColor: `${style.color}20`,
                }}
              >
                <div className="activity-vote-deputy">
                  <Avatar name={v.name || "??"} size={32} photo={v.photo} />
                  <div className="activity-vote-deputy-info">
                    <span className="activity-vote-deputy-name">{v.name}</span>
                    <span className="activity-vote-deputy-party">
                      {v.party} · {v.state}
                    </span>
                  </div>
                </div>
                <div className="activity-vote-result">
                  <span className="activity-vote-emoji">{style.emoji}</span>
                  <VoteBadge vote={v.vote} />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      <div className="feed-card-footer">
        <span className="feed-card-author">✍️ {author}</span>
      </div>
    </article>
  );
}

export default ActivityFeedCard;
