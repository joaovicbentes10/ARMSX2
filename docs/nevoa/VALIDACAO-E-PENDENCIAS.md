# Validação e pendências

[Índice do Névoa](../NEVOA.md)

Referência: 24/10/2026. Este registro delimita a evidência disponível; implementação não significa teste de todos os caminhos.

## Situação por área

| Área | Evidência | Limite atual |
|---|---|---|
| Abertura e passagem | Implementadas e passagem aprovada no F6 Pro no histórico | Revalidar após alterar cena, vídeo ou tema inicial |
| Fundo e tema | AGSL, fallback, modo calmo e parada do relógio implementados | Falha real do shader e aparelhos antigos não exercitados nesta rodada |
| Biblioteca visual | Capas, painel, seleção e destaque de Jogar conferidos no F6 Pro | Última inspeção com dois itens: BIOS e Scooby-Doo |
| Busca e ordem | `3222e96576`, release local instalada e inspecionada | Reordenação entre vários jogos ainda sem inspeção no aparelho |
| Configurações | Quatro grupos, descrições sob demanda e painel translúcido | Não equivale a retestar todas as opções globais e por jogo |
| BIOS, cartões e saves | Layout compacto e estados vazios implementados | Importação/exportação de todos os tipos não reexecutada nesta rodada |
| Controles, patches e texturas | APK `7c71555d6c` inspecionado conforme registro anterior | Controles de toque ainda pendentes de revisão |
| Conquistas | Entrada/login e detalhes recolhíveis inspecionados | Conta autenticada e jogo real não testados nesta rodada |
| Menu em jogo | `c51ef39430` verde no CI; abas, expansão, continuar e fechar conferidos | Inspeção com BIOS não comprova gameplay |
| Categorias na tela Névoa | Não integradas | Recursos existentes na biblioteca clássica |
| Saves no painel do jogo | Não implementados | Exigir associação confiável ao título |
| Launcher Windows | Arquitetura planejada | Implementação não iniciada |

## Seletor animado, apresentação de início e pausa contida — 24/10/2026

O pedido incluiu animar a troca de jogo, dar continuidade visual ao início e retirar a aparência original/emojis da área de pausa marcada na captura.

- Capa, brilho e borda animam em 320 ms. Os painéis antigo e novo usam fade e expansão/recolhimento horizontal. O espaçamento entre capa e painel faz parte da largura animada, evitando um salto quando o painel entra ou sai. As ações do painel que está saindo ficam inativas e perdem o ID de controle.
- A partida passa a usar uma apresentação de 900 ms: a capa parte do retângulo medido no clique, cresce e se desloca até o centro sobre a cena escurecida; o título aparece e o conjunto fecha em preto antes da emulação. A origem é congelada no clique para não acompanhar mudanças posteriores do layout. O bloqueio de início duplicado e a cobertura do carregamento foram mantidos.
- Menu em paisagem limitado a 44% da largura, com máximo de 520 dp, e 78% da altura do contêiner. Scrim externo reduzido e abas menores. Continuar/Avanço rápido ficam na mesma linha; Salvar/Carregar na seguinte. Sem capa, relógio, bateria ou CRC no cabeçalho principal.
- Ações e amigos usam vetores Line Awesome, incluindo os atalhos das outras abas. Dados técnicos e presença continuam em Mais ações → Detalhes do jogo. Hardcore continua visível quando aplicável. Seções da sessão usam cabeçalhos compactos com seta vetorial.

Validação no F6 Pro: troca de seleção por arrasto nos dois sentidos e início do Scooby gravados; apresentação da capa inspecionada. Menu conferido em paisagem, incluindo contraste de Salvar/Carregar, abas e ações principais. Direcional alternou para Renderizador e retornou à Sessão. Mais ações e Detalhes abriram; serial SLUS-21928 e CRC 38E42973 foram conferidos. Continuar voltou ao jogo e o menu reabriu. Não foi feita escrita/carregamento de saves nem teste autenticado de amigos/conquistas nesta rodada.

