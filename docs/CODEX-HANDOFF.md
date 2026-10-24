# Prompt de passagem para o Codex — ARMSX2 Névoa

Documentação de referência: [índice do projeto](NEVOA.md), [desenvolvimento](nevoa/DESENVOLVIMENTO.md) e [validação e pendências](nevoa/VALIDACAO-E-PENDENCIAS.md). Esta passagem resume a operação; os guias detalham o estado atual.

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
2. **Nenhum asset da Sony** (logo, som, fonte, imagem) embutido no app. A abertura é original; a marca final é a palavra "ARMSX2". O som da abertura é um arquivo que o próprio usuário escolhe no app.
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
- **Som da abertura** escolhido pelo usuário (`nevoa/intro/IntroSound.kt`, linha em Configurações → App).
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
- Som da abertura: `application/ogg` rejeitado; `MediaPlayer.prepare()` na thread principal; não para no `onStop`; linha continua visível quando há vídeo de abertura próprio.
- Estado da explosão não sobrevive a recriação da Activity.

**Parte 2 (em andamento):** plano em `docs/superpowers/plans/2026-10-23-nevoa-android-parte2.md`. O usuário aprovou a transparência do painel de configurações no F6 Pro e pediu menos informação simultânea e menos aparência de interface genérica. Já foram aplicados fundo calmo e vidro aos componentes comuns, a paleta Névoa ao `ThemeBridge` e ao `MaterialTheme`, quatro grupos de configurações, linhas compactas com detalhes no ícone `i`, navegação tipográfica nas configurações e menu lateral compacto. Na tela geral, o idioma ficou em uma linha e os ajustes do fundo da biblioteca foram recolhidos. BIOS e cartões de memória exibem linhas compactas com ações sob demanda; estados vazios de cartões e saves foram simplificados. Controles têm mapeamentos compactos e opções secundárias recolhidas; patches, texturas e a entrada de RetroAchievements receberam a mesma revisão. A busca, o reset, os escopos global/por jogo e as categorias originais foram mantidos. O APK do commit `7c71555d6c` passou no CI e foi instalado no F6 Pro; as capturas confirmaram Texturas, Patches, Controles e login. O APK do commit `c51ef39430` passou no CI e foi instalado no F6 Pro; menu em jogo e aba de conquistas inspecionados em paisagem, com rótulos, seções recolhidas, direcional, Continuar e Fechar verificados. O aviso de temperatura foi centralizado para não cobrir o botão Menu. A rotação automática do dispositivo foi restaurada após os testes. Na biblioteca, Menu, Biblioteca completa e Configurar foram reduzidos a ações discretas, Jogar ficou em destaque e textos redundantes foram removidos; esse ajuste passou no CI e foi instalado e conferido no F6 Pro. Busca e ordenação foram integradas à biblioteca Névoa: consulta pelo teclado existente, limpar busca, estado sem resultados e ordem por Título/Recentes/Compatibilidade, preservando seleção por URI. APK release local assinado instalado por cima no F6 Pro; toque e entrada por direcional/A verificados. Os 37 testes Névoa passaram; a suíte completa local teve 260 testes, 2 falhas fora do escopo no Windows (permissões em TexturePackInstallerTest e CRLF em LicenseDocsTest), 9 ignorados. Núcleo nativo reaproveitado do APK verde 1190fcc467 e conferido por SHA-256. Categorias, saves no painel e revisão dos controles de toque continuam pendentes; conta autenticada de RetroAchievements e jogo real não foram testados nesta rodada.

**Windows (planejado, não iniciado):** launcher próprio (ver "Contexto") com a mesma abertura e biblioteca, abrindo `pcsx2-qt.exe` com o jogo pela linha de comando (confirmar os argumentos do PCSX2), suporte a controle e tela cheia, build pelo GitHub Actions, repositório separado.

**Manutenção:** puxar atualizações do upstream conforme `docs/NEVOA.md` (até 2026-10-24 o upstream não tinha commits novos).
