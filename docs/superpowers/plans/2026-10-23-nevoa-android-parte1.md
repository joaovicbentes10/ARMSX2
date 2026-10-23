# Névoa da BIOS — Android Parte 1 (fork, tema, biblioteca, abertura) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Um APK do ARMSX2 com identidade própria ("ARMSX2 Névoa"), assinado com chave fixa, gerado pelo GitHub Actions, com paleta Névoa, fundo animado, biblioteca estilo PS5 com caixas de DVD, explosão de luz ao Jogar e abertura nova estilo PS2.

**Architecture:** Fork do ARMSX2 com branch `nevoa`. Todo código novo mora no pacote `com.armsx2.nevoa` (`platforms/android/app/src/main/java/com/armsx2/nevoa/`). Nos arquivos originais, só ganchos de uma ou poucas linhas: `AppNavigation.kt` (rota Home), `Theme.kt` (esquema de cores e tipografia), `Pasx2Application.kt` (cores legadas), `BootSplashActivity.kt` (som da abertura), `AppTab.kt` (linha de configuração do som) e `res/raw/boot_intro.mp4` (vídeo). Build e testes só no CI, porque o usuário não tem Android SDK local.

**Tech Stack:** Kotlin, Jetpack Compose (Material3), AGSL `RuntimeShader` (API 33+), JUnit4 (testes JVM em `app/src/test`), GitHub Actions; ferramenta da abertura: Node + Puppeteer + ffmpeg (já instalados nesta máquina: `node`, `ffmpeg 9.0.2`, `java 21`/`keytool`).

**Spec:** `docs/2026-10-23-nevoa-da-bios-design.md`

**Fora desta parte (Parte 2 e plano Windows):** restilizar componentes comuns e reorganizar configurações; demais telas; linha "saves no memory card" no painel do jogo; busca/ordenar/categorias no visual novo (nesta parte ficam acessíveis pela biblioteca clássica via △); launcher Windows.

## Global Constraints

- Nenhum asset da Sony (logo, som, fonte, imagem). A marca final da abertura é a palavra "ARMSX2" em tipografia fina e espaçada.
- Código novo só em `com.armsx2.nevoa`; arquivos originais recebem ganchos mínimos, listados em cada tarefa.
- Proibido usar `AlertDialog`, `ModalBottomSheet`, `DropdownMenu` e `Dialog` do Compose: o build falha, porque eles roubam o foco do controle.
- Textos da interface nova em PT-BR.
- `applicationId` = `com.armsx2.nevoa`, rótulo = `ARMSX2 Névoa`. O app começa do zero, sem migrar dados.
- Paleta: fundo `#050812`, névoa `#5A8CFF`, torres `#9CC3FF` / `#B9A3FF` / `#7FE0FF`, texto `#E8EEFF`, texto secundário `#9AA6C8`, vidro = branco a 8% com borda branca a 20%; selecionado = borda `#9CC3FF` com brilho.
- Alvo: Poco F6 Pro, paisagem, toque e controle. O fundo animado para enquanto `MainActivityRuntime.eState.value` é `RUNNING` ou `PAUSED`.
- O usuário faz todo login e clique no GitHub; o executor dá o passo a passo em PT-BR e espera ele confirmar.

## Review Focus

1. **Jogo sem capa:** a caixa mostra o título sobre um fundo de vidro, sem buraco nem crash (teste na Task 4: `coverLabel_fallsBackToTitle`).
2. **Toque duplo em Jogar** (ou ✕ e toque juntos): o jogo abre uma vez só (teste na Task 5: `LaunchGate`).
3. **Biblioteca vazia, sem pasta ou escaneando:** cai na tela clássica, que já sabe pedir a pasta, e não fica numa fileira vazia (teste na Task 4: `shouldShowClassic`).
4. **Títulos longos** (PT-BR, nomes compostos): no máximo 2 linhas no painel e reticências na caixa (verificação manual na Task 8).
5. **Fileira grande** (200+ jogos) e jogo recente que também está na lista completa: sem duplicata e com rolagem fluida (teste na Task 4: `libraryOrder_dedupesRecentFirst`; `LazyRow` obrigatório).