Evidências locais não versionadas: `.superpowers/validation/selection-launch-menu/`, incluindo `selection-launch.mp4`, `motion-preview.mp4`, `launch-contact.png` e `menu.png`. A gravação final `selection-launch-final.mp4` e a prévia foram atualizadas após o ajuste do espaçamento; a instalação final foi conferida no aparelho. Release local assinado compilado e 46 testes Névoa passaram. Nenhum código nativo ou preset de renderizador foi alterado neste incremento. O print de menu corresponde à revisão compacta final, após corrigir contraste e espaçamento dos grupos.

## Paisagem na partida, seleção por gesto e áudio V6 — 24/10/2026

Implementação:

- O modo de orientação 0, antes Dispositivo, passa a significar Padrão (paisagem) no Névoa. O valor legado já salvo não libera mais a trava horizontal quando o runtime assume a tela. As opções explícitas Retrato e rotação automática permanecem disponíveis.
- A fileira principal trata arrastos horizontais com limiar de 48 dp: esquerda avança, direita volta, um item por gesto. Movimentos curtos e cancelados não alteram a seleção; arrastar não inicia o jogo. A lista acompanha a seleção com rolagem animada.
- Áudio padrão substituído pelo arquivo fornecido `ARMSX2_Nevoa_Suspense_V6.mp3`, copiado sem alteração. O MP3 e a prévia de referência `Nevoa_Previa_2_Ritmo_V6_Inicio_Suave.mp4` têm 8 s. O vídeo foi usado como referência, sem substituir a abertura por uma gravação de tela com rotação/barras.
- O círculo branco e as partículas da antiga explosão de Jogar foram substituídos por uma dissolução de 520 ms: escurecimento da cena, véu azul discreto e quadro final preto. A cobertura permanece até o runtime assumir a tela.

Validação no F6 Pro, após instalar o release local assinado:

- Movimento curto preservou BIOS; arrasto à esquerda selecionou Scooby sem iniciar; à direita retornou à BIOS; novo arrasto à esquerda selecionou Scooby novamente.
- Partida do Scooby por Jogar gravada. Quatro amostras do WindowManager mostraram `SCREEN_ORIENTATION_SENSOR_LANDSCAPE`, `mLastOrientation=6`, display `3200x1440`; a captura durante a emulação também tem 3200x1440. A gravação mostra a partida horizontal, sem liberar a orientação para retrato.
- Inspeção dos quadros da transição confirmou a dissolução, sem círculo branco. Esta rodada valida a entrada, não gameplay prolongado.
- Hash do áudio incorporado igual ao V6 fornecido; log confirmou `Playback started: bundled`.
- **46 testes Névoa passaram**, incluindo 4 novos casos de gesto e 5 de orientação. Build release local concluído com núcleo pré-compilado; nenhum código nativo alterado.

Evidências locais não versionadas: `.superpowers/validation/v6-landscape-swipe/`, com `swipe-scooby.png`, `launch.mp4`, `landscape.png`, `orientation.txt`, `transition-contact.png` e `transition-preview.mp4`.

## Som padrão, biblioteca completa e falhas pretas — 24/10/2026

Implementado e instalado no F6 Pro:

- MP3 fornecido pelo usuário (`ARMSX2_Nevoa_Suspense_V4.mp3`, 8 s, 48 kHz estéreo) incluído como `res/raw/nevoa_intro_sound.mp3`, sem alterar os bytes. É o som padrão; o arquivo personalizado tem prioridade. Log `NevoaIntroSound: Playback started: bundled` confirmou o início da faixa padrão.
- Preparação assíncrona do player e liberação em `onStop`. A opção de reset agora restaura o som padrão.
- Biblioteca completa com fundo Névoa Compose, cabeçalho discreto e recentes compactos. Saiu o fundo XMB/Flurry independente; imagem de fundo personalizada continua disponível. O relógio do fundo pausa enquanto o protetor de tela o cobre, preservando a composição.
- Cobertura escura no intervalo entre o fim do burst e a inicialização assíncrona do runtime, para não reexibir a biblioteca nessa passagem.
- Banner visual de temperatura suprimido em BatteryWatcher, sem mudar os mecanismos térmicos nativos/Android ou o aviso de bateria baixa.
- Troca do nome da animação adiada por decisão do usuário; a marca atual foi mantida.

Compilação release local e 37 testes Névoa passaram. O núcleo nativo é o do APK-base já registrado; não houve recompilação ou alteração do núcleo para este incremento.

