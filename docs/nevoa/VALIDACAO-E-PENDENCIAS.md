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
| `LibraryLogicTest` | 15 | Seleção, ordem auxiliar, rótulos e fallback, incluindo busca vazia |
| `IntroSoundTest` | 4 | Validação de áudio |
| `NevoaPaletteTest` | 6 | Paleta e contraste |
| **Total** | **37** | **Todos passaram localmente** |

Esses testes não comprovam renderização, reprodução real de mídia ou todo o runtime.

A suíte completa local teve **260 testes: 249 passaram, 2 falharam e 9 foram ignorados**. As falhas foram fora do incremento:

| Teste | Evidência e motivo identificado |
|---|---|
| `TexturePackInstallerTest.aFileThatWillNotDeleteStillTakesThePackOutOfUse` | Falha na preparação de permissões (`setWritable(false)`), dependente do sistema de arquivos no Windows |
| `LicenseDocsTest.theAndroidListEndsWithTheApacheLicense` | Falha no prefixo da licença WebRTC; o parser remove `\n` inicial e preserva `\r` do checkout CRLF |

Não foram feitas mudanças nesses módulos para contornar a suíte. Preservar as falhas no relato; CI Linux é evidência separada.

## Próximos incrementos

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
