import React from "react";

function Header({ onOpenFollow }) {
  return (
    <header className="header">
      <div className="header-content">
        <div className="header-logo">
          <span className="header-icon">👁</span>
          <h1>de olho neles</h1>
        </div>
        <button className="header-btn" onClick={onOpenFollow} title="Seguir deputados">
          +
        </button>
      </div>
    </header>
  );
}

export default Header;
