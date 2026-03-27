const projects = [
  {
    id: 1,
    title: "PL 1234/2026 - Reforma Tributária",
    summary:
      "Simplifica o sistema tributário brasileiro, unificando cinco impostos em um único Imposto sobre Bens e Serviços (IBS). Prevê período de transição de 7 anos e cashback para famílias de baixa renda.",
    author: "Comissão Especial da Câmara",
    category: "Economia",
    voteDate: "2026-03-25",
  },
  {
    id: 2,
    title: "PL 567/2026 - Marco da Inteligência Artificial",
    summary:
      "Estabelece princípios, direitos e deveres para o uso de inteligência artificial no Brasil. Cria sistema de classificação de risco e exige transparência algorítmica em decisões automatizadas.",
    author: "Dep. Fernanda Lima (MDB-MG)",
    category: "Tecnologia",
    voteDate: "2026-03-24",
  },
  {
    id: 3,
    title: "PL 890/2026 - Piso Salarial da Enfermagem",
    summary:
      "Reajusta o piso salarial dos profissionais de enfermagem para R$ 5.500 e define jornada máxima de 30 horas semanais. Inclui técnicos e auxiliares com pisos proporcionais.",
    author: "Dep. Carlos Mendes (PT-RJ)",
    category: "Saúde",
    voteDate: "2026-03-23",
  },
  {
    id: 4,
    title: "PEC 45/2026 - Segurança Pública",
    summary:
      "Propõe a constitucionalização do Sistema Único de Segurança Pública (SUSP), com integração das forças policiais federais, estaduais e municipais e criação de fundo nacional.",
    author: "Dep. Roberto Alves (PL-BA)",
    category: "Segurança",
    voteDate: "2026-03-22",
  },
  {
    id: 5,
    title: "PL 2345/2026 - Programa Escola Digital",
    summary:
      "Universaliza o acesso à internet banda larga em todas as escolas públicas do país e cria programa de distribuição de tablets para alunos do ensino fundamental e médio.",
    author: "Dep. Juliana Costa (PSOL-RS)",
    category: "Educação",
    voteDate: "2026-03-21",
  },
  {
    id: 6,
    title: "PL 678/2026 - Proteção de Dados de Menores",
    summary:
      "Proíbe a coleta de dados pessoais de menores de 16 anos por plataformas digitais sem consentimento expresso dos responsáveis. Prevê multas de até 2% do faturamento.",
    author: "Dep. Patricia Santos (PSDB-CE)",
    category: "Tecnologia",
    voteDate: "2026-03-20",
  },
  {
    id: 7,
    title: "PL 3456/2026 - Energia Renovável",
    summary:
      "Cria incentivos fiscais para empresas que investirem em energia solar e eólica. Reduz o ICMS sobre equipamentos de geração distribuída e facilita financiamento para micro e pequenas empresas.",
    author: "Dep. Marcos Oliveira (PP-PR)",
    category: "Meio Ambiente",
    voteDate: "2026-03-19",
  },
  {
    id: 8,
    title: "PL 4567/2026 - Combate às Fake News",
    summary:
      "Obriga plataformas digitais a identificar e rotular conteúdos gerados por IA, cria mecanismos de rastreabilidade de mensagens virais e estabelece responsabilidade das plataformas.",
    author: "Dep. Ana Souza (PSD-SP)",
    category: "Comunicação",
    voteDate: "2026-03-18",
  },
  {
    id: 9,
    title: "PL 5678/2026 - Moradia Popular",
    summary:
      "Amplia o programa Minha Casa Minha Vida com nova faixa de renda e subsídios maiores. Inclui obrigatoriedade de áreas verdes e equipamentos comunitários nos empreendimentos.",
    author: "Dep. Eduardo Silva (REPUBLICANOS-PE)",
    category: "Habitação",
    voteDate: "2026-03-17",
  },
  {
    id: 10,
    title: "PL 6789/2026 - Transporte Público Gratuito",
    summary:
      "Institui a tarifa zero no transporte público municipal para estudantes, idosos e pessoas com deficiência. Cria fundo federal para compensar as empresas operadoras.",
    author: "Dep. Lucia Ferreira (PDT-SC)",
    category: "Transporte",
    voteDate: "2026-03-16",
  },
  {
    id: 11,
    title: "PL 7890/2026 - Telemedicina",
    summary:
      "Regulamenta a prática da telemedicina no Brasil, permitindo consultas, diagnósticos e prescrições remotas. Estabelece requisitos de segurança digital e proteção de dados dos pacientes.",
    author: "Dep. Ricardo Barbosa (UNIÃO-GO)",
    category: "Saúde",
    voteDate: "2026-03-15",
  },
  {
    id: 12,
    title: "PEC 12/2026 - Licença Paternidade",
    summary:
      "Amplia a licença paternidade de 5 para 30 dias, com possibilidade de extensão para 60 dias em empresas do programa Empresa Cidadã. Inclui pais adotivos e homoafetivos.",
    author: "Dep. Juliana Costa (PSOL-RS)",
    category: "Trabalho",
    voteDate: "2026-03-14",
  },
  {
    id: 13,
    title: "PL 9012/2026 - Agrotóxicos",
    summary:
      "Endurece as regras para registro e uso de agrotóxicos no Brasil. Proíbe substâncias banidas na União Europeia e cria sistema de rastreabilidade do campo à mesa.",
    author: "Dep. Fernanda Lima (MDB-MG)",
    category: "Meio Ambiente",
    voteDate: "2026-03-13",
  },
  {
    id: 14,
    title: "PL 1357/2026 - Startup Brasil",
    summary:
      "Cria regime tributário simplificado para startups com faturamento de até R$ 16 milhões. Facilita contratação de estrangeiros qualificados e cria visto especial para empreendedores.",
    author: "Dep. Ana Souza (PSD-SP)",
    category: "Economia",
    voteDate: "2026-03-12",
  },
  {
    id: 15,
    title: "PL 2468/2026 - Saúde Mental nas Escolas",
    summary:
      "Torna obrigatória a presença de psicólogos em todas as escolas públicas do país. Cria programa de prevenção ao bullying e suicídio com capacitação de professores.",
    author: "Dep. Carlos Mendes (PT-RJ)",
    category: "Educação",
    voteDate: "2026-03-11",
  },
];

function generateVote() {
  const options = ["SIM", "NÃO", "ABSTENÇÃO", "AUSENTE"];
  const weights = [0.45, 0.35, 0.1, 0.1];
  const rand = Math.random();
  let cumulative = 0;
  for (let i = 0; i < options.length; i++) {
    cumulative += weights[i];
    if (rand < cumulative) return options[i];
  }
  return options[0];
}

function generateVotes(deputies) {
  const votes = [];
  let id = 1;

  for (const project of projects) {
    for (const dep of deputies) {
      votes.push({
        id: id++,
        projectId: project.id,
        deputyId: dep.id,
        vote: generateVote(),
        project,
      });
    }
  }

  return votes;
}

export { projects, generateVotes };
