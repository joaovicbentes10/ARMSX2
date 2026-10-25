# Validação e pendências

## Encerramento Android — 25/10/2026

**Implementações desta rodada:** estados no painel do jogo, lista de slots/datas e Continuar, com recibo após sucesso nativo e vínculo à URI da imagem aberta. SHA-256 do estado é conferido antes da transição; serial/CRC do disco e caminho exato do slot são confirmados após o boot. Estados antigos exigem carregamento manual bem-sucedido para associação. Uma imagem substituída na mesma URI pode manter a oferta até o boot, mas a identidade nativa diferente bloqueia a carga.

Removido o wrap de 600 s do relógio do fundo; acrescentados casos na fronteira e em 24 h calculadas. Mantidos o intervalo de 30 ms, partículas discretas e o shader visual para preservar a passagem da abertura. Aceito MIME `application/ogg`; trilha substituta aparece somente quando o vídeo padrão está selecionado. Solicitação de partida preserva URI/slot na recriação e relê o recibo. Textos principais Névoa, teclado, editor e ações de estados foram localizados; menus de biblioteca perderam os emojis. Parser de licenças normaliza CRLF; teste de permissões Unix declara a condição de execução no Windows.

**Build final:** release assinado instalado por cima no Poco F6 Pro. SHA256 do APK: `1C842A15B06FD7234DDAC1684C1191D38C8999E8B79735A50C9F248B2EFB6760`. Suíte completa: **303 testes, 293 aprovados, 10 ignorados, nenhuma falha**; 79 casos no pacote Névoa. Build concluído em 2 min 4 s. Todas as 15 bibliotecas arm64 presentes no APK foram comparadas byte a byte por SHA-256 com a referência nativa local; nenhuma alteração de núcleo. Cristal V2 preserva o hash aprovado `2556e6a8539d8934c112c64d687eeece66dae4fd7d3d988ef6c175f036f772ef`; abertura V6 e título no impacto final não foram modificados.

**Poco:** ISO original de 56 KB gerada por `tools/nevoa-diagnostic/build_iso.py`, copiada para `/sdcard/Download/Ps2/Nevoa_Diagnostico.iso` e reconhecida como terceiro item. Boot exibiu o fundo azul pulsante previsto. Identidade nativa `NEVO-00001 / CD915601`; gravado slot 10 inicialmente vazio e conferido recibo com hash/tamanho/data reais. Após fechar, home mostrou Estados salvos (1), Continuar e lista com data/slot. Retomada pela lista carregou o estado (`Loaded state from slot 9`, índice interno).

Durante a transição, escala de fonte temporária de 1,0 para 1,05 provocou recriação real da Activity, confirmada pelo WindowManager. Uma única emissão LAUNCH e uma única chamada de partida foram registradas; a retomada carregou o slot correto. Escala original 1,0 restaurada, com nova recriação durante a emulação. Saída posterior retornou à home. O estado do diagnóstico não apareceu no Scooby, que mostrou zero estados associados. Nenhum estado comercial ou save de cartão foi criado ou substituído. A ISO e o estado de diagnóstico ficam no aparelho para repetir a conferência.

Scooby foi novamente iniciado: `SLUS-21928 / 38E42973`, execução do ELF confirmada e pausa/saída para a biblioteca. Isso não equivale a gameplay prolongado. Y/△ abriu a biblioteca após fechar a gaveta e após Concluir na busca; entrada ADB com fonte gamepad. Teclado PT-BR e menu Mais sem emojis inspecionados. Medição de aproximadamente 10,3 s na biblioteca registrou 622 quadros de UI e GPU mediana de 23 ms; esse total inclui o trabalho da interface e não comprova taxa fixa do relógio do shader.

Evidências locais ignoradas em `.superpowers/validation/final-pendencias/`: `diagnostic.png`, `states-home.png`, `states-list.png`, `continued.png`, `continue-emulog.txt`, `continue-logcat.txt`, `recreation.txt`, `scooby-emulog.txt`, `library-menu.png` e `gfxinfo.txt`. Os testes de tempo longo são calculados, sem alegar sessão física de 24 h. Não houve nova importação física de áudio/vídeo próprio nem troca de idioma nesta rodada; esses ajustes foram compilados e o MIME foi testado por unidade.

