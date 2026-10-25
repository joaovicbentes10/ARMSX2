# Prompt de passagem para o Codex — ARMSX2 Névoa

Documentação de referência: [índice do projeto](NEVOA.md), [desenvolvimento](nevoa/DESENVOLVIMENTO.md) e [validação e pendências](nevoa/VALIDACAO-E-PENDENCIAS.md). Esta passagem resume a operação; os guias detalham o estado atual.

O usuário confirmou V6 para o sincronismo da abertura. A trilha foi incorporada ao MP4 padrão para compartilhar o relógio da imagem; build, 50 testes e passagem no F6 Pro conferidos; ver o registro de validação no guia de pendências. Não substituir por V5.

> Cole tudo abaixo no Codex.

---

Você vai assumir o projeto **ARMSX2 Névoa**, uma versão pessoal do emulador de PS2 ARMSX2 (fork do PCSX2 para ARM/Android) com visual novo "PS5 com alma de PS2". Responda sempre em **português do Brasil**; código, comandos e termos técnicos podem ficar no original.

## Contexto e objetivo do usuário

- O usuário joga PS2 num **Poco F6 Pro** (chip SM8550/Snapdragon 8 Gen 2 confirmado por ADB, tela 3200×1440 = 20:9, Android 16). Jogo principal: *Scooby-Doo! and the Spooky Swamp* (tradução PT-BR).
- Ele achava a interface original do ARMSX2 feia ("feita com IA básica", botões desorganizados). Pediu um visual **PS5 com elementos do PS2**: escolheu a direção **"Névoa da BIOS"** — universo escuro da abertura do PS2, torres de luz finas e **translúcidas** (ele testou torres grossas e **preferiu as finas/transparentes**), névoa azul, partículas, cards de vidro.
- O celular estará **conectado por USB com depuração ativada** — use `adb` para instalar APKs, ver `logcat`, gravar a tela (`adb shell screenrecord`) e tirar screenshots para verificar o resultado você mesmo, em vez de depender só da descrição do usuário.
- Ele também quer, **no futuro**, um launcher para **Windows** (notebook Acer Nitro V15, Intel i5-13420H + RTX 3050, x86). Decisão já tomada: **não** fazer fork do PCSX2; fazer um **launcher próprio** com a mesma abertura e biblioteca que abre o **PCSX2 oficial** em tela cheia no jogo escolhido (repositório separado, build pelo GitHub Actions). Ainda não começou.

## Regras do usuário (obrigatórias)

1. **Nunca** colocar `Co-Authored-By`, "Generated with…" ou qualquer menção a IA/Claude/Codex em commits ou PRs. Commits só com a identidade git dele (João Victor <joaovicbentes10@gmail.com>).
2. **Nenhum asset da Sony** (logo, som, fonte, imagem) embutido no app. A abertura é original; a marca final é a palavra "ARMSX2". O som padrão V6 foi fornecido pelo usuário; ele também pode substituí-lo no app.
3. Textos novos da interface em **PT-BR**.
4. O usuário prefere que o trabalho seja feito direto, sem ficar delegando.
5. Mostre resultados visuais (prints, quadros de vídeo) antes de considerar algo visual "pronto" — ele avalia pelo olho e pede ajustes finos.
6. Faça verificação proporcional no aparelho: apenas a tela alterada e seus estados relevantes. Não percorrer todo o app nem repetir capturas sem necessidade; o usuário pediu economia de tokens para chegar até a entrega. Executar uma função por vez, ou duas quando uma for simples.

## Repositório

- Local: `C:\Projetos\armsx2-nevoa` — fork `github.com/joaovicbentes10/ARMSX2`, remote `upstream` = `github.com/ARMSX2/ARMSX2`.
- Branch de trabalho: **`nevoa`** (base: upstream `master` em `90126bdfea`). Último commit: ver `git log`.
- Guia de manutenção: `docs/NEVOA.md` (leia primeiro). Spec: `docs/superpowers/specs/2026-10-23-nevoa-da-bios-design.md`. Plano da Parte 1: `docs/superpowers/plans/2026-10-23-nevoa-android-parte1.md`. Histórico detalhado de decisões (git-ignorado): `.superpowers/sdd/2026-10-23-nevoa-android-parte1/progress.md`.
- Interface Android: **Jetpack Compose**, módulo `platforms/android/app`. Todo código novo fica em `com.armsx2.nevoa` (`platforms/android/app/src/main/java/com/armsx2/nevoa/`); arquivos originais têm só ganchos mínimos (lista em `docs/NEVOA.md`) para facilitar puxar atualizações do upstream.

## Build, assinatura e instalação

