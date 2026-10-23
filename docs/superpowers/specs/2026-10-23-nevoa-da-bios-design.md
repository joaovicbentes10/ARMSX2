# Névoa da BIOS — design

Data: 2026-10-23
Base: ARMSX2 (`github.com/ARMSX2/ARMSX2`, commit `90126bd`, GPLv3) no Android; PCSX2 oficial no Windows.

## Intenção

O usuário joga PS2 (começando por *Scooby-Doo! and the Spooky Swamp*, tradução PT-BR) num Poco F6 Pro e num notebook Windows. O ARMSX2 roda bem, mas o visual incomoda: "parece feito com IA básica", botões desordenados e confusos, da tela inicial até as configurações. Ele quer um app com cara de **PS5 com alma de PS2**: a organização limpa do PS5 com a atmosfera da abertura/BIOS do PS2.

Sucesso = abrir o app e sentir que é um console moderno da linha PS2, e achar qualquer configuração sem se perder.

## Metas

1. **Abertura** no estilo da abertura do PS2 (o que faltou na tentativa do Gemini): preto → partículas → névoa azul → torres de luz subindo → logo → câmera atravessa as torres até a biblioteca.
2. **Biblioteca** estilo PS5: fileira horizontal de capas no formato caixa de DVD do PS2; o selecionado fica maior, com borda brilhante e painel ao lado (nome, tempo jogado, saves no memory card, botões **Jogar** e **Configurar**); rodapé "Pressione ✕ para Iniciar". Fundo: universo escuro, torres ao longe, faixas de luz azul, partículas, tudo animado devagar. Cards de vidro translúcidos, tipografia fina e espaçada.
3. **Ao apertar Jogar**: explosão de luz azul antes de entrar no jogo.
4. **Todas as outras telas** (configurações, memory cards, saves, BIOS, controles, patches, texturas, conquistas, menu dentro do jogo etc.) no mesmo estilo, com fundo mais calmo, e **configurações reorganizadas** em poucas categorias claras.
5. **Pular abertura** com um toque/botão, e opção para desligá-la.
6. Interface em **PT-BR** (o ARMSX2 já tem `pt-BR` em `assets/i18n`).
7. **Acompanhar o ARMSX2**: a versão do usuário continua recebendo as melhorias do original.
8. **Windows**: um launcher próprio com a mesma abertura e biblioteca, que abre o PCSX2 oficial em tela cheia no jogo escolhido.

Referências visuais: mockup "B · Névoa da BIOS" (`.superpowers/brainstorm/.../direcao-visual-v2.html`) e o vídeo do Gemini do usuário (`D:\Downloads\gemini_generated_video_69f6cff1.mp4`, 10 s: torres → faixas de luz → carrossel de caixas → selecionado com painel → explosão de luz).

## Não-metas

- Mexer no núcleo do emulador (JIT, renderer, compatibilidade de jogos).
- Fork do PCSX2 no Windows; as configurações do PCSX2 continuam como são.
- iOS, Play Store, distribuição pública.
- Funcionalidades novas além das listadas; nenhuma função existente é removida.

## Anti-metas (falharia mesmo funcionando)

- Continuar com cara de template genérico.
- Derrubar o desempenho no jogo ou drenar bateria com o fundo animado.
- Quebrar a navegação por controle (gamepad). O build já falha se alguém usar `AlertDialog`/`ModalBottomSheet`/`DropdownMenu` do Compose, porque roubam o foco do controle.
- Embutir logo, som ou qualquer asset da Sony.
- Mexer tanto nos arquivos originais que puxar atualizações vire um pesadelo.

## Restrições