**Dependências externas restantes:** usuário informou não ter gamepad físico nem outro jogo. O diagnóstico comprova outro disco e o ciclo de estados, sem substituir compatibilidade com outro jogo comercial, multitoque amplo, skins próprias ou teste autenticado de RetroAchievements. O login deve ser feito pelo usuário no app. Essas validações ficam registradas como não executadas; não são implementações concluídas por simulação. Nome definitivo continua adiado por solicitação, e o launcher Windows permanece fora desta rodada Android.

[Índice do Névoa](../NEVOA.md)

Referência: 25/10/2026. Este registro delimita a evidência disponível; implementação não significa teste de todos os caminhos. As seções por rodada preservam o histórico; a situação atual está na tabela e no encerramento abaixo.

## Situação por área

| Área | Evidência | Limite atual |
|---|---|---|
| Abertura e passagem | Implementadas e passagem aprovada no F6 Pro no histórico | Revalidar após alterar cena, vídeo ou tema inicial |
| Fundo e tema | AGSL, fallback, modo calmo e parada do relógio implementados | Falha real do shader e aparelhos antigos não exercitados nesta rodada |
| Biblioteca visual | Capas, painel, seleção e destaque de Jogar conferidos no F6 Pro | Última inspeção com dois itens: BIOS e Scooby-Doo |
| Busca e ordem | `3222e96576`, release local instalada e inspecionada | Reordenação entre vários jogos ainda sem inspeção no aparelho |
| Configurações | Quatro grupos, descrições sob demanda e painel translúcido | Não equivale a retestar todas as opções globais e por jogo |
| BIOS, cartões e saves | Layout compacto e estados vazios implementados | Importação/exportação de todos os tipos não reexecutada nesta rodada |
| Controles, patches e texturas | APK `7c71555d6c` inspecionado conforme registro anterior | Arte padrão e editor revisados; ver rodada de controles de toque abaixo |
| Conquistas | Entrada/login e detalhes recolhíveis inspecionados | Conta autenticada e jogo real não testados nesta rodada |
| Menu em jogo | `c51ef39430` verde no CI; abas, expansão, continuar e fechar conferidos | Inspeção com BIOS não comprova gameplay |
| Categorias na tela Névoa | Integradas na home e nos três modos da biblioteca | Validação adicional com bibliotecas grandes e controle físico |
| Saves no painel do jogo | Painel, recibos e retomada implementados | Conferência do ciclo de gravação/retomada descrita no encerramento |
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
| `ShaderClockTest` | 9 | Relógio contínuo, fronteira de 600 s, sessão longa e intervalo de atualização |
| `LaunchGateTest` | 6 | Início duplicado e reabertura |
| `LibraryLogicTest` | 19 | Seleção, ordem auxiliar, rótulos e fallback, incluindo busca vazia |
| `IntroSoundTest` | 7 | Validação de áudio e MIME OGG |
| `NevoaPaletteTest` | 6 | Paleta e contraste |
| `NevoaOrientationTest` | 5 | Padrão antigo em paisagem e preservação de escolhas explícitas |
| `SaveSlotFilesTest` | 4 | Caminho candidato do JNI, arquivo existente, diretório e caminho vazio |
| `FaceButtonPositionsTest` | 5 | Losango em pixels, retrato/paisagem, posições próprias, invariantes e dimensões indisponíveis |
| `CategoryPoolTest` | 6 | Associação por chave, várias categorias, vazio válido, exclusão e atualização de membros |
| `StateAssociationTest` | 12 | Identidade nativa, URI confirmada, SHA-256, recibos, substituição e destino exato |
| **Total** | **79** | **Todos passaram localmente** |

Esses testes não comprovam renderização, reprodução real de mídia ou todo o runtime.

Na execução inicial de busca/ordenação, a suíte completa local teve **260 testes: 249 passaram, 2 falharam e 9 foram ignorados**. As falhas foram fora do incremento:

| Teste | Evidência e motivo identificado |
|---|---|
| `TexturePackInstallerTest.aFileThatWillNotDeleteStillTakesThePackOutOfUse` | Falha na preparação de permissões (`setWritable(false)`), dependente do sistema de arquivos no Windows |
| `LicenseDocsTest.theAndroidListEndsWithTheApacheLicense` | Falha no prefixo da licença WebRTC; o parser remove `\n` inicial e preserva `\r` do checkout CRLF |