- Workflow próprio: `.github/workflows/nevoa-android.yml`, roda em push na `nevoa` e `workflow_dispatch`. Comando: `./gradlew :app:testGithubDebugUnitTest :app:assembleGithubRelease -Parmsx2.applicationId=com.armsx2.nevoa "-Parmsx2.appLabel=ARMSX2 Névoa"`. Leva **~40 min** (compila o núcleo nativo). Artefato: `armsx2-nevoa-android`.
- Se falhar, o próprio workflow publica o trecho do erro do Gradle como *annotation* (legível sem login).
- Assinatura fixa: keystore em `C:\Projetos\nevoa-keystore\` (`nevoa.jks`, senha em `LEIA-ME.txt` — **não** coloque em commits nem em logs) e secrets do repo `NEVOA_KEYSTORE_B64`, `NEVOA_KEYSTORE_PASSWORD`, `NEVOA_KEY_ALIAS`, `NEVOA_KEY_PASSWORD`. Com isso cada APK instala **por cima** do anterior sem perder dados.
- O app instala ao lado do ARMSX2 oficial (`applicationId com.armsx2.nevoa`, rótulo "ARMSX2 Névoa").
- Com o Poco no USB: `adb install -r <apk>` (o `.apk` vem dentro do zip do artefato).

## O que já está pronto e testado no aparelho

- **Paleta/tipografia Névoa** (`nevoa/theme/NevoaPalette.kt`, `Contrast.kt`): fundo `#050812`, névoa `#5A8CFF`, torres `#9CC3FF` / `#B9A3FF` / `#7FE0FF`, texto `#E8EEFF`, secundário `#9AA6C8`, vidro branco 8% / borda 20%, selecionado `#9CC3FF`. Aplicada via gancho em `ui/theme/Theme.kt` (o seletor de tema antigo não tem mais efeito).
- **Fundo animado** (`nevoa/background/NevoaShader.kt` AGSL + `NevoaBackground.kt`): limitado a ~30 fps, para durante o jogo, cai para gradiente estático se o shader falhar ou se a API for < 33.
- **Biblioteca estilo PS5** (`nevoa/home/NevoaHome.kt`, `DvdCase.kt`, `GameDetailsPanel.kt`, `LibraryLogic.kt`): fileira de caixas de DVD, selecionada maior com brilho, painel (título, "Última vez", "Tempo de jogo", Jogar/Configurar), botões Menu e Biblioteca completa, "Pressione ✕ para Iniciar". Seleção guardada pelo jogo (uri), não pela posição. Controle passa pelo registro `SettingsControllerNav` (o pad não chega no Compose diretamente). △ abre a biblioteca clássica (`HomeScreen`), que também aparece sozinha quando não há pasta/jogos. Entrada estilo PS5 (botões → capas → painel → rodapé) uma vez por processo.
- **Explosão de luz ao Jogar** (`nevoa/home/LaunchBurst.kt`): `LaunchGate` impede abrir o jogo duas vezes; entrada bloqueada durante a animação.
- **Abertura própria** (`tools/nevoa-intro/`: `intro.html` WebGL determinístico + `render.mjs` Puppeteer/ffmpeg): 8 s, **2400×1080 (20:9)**, ~1,2 MB, em `res/raw/boot_intro.mp4`. O último quadro é idêntico ao fundo da biblioteca (o script verifica e recusa gerar se não bater) e é usado como `windowBackground` da tela principal (`res/drawable-nodpi/nevoa_last_frame.webp`, tema `Theme.ARMSX2.Nevoa`) para a passagem abertura → biblioteca ser contínua. **O usuário confirmou no aparelho que agora emenda certinho.**
- **Som da abertura** padrão Névoa Suspense (MP3 de 8 s fornecido pelo usuário), com substituição por áudio próprio (`nevoa/intro/IntroSound.kt`, linha em Configurações → App).
- **Sempre em horizontal** (manifest `sensorLandscape` + padrão "Paisagem" em `LauncherOrientationPreferences`).

## Lições técnicas (não repita esses erros)

- O app tem *product flavors* `github`/`play`: use tarefas `…Github…`; testes unitários só existem para **debug** (`testGithubDebugUnitTest`).
- `BootSplashActivity` cortava qualquer vídeo em 6 s (`HARD_TIMEOUT_MS`); agora usa a duração do vídeo.
- `VideoView` faz letterbox; usamos `nevoa/intro/FillVideoView.kt` para preencher a tela igual ao shader.
- **Hash com `sin()` dá resultados diferentes na GPU do celular e no desktop** — as torres mudavam de lugar na passagem. Os dois shaders usam agora um hash só com multiplicação/soma (`hash1` em `NevoaShader.kt` e `intro.html`); mantenha os dois sincronizados.
- Sombras coloridas (`Modifier.shadow`) viram caixas pretas em Adreno/Mali; use `drawBehind`.
- Não use `AlertDialog`/`ModalBottomSheet`/`DropdownMenu`/`Dialog` do Compose: o build falha (roubam o foco do controle).
- O build oficial roda no GitHub Actions. Na sessão de 24/10, o SDK local foi encontrado em `C:\Users\joaov\AppData\Local\Android\Sdk`, com NDK 29 incompleto e NDK 30 íntegro. A interface foi compilada localmente com o núcleo do último APK verde, sem alterar o CI; veja o guia de desenvolvimento.

