import React from "react";
import Avatar from "./Avatar";

function StoriesBar({ deputies, activeDeputyId, onSelectDeputy, onAddClick }) {
  return (
    <div className="stories-bar">
      <div className="stories-scroll">
        <button className="story-item story-add" onClick={onAddClick}>
          <div className="story-add-ring">
            <span>+</span>
          </div>
          <span className="story-name">Adicionar</span>
        </button>

        {deputies.map((dep) => {
          const displayName = dep.nome || dep.name || "??";
          return (
            <button
              key={dep.id}
              className={`story-item ${activeDeputyId === dep.id ? "story-active" : ""}`}
              onClick={() => onSelectDeputy(dep.id)}
            >
              <Avatar name={displayName} size={56} />
              <span className="story-name">{displayName.split(" ")[0]}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}

export default StoriesBar;
