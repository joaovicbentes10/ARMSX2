# ARMSX2 Névoa — Android Parte 2

## Objetivo

Levar a identidade “PS2 mais moderno” da abertura e da biblioteca para configurações e telas auxiliares, preservando todos os controles e a navegação por toque e gamepad. O núcleo do emulador continua intacto.

## Estado de partida

- Branch `nevoa`, base upstream `90126bdfea`; Parte 1 compilada com sucesso no GitHub Actions no commit `38a217bf6d`.
- A abertura, o fundo animado, a biblioteca e a transição para o jogo já estão implementados.
- As configurações ainda exibem 12 seções num carrossel único. Elas usam `SettingsCategory`, `SettingsScreen`, `SettingsSearchIndex` e componentes compartilhados que também aparecem nas telas de BIOS, memory cards, saves e controles.
- O seletor de tema antigo continua visível, embora `MaterialTheme` use a paleta Névoa. `ThemeBridge.scheme` ainda recebe o esquema antigo.

## Direção visual

Fundo escuro `#050812`, névoa azul suave, torres finas e translúcidas, linhas de luz discretas, painéis de vidro e tipografia clara e espaçada. Nas telas de trabalho, o fundo fica mais calmo que na biblioteca para manter leitura e desempenho. Nenhum asset da Sony é incorporado.

Prioridade visual do usuário: reduzir a quantidade de informação simultânea e abandonar a aparência de interface genérica. Mostrar primeiro as ações e valores essenciais; usar hierarquia, espaços vazios e ícones de uma família coerente. Detalhes e destinos secundários ficam recolhidos, sempre acessíveis.

## Sequência de implementação

### 1. Base compartilhada

- Aplicar o fundo Névoa calmo a `ArmsBackdrop`, respeitando um `backgroundLayer` explícito.
- Adaptar `ArmsTopBar`, `GlassPanel` e controles compartilhados para superfícies translúcidas, bordas finas e foco visível, sem usar sombras coloridas de `Modifier.shadow`.
- Reduzir a informação visível por padrão: linhas mostram nome e controle; descrições longas ficam no ícone de informação já existente. Manter todas as ações e valores.
- Fazer `ThemeBridge.scheme` usar a mesma paleta Névoa da tela principal e ocultar o seletor de tema antigo no app Névoa.
- Verificar telas globais e por jogo, foco por controle, contraste e rolagem em paisagem.

### 2. Configurações em grupos

Manter `SettingsCategory` e cada tela de opções existentes para preservar rotas, busca, reset e escopo global/por jogo. A interface mostra grupos principais e, dentro de cada grupo, as seções originais:

| Grupo proposto | Seções existentes |
|---|---|
| Sistema | Geral, Informações do jogo, Desempenho, Avançado |
| Imagem e som | Gráficos, Áudio |
| Controle | Controles, Atalhos, Skins, Controles na tela |
| Conexões e extras | Rede, Patches |

Exibir somente seções válidas para o escopo atual. Um resultado de busca deve abrir diretamente o grupo e a seção corretos. L1/R1 percorre as seções visíveis; D-pad e toque alcançam grupos, seções e controles. Nenhuma opção é removida.

### 3. Demais telas

Restilizar primeiro o menu lateral, trocando emojis por ícones consistentes e recolhendo destinos secundários. Depois, verificar nessa ordem: BIOS e memory cards; saves; controles; patches e texturas; conquistas; menu durante o jogo e controles de toque. Reutilizar ViewModels e ações existentes. Cada tela deve passar por verificação visual e de navegação antes da próxima.

Implementados em código: menu lateral, BIOS, cartões de memória, estado vazio de saves, gerenciador de controles, patches, texturas e a entrada de conquistas. Os detalhes das listas foram colocados sob demanda. O APK do commit `7c71555d6c` passou no CI e foi instalado no F6 Pro; a inspeção confirmou abas inteiras em Controles, estados vazios compactos em Patches e Texturas e o login centralizado em Retroconquistas. O APK do commit `c51ef39430` também passou no CI e foi instalado no F6 Pro. O menu durante o jogo mostra Continuar, Avanço rápido, Salvar e Carregar; ações técnicas, opções de exibição, slots e detalhes das conquistas abrem sob demanda. Foram verificados visualmente os rótulos, a navegação entre abas por direcional, expansão das seções, retorno ao jogo e fechamento da BIOS. A conta de RetroAchievements autenticada e um jogo real ainda não foram usados nessa inspeção.

### 4. Biblioteca

Revisar a composição dos botões superiores e do painel para reduzir elementos com aparência genérica, preservando a névoa e as torres aprovadas. Integrar busca, ordenação e categorias ao visual Névoa, hoje acessíveis pela biblioteca clássica. Mostrar os saves do memory card no painel do jogo quando houver dados confiáveis para aquele título.

Primeiro ajuste aplicado em código: Menu, Biblioteca completa e Configurar viraram ações tipográficas discretas; Jogar permanece como ação principal. A indicação redundante de X no rodapé e o texto “Nunca jogado” foram retirados. Busca, ordenação, categorias e saves por jogo continuam pendentes.

### 5. Validação

- Build: `:app:testGithubDebugUnitTest` e `:app:assembleGithubRelease` no workflow Névoa.
- Instalar o APK no POCO F6 Pro por ADB e registrar screenshots/trechos de vídeo das telas modificadas.
- Conferir toque, gamepad, busca, reset, escopo por jogo, reabertura de telas e retorno ao jogo.
- Confirmar que o fundo para durante o jogo e que nenhuma opção anterior desapareceu.

## Primeiro incremento

Implementar a base compartilhada em um commit próprio, gerar APK e avaliar no F6 Pro antes de alterar a hierarquia de configurações. Assim, ajustes de cor, transparência e legibilidade podem ser feitos sobre telas reais.
