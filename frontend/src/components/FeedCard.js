import React, { useState } from "react";
import Avatar from "./Avatar";
import VoteBadge from "./VoteBadge";

function normalize(str) {
  return (str || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();
}

const CATEGORY_EMOJIS = {
  economia: "💰",
  tecnologia: "💻",
  saude: "🏥",
  seguranca: "🚔",
  educacao: "📚",
  "meio ambiente": "🌱",
  comunicacao: "📡",
  habitacao: "🏠",
  transporte: "🚌",
  trabalho: "💼",
  plenario: "⚖️",
  justica: "⚖️",
  legislativo: "📜",
};

const CATEGORY_DISPLAY = {
  economia: "Economia",
  tecnologia: "Tecnologia",
  saude: "Saúde",
  seguranca: "Segurança",
  educacao: "Educação",
  "meio ambiente": "Meio Ambiente",
  comunicacao: "Comunicação",
  habitacao: "Habitação",
  transporte: "Transporte",
  trabalho: "Trabalho",
  plenario: "Plenário",
  justica: "Justiça",
  legislativo: "Legislativo",
};

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

function FeedCard({ item }) {
  const [expanded, setExpanded] = useState(false);

  const title = item.name;
  const summary = item.description;
  const author = item.author;
  const categoryKey = normalize(item.category);
  const category = CATEGORY_DISPLAY[categoryKey] || item.category;
  const categoryEmoji = CATEGORY_EMOJIS[categoryKey] || "📋";
  const voteDate = item.voteDate;
  const vote = item.vote || "AUSENTE";
  const style = VOTE_STYLES[vote] || VOTE_STYLES["AUSENTE"];
  const deputyName = item.deputieName;
  const deputyParty = item.deputieParty;

  return (
    <article className="feed-card" onClick={() => setExpanded(!expanded)}>
      <div className="feed-card-header">
        <div className="feed-card-deputy">
          <Avatar name={deputyName || "??"} size={44} />
          <div className="feed-card-deputy-info">
            <span className="deputy-name">{deputyName}</span>
            <span className="deputy-detail">
              {deputyParty} · {formatDate(voteDate)}
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
              {categoryEmoji} {category}
            </span>
          </div>
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

        <div className="feed-card-footer">
          <span className="feed-card-author">✍️ {author}</span>
        </div>
      </div>
    </article>
  );
}

export default FeedCard;
