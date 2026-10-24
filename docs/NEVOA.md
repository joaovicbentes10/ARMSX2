# ARMSX2 Névoa

Documentação do projeto pessoal **ARMSX2 Névoa**: uma interface Android para o ARMSX2 com a direção visual “Névoa da BIOS”, organização de console moderno, fundo escuro, torres finas e translúcidas e superfícies de vidro.

Referência: **24/10/2026**, base funcional `3222e96576` e incrementos registrados em Validação e pendências. O registro de validação distingue implementação de teste no aparelho.

## Guias

| Documento | Conteúdo |
|---|---|
| [Guia de uso](nevoa/GUIA-DE-USO.md) | Instalação, abertura, biblioteca, configurações e dúvidas |
| [Arquitetura](nevoa/ARQUITETURA.md) | Fluxos, mapa do código, dados e integrações |
| [Identidade visual](nevoa/DESIGN.md) | Paleta, composição, animações e regras de interação |
| [Desenvolvimento e manutenção](nevoa/DESENVOLVIMENTO.md) | Builds, assinatura, ADB, abertura e atualização do upstream |
| [Validação e pendências](nevoa/VALIDACAO-E-PENDENCIAS.md) | Evidências, limitações, problemas conhecidos e próximos incrementos |
| [Passagem de contexto](CODEX-HANDOFF.md) | Contexto operacional para retomar o trabalho |

## Objetivo e limites

O Névoa modifica a experiência visual e a organização da interface. O núcleo de emulação, recompiladores, renderizadores e compatibilidade dos jogos continuam sendo os do ARMSX2. Componentes e ViewModels originais são reaproveitados sempre que possível.

O alvo principal é o **Poco F6 Pro**, Snapdragon 8 Gen 2/SM8550, Android 16, tela de 3200 × 1440 em paisagem. Toque e gamepad precisam funcionar. Esse aparelho é a referência de validação, não uma garantia de comportamento idêntico em outros dispositivos.

O launcher Windows é uma etapa futura, em repositório separado: mesma direção visual, abrindo o **PCSX2 oficial** no jogo escolhido. A implementação ainda não começou.

## Identificação

| Item | Valor |
|---|---|
| Repositório | [joaovicbentes10/ARMSX2](https://github.com/joaovicbentes10/ARMSX2) |
| Upstream | [ARMSX2/ARMSX2](https://github.com/ARMSX2/ARMSX2) |
| Branch de trabalho | `nevoa` |
| Base inicial | `90126bdfea` |
| Checkout de referência | `C:\Projetos\armsx2-nevoa` |
| Nome Android | ARMSX2 Névoa |
| Application ID | `com.armsx2.nevoa` |
| Namespace Kotlin/Android | `com.armsx2` |
| Pacote das implementações Névoa | `com.armsx2.nevoa` |
| Workflow | [nevoa-android.yml](../.github/workflows/nevoa-android.yml) |
| Artefato | `armsx2-nevoa-android` |
| Licença do repositório | [GPLv3](../COPYING.GPLv3) |

O identificador separado permite coexistir com o ARMSX2 oficial. Os aplicativos têm dados independentes; não existe migração automática entre eles.

## Estado atual

Implementados: abertura própria com passagem contínua para a biblioteca, som padrão Névoa Suspense com opção de arquivo próprio, fundo animado com fallback estático, carrossel de capas, painel do jogo, explosão de luz ao iniciar, busca, ordenação, configurações em quatro grupos e revisão visual das principais telas auxiliares e do menu em jogo.

Pendentes: categorias na biblioteca Névoa, saves no painel do jogo, revisão dos controles de toque e validações adicionais com jogo real e conta de RetroAchievements autenticada. Ajustes técnicos menores estão no registro de pendências.

## Regras permanentes

- Textos novos da interface em português do Brasil.
- Abertura e identidade próprias, sem assets da Sony; áudio fornecido pelo usuário.
- Preservar as funções do ARMSX2 e a navegação por controle.
- Preferir código isolado no pacote Névoa e mudanças pequenas nas integrações originais.
- Validar mudanças visuais com capturas pontuais da área alterada.
- Trabalhar diretamente, sem delegação, conforme a preferência do responsável pelo projeto.
- Commits com a identidade configurada de João Victor, sem trailers `Co-Authored-By` ou atribuições de geração automática.
- Nunca versionar ou expor senhas, keystores ou outros segredos.

## Histórico e atualização desta documentação

A [especificação inicial](superpowers/specs/2026-10-23-nevoa-da-bios-design.md), o [plano da Parte 1](superpowers/plans/2026-10-23-nevoa-android-parte1.md) e o [plano da Parte 2](superpowers/plans/2026-10-23-nevoa-android-parte2.md) preservam decisões antigas. Alguns trechos descrevem intenções superadas; para manutenção, priorize estes guias e o código.

Ao entregar um incremento, atualize o registro de validação, os guias afetados e a passagem de contexto. Não apresente resultados antigos como testes recém-executados.