Esse resultado é histórico. Na rodada de encerramento, a normalização CRLF do parser foi corrigida e o teste de permissões Unix passou a declarar sua limitação no Windows. A suíte completa posterior teve 303 testes: 293 aprovados, 10 ignorados e nenhuma falha. CI Linux é evidência separada.

## Próximos incrementos

**Sincronismo da abertura:** o usuário confirmou V6. A correção passa a incorporar a faixa no vídeo padrão, com relógio único e passagem à biblioteca na conclusão audiovisual. Resultado da conferência no aparelho registrado abaixo.

1. **Categorias:** integradas na rodada de 25/10/2026; manter validação adicional em bibliotecas grandes e controle físico.
2. **Saves no painel:** implementados com recibo após sucesso nativo e verificação de identidade/conteúdo; ver rodada de encerramento.
3. **Controles de toque:** arte padrão, acesso ao editor e losango uniforme implementados; ampliar validação para multitoque simultâneo, skins próprias, outros jogos e gameplay prolongado.
4. **Validação adicional:** vários jogos, gameplay, conta RetroAchievements autenticada e gamepad físico.
5. **Ajustes menores:** tratar os pontos técnicos abaixo em incrementos proporcionais.

Executar uma função por vez, ou duas quando uma for simples. Não há data de entrega definida.

## Pontos conhecidos

| Item | Situação |
|---|---|
| Partículas discretas na resolução nativa | Inspecionadas; mantido o tamanho discreto aprovado, sem aumentar custo |
| Wrap do tempo a cada 600 s | Removido; testes na fronteira e em sessão de 24 h, sem alegar espera física de 24 h |
| Cadência do fundo | Intervalo de 30 ms preservado; medição local registrada abaixo, sem prometer fps fixo |
| MIME `application/ogg` | Aceito, inclusive com variação de maiúsculas; testes adicionados |
| Preparação do áudio | V6 padrão incorporada no MP4; `prepareAsync()` fica para substituição personalizada |
| Ciclo do som | Vídeo e som param ao sair; abertura interrompida é pulada ao voltar |
| Áudio com vídeo próprio | Controles de trilha substituta ocultos quando há vídeo próprio |
| Recriação durante o burst | URI/slot preservados; prova do estado relida, sem repetir o som |
| Textos e tema legados | Textos principais Névoa, teclado e editor localizados; menus de biblioteca sem emojis |

O atalho Y/△ foi retestado no Poco após fechar a gaveta e após Concluir na busca. Nos dois casos abriu Biblioteca completa; eventos ADB com fonte gamepad, sem substituir controle físico.

O aviso de temperatura foi primeiro centralizado e, depois, removido por solicitação do usuário; o aviso de bateria baixa permanece. A importação do chip de status foi preservada após ajuste. São histórico de correção, não pendências atuais confirmadas.

## Windows

Planejado: launcher separado com a mesma identidade, suporte a controle e abertura do PCSX2 oficial em tela cheia. Não criar um fork Windows do emulador para esse objetivo. Tecnologia, descoberta da instalação, argumentos e empacotamento precisam ser definidos e verificados ao iniciar a implementação.

## Manter o registro

Anotar commit, origem e assinatura do APK, ambiente, testes, capturas e caminhos não exercitados. Preservar resultados anteriores como histórico e corrigir o estado quando houver nova evidência.

## Rodada de abas e estados — 24/10/2026

Abas internas usam cartões menores, preenchimentos discretos e seções recolhíveis compartilhadas (`NevoaSection`). Salvar/carregar usam o cabeçalho Névoa, ações em texto e duas linhas de slots de 120 dp. Opções automáticas ficam recolhidas após os slots.

Corrigida a indicação falsa de slot ocupado: `getGamePathSlot` fornece um nome candidato mesmo sem arquivo. `SaveSlotFiles` verifica a existência antes de mostrar conteúdo ou habilitar carregamento.

APK local assinado instalado no F6 Pro, com bibliotecas nativas reutilizadas do APK de referência (núcleo sem mudanças). Build e 50 testes Névoa passaram. Conferidos Salvar, Carregar, abertura da seção automática e aba gráfica no Scooby. Nenhum estado foi salvo, carregado, importado ou excluído; esses fluxos de escrita não foram exercitados. Capturas locais em `.superpowers/validation/internal-tabs-states/`. A revisão visual identificou um título legado escuro na aba gráfica; a cor de conteúdo da página foi explicitada e a captura `graphics-final.png` confirmou o título legível no APK final.

