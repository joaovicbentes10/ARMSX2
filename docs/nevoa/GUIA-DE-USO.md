# Guia de uso

[Índice do Névoa](../NEVOA.md)

## Instalação e atualização

1. No fork do projeto, abra **Actions → Nevoa Android**.
2. Escolha uma execução concluída com sucesso da branch `nevoa`.
3. Baixe `armsx2-nevoa-android`, extraia o ZIP e instale o APK.

O aplicativo usa `com.armsx2.nevoa` e pode coexistir com o ARMSX2 oficial. Dados e permissões dos dois aplicativos são independentes. Não há migração automática da instalação oficial.

APKs assinados com a chave estável do Névoa podem ser instalados por cima, preservando os dados. Desinstalar ou limpar o armazenamento pode apagar configurações e arquivos internos; isso não é necessário para atualizar normalmente.

## Primeiro uso

Use o fluxo existente do ARMSX2 para selecionar sua BIOS, autorizar pastas de jogos e ajustar os controles. Sem uma biblioteca utilizável, a tela clássica assume o fluxo de preparação. A abertura e a tela principal usam orientação horizontal com os dois lados de paisagem disponíveis.

### Orientação dos jogos

O padrão de orientação do Névoa mantém também os jogos em paisagem, inclusive configurações antigas com valor Dispositivo. Retrato ou rotação automática continuam disponíveis quando escolhidos explicitamente nas configurações do jogo.

## Abertura

A abertura própria dura aproximadamente oito segundos e termina no cenário da biblioteca. Ela usa o som padrão Névoa Suspense V6 (8 segundos), fornecido pelo responsável pelo projeto. Nas opções de App, é possível escolher outro áudio para substituí-lo. O arquivo personalizado é copiado para o armazenamento privado do aplicativo; Restaurar som padrão volta à faixa incluída.

O limite do áudio é **5 MiB**, com tipo MIME `audio/*`. Arquivos informados pelo provedor como `application/ogg` são atualmente recusados, mesmo que sejam áudio.

As opções originais também permitem habilitar a abertura e escolher um vídeo próprio. O áudio Névoa acompanha somente o vídeo padrão; um vídeo personalizado usa seu próprio áudio. Toque e Voltar permitem pular a abertura.

## Biblioteca

Uma fileira horizontal apresenta caixas de DVD. A selecionada cresce e recebe brilho; ao lado aparecem título e informações de última sessão e tempo jogado, quando disponíveis.

| Ação | Resultado |
|---|---|
| Jogar | Destaca a capa selecionada e inicia o item em uma passagem suave |
| Configurar | Abre as opções do jogo selecionado |
| Menu | Abre a navegação lateral |
| Buscar | Abre o teclado existente para filtrar os jogos |
| Ordem | Alterna a ordenação |
| Biblioteca completa | Abre a grade/lista/estante com visual Névoa e os recursos completos |

Por toque, tocar em outra capa muda a seleção; tocar na selecionada inicia o jogo. Jogar também inicia o item. A capa, o brilho e o painel de informações animam juntos ao trocar de jogo. Arrastar para a esquerda seleciona o próximo jogo; para a direita, o anterior. Cada gesto avança um item e movimentos pequenos não mudam a seleção. Arrastar não inicia o jogo. Durante a transição, as ações ficam bloqueadas para evitar disparos duplicados.

Por controle, o direcional percorre os elementos e a fileira. Confirmar sobre a fileira inicia o item selecionado. Y/△ abre a biblioteca completa. Ações do topo também participam da navegação por controle.

### Busca

O teclado aceita toque e controle e respeita a opção existente de usar o teclado do sistema. A consulta reutiliza a lógica original: título, nomes alternativos, serial/código e extensão, sem diferenciar maiúsculas de minúsculas.

Use **Done** no teclado atual para concluir. Alguns rótulos do teclado original ainda estão em inglês. Enquanto houver consulta, aparecem o texto pesquisado e **Limpar busca**. Uma consulta sem correspondências mostra “Nenhum jogo encontrado” sem sair da tela Névoa.

### Ordenação

| Opção | Critério |
|---|---|
| Título | Nome de ordenação fornecido pelo ARMSX2 |
| Recentes | Lista de recentes primeiro; demais jogos por nome |
| Compatibilidade | Classificação de compatibilidade decrescente; desempate por nome |

A ordem inicial do estado atual é Título. Busca e ordem compartilham o ViewModel da biblioteca clássica. A ordem não é gravada pelo Névoa para sobreviver ao encerramento do processo.

A seleção acompanha o URI do jogo quando ele muda de posição. Se o item sair dos resultados, o primeiro resultado disponível passa a ser selecionado.

## Configurações e telas auxiliares

| Grupo | Seções, conforme o contexto |
|---|---|
| Sistema | Geral, informações do jogo, desempenho, avançado e sobre |
| Imagem e som | Gráficos e áudio |
| Controle | Controles, atalhos, skins e controles na tela |
| Conexões e extras | Rede e patches |

O escopo global ou por jogo determina as opções disponíveis. Busca e reset continuam no fluxo original. Descrições ficam no ícone de informação; detalhes secundários ficam recolhidos.

BIOS, cartões de memória, saves, controles, patches, texturas e conquistas receberam ajustes visuais. As operações e os dados continuam sob responsabilidade dos componentes do ARMSX2.

No menu durante a emulação, as ações principais incluem Continuar, Avanço rápido, Salvar e Carregar. Slots, ações técnicas, exibição e detalhes de conquistas ficam em seções recolhíveis. Os saves ainda não aparecem no painel da biblioteca Névoa; use o gerenciamento existente.

## Dúvidas comuns

| Situação | Conferir |
|---|---|
| Só aparece a BIOS | Pastas autorizadas e jogos encontrados na biblioteca completa |
| Busca não encontra um jogo | Consulta, filtros e categorias existentes na biblioteca completa |
| Fundo estático | Fallback previsto em Android anterior ao 13 ou falha ao compilar o shader |
| Áudio recusado | Tipo MIME e limite de 5 MiB |
| Vídeo próprio sem áudio Névoa | Esse áudio só acompanha o vídeo padrão |
| Atualização recusada pelo Android | Application ID e assinatura; não desinstalar como primeira tentativa |

Consulte os [limites de validação e problemas conhecidos](VALIDACAO-E-PENDENCIAS.md) para distinguir uma pendência de uma regressão confirmada.
