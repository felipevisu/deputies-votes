const PARTY_COLORS = {
  PSD: "#1e88e5",
  PT: "#d32f2f",
  MDB: "#f9a825",
  PL: "#1565c0",
  PSOL: "#7b1fa2",
  PP: "#2e7d32",
  PSDB: "#0277bd",
  REPUBLICANOS: "#00838f",
  PDT: "#c62828",
  "UNIÃO": "#e65100",
};

const deputies = [
  { id: 1, name: "Ana Souza", party: "PSD", state: "SP", following: true },
  { id: 2, name: "Carlos Mendes", party: "PT", state: "RJ", following: true },
  { id: 3, name: "Fernanda Lima", party: "MDB", state: "MG", following: false },
  { id: 4, name: "Roberto Alves", party: "PL", state: "BA", following: true },
  { id: 5, name: "Juliana Costa", party: "PSOL", state: "RS", following: false },
  { id: 6, name: "Marcos Oliveira", party: "PP", state: "PR", following: true },
  { id: 7, name: "Patricia Santos", party: "PSDB", state: "CE", following: false },
  { id: 8, name: "Eduardo Silva", party: "REPUBLICANOS", state: "PE", following: true },
  { id: 9, name: "Lucia Ferreira", party: "PDT", state: "SC", following: false },
  { id: 10, name: "Ricardo Barbosa", party: "UNIÃO", state: "GO", following: false },
];

export { PARTY_COLORS };
export default deputies;