- **Sem assets da Sony.** A animação é recriada do zero. O "logo" final é uma marca própria. O som da abertura é um arquivo que o próprio usuário escolhe no app.
- **Visual isolado.** Telas novas em pacote próprio (ex.: `com.armsx2.nevoa`); nos arquivos originais, só os ganchos mínimos (navegação, tema, splash). Reaproveitar os ViewModels existentes (`HomeViewModel`, `SaveManagerViewModel`...) em vez de reescrever lógica.
- **Build sem PC pesado.** O APK é gerado pelo job `android` do `.github/workflows/build-all.yml` no fork do usuário (artefato `armsx2-android-arm64`). O usuário não instala Android Studio.
- **O usuário faz todo login e clique no GitHub**; o builder só orienta passo a passo. Push a partir desta máquina com o usuário autenticando na janela do Git Credential Manager.
- Alvo: Poco F6 Pro (Snapdragon 8s Gen 3) em paisagem; deve funcionar em toque e em controle.

## O que já existe no ARMSX2 e deve ser aproveitado

- Interface em **Jetpack Compose** (`platforms/android/app/src/main/java/com/armsx2/ui`, ~42 mil linhas, ~30 telas; configurações em `ui/settings/*Tab.kt`).
- **Abertura**: `BootSplashActivity` toca `res/raw/boot_intro.mp4`; `BootIntro.kt` já permite o usuário trocar por um vídeo próprio.
- **Fundo da biblioteca**: `LibraryBackground.kt` (imagem/GIF/WebP escolhido pelo usuário) e `XmbGlView.kt` (onda do PS3 em OpenGL; já existe infraestrutura GL).
- **Sons de menu** (`MenuSfx.kt`, `res/raw/sfx_*.wav`) e **música da biblioteca** (`LibraryMusic.kt`).
- **Cores** centralizadas em parte (`ui/Colors.kt`, `ui/theme/`); componentes comuns em `ui/common/ArmsComponents.kt`, `SettingControls.kt`, `settings/SettingsWidgets.kt`. Restilizar esses componentes espalha o visual para muitas telas de uma vez.

## Decisões tomadas

| Decisão | Quem decidiu |
|---|---|
| Direção visual "Névoa da BIOS" | usuário |
| Escopo: o app Android inteiro, incluindo configurações | usuário |
| Abertura estilo PS2 + fluxo descrito em Metas | usuário (aprovado) |
| Acompanhar as atualizações do ARMSX2 via fork | usuário |
| Windows via launcher + PCSX2 oficial | Claude (o usuário delegou: "o que for recomendado") |
| Visual em pacote isolado, ganchos mínimos no código original | Claude |
| Sem assets da Sony; som da abertura fornecido pelo usuário | Claude |
| Sincronizar com o original não precisa ser "um clique": conflitos são resolvidos numa sessão com o Claude quando aparecerem (o usuário aceitou: "você me chama e eu resolvo"). Restilizar os componentes comuns é permitido. | usuário |
| Assinatura estável: gerar uma keystore própria e guardá-la como secret do GitHub (o usuário cola o secret), para cada APK novo instalar por cima do anterior sem perder dados. O CI hoje assina com debug keystore criada a cada execução. | Claude |
| Base: o job `Android APK` do `build-all.yml` passou em `90126bd` (2026-10-23) e nos commits anteriores. Se uma sincronização futura quebrar o build, fixar no último commit verde. | Claude (verificado) |
| Dados: o app novo começa do zero; nada de importação/migração do ARMSX2 oficial. | usuário |
| Launcher Windows: app separado que imita o visual (não compartilha código com o Android), em repositório próprio do usuário, compilado por GitHub Actions como o APK. | Claude |

## Deixado para o builder

- Como renderizar abertura e fundo (vídeo pré-renderizado vs. OpenGL/AGSL em tempo real), desde que mantenha a fluidez e pare a animação durante o jogo.
- Agrupamento exato das categorias de configuração (poucas, claras, sem perder nenhuma opção).
- Tecnologia do launcher do Windows (precisa: tela cheia, controle, partida rápida, encontrar a pasta de jogos e as capas, abrir `pcsx2-qt.exe` com o jogo pela linha de comando; confirmar os argumentos do PCSX2).
- Ordem de entrega. Sugestão: tema + componentes comuns → biblioteca → abertura/transições → configurações → demais telas → launcher Windows.
- `applicationId` diferente para instalar ao lado do ARMSX2 oficial (verificar assinatura do APK gerado pelo CI).
