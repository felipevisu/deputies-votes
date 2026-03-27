// API utility for Câmara dos Deputados
const BASE_URL = "https://dadosabertos.camara.leg.br/api/v2";

export async function fetchDeputies(params = {}) {
  const url = new URL(BASE_URL + "/deputados");
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null)
      url.searchParams.append(key, value);
  });
  const res = await fetch(url);
  if (!res.ok) throw new Error("Failed to fetch deputies");
  const data = await res.json();
  return data.dados;
}

export async function fetchDeputyVotes(voteId) {
  // Fetches all votes for a voting session (not by deputy directly)
  const url = `${BASE_URL}/votacoes/${voteId}/votos`;
  const res = await fetch(url);
  if (!res.ok) throw new Error("Failed to fetch votes for voting session");
  const data = await res.json();
  return data.dados;
}

export async function fetchVotes(params = {}) {
  const url = new URL(BASE_URL + "/votacoes");
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null)
      url.searchParams.append(key, value);
  });
  const res = await fetch(url);
  if (!res.ok) throw new Error("Failed to fetch votes");
  const data = await res.json();
  return data.dados;
}

export async function fetchVoteDetails(voteId) {
  const url = `${BASE_URL}/votacoes/${voteId}`;
  const res = await fetch(url);
  if (!res.ok) throw new Error("Failed to fetch vote details");
  const data = await res.json();
  return data.dados;
}
