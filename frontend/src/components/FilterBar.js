import React from "react";

const CATEGORIES = [
  { name: "Todos", emoji: "🔥" },
  { name: "Economia", emoji: "💰" },
  { name: "Tecnologia", emoji: "💻" },
  { name: "Saúde", emoji: "🏥" },
  { name: "Segurança", emoji: "🚔" },
  { name: "Educação", emoji: "📚" },
  { name: "Meio Ambiente", emoji: "🌱" },
  { name: "Comunicação", emoji: "📡" },
  { name: "Habitação", emoji: "🏠" },
  { name: "Transporte", emoji: "🚌" },
  { name: "Trabalho", emoji: "💼" },
  { name: "Plenário", emoji: "⚖️" },
  { name: "Justiça", emoji: "⚖️" },
  { name: "Legislativo", emoji: "📜" },
];

function FilterBar({ activeFilter, onFilter }) {
  return (
    <div className="filter-bar">
      <div className="filter-scroll">
        {CATEGORIES.map((cat) => (
          <button
            key={cat.name}
            className={`filter-chip ${activeFilter === cat.name ? "active" : ""}`}
            onClick={() => onFilter(cat.name)}
          >
            <span className="filter-emoji">{cat.emoji}</span>
            {cat.name}
          </button>
        ))}
      </div>
    </div>
  );
}

export default FilterBar;