## Rodada de sincronismo V6 — 24/10/2026

O usuário confirmou a V6 instalada. `boot_intro.mp4` agora contém sua trilha em AAC a 192 kb/s. Áudio e vídeo começam em 0 e duram 8 s, reproduzidos pelo mesmo player. A biblioteca abre em `onCompletion`, após a conclusão audiovisual, sem interromper um segundo player da faixa padrão. Áudio personalizado continua substituindo a trilha (MP4 silenciado); vídeo personalizado conserva o próprio áudio.

O vídeo H.264 foi copiado sem recodificação; hash SHA256 do fluxo visual antes/depois: `8025d2b0646e286d8aa56f6e351af36d17b27a80de442bc70652b3f8a8ae45e3`. O MP3 original preserva os bytes da V6 fornecida. `render.mjs` inclui a faixa ao gerar novos vídeos; sintaxe verificada com `node --check`.

Build local release e 50 testes Névoa passaram. APK instalado no F6 Pro, SHA256 `1A76CFF0766678F637D6DEE2F7F1DB084C09692149A72C0DDFF1013ABC9331D5`, com assinatura do projeto e bibliotecas nativas reutilizadas da referência anterior. Nenhuma alteração no núcleo.

Na partida a frio, log `NevoaIntro` registrou início às 22:58:11.235 (`customVideo=false overrideSound=false duration=8000`) e conclusão audiovisual às 22:58:19.612, seguida da biblioteca. Gravação visual de passagem: `.superpowers/validation/intro-v6-sync/handoff.mp4`; o `screenrecord` usado não captura áudio, portanto a evidência sonora é a faixa muxada, o relógio comum e o término do player, sem alegar audição física do alto-falante. Teste Home durante a abertura e retorno trouxe a biblioteca sem reiniciar a faixa nem gerar erro AndroidRuntime; UI confirmou Menu, Biblioteca completa e Jogar. Conferência final da aba gráfica no Scooby também concluída.

Ao ir para segundo plano, a abertura pausa e remove o timeout; ao retornar, entra na biblioteca. Pular por toque/Voltar libera o vídeo e o som. Substituições de áudio/vídeo e erros de codec não foram novamente exercitados nesta rodada; a mudança de sincronismo automático se aplica ao MP4 padrão com V6.

## Título no impacto final — 24/10/2026

Por solicitação do usuário, somente o momento do ARMSX2 mudou. O ataque final da V6 ocorre em aproximadamente 7,0 s (aumento de amplitude entre 7,00 e 7,05 s). Antes, o título entrava de 4,8 a 6,0 s e saía de 6,25 a 7,2 s. Agora entra de 6,97 a 7,10 s, assenta até 7,15 s e dissolve de 7,50 a 7,95 s. Cores, composição, duração total e som permanecem.

A opção `render.mjs --audio-from` copia a trilha AAC do MP4 anterior sem recodificação. Hash dos pacotes AAC preservados: `adafd68e8ef898c829a5fb65e08a8c236a28ea83e275483a931a44a69e91bdde`. A checagem do quadro final reportou `maxDiff=1`, zero pixels acima de 3 e opacidade do título zero. Render local acelerado por D3D11; o script versionado mantém SwiftShader como padrão.

O MP4 final mantém 240 quadros e 8 s de vídeo, com 8 s e os mesmos 376 pacotes de áudio. A semelhança SSIM do trecho anterior à antiga entrada do título (0–4,7 s) foi 0,998644 após a nova codificação visual. Nenhuma mudança no player Android ou no MP3 original.

Build release concluído em 1 min 43 s; testes Névoa reutilizados pelo Gradle (`UP-TO-DATE`), sem nova lógica de aplicativo para testar. APK instalado no F6 Pro, SHA256 `DDC14437B310C17E49FC284139C5F5CD9569A0CAC021EF8B58865C98DC94E06F`, com bibliotecas nativas de referência e assinatura do projeto. A partida a frio registrou player padrão com duração 8000, seguida de conclusão audiovisual e biblioteca, sem erro AndroidRuntime. A gravação visual confirmou a entrada tardia do título e a passagem para os jogos. Evidência local em `.superpowers/validation/title-final-impact/`; `screenrecord` não contém áudio, e a prévia audiovisual é o próprio MP4 incorporado. Não houve navegação por outras telas nesta validação.

