# Diretrizes de Engenharia e Roteamento de Modelos (Google Antigravity)

## 0. Protocolo Obrigatório de Parada e Alerta (Gatekeeper)
**REGRA CRÍTICA:** Antes de ler extensivamente o repositório, editar arquivos ou executar comandos no terminal, avalie a complexidade da solicitação do usuário. **Você DEVE PARAR IMEDIATAMENTE e emitir um alerta claro** pedindo confirmação antes de agir sempre que detectar qualquer um dos cenários abaixo:

1. **Troca de Modelo para Gemini 4 Carbon + `/boost`:**
   - **Gatilhos:** Bugs intermitentes, *race conditions*, problemas de concorrência/threads, vazamento de memória, gargalos graves de performance, falhas sem stacktrace óbvio ou erros que persistiram após uma primeira tentativa de correção.
   - **Ação exigida:** Não tente adivinhar nem aplicar correções rápidas. Pare e responda:
     > ⚠️ **Alerta de Complexidade (Debug Profundo):** Esta demanda envolve investigação crítica. Recomendo que você execute `/compact`, altere o seletor de modelo para **Gemini 4 Carbon** e rode **`/boost`** antes de prosseguirmos. Deseja trocar agora ou continuar com o modelo atual?

2. **Troca de Modelo para Gemini 4 Carbon + `/plan`:**
   - **Gatilhos:** Criação de arquitetura inicial de projetos, refatorações estruturais que impactem mais de 3 arquivos/camadas, migrações de banco de dados ou alterações em regras centrais de negócio.
   - **Ação exigida:** Não edite arquivos diretamente. Pare e responda:
     > ⚠️ **Alerta de Escopo (Arquitetura / Multi-arquivo):** Esta alteração impacta múltiplas camadas do sistema. Recomendo selecionar o **Gemini 4 Carbon** (ou **Gemini 4 Neon** em nível `xhigh`) e acionar **`/plan`** para revisarmos a estratégia antes de alterar o código. Como prefere seguir?

3. **Uso de `/grill-me` antes de `/plan` ou `/goal`:**
   - **Gatilhos:** Requisitos vagos, regras de negócio com lacunas ou múltiplas interpretações possíveis.
   - **Ação exigida:** Pare e sugira rodar **`/grill-me`** para alinhar os requisitos antes de gerar qualquer plano ou código.

---

## 1. Roteamento de Modelos e Comandos (Google AI Pro)
Respeite esta matriz para otimizar precisão e preservação de cota:

- **Gemini 4 Carbon (Fronteira / Raciocínio Profundo):**
  - **Uso:** Arquitetura inicial de projetos complexos (`/plan`), refatorações estruturais amplas e bugs críticos (`/boost`).
  - **Regra:** Antes de editar qualquer arquivo em um bug complexo, formule a hipótese causal e escreva um teste que reproduza o erro.

- **Gemini 4 Neon (Modelo Padrão / Principal):**
  - **Uso:** Desenvolvimento diário (`/goal`), entrevistas de requisitos (`/grill-me`), planejamento de features médias (`/plan`), fases **Red** e **Refactor** do TDD, revisão de código e documentação técnica (`README`, ADRs, contratos de API).
  - **Nível de raciocínio:** Use `medium`/`high` no fluxo contínuo e `xhigh` para regras de negócio sensíveis ou integrações complexas.

- **Gemini 4 Argon (Precisão Factual / Legado Intermediário):**
  - **Uso:** Apenas como alternativa secundária para revisão documental estrita baseada no código existente.

- **Gemini 4 Hydrogen (Executor Ultrarrápido):**
  - **Uso:** Subagentes em paralelo (`/swarm`), fase **Green** do TDD (implementação mínima para o teste passar), geração em lote de Javadocs/docstrings, dúvidas laterais (`/btw`) e compactação de contexto (`/compact`).

- **Gemini 3.8 Flash (Fallback Econômico):**
  - **Uso:** Tarefas mecânicas simples, formatação de arquivos, resumos rápidos e checagens pontuais para preservar a cota dos modelos Gemini 4.

---

## 2. Regras de Execução e Fluxo de Trabalho
1. **Fluxo Padrão:** Nunca inicie demandas médias ou grandes direto com `/goal`. Siga sempre: `/grill-me` (fechar lacunas) → `/plan` (revisar arquitetura e arquivos afetados) → `/goal` (executar e validar).
2. **Rigor no TDD (Red → Green → Refactor):**
   - **Proibido burlar testes:** Nunca apague, comente ou enfraqueça asserções de um teste falhando para forçar o status verde.
   - **Red:** Escreva o teste primeiro, rode-o e comprove que ele falha pelo motivo esperado.
   - **Green:** Escreva apenas o código necessário para passar no teste.
   - **Refactor:** Limpe duplicações e melhore a legibilidade mantendo a suíte 100% verde.
3. **Documentação Fiel ao Código:** Ao gerar ou atualizar documentação, inspecione os arquivos reais antes de escrever. Cite classes, rotas, parâmetros e comportamentos verificados no repositório; nunca invente endpoints ou configurações.
4. **Higiene de Contexto:** Sugira `/compact` sempre que uma funcionalidade for concluída ou antes de iniciar um tópico não relacionado, mantendo a janela de contexto enxuta.