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

function ProposalFeedCard({ item }) {
  const [expanded, setExpanded] = useState(false);

  const title = item.name;
  const summary = item.description;
  const author = item.author;
  const voteDate = item.voteDate;
  const votes = item.votes || [];

  return (
    <article className="feed-card" onClick={() => setExpanded(!expanded)}>
      <div className="proposal-card-header">
        <span className="proposal-card-date">{formatDate(voteDate)}</span>
      </div>

      <h3 className="feed-card-title">{title}</h3>

      <p className={`feed-card-summary ${expanded ? "expanded" : ""}`}>
        {summary}
      </p>

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

      <div className="proposal-votes-section">
        <span className="proposal-votes-label">
          {votes.length} {votes.length === 1 ? "voto" : "votos"}
        </span>
        <div className="proposal-votes-list">
          {votes.map((v) => {
            const style = VOTE_STYLES[v.vote] || VOTE_STYLES["AUSENTE"];
            return (
              <div
                key={v.deputyId}
                className="proposal-vote-row"
                style={{
                  backgroundColor: `${style.color}08`,
                  borderColor: `${style.color}20`,
                }}
              >
                <div className="proposal-vote-deputy">
                  <Avatar name={v.name || "??"} size={32} photo={v.photo} />
                  <div className="proposal-vote-deputy-info">
                    <span className="proposal-vote-deputy-name">{v.name}</span>
                    <span className="proposal-vote-deputy-party">
                      {v.party} · {v.state}
                    </span>
                  </div>
                </div>
                <div className="proposal-vote-result">
                  <span className="proposal-vote-emoji">{style.emoji}</span>
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

export default ProposalFeedCard;