---

### Task 0: Fork, cópia local e branch (guiado com o usuário)

**Files:** nenhum código. Pasta local: `C:\Projetos\armsx2-nevoa`.

- [ ] **Step 1:** Orientar o usuário a abrir `https://github.com/ARMSX2/ARMSX2`, clicar em **Fork** e manter o nome `ARMSX2`. Perguntar o nome de usuário do GitHub dele.
- [ ] **Step 2:** Clonar: `git -c core.longpaths=true clone https://github.com/<usuario>/ARMSX2 C:\Projetos\armsx2-nevoa` e, dentro da pasta, rodar `git remote add upstream https://github.com/ARMSX2/ARMSX2` e `git checkout -b nevoa`. Mover a sessão para essa pasta com `change_directory`.
- [ ] **Step 3:** Copiar o spec e este plano para `docs/superpowers/specs/2026-10-23-nevoa-da-bios-design.md` e `docs/superpowers/plans/2026-10-23-nevoa-android-parte1.md` no fork.
- [ ] **Step 4:** Commit `docs: spec e plano Névoa da BIOS` e push com `git push -u origin nevoa`. O usuário entra na janela do Git Credential Manager quando ela abrir.

### Task 1: Workflow próprio, chave fixa e identidade do app

**Files:**
- Create: `.github/workflows/nevoa-android.yml`
- Modify: `.gitignore` (adicionar `platforms/android/armsx2_keystore.properties` e `*.jks`)
- Fora do repositório: `C:\Projetos\nevoa-keystore\nevoa.jks` (o usuário guarda um backup)

**Interfaces:**
- Produces: artefato `armsx2-nevoa-android` com o APK; secrets `NEVOA_KEYSTORE_B64`, `NEVOA_KEYSTORE_PASSWORD`, `NEVOA_KEY_ALIAS`, `NEVOA_KEY_PASSWORD`; o passo de testes JVM que as próximas tarefas usam para validar.

- [ ] **Step 1:** Gerar a chave com `keytool -genkeypair -v -keystore C:\Projetos\nevoa-keystore\nevoa.jks -alias nevoa -keyalg RSA -keysize 4096 -validity 36500 -dname "CN=ARMSX2 Nevoa"`, usando senha aleatória gerada pelo executor. Gerar o base64 com `base64 -w0 nevoa.jks > nevoa.jks.b64`. Não repetir a senha no chat: gravar em `C:\Projetos\nevoa-keystore\LEIA-ME.txt`.
- [ ] **Step 2:** Guiar o usuário em **Settings → Secrets and variables → Actions → New repository secret** no fork para criar os 4 secrets, colando do `LEIA-ME.txt` e do `.b64`.
- [ ] **Step 3:** Escrever `nevoa-android.yml`, disparado por `push` na branch `nevoa` e por `workflow_dispatch`. Copiar literalmente os passos do job `android` de `build-all.yml` (checkout recursivo, JDK 17, setup-android `platform-tools`, NDK `29.0.14206865` + cmake `3.31.6`, `git-sync-deps` do shaderc, `rustup target add aarch64-linux-android`). Antes do build, um passo que decodifica `NEVOA_KEYSTORE_B64` para `platforms/android/nevoa.jks` e escreve `platforms/android/armsx2_keystore.properties` com `storeFile=nevoa.jks`, `storePassword`, `keyAlias` e `keyPassword` vindos dos secrets. Build: `./gradlew :app:testReleaseUnitTest :app:assembleRelease -Parmsx2.applicationId=com.armsx2.nevoa "-Parmsx2.appLabel=ARMSX2 Névoa" --stacktrace`. Upload como `armsx2-nevoa-android`. Sem `continue-on-error`.
- [ ] **Step 4:** Commit `ci: build Névoa com chave fixa` e push. Rodar o workflow.
  Expected: job verde, testes JVM passam, artefato com 1 APK.