## Controles de toque e losango uniforme — 24/10/2026

Botões padrão substituídos por arte vetorial própria: contornos claros, preenchimento discreto e destaque azul ao pressionar; direcional sem cruzamento de bordas, analógicos com dois anéis, rótulos L1/L2/R1/R2 proporcionais. Pause usa a paleta Névoa. O editor tem painel de até 480 dp, opções adicionais recolhidas, contorno azul de seleção e acesso direto no início da aba Controles do menu em jogo. O pipeline de skins continua priorizando arquivos próprios; coordenadas, tamanhos, keycodes, gestos e perfis não foram redefinidos pela revisão visual.

A pedido do usuário, as posições padrão de triângulo, círculo, X e quadrado passaram a formar um losango regular em pixels. Corrige a distorção dos espaçamentos percentuais em telas largas: no Poco, a distância horizontal antiga era 192 px e a vertical 144 px; agora ambas ficam em aproximadamente 169,7 px. Preserva o centro (2752, 792 px), o espaçamento entre vizinhos e todos os campos além de x/y. Layouts com posições de face personalizadas ficam intactos. A correção atualiza o layout vivo compartilhado pelo desenho e hit testing, sem gravar um perfil automaticamente.

Build release passou; 55 testes Névoa passaram, incluindo 5 casos de geometria. O núcleo nativo permanece o das bibliotecas de referência usadas nas validações anteriores. Capturas e evidências desta rodada em `.superpowers/validation/touch-nevoa/`. O primeiro registro de arte antiga (`before.png`) foi capturado na BIOS; os demais registros usam Scooby, portanto não comparar os fundos como mudança da interface.

APK final assinado instalado no F6 Pro, SHA256 `D577C7582C2526095898F2D5BE7D4E966650D9E5D80B1C51D51C47441D418566`. No Scooby foram conferidos arte padrão, destaque de L1 pressionado e soltura, abertura do editor pela primeira ação da aba Controles, expansão/recolhimento de Mais opções e seleção de botão com painel reposicionado e slider de tamanho. Não foram alterados tamanhos, perfis ou opacidade na verificação. A sessão de edição foi encerrada por Descartar.

Os bounds de acessibilidade confirmaram os centros dos quatro botões: triângulo (2752,5; 622,5), quadrado (2582,5; 792,5), círculo (2921,5; 792,5), X (2752,5; 961,5), com diferença de até 1 px pelo arredondamento da UI. Todos mantiveram 203 × 203 px de bounds. Sem erro AndroidRuntime na conferência.

A validação visual e de editor não comprova gameplay prolongado, multitoque simultâneo ou skins personalizadas. As injeções de Start durante a sequência de abertura não estabeleceram navegação pelo menu nativo do jogo; não registrar Start/direcional/analógico como gameplay validado nesta rodada. Nenhum estado foi salvo/carregado/excluído, e não foram iniciadas novas partidas.

## Categorias compartilhadas — 25/10/2026

Home e biblioteca completa usam a mesma faixa Névoa, sem emoji, com Todos os jogos, categorias existentes e Gerenciar quando há seleção. A associação permanece por chave estável e reutiliza os editores antigos em cartões compactos. As prateleiras duplicadas por categoria foram substituídas pelo filtro nos três modos de exibição. Busca e ordenação atuam sobre o conjunto selecionado, e recentes não trazem jogos de outras categorias. Remover uma categoria ativa restaura a categoria nula no ViewModel, evitando rótulo inválido ou seleção presa.

Build release final passou em 1 min 50 s. Os 61 testes Névoa passaram, incluindo 6 novos de associação, múltiplas categorias, chave ausente, vazio válido, categoria removida e atualização dos membros. Núcleo nativo reutilizado da referência das rodadas anteriores, sem modificações. APK final assinado instalado no F6 Pro, SHA256 `E7E0EDDAA1B7539A9885E5827154CF965B04BA9E11270B973817E3288BA0BD91`.

