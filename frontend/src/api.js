const API_BASE = process.env.REACT_APP_API_BASE || "";

export async function fetchDeputies(page = 0, size = 100) {
  const res = await fetch(`${API_BASE}/deputies?page=${page}&size=${size}`);
  if (!res.ok) throw new Error("Failed to fetch deputies");
  return res.json();
}

export async function fetchFeed(deputyIds, page = 0, size = 10) {
  const res = await fetch(`${API_BASE}/feed/deputies?page=${page}&size=${size}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ deputyIds }),
  });
  if (!res.ok) throw new Error("Failed to fetch feed");
  return res.json();
}

export async function fetchProposalFeed(deputyIds, page = 0, size = 10) {
  const res = await fetch(`${API_BASE}/feed/proposals?page=${page}&size=${size}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ deputyIds }),
  });
  if (!res.ok) throw new Error("Failed to fetch proposal feed");
  return res.json();
}