- [ ] **Step 5:** O usuário baixa o APK pelo celular (aba Actions → execução → Artifacts) e instala. Expected: aparece "ARMSX2 Névoa" ao lado de qualquer ARMSX2 oficial.
- [ ] **Step 6:** Disparar o workflow de novo (`workflow_dispatch`) e instalar o novo APK por cima. Expected: instala sem pedir para desinstalar (prova da assinatura fixa).

### Task 2: Paleta e tipografia Névoa

**Files:**
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/theme/NevoaPalette.kt`
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/theme/Contrast.kt`
- Modify: `platforms/android/app/src/main/java/com/armsx2/ui/theme/Theme.kt:593-597` (`colorScheme = NevoaPalette.scheme`, `typography = NevoaPalette.typography`)
- Modify: `platforms/android/app/src/main/java/com/armsx2/Pasx2Application.kt` (`onCreate`: chamar `NevoaPalette.applyLegacyColors()`)
- Test: `platforms/android/app/src/test/java/com/armsx2/nevoa/theme/NevoaPaletteTest.kt`

**Interfaces:**
- Produces: `object NevoaPalette { val Background: Color; val Fog: Color; val TowerBlue: Color; val TowerViolet: Color; val TowerCyan: Color; val Text: Color; val TextDim: Color; val Glass: Color; val GlassBorder: Color; val Selected: Color; val scheme: ColorScheme; val typography: Typography; fun applyLegacyColors() }` (os valores vêm de Global Constraints). `applyLegacyColors()` escreve nos `mutableStateOf` de `com.armsx2.ui.Colors` (`surface`, `surfaceDark`, `surfaceDarker`, `secondary`, `secondarySurface`).
- Produces: `fun contrastRatio(a: Color, b: Color): Double` (WCAG 2.x, luminância relativa).

- [ ] **Step 1: Write the failing test**

```kotlin
@Test fun text_on_background_is_AAA() = assertTrue(contrastRatio(NevoaPalette.Text, NevoaPalette.Background) >= 7.0)
@Test fun dim_text_on_background_is_AA() = assertTrue(contrastRatio(NevoaPalette.TextDim, NevoaPalette.Background) >= 4.5)
@Test fun selected_on_background_is_AA() = assertTrue(contrastRatio(NevoaPalette.Selected, NevoaPalette.Background) >= 4.5)
@Test fun contrast_black_white_is_21() = assertEquals(21.0, contrastRatio(Color.Black, Color.White), 0.01)
@Test fun scheme_background_matches_palette() = assertEquals(NevoaPalette.Background, NevoaPalette.scheme.background)
```

- [ ] **Step 2:** Push; o CI falha em `testReleaseUnitTest` com "unresolved reference: NevoaPalette".
- [ ] **Step 3:** Implementar `NevoaPalette` e `contrastRatio`. O esquema parte de `darkColorScheme` com `background`/`surface` = `Background`, `primary` = `Selected`, `onBackground`/`onSurface` = `Text` e `surfaceVariant` = `Glass` sobre `Background`. Tipografia: `ArmsTypography` com `fontWeight = Light` e `letterSpacing = 0.06.em` em `display*`/`headline*`/`title*`.
- [ ] **Step 4:** Aplicar os ganchos em `Theme.kt` e `Pasx2Application.kt`. Push. Expected: CI verde; o app instalado já fica escuro azulado em todas as telas.
- [ ] **Step 5:** Commit `feat(nevoa): paleta e tipografia`.

### Task 3: Fundo animado "Névoa da BIOS"

**Files:**
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/background/NevoaShader.kt` (código AGSL como `const val NEVOA_AGSL`)
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/background/NevoaBackground.kt`
- Test: `platforms/android/app/src/test/java/com/armsx2/nevoa/background/ShaderClockTest.kt`

**Interfaces:**
- Consumes: `NevoaPalette` (Task 2).
- Produces: `@Composable fun NevoaBackground(modifier: Modifier = Modifier, calm: Boolean = false)`. `calm = true` reduz velocidade e brilho pela metade (para as telas da Parte 2). Também `fun shaderSeconds(nowNanos: Long, startNanos: Long, wrapSeconds: Float = 600f): Float`.