Conferido no Poco: criar categoria temporária, associar/desassociar Scooby, selecionar filtro com um jogo, manter categoria vazia sem cair na interface antiga, recuperar Todos os jogos, combinar categoria e busca sem resultados, limpar busca mantendo a categoria, manter seleção ao abrir a biblioteca completa, foco na faixa via entrada com fonte gamepad, modos Lista/Estante e retorno à Grade, renomear categoria ativa e excluir com confirmação. O teste de navegação usa eventos ADB com fonte gamepad e espaçamento entre comandos por causa do debounce existente; não substitui controle físico. Uma sequência inicial de eventos rápidos abriu Scooby; o teste foi reiniciado na biblioteca, sem iniciar partida ou gravar estado.

Apenas a categoria temporária de prefixo `QA_Nevoa` foi criada, renomeada e removida; a captura `cleaned.png` confirma o retorno a Todos os jogos sem esse filtro. Nenhum save foi criado/carregado/excluído. Artefatos locais: `.superpowers/validation/categories/` (`filtered-home.png`, `empty-home.png`, `filtered-complete.png`, `category-and-search.png`, `shelf-controller.png`, `cleaned.png`). Sem FATAL EXCEPTION no log AndroidRuntime da conferência final. Bibliotecas com muitas categorias, controle físico e retrato ficam como validação adicional.

## Efeito de iniciar jogo — 25/10/2026

Adicionado efeito original ao confirmar Jogar, separado do clique de menu. O WAV determinístico usa ar estéreo, grave descendente, harmônicos suaves e chegada grave. PCM16 estéreo, 48 kHz, 820 ms, pico medido 0,77997 e RMS -13,93 dBFS, com primeiro e último samples zerados. Não inclui amostras externas. A faixa V6 e a animação da abertura não foram alteradas.

O evento respeita a habilitação e o volume existentes de MenuSfx, e aceita substituição `game_launch` em pacotes. A home emite no início da transição, uma vez dentro do gate; o ViewModel não repete ao receber a conclusão. O restante da biblioteca emite pelo ViewModel. O clique genérico de confirmação por controle passou a seguir o mesmo mecanismo de deduplicação usado no registro de configurações.

Build release passou em 3 min 7 s; 61 testes Névoa passaram. Script Python validado sintaticamente e asset inspecionado quanto a duração, pico e extremos. APK final assinado instalado no F6 Pro, SHA256 `117BDC1A47C76B994D1C68243F32EBCF132EBAA5339E523E4E6CA5174ECA2164`, com o mesmo núcleo nativo de referência.

Conferência no Poco: duas chamadas de toque rápidas em Jogar da home produziram uma única reprodução aceita pelo SoundPool (`Game launch sound started, volume=15%`); abrir Scooby pela biblioteca completa também produziu uma única reprodução com 15%. Sem erro AndroidRuntime nesses logs. A sessão foi pausada após o início, sem jogar nova partida nem operar estados. Evidências em `.superpowers/validation/game-launch-sound/logcat.txt` e `full-library-logcat.txt`. Os logs comprovam a emissão do stream, sem alegar uma gravação ou audição física do alto-falante. Prévia audível: o próprio WAV incorporado.

## Cristal V2 aprovada e instalada — 25/10/2026

O usuário aprovou a segunda prévia de som. Ela substituiu o WAV padrão e o gerador reproduz exatamente seus bytes: SHA256 `2556e6a8539d8934c112c64d687eeece66dae4fd7d3d988ef6c175f036f772ef`. Acorde curto em FM, grave estável e reflexões estéreo amortecidas; 880 ms, PCM16/48 kHz estéreo, pico 0,78, RMS -19,09 dBFS e extremos zerados. README e arquitetura atualizados para a V2.

Build release passou em 1 min 4 s; os 61 testes existentes ficaram UP-TO-DATE, sem mudança na lógica de reprodução. APK assinado instalado no F6 Pro, SHA256 `81430F903E3DA7066BC02E75FC2AAB9637CFCF4CBA417841803C56D468ABE475`. Ao iniciar Scooby, SoundPool confirmou uma reprodução com o volume existente de 15%, sem erro AndroidRuntime no log filtrado; evidência em `.superpowers/validation/game-launch-sound-v2/logcat.txt`. O jogo foi pausado após a abertura. Não houve operações de estados, alteração de volume ou mudança na abertura V6. A conferência técnica de emissão não é uma gravação do alto-falante.
