import React from "react";
import Avatar from "./Avatar";

function FollowModal({ deputies, onToggleFollow, onClose }) {
  const followingCount = deputies.filter((d) => d.following).length;

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
        <div className="modal-body">
          {deputies.map((dep) => (
            <div
              key={dep.id}
              className={`deputy-item ${dep.following ? "item-following" : ""}`}
              onClick={() => onToggleFollow(dep.id)}
            >
              <div className="deputy-item-info">
                <Avatar name={dep.name} size={48} />
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