- [ ] **Step 1: Write the failing test**

```kotlin
@Test fun starts_at_zero() = assertEquals(0f, shaderSeconds(5_000L, 5_000L), 1e-6f)
@Test fun counts_seconds() = assertEquals(2.5f, shaderSeconds(2_500_000_000L, 0L), 1e-4f)
@Test fun wraps_to_keep_float_precision() = assertEquals(1f, shaderSeconds(601_000_000_000L, 0L), 1e-3f)
```

- [ ] **Step 2:** Push; o CI falha (unresolved reference).
- [ ] **Step 3:** Implementar `shaderSeconds` e `NevoaBackground`. O shader AGSL usa os uniforms `float2 iResolution`, `float iTime`, `float calm` e desenha: céu `Background`; névoa `Fog` subindo da base em ruído fbm lento; 9 a 14 torres retangulares ao longe, finas e com gradiente vertical para transparente, nas três cores de torre, com brilho pulsando defasado; 2 faixas de luz azul em seno passando devagar; e ~60 partículas piscando. No API 33+, `RuntimeShader` em `Modifier.drawBehind` com `withFrameNanos`. Abaixo do 33, `Brush.verticalGradient(Background → Fog a 35%)` estático. O relógio só avança com o lifecycle em `RESUMED` e quando `MainActivityRuntime.eState.value` não é `RUNNING` nem `PAUSED`.
- [ ] **Step 4:** Push; CI verde.
- [ ] **Step 5:** Commit `feat(nevoa): fundo animado`.

### Task 4: Biblioteca Névoa

