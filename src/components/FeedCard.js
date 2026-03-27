import React, { useState } from "react";
import Avatar from "./Avatar";
import VoteBadge from "./VoteBadge";

const CATEGORY_EMOJIS = {
  Economia: "💰",
  Tecnologia: "💻",
  Saúde: "🏥",
  Segurança: "🚔",
  Educação: "📚",
  "Meio Ambiente": "🌱",
  Comunicação: "📡",
  Habitação: "🏠",
  Transporte: "🚌",
  Trabalho: "💼",
};

const VOTE_STYLES = {
  SIM: { emoji: "👍", color: "#10b981" },
  NÃO: { emoji: "👎", color: "#ef4444" },
  ABSTENÇÃO: { emoji: "🤷", color: "#f59e0b" },
  AUSENTE: { emoji: "👻", color: "#9ca3af" },
};

function formatDate(dateStr) {
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

function FeedCard({ voteData, deputy }) {
  const [expanded, setExpanded] = useState(false);
  // Support both mock and API data
  const project = voteData.project ||
    voteData.project || {
      title: voteData.proposicaoTexto || voteData.descricao || "Votação",
      summary: voteData.objVotacao || voteData.descricao || "",
      author: "",
      category: voteData.siglaOrgao || "",
      voteDate: voteData.data || voteData.dataHoraRegistro,
    };
  const vote = voteData.vote || voteData.tipoVoto || "AUSENTE";
  const style = VOTE_STYLES[vote] || VOTE_STYLES["AUSENTE"];

  return (
    <article className="feed-card" onClick={() => setExpanded(!expanded)}>
      <div className="feed-card-header">
        <div className="feed-card-deputy">
          <Avatar
            name={(deputy && (deputy.nome || deputy.name)) || "??"}
            size={44}
          />
          <div className="feed-card-deputy-info">
            <span className="deputy-name">{deputy.nome || deputy.name}</span>
            <span className="deputy-detail">
              {deputy.siglaPartido || deputy.party || ""} ·{" "}
              {deputy.siglaUf || deputy.state || ""} ·{" "}
              {formatDate(project.voteDate)}
            </span>
          </div>
        </div>
      </div>

      <div className="feed-card-body">
        <div
          className="feed-card-vote-highlight"
          style={{
            backgroundColor: `${style.color}12`,
            borderColor: `${style.color}30`,
          }}
        >
          <span className="vote-highlight-emoji">{style.emoji}</span>
          <div className="vote-highlight-info">
            <VoteBadge vote={vote} />
            <span className="feed-card-category">
              {CATEGORY_EMOJIS[project.category]} {project.category}
            </span>
          </div>
        </div>

        <h3 className="feed-card-title">{project.title}</h3>

        <p className={`feed-card-summary ${expanded ? "expanded" : ""}`}>
          {project.summary}
        </p>

        {!expanded && (
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

        <div className="feed-card-footer">
          <span className="feed-card-author">✍️ {project.author}</span>
        </div>
      </div>
    </article>
  );
}

export default FeedCard;