## Pendências e próximos passos

**Detalhes menores conhecidos (Parte 1):**
- Aviso de temperatura "O dispositivo está esquentando" fica por cima do botão Menu na tela nova.
- Partículas pequenas na tela de 3200 px; possível salto do fundo no wrap de 600 s. O intervalo mínimo atual do relógio é 30 ms e a cadência depende do vsync; medir antes de alterar.
- O pedido de foco passou a reagir à gaveta e ao teclado em `3222e96576`; retestar especificamente △ após essas transições. A variável `resolved` sem uso em `Theme.kt` permanece no registro histórico de limpeza.
- Som da abertura: `application/ogg` rejeitado; preparação agora assíncrona e liberação em `onStop` implementadas; validar ciclo completo; linha continua visível quando há vídeo de abertura próprio.
- Estado da explosão não sobrevive a recriação da Activity.

**Parte 2 (em andamento):** plano em `docs/superpowers/plans/2026-10-23-nevoa-android-parte2.md`. O usuário aprovou a transparência do painel de configurações no F6 Pro e pediu menos informação simultânea e menos aparência de interface genérica. Já foram aplicados fundo calmo e vidro aos componentes comuns, a paleta Névoa ao `ThemeBridge` e ao `MaterialTheme`, quatro grupos de configurações, linhas compactas com detalhes no ícone `i`, navegação tipográfica nas configurações e menu lateral compacto. Na tela geral, o idioma ficou em uma linha e os ajustes do fundo da biblioteca foram recolhidos. BIOS e cartões de memória exibem linhas compactas com ações sob demanda; estados vazios de cartões e saves foram simplificados. Controles têm mapeamentos compactos e opções secundárias recolhidas; patches, texturas e a entrada de RetroAchievements receberam a mesma revisão. A busca, o reset, os escopos global/por jogo e as categorias originais foram mantidos. O APK do commit `7c71555d6c` passou no CI e foi instalado no F6 Pro; as capturas confirmaram Texturas, Patches, Controles e login. O APK do commit `c51ef39430` passou no CI e foi instalado no F6 Pro; menu em jogo e aba de conquistas inspecionados em paisagem, com rótulos, seções recolhidas, direcional, Continuar e Fechar verificados. O aviso de temperatura foi centralizado para não cobrir o botão Menu. A rotação automática do dispositivo foi restaurada após os testes. Na biblioteca, Menu, Biblioteca completa e Configurar foram reduzidos a ações discretas, Jogar ficou em destaque e textos redundantes foram removidos; esse ajuste passou no CI e foi instalado e conferido no F6 Pro. Busca e ordenação foram integradas à biblioteca Névoa: consulta pelo teclado existente, limpar busca, estado sem resultados e ordem por Título/Recentes/Compatibilidade, preservando seleção por URI. APK release local assinado instalado por cima no F6 Pro; toque e entrada por direcional/A verificados. Os 37 testes Névoa passaram; a suíte completa local teve 260 testes, 2 falhas fora do escopo no Windows (permissões em TexturePackInstallerTest e CRLF em LicenseDocsTest), 9 ignorados. Núcleo nativo reaproveitado do APK verde 1190fcc467 e conferido por SHA-256. Categorias, saves no painel e revisão dos controles de toque continuam pendentes; conta autenticada de RetroAchievements e jogo real não foram testados nesta rodada.

**Windows (planejado, não iniciado):** launcher próprio (ver "Contexto") com a mesma abertura e biblioteca, abrindo `pcsx2-qt.exe` com o jogo pela linha de comando (confirmar os argumentos do PCSX2), suporte a controle e tela cheia, build pelo GitHub Actions, repositório separado.

**Manutenção:** puxar atualizações do upstream conforme `docs/NEVOA.md` (até 2026-10-24 o upstream não tinha commits novos).

## Último incremento: áudio, biblioteca completa e diagnóstico de blocos pretos