**Files:**
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/home/LibraryLogic.kt`
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/home/DvdCase.kt`
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/home/GameDetailsPanel.kt`
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/home/NevoaHome.kt`
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/NevoaStrings.kt`
- Modify: `platforms/android/app/src/main/java/com/armsx2/navigation/AppNavigation.kt:86-89` (`AppRoute.Home -> NevoaHome(` com os mesmos dois callbacks)
- Test: `platforms/android/app/src/test/java/com/armsx2/nevoa/home/LibraryLogicTest.kt`

**Interfaces:**
- Consumes: `HomeViewModel.state.value: HomeUiState` (`allGames`, `visibleGames`, `recentGames`, `scanning`, `initialized`); `HomeViewModel.launch(game: GameInfo)`; `MainActivityRuntime.romsDirs.value`; `GameCoverArt(game, modifier)`; `PlayTime.playedSeconds(serial)`, `PlayTime.lastPlayedMillis(serial)`, `PlayTime.formatPlayed(seconds)`, `PlayTime.formatLastPlayed(millis)`; `MenuSfx.play(MenuSfx.Event.NAV | SELECT | BACK)`; `Modifier.controllerFocusable(onConfirm, onLeft, onRight)` (é `internal` em `com.armsx2.ui.settings`, mesmo módulo); `HomeScreen(onOpenMenu, onOpenGameSettings)`; `NevoaBackground` (Task 3).
- Produces:
  As funções puras não recebem tipos do Android (`Uri`/`GameInfo`), para os testes JVM rodarem sem stubs. O composable chama cada uma com `key = { it.uri.toString() }` e `fileName = game.uri.lastPathSegment`.
  - `fun <T> libraryOrder(recent: List<T>, all: List<T>, key: (T) -> String): List<T>`: recentes primeiro, depois o resto, sem repetir `key`.
  - `fun moveSelection(current: Int, delta: Int, count: Int): Int`: trava nas pontas, sem dar a volta; retorna 0 quando `count == 0`.
  - `fun shouldShowClassic(initialized: Boolean, scanning: Boolean, gameCount: Int, hasRomDirs: Boolean, classicRequested: Boolean): Boolean`: true se pedido, sem pasta, não inicializado, ou `gameCount == 0` (escaneando ou não).
  - `fun coverLabel(title: String, fileName: String?): String`: `title` com trim; se vazio, `fileName` sem extensão; se também for nulo, "?".
  - `@Composable fun NevoaHome(onOpenMenu: () -> Unit, onOpenGameSettings: (GameInfo) -> Unit, viewModel: HomeViewModel = viewModel())`.
  - `object NevoaStrings { const val Play = "Jogar"; const val Configure = "Configurar"; const val PressToStart = "Pressione ✕ para Iniciar"; const val FullLibrary = "Biblioteca completa"; const val Played = "Jogado"; const val NeverPlayed = "Nunca jogado" }`. Shortcut: strings fixas em PT-BR, sem passar pelo `I18n`; migrar se um dia o app for público.

- [ ] **Step 1: Write the failing test**

```kotlin
@Test fun libraryOrder_dedupesRecentFirst() = assertEquals(listOf("b","a","c"), libraryOrder(listOf("b"), listOf("a","b","c")) { it })
@Test fun libraryOrder_200games_noDuplicates() { val all = (1..200).map { "g$it" }; assertEquals(200, libraryOrder(all.take(5), all) { it }.toSet().size) }
@Test fun moveSelection_clampsAtEnds() { assertEquals(0, moveSelection(0, -1, 3)); assertEquals(2, moveSelection(2, 1, 3)); assertEquals(1, moveSelection(0, 1, 3)) }
@Test fun moveSelection_emptyIsZero() = assertEquals(0, moveSelection(4, 1, 0))
@Test fun shouldShowClassic_whenNoDirs() = assertTrue(shouldShowClassic(initialized = true, scanning = false, gameCount = 3, hasRomDirs = false, classicRequested = false))
@Test fun shouldShowClassic_whenScanningEmpty() = assertTrue(shouldShowClassic(initialized = true, scanning = true, gameCount = 0, hasRomDirs = true, classicRequested = false))
@Test fun shouldShowNevoa_whenGamesPresent() = assertFalse(shouldShowClassic(initialized = true, scanning = false, gameCount = 3, hasRomDirs = true, classicRequested = false))
@Test fun coverLabel_fallsBackToTitle() = assertEquals("Scooby", coverLabel("  Scooby ", "x.iso"))
@Test fun coverLabel_fallsBackToFileName() = assertEquals("x", coverLabel(" ", "x.iso"))
@Test fun coverLabel_nothingAtAll() = assertEquals("?", coverLabel("", null))
```

- [ ] **Step 2:** Push; o CI falha (unresolved reference).
- [ ] **Step 3:** Implementar `LibraryLogic.kt`.
- [ ] **Step 4:** Implementar os composables:
  - **`DvdCase(game, selected, onClick)`:** proporção 135:190, faixa superior escura de 9% da altura com "PlayStation 2" escrito em texto próprio e pequeno; capa via `GameCoverArt`; sem capa, `coverLabel` sobre `Glass`. Não selecionado: 128 dp de altura. Selecionado: 176 dp, borda `Selected` 2 dp e sombra/brilho azul, com animação de escala em 180 ms.
  - **`GameDetailsPanel(game, onPlay, onConfigure)`:** título em no máximo 2 linhas com reticências; "Jogado há …" ou `NeverPlayed`; tempo total; botões de vidro `Jogar` (primário) e `Configurar`.
  - **`NevoaHome`:** `NevoaBackground` de fundo, `LazyRow` de `DvdCase` com rolagem que mantém o selecionado visível, painel à direita do selecionado e rodapé `PressToStart` centralizado. No canto superior direito, botão de vidro `FullLibrary`. Ele e a tecla △ (`KEYCODE_BUTTON_Y`) abrem a clássica (`HomeScreen`), e o `BackHandler` volta para a Névoa. Teclas: D-pad esquerda/direita chamam `moveSelection` + `MenuSfx` NAV; ✕ (`BUTTON_A`) dispara Jogar; ○ volta. Toque numa caixa seleciona; toque na caixa já selecionada dispara Jogar. Jogar chama `viewModel.launch(game)` (a Task 5 troca isso pela explosão).
- [ ] **Step 5:** Aplicar o gancho em `AppNavigation.kt`. Push; CI verde.
- [ ] **Step 6:** No celular, apontar a pasta de jogos pela clássica e voltar. Expected: fileira de caixas, Scooby-Doo selecionável no toque e no controle, Configurar abre as configurações do jogo e △ abre a clássica.
- [ ] **Step 7:** Commit `feat(nevoa): biblioteca estilo PS5`.

### Task 5: Explosão de luz ao Jogar

**Files:**
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/home/LaunchBurst.kt`
- Modify: `platforms/android/app/src/main/java/com/armsx2/nevoa/home/NevoaHome.kt` (Jogar passa pelo `LaunchGate` e pelo `LaunchBurst`)
- Test: `platforms/android/app/src/test/java/com/armsx2/nevoa/home/LaunchGateTest.kt`

**Interfaces:**
- Produces: `class LaunchGate { fun tryStart(): Boolean; fun reset() }` (a primeira chamada retorna true; as seguintes retornam false até `reset()`). Também `@Composable fun LaunchBurst(active: Boolean, onFinished: () -> Unit)`, com 700 ms: um ponto branco-azulado no centro cresce até cobrir a tela, em gradiente radial `Text → TowerCyan → Fog → Background`, com ~40 partículas saindo radialmente e fade para preto nos últimos 150 ms.

- [ ] **Step 1: Write the failing test**

```kotlin
@Test fun only_first_start_wins() { val g = LaunchGate(); assertTrue(g.tryStart()); assertFalse(g.tryStart()) }
@Test fun reset_allows_again() { val g = LaunchGate(); g.tryStart(); g.reset(); assertTrue(g.tryStart()) }
```

- [ ] **Step 2:** Push; o CI falha.
- [ ] **Step 3:** Implementar. Fluxo em `NevoaHome`: `if (gate.tryStart())` → `MenuSfx.play(SELECT)` → `LaunchBurst(active = true)` → `onFinished = { viewModel.launch(game) }`. Chamar `gate.reset()` quando `eState` volta para `STOPPED`.
- [ ] **Step 4:** Push; CI verde. No celular: tocar duas vezes rápido em Jogar. Expected: uma explosão e o jogo abre uma vez.
- [ ] **Step 5:** Commit `feat(nevoa): explosão ao jogar`.

### Task 6: Abertura nova (vídeo próprio)

**Files:**
- Create: `tools/nevoa-intro/intro.html`, `tools/nevoa-intro/render.mjs`, `tools/nevoa-intro/package.json` (dependência `puppeteer`), `tools/nevoa-intro/README.md` (como regerar)
- Replace: `platforms/android/app/src/main/res/raw/boot_intro.mp4`

**Interfaces:**
- Consumes: as cores de Global Constraints, copiadas como constantes JS no `intro.html`.
- Produces: `boot_intro.mp4`, que o `BootSplashActivity` já toca, já pula com toque/Back e já respeita `ui.bootLogo`.

- [ ] **Step 1:** Escrever `intro.html`: canvas 1920×1080 e uma função determinística `window.renderFrame(t)` (t em segundos, sem `Math.random` sem semente). Linha do tempo de 8 s: **0–1,2 s** preto e partículas acendendo; **1,2–3,0 s** névoa azul subindo da base; **2,4–4,8 s** torres de luz subindo do chão, em perspectiva, uma após a outra; **4,8–6,0 s** a palavra "ARMSX2" surge no centro em fade e espaçamento que diminui; **6,0–8,0 s** a câmera avança entre as torres (escala e perspectiva) e termina num quadro igual ao `NevoaBackground` em repouso.
- [ ] **Step 2:** Escrever `render.mjs`: o Puppeteer abre o HTML, chama `renderFrame(i/30)` para i = 0…239, salva PNGs numa pasta temporária fora do repo e roda `ffmpeg -framerate 30 -i %04d.png -c:v libx264 -pix_fmt yuv420p -crf 20 -movflags +faststart -an boot_intro.mp4`.
- [ ] **Step 3:** Rodar `npm install` e `node render.mjs` em `tools/nevoa-intro`. Verificar com `ffprobe -v error -show_entries format=duration:stream=width,height -of csv=p=0 boot_intro.mp4`.
  Expected: `1920,1080` e duração entre 7,9 e 8,1; arquivo com até 4 MB (se passar disso, subir o `-crf`).
- [ ] **Step 4:** Extrair 6 quadros (`ffmpeg -vf fps=0.75,tile=3x2`), mostrar ao usuário e ajustar até ele aprovar.
- [ ] **Step 5:** Copiar para `res/raw/boot_intro.mp4`. Adicionar `tools/nevoa-intro/node_modules/` ao `.gitignore`. Commit `feat(nevoa): abertura própria`. Push; CI verde; no celular, a abertura toca ao abrir o app e o toque pula.

### Task 7: Som da abertura escolhido pelo usuário

**Files:**
- Create: `platforms/android/app/src/main/java/com/armsx2/nevoa/intro/IntroSound.kt`
- Modify: `platforms/android/app/src/main/java/com/armsx2/BootSplashActivity.kt` (tocar o som junto com o vídeo do pacote; parar e liberar o player no skip e no `onDestroy`)
- Modify: `platforms/android/app/src/main/java/com/armsx2/ui/settings/AppTab.kt:~465` (logo abaixo do seletor de vídeo de abertura, uma linha "Som da abertura" com escolher e remover, no mesmo padrão do seletor existente)
- Test: `platforms/android/app/src/test/java/com/armsx2/nevoa/intro/IntroSoundTest.kt`

**Interfaces:**
- Produces: `object IntroSound { const val MaxBytes = 5L * 1024 * 1024; fun file(context: Context): File /* filesDir/nevoa_intro_sound */; fun validate(sizeBytes: Long, mime: String?): String? /* null = ok; senão mensagem PT-BR */; fun import(context: Context, uri: Uri): String? /* copia, mesma convenção */; fun clear(context: Context); fun exists(context: Context): Boolean }`.

- [ ] **Step 1: Write the failing test**

```kotlin
@Test fun accepts_small_audio() = assertNull(IntroSound.validate(300_000, "audio/mpeg"))
@Test fun rejects_too_big() = assertNotNull(IntroSound.validate(IntroSound.MaxBytes + 1, "audio/mpeg"))
@Test fun rejects_non_audio() = assertNotNull(IntroSound.validate(1000, "video/mp4"))
@Test fun rejects_unknown_mime() = assertNotNull(IntroSound.validate(1000, null))
```

- [ ] **Step 2:** Push; o CI falha.
- [ ] **Step 3:** Implementar `IntroSound`. A cópia é feita como em `BootIntro`: arquivo privado, fora da thread principal.
- [ ] **Step 4:** Gancho no splash: o som só toca quando a abertura está ligada, nenhum vídeo próprio está escolhido e `IntroSound.exists`, com um `MediaPlayer` criado do arquivo. Gancho no `AppTab`.
- [ ] **Step 5:** Push; CI verde. No celular, o usuário escolhe o próprio arquivo de som. Expected: toca na abertura seguinte; o toque pula e corta o som; remover volta ao silêncio.
- [ ] **Step 6:** Commit `feat(nevoa): som da abertura do usuário`.

### Task 8: Verificação no celular e primeira sincronização

- [ ] **Step 1:** Checklist no Poco F6 Pro com o APK final, com o usuário confirmando cada item: abertura toca, é pulável e pode ser desligada; a câmera termina no fundo da biblioteca sem salto brusco; fileira, painel e rodapé corretos; título longo com no máximo 2 linhas; navegação completa por controle e por toque; explosão uma vez só; o Scooby-Doo abre e roda; ao voltar do jogo o fundo anima de novo; instalar o APK seguinte por cima mantém a BIOS e as configurações.
- [ ] **Step 2:** Ensinar a sincronização: no GitHub, **Sync fork** em `master`; localmente, `git fetch upstream && git merge upstream/master` em `nevoa`; resolver os conflitos (esperados só nos ganchos); push; CI verde.
- [ ] **Step 3:** Registrar no `tools/nevoa-intro/README.md` uma seção "Manutenção" com a lista dos arquivos originais tocados, para facilitar conflitos futuros. Commit e push.