**Biblioteca:** gravação de 40 s, 1.204 quadros analisados; maior queda de luminância entre quadros de 0,0208 na escala 0–255, sem grandes áreas pretas no trecho inspecionado. Isso não comprova estabilidade de vários minutos nem cobre o wrap de 600 s.

**Scooby-Doo, SLUS-21928 / CRC 38E42973:** iniciado pela Biblioteca completa conforme pedido do usuário. O caso de blocos pretos foi reproduzido na tela de carregamento usando Vulkan com `Mesa 27.0.0-devel (git-axfl2-005)`, driver personalizado `armsx2turnip-axfl2-005-turnip-axfl2-005-android`. A imagem do jogo apresentou os blocos enquanto o overlay de controles permaneceu normal; não atribuir esse caso apenas ao fundo da biblioteca.

Comparação controlada: apenas o Scooby passou a ter overrides `renderer=vulkan` e `customDriverId=""`, usando o driver Vulkan Qualcomm do sistema. A configuração global permaneceu `renderer=auto` com o Turnip original. No novo vídeo de 35 s, o mesmo carregamento não apresentou o defeito nas amostras inspecionadas e o jogo avançou à animação inicial e ao título. Na região interna do carregamento, a queda instantânea máxima de luminância foi 73,47 com Turnip (550 quadros) e 0,13 com o driver do sistema (732 quadros). A análise fotométrica auxilia a comparação; não é um teste completo de compatibilidade nem substitui gameplay prolongado.

O ajuste por jogo ficou aplicado no aparelho. Não é um preset de driver imposto a todos os usuários pelo APK. Se o problema reaparecer com o driver do sistema, registrar a cena e o driver efetivo antes de ampliar a investigação.

Evidências locais em `.superpowers/validation/audio-library/`: `complete.png`, `idle.mp4`, `idle-contact.png`, `scooby-launch.mp4`, `scooby-artifact.png`, `scooby-system.mp4`, `scooby-system-contact.png` e `scooby-system.png`. A cópia das configurações anteriores e a de comparação ficam em `%TEMP%/nevoa-validation-search/`; são dados locais e não devem entrar no Git.

## Busca e ordenação

Código: `3222e9657688df82c128f72f6374f846a12be88f`. O APK release local usou o núcleo do APK verde `1190fcc467`; o SHA-256 de `libemucore_4k.so` no pacote final foi conferido e é idêntico. Foi instalado por cima com a chave estável.

Casos verificados no F6 Pro:

- Abrir a busca e digitar por toque.
- Consulta `p` encontra `PS2BIOS-USA-NTSC`.
- Consulta `pq` mostra o estado sem resultados sem recolocar o item pelos recentes.
- Limpar restaura a biblioteca.
- Alternar Título → Recentes → Compatibilidade → Título por toque.
- Direcional/A via ADB: alcançar Ordem, trocar para Recentes, abrir busca e inserir um caractere.
- Inspeção visual da composição normal, com consulta e sem resultados.

Teclas via ADB exercitam o caminho de entrada do app, mas não substituem um gamepad físico, seu mapeamento e eixos analógicos. Com um único item, observa-se a troca da opção de ordem, mas não mudanças de posição entre jogos.

Capturas locais não versionadas: `.superpowers/validation/search-sort/`, incluindo `library.png`, `match.png` e `no-results.png`. Elas não acompanham automaticamente um clone do repositório.