Som padrão Névoa Suspense incluído inicialmente (atualizado depois para V6) como raw MP3 (8 s); faixa padrão iniciada e confirmada por log no F6 Pro. Arquivo próprio ainda tem prioridade. MediaPlayer usa prepareAsync e é liberado em onStop. Biblioteca completa/HomeScreen recebeu fundo Névoa sem o XMB/Flurry independente, cabeçalho discreto e recentes compactos. O fundo conserva sua composição e pausa quando o protetor o cobre. A transição mantém a cobertura escura até o runtime iniciar. O usuário pediu retirar o aviso de temperatura: banner suprimido em BatteryWatcher; bateria baixa e mecanismos térmicos não foram alterados. Nome da animação: o usuário preferiu pensar depois, portanto manter ARMSX2 por enquanto.

O usuário pediu iniciar Scooby-Doo e reproduzimos blocos pretos no carregamento com Vulkan + Turnip Mesa 27.0.0-devel (git-axfl2-005). Com o driver Vulkan do sistema, o mesmo carregamento não apresentou os blocos no vídeo comparativo e o jogo avançou ao título. No aparelho, somente SLUS-21928 tem overrides renderer=vulkan e customDriverId vazio; a configuração global continua auto + Turnip. O teste foi de abertura/carregamento/animação, sem gameplay prolongado. Biblioteca gravada por 40 s sem falhas amplas no trecho. Ver VALIDACAO-E-PENDENCIAS.md para métricas e limites. Release local instalado e 37 testes Névoa passaram; núcleo preservado. Evidências locais: .superpowers/validation/audio-library/.
## Último incremento: paisagem, gestos e V6

O usuário pediu que jogos iniciem em paisagem, arrastar para mudar a seleção, áudio V6 conforme a prévia e substituição do flash branco de Jogar. Implementado: modo de orientação legado 0 agora resolve para paisagem via nevoa/intro/NevoaOrientation.kt; escolhas explícitas 2/3 continuam disponíveis. Fileira usa gesto horizontal de 48 dp, um item por gesto, sem lançar ao arrastar. Som padrão substituído pelo MP3 V6 de 8 s fornecido; a prévia tem 8 s e foi consultada como referência, sem incorporar a gravação de tela como vídeo de abertura. Transição de Jogar agora dissolve a cena em névoa azul e preto em 520 ms, sem círculo branco/partículas soltas, conservando a cobertura até o runtime iniciar.

Release local instalado por cima no F6 Pro. Gestos curtos, esquerda e direita conferidos. Scooby iniciado por Jogar; quatro amostras do WindowManager e a captura confirmaram sensorLandscape, 3200x1440, desde a partida. Transição gravada e inspecionada. 46 testes Névoa passaram. Evidências: .superpowers/validation/v6-landscape-swipe/. Driver por jogo do Scooby continua Vulkan do sistema conforme a rodada anterior. Nome da abertura continua adiado.
## Último incremento: seletor animado e menu de pausa contido

Capa/brilho/painéis agora transitam juntos em 320 ms, com ações e registro de controle desativados no painel que está saindo. Partida de 900 ms com a capa viajando de seus limites congelados no clique até o centro, título e fade para preto; sem círculo/partículas. Menu em paisagem com até 44% da largura (520 dp) e 78% da altura, abas menores, scrim reduzido, ações principais em duas linhas, sem capa/relógio/bateria/identificadores expostos no cabeçalho. Emojis/glyphs de ações e amigos substituídos por Line Awesome. Serial/CRC/presença em Mais ações → Detalhes do jogo, sob demanda. Hardcore preservado.

Poco: seletor/partida gravados; menu, contraste, sete abas, direcional, acesso a detalhes SLUS-21928 / CRC 38E42973, Continuar e reabertura conferidos. Não escrever saves nem afirmar teste de amigos/RA autenticados. Evidências: .superpowers/validation/selection-launch-menu/. Release local assinado instalado; 46 testes Névoa passaram. A gravação final inclui o ajuste de espaçamento. Ver registro de validação para limites.

## Rodadas concluídas em 24/10/2026

Abas e estados receberam apresentação compacta, opções automáticas recolhíveis e detecção real de slots ocupados. A V6 padrão passou a usar o relógio do próprio vídeo, com entrada na biblioteca após o término audiovisual. APK final instalado no Poco; build e 50 testes Névoa passaram. Não foram escritos estados na validação. Categorias, associação de saves no painel, controles de toque e validação ampla continuam como próximos incrementos, listados no guia.

O usuário pediu que o ARMSX2 apareça junto do “tum” final, sem alterar o áudio. A linha do tempo do título foi deslocada para o ataque em 7,0 s, com saída até 7,95 s. A trilha AAC é copiada do MP4 anterior; manter essa preservação ao gerar revisões apenas visuais.
