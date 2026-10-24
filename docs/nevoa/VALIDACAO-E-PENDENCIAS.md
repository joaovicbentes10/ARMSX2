# Validação e pendências

[Índice do Névoa](../NEVOA.md)

Referência: 24/10/2026. Este registro delimita a evidência disponível; implementação não significa teste de todos os caminhos.

## Situação por área

| Área | Evidência | Limite atual |
|---|---|---|
| Abertura e passagem | Implementadas e passagem aprovada no F6 Pro no histórico | Revalidar após alterar cena, vídeo ou tema inicial |
| Fundo e tema | AGSL, fallback, modo calmo e parada do relógio implementados | Falha real do shader e aparelhos antigos não exercitados nesta rodada |
| Biblioteca visual | Capas, painel, seleção e destaque de Jogar conferidos no F6 Pro | Biblioteca atual contém apenas o item da BIOS |
| Busca e ordem | `3222e96576`, release local instalada e inspecionada | Reordenação entre vários jogos ainda sem inspeção no aparelho |
| Configurações | Quatro grupos, descrições sob demanda e painel translúcido | Não equivale a retestar todas as opções globais e por jogo |
| BIOS, cartões e saves | Layout compacto e estados vazios implementados | Importação/exportação de todos os tipos não reexecutada nesta rodada |
| Controles, patches e texturas | APK `7c71555d6c` inspecionado conforme registro anterior | Controles de toque ainda pendentes de revisão |
| Conquistas | Entrada/login e detalhes recolhíveis inspecionados | Conta autenticada e jogo real não testados nesta rodada |
| Menu em jogo | `c51ef39430` verde no CI; abas, expansão, continuar e fechar conferidos | Inspeção com BIOS não comprova gameplay |
| Categorias na tela Névoa | Não integradas | Recursos existentes na biblioteca clássica |
| Saves no painel do jogo | Não implementados | Exigir associação confiável ao título |
| Launcher Windows | Arquitetura planejada | Implementação não iniciada |

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
| Preparação do áudio | `MediaPlayer.prepare()` síncrono |
| Ciclo do som | Liberação em conclusão/destruição existe; tratamento explícito de `onStop` pendente |
| Áudio com vídeo próprio | Linha pode continuar visível, embora só toque com o vídeo padrão |
| Recriação durante o burst | Estado pendente não é integralmente preservado |
| Textos e tema legados | Textos Névoa fixos fora de I18n e pontos antigos de tema a limpar |

O atalho Y/△ tinha registro de recuperação de foco insuficiente após a gaveta. Em `3222e96576`, o pedido de foco passou a reagir à gaveta e ao teclado. Retestar Y/△ nessas transições antes de encerrar o item; mudança no código não é comprovação de resolução.

O aviso de temperatura que cobria Menu foi centralizado anteriormente. A importação do chip de status foi preservada após ajuste. São histórico de correção, não pendências atuais confirmadas.

## Windows

Planejado: launcher separado com a mesma identidade, suporte a controle e abertura do PCSX2 oficial em tela cheia. Não criar um fork Windows do emulador para esse objetivo. Tecnologia, descoberta da instalação, argumentos e empacotamento precisam ser definidos e verificados ao iniciar a implementação.

## Manter o registro

Anotar commit, origem e assinatura do APK, ambiente, testes, capturas e caminhos não exercitados. Preservar resultados anteriores como histórico e corrigir o estado quando houver nova evidência.