O [CI de `3222e96576`](https://github.com/joaovicbentes10/ARMSX2/actions/runs/40140942700) ainda estava em execução na consulta feita durante esta documentação. Consultar a execução para o resultado posterior; não atribuir sucesso ao job com base apenas na validação local.

## Testes automatizados

Base: `platforms/android/app/src/test/java/com/armsx2/nevoa/`.

| Classe | Casos na execução de referência | Área |
|---|---:|---|
| `ShaderClockTest` | 6 | Relógio e intervalo de atualização |
| `LaunchGateTest` | 6 | Início duplicado e reabertura |
| `LibraryLogicTest` | 19 | Seleção, ordem auxiliar, rótulos e fallback, incluindo busca vazia |
| `IntroSoundTest` | 4 | Validação de áudio |
| `NevoaPaletteTest` | 6 | Paleta e contraste |
| `NevoaOrientationTest` | 5 | Padrão antigo em paisagem e preservação de escolhas explícitas |
| **Total** | **46** | **Todos passaram localmente** |

Esses testes não comprovam renderização, reprodução real de mídia ou todo o runtime.

Na execução inicial de busca/ordenação, a suíte completa local teve **260 testes: 249 passaram, 2 falharam e 9 foram ignorados**. As falhas foram fora do incremento:

| Teste | Evidência e motivo identificado |
|---|---|
| `TexturePackInstallerTest.aFileThatWillNotDeleteStillTakesThePackOutOfUse` | Falha na preparação de permissões (`setWritable(false)`), dependente do sistema de arquivos no Windows |
| `LicenseDocsTest.theAndroidListEndsWithTheApacheLicense` | Falha no prefixo da licença WebRTC; o parser remove `\n` inicial e preserva `\r` do checkout CRLF |

Não foram feitas mudanças nesses módulos para contornar a suíte. Preservar as falhas no relato; CI Linux é evidência separada.

## Próximos incrementos

**Sincronismo do áudio da abertura:** alinhar o efeito sonoro final à passagem imediatamente anterior à exibição da lista de jogos. O som não deve ser cortado pela saída da abertura nem continuar perceptivelmente sobre a biblioteca. Validar a sequência audiovisual no F6 Pro; apenas ter vídeo e áudio com 8 s não comprova sincronismo. O usuário mencionou V5 nesta solicitação; o arquivo atualmente incorporado é V6. Confirmar a faixa pretendida antes de substituir o asset.

1. **Categorias:** integrar as existentes com apresentação discreta, filtro vazio recuperável e navegação por controle.
2. **Saves no painel:** usar associação confiável ao jogo, sem inferir apenas pelo nome de arquivo.
3. **Controles de toque:** revisar apresentação, acesso às opções e interação com jogo real.
4. **Validação adicional:** vários jogos, gameplay, conta RetroAchievements autenticada e gamepad físico.
5. **Ajustes menores:** tratar os pontos técnicos abaixo em incrementos proporcionais.

Executar uma função por vez, ou duas quando uma for simples. Não há data de entrega definida.

## Pontos conhecidos

| Item | Situação |
|---|---|
| Partículas discretas na resolução nativa | Avaliar tamanho antes de aumentar custo do shader |
| Wrap do tempo a cada 600 s | Continuidade de longo prazo pendente |
| Cadência do fundo | Intervalo atual é 30 ms; medir antes de prometer fps fixo |
| MIME `application/ogg` | Rejeitado pela validação `audio/*` |
| Preparação do áudio | Passou a `prepareAsync()` no incremento de som padrão; validar reprodução no aparelho |
| Ciclo do som | Liberação em conclusão, destruição e `onStop` implementada; retomada completa após interrupção ainda merece teste específico |
| Áudio com vídeo próprio | Linha pode continuar visível, embora só toque com o vídeo padrão |
| Recriação durante o burst | Estado pendente não é integralmente preservado |
| Textos e tema legados | Textos Névoa fixos fora de I18n e pontos antigos de tema a limpar |

O atalho Y/△ tinha registro de recuperação de foco insuficiente após a gaveta. Em `3222e96576`, o pedido de foco passou a reagir à gaveta e ao teclado. Retestar Y/△ nessas transições antes de encerrar o item; mudança no código não é comprovação de resolução.

O aviso de temperatura foi primeiro centralizado e, depois, removido por solicitação do usuário; o aviso de bateria baixa permanece. A importação do chip de status foi preservada após ajuste. São histórico de correção, não pendências atuais confirmadas.

## Windows

Planejado: launcher separado com a mesma identidade, suporte a controle e abertura do PCSX2 oficial em tela cheia. Não criar um fork Windows do emulador para esse objetivo. Tecnologia, descoberta da instalação, argumentos e empacotamento precisam ser definidos e verificados ao iniciar a implementação.

## Manter o registro

Anotar commit, origem e assinatura do APK, ambiente, testes, capturas e caminhos não exercitados. Preservar resultados anteriores como histórico e corrigir o estado quando houver nova evidência.
