# Abertura do ARMSX2 Névoa

Gera `boot_intro.mp4` (2400×1080 = 20:9 do Poco F6 Pro, 30 fps, 8 s, com a trilha V6 em AAC) e `nevoa_last_frame.webp` (fundo da janela do app enquanto ele abre) a partir de `intro.html`.

- `intro.html`: canvas WebGL2 com `window.renderFrame(t)` determinístico (PRNG com semente, sem relógio).
  O último quadro é o `NevoaBackground` em repouso (porta de `NevoaShader.kt` com `iTime = 0`);
  `window.selfCheck()` compara os dois e o render aborta se diferirem.
- `render.mjs`: Puppeteer (SwiftShader) renderiza os 240 quadros numa pasta temporária e chama o `ffmpeg`, incluindo `res/raw/nevoa_intro_sound.mp3` como trilha de áudio. Vídeo e áudio começam em zero e usam o mesmo relógio no Android.

## Regerar

```sh
cd tools/nevoa-intro
npm install
node render.mjs            # -> out/boot_intro.mp4 (opcional: --crf 23)
node render.mjs --still 3.4,5.5   # quadros soltos em out/still-<t>.png
```

Requer `ffmpeg` no PATH. Depois de aprovado, copie os dois arquivos (destinos relativos à raiz do repositório):

- `out/boot_intro.mp4` → `platforms/android/app/src/main/res/raw/boot_intro.mp4`.
- `out/nevoa_last_frame.webp` → `platforms/android/app/src/main/res/drawable-nodpi/nevoa_last_frame.webp`.

Se mudar cores ou composição em `NevoaShader.kt`/`NevoaPalette.kt`, replique em `intro.html`.

Veja também o [guia de desenvolvimento do Névoa](../../docs/nevoa/DESENVOLVIMENTO.md) e as [regras visuais](../../docs/nevoa/DESIGN.md).

A V6 fornecida tem 8 s. O efeito final e sua cauda permanecem dentro da abertura; a biblioteca só abre na conclusão audiovisual. Ao trocar a música, verificar duração, início e final antes de gerar: `-shortest` limita o MP4 à menor das duas faixas. O MP3 original permanece no repositório.

### Sincronismo do título

O ataque final da V6 começa em 7,0 s. O título ganha opacidade entre 6,97 e 7,10 s, assenta até 7,15 s e dissolve entre 7,50 e 7,95 s. O último quadro continua sem texto, compatível com a biblioteca.

Para mudar apenas a imagem mantendo os pacotes de áudio existentes, use `node render.mjs --audio-from caminho/abertura-anterior.mp4`. A opção copia a trilha de áudio sem recodificar; a geração padrão continua usando o MP3 V6.
