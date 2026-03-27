import React, { useState, useMemo } from "react";
import Avatar from "./Avatar";

function FollowModal({ deputies, onToggleFollow, onClose }) {
  const [search, setSearch] = useState("");
  const followingCount = deputies.filter((d) => d.following).length;

  const filtered = useMemo(() => {
    if (!search.trim()) return deputies;
    const term = search.toLowerCase();
    return deputies.filter(
      (d) =>
        d.name.toLowerCase().includes(term) ||
        d.party.toLowerCase().includes(term) ||
        (d.state || "").toLowerCase().includes(term),
    );
  }, [deputies, search]);

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
