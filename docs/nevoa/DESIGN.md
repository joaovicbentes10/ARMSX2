# Identidade visual e interação

[Índice do Névoa](../NEVOA.md)

## Direção aprovada

“Névoa da BIOS” combina organização limpa de console moderno com a atmosfera escura de uma abertura de PS2. A cena é própria. Não incorporar logotipos, sons, fontes ou imagens proprietárias da Sony.

O responsável escolheu **torres finas e translúcidas**. A variante grossa foi testada e rejeitada. Preservar profundidade, transparência, bases dissolvidas na névoa e movimento lento.

## Paleta e tipografia

Fonte de verdade: [NevoaPalette.kt](../../platforms/android/app/src/main/java/com/armsx2/nevoa/theme/NevoaPalette.kt).

| Uso | Valor |
|---|---|
| Fundo | `#050812` |
| Névoa | `#5A8CFF` |
| Azul e seleção | `#9CC3FF` |
| Violeta | `#B9A3FF` |
| Ciano | `#7FE0FF` |
| Texto | `#E8EEFF` |
| Texto secundário | `#9AA6C8` |
| Vidro | Branco a 8% |
| Borda | Branco a 20% |
| Superfície selecionada | `#213A69` |

O tema compõe vidro sobre o fundo quando precisa de uma cor opaca. Manter MaterialTheme, ThemeBridge e holders legados coerentes.

A base é `ArmsTypography`; títulos e displays recebem peso leve e espaçamento de `0.06.em`. Textos menores e controles precisam continuar legíveis em paisagem.

## Hierarquia

Mostrar primeiro ações e valores essenciais. Descrições extensas ficam no ícone de informação; opções secundárias em seções recolhidas. Evitar rótulos redundantes, cartões decorativos e todas as ações com o mesmo destaque.

Na biblioteca, Jogar é principal. Menu, Buscar, Ordem, Biblioteca completa e Configurar são discretos. O rodapé redundante com a instrução de X foi removido; sua presença na especificação inicial é histórica.

## Movimento e continuidade

A abertura tem formato 20:9, 2400 × 1080, 30 fps e cerca de oito segundos. O fundo se move lentamente; configurações usam modo calmo. A entrada da biblioteca acontece uma vez por processo, e a transição para o jogo dissolve a própria cena em névoa azul e preto, em 520 ms, sem círculo branco ou partículas avulsas. O relógio do fundo para durante a emulação ativa ou pausada.

Gerador WebGL, shader AGSL e fundo inicial da janela precisam manter a mesma composição na passagem. Mudou cores, hash, torres ou enquadramento: revisar os três e regenerar vídeo e imagem final.

O hash compartilhado usa multiplicação e soma. A versão com `sin()` produzia posições diferentes entre desktop e GPU do celular; não reintroduzir sem comprovar equivalência. O `selfCheck()` verifica numericamente as cenas renderizadas pelo gerador, mas não substitui conferir a passagem no aparelho.

## Entrada e renderização

Cada nova ação deve funcionar por toque e controle, com foco visível e registro nos componentes existentes. Reutilizar `LibraryKeyboard` e overlays preparados para gamepad; evitar modais em novas janelas Compose.

Para brilho colorido, usar desenho próprio com `drawBehind`. Sombras coloridas de `Modifier.shadow` já produziram caixas pretas em Adreno/Mali no histórico do projeto.

## Verificação visual proporcional

1. Identificar a tela e os estados modificados.
2. Compilar e instalar a versão correspondente.
3. Capturar apenas os estados necessários, como busca vazia ou seção expandida.
4. Conferir leitura, alinhamento, cortes, foco e ações em paisagem.
5. Mostrar o resultado e registrar o que foi realmente testado.

Não percorrer o app inteiro em todo ajuste. Ampliar a inspeção quando uma dependência compartilhada ou uma falha indicar impacto adicional.
