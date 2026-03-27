import React from "react";

const GRADIENTS = [
  "linear-gradient(135deg, #667eea, #764ba2)",
  "linear-gradient(135deg, #f093fb, #f5576c)",
  "linear-gradient(135deg, #4facfe, #00f2fe)",
  "linear-gradient(135deg, #43e97b, #38f9d7)",
  "linear-gradient(135deg, #fa709a, #fee140)",
  "linear-gradient(135deg, #a18cd1, #fbc2eb)",
  "linear-gradient(135deg, #fccb90, #d57eeb)",
  "linear-gradient(135deg, #84fab0, #8fd3f4)",
  "linear-gradient(135deg, #f6d365, #fda085)",
  "linear-gradient(135deg, #96fbc4, #f9f586)",
];

function Avatar({ name, size = 40 }) {
  const initials = name
    .split(" ")
    .filter((p) => p.length > 2)
    .slice(0, 2)
    .map((p) => p[0])
    .join("");

  const colorIndex =
    name.split("").reduce((acc, c) => acc + c.charCodeAt(0), 0) % GRADIENTS.length;

  return (
    <div
      className="avatar"
      style={{
        width: size,
        height: size,
        background: GRADIENTS[colorIndex],
        fontSize: size * 0.36,
      }}
    >
      {initials}
    </div>
  );
}

export default Avatar;
