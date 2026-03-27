import React from "react";

const VOTE_CONFIG = {
  SIM: { className: "vote-yes", label: "Votou SIM" },
  "NÃO": { className: "vote-no", label: "Votou NÃO" },
  "ABSTENÇÃO": { className: "vote-abstain", label: "Se absteve" },
  AUSENTE: { className: "vote-absent", label: "Faltou" },
};

function VoteBadge({ vote }) {
  const { className, label } = VOTE_CONFIG[vote] || VOTE_CONFIG["AUSENTE"];

  return (
    <span className={`vote-badge ${className}`}>
      {label}
    </span>
  );
}

export default VoteBadge;
