import React, { useState, useMemo } from "react";
import Avatar from "./Avatar";

function FollowModal({ deputies, onToggleFollow, onClose }) {
  const [search, setSearch] = useState("");
  const [partyFilter, setPartyFilter] = useState("");
  const [stateFilter, setStateFilter] = useState("");
  const [followedOnly, setFollowedOnly] = useState(false);

  const followingCount = deputies.filter((d) => d.following).length;

  const parties = useMemo(() => {
    const set = new Set(deputies.map((d) => d.party).filter(Boolean));
    return [...set].sort();
  }, [deputies]);

  const states = useMemo(() => {
    const set = new Set(deputies.map((d) => d.state).filter(Boolean));
    return [...set].sort();
  }, [deputies]);

  const filtered = useMemo(() => {
    let result = deputies;

    if (followedOnly) {
      result = result.filter((d) => d.following);
    }

    if (partyFilter) {
      result = result.filter((d) => d.party === partyFilter);
    }

    if (stateFilter) {
      result = result.filter((d) => d.state === stateFilter);
    }

    if (search.trim()) {
      const term = search.toLowerCase();
      result = result.filter(
        (d) =>
          d.name.toLowerCase().includes(term) ||
          d.party.toLowerCase().includes(term) ||
          (d.state || "").toLowerCase().includes(term),
      );
    }

    return result;
  }, [deputies, search, partyFilter, stateFilter, followedOnly]);

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h2>Escolha seus deputados</h2>
            <p className="modal-subtitle">
              Seguindo {followingCount} de {deputies.length}
            </p>
          </div>
          <button className="modal-close" onClick={onClose}>
            ✕
          </button>
        </div>
        <div className="modal-search">
          <input
            type="text"
            placeholder="Buscar deputado..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            autoFocus
          />
        </div>
        <div className="modal-filters">
          <select
            className="modal-filter-select"
            value={partyFilter}
            onChange={(e) => setPartyFilter(e.target.value)}
          >
            <option value="">Partido</option>
            {parties.map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
          <select
            className="modal-filter-select"
            value={stateFilter}
            onChange={(e) => setStateFilter(e.target.value)}
          >
            <option value="">Estado</option>
            {states.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
          <button
            className={`modal-filter-toggle ${followedOnly ? "active" : ""}`}
            onClick={() => setFollowedOnly((v) => !v)}
          >
            Seguindo
          </button>
        </div>
        <div className="modal-body">
          {filtered.map((dep) => (
            <div
              key={dep.id}
              className={`deputy-item ${dep.following ? "item-following" : ""}`}
              onClick={() => onToggleFollow(dep.id)}
            >
              <div className="deputy-item-info">
                <Avatar
                  name={dep.name}
                  size={48}
                  photo={dep.avatar || dep.photo}
                />
                <div className="deputy-item-text">
                  <span className="deputy-item-name">{dep.name}</span>
                  <span className="deputy-item-party">
                    {dep.party} · {dep.state}
                  </span>
                </div>
              </div>
              <button
                className={`follow-btn ${dep.following ? "following" : ""}`}
                onClick={(e) => {
                  e.stopPropagation();
                  onToggleFollow(dep.id);
                }}
              >
                {dep.following ? "✓ Seguindo" : "Seguir"}
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default FollowModal;
