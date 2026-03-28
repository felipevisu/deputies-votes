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

function ProposalFeedCard({ item }) {
  const [expanded, setExpanded] = useState(false);

  const typeCode = item.proposalTypeCode;
  const number = item.proposalNumber;
  const year = item.proposalYear;
  const ementa = item.proposalEmenta;
  const status = item.proposalStatus;
  const authors = item.authors || [];
  const date = item.date;

  const title = `${typeCode} ${number}/${year}`;

  return (
    <article className="feed-card" onClick={() => setExpanded(!expanded)}>
      <div className="proposal-card-header">
        <span className="proposal-type-badge">{typeCode}</span>
        <span className="proposal-card-date">{formatDate(date)}</span>
      </div>

      <h3 className="feed-card-title">{title}</h3>

      <p className={`feed-card-summary ${expanded ? "expanded" : ""}`}>
        {ementa}
      </p>

      {!expanded && ementa && ementa.length > 100 && (
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

      {status && (
        <div className="proposal-status">
          <span className="proposal-status-label">Status:</span> {status}
        </div>
      )}

      {authors.length > 0 && (
        <div className="proposal-authors-section">
          <span className="proposal-authors-label">
            {authors.length === 1 ? "Autor" : "Autores"}
          </span>
          <div className="proposal-authors-list">
            {authors.map((a) => (
              <div key={a.deputyId} className="proposal-author-row">
                <Avatar name={a.name || "??"} size={28} photo={a.photo} />
                <div className="proposal-author-info">
                  <span className="proposal-author-name">{a.name}</span>
                  <span className="proposal-author-party">
                    {a.party} · {a.state}
                  </span>
                </div>
                {a.proponent && (
                  <span className="proposal-author-badge">autor principal</span>
                )}
              </div>
            ))}
          </div>
        </div>
      )}
    </article>
  );
}

export default ProposalFeedCard;
