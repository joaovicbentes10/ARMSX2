# Abertura do ARMSX2 Névoa

Gera `boot_intro.mp4` (2400×1080 = 20:9 do Poco F6 Pro, 30 fps, 8 s, sem áudio) e `nevoa_last_frame.webp` (fundo da janela do app enquanto ele abre) a partir de `intro.html`.

- `intro.html`: canvas WebGL2 com `window.renderFrame(t)` determinístico (PRNG com semente, sem relógio).
  O último quadro é o `NevoaBackground` em repouso (porta de `NevoaShader.kt` com `iTime = 0`);
  `window.selfCheck()` compara os dois e o render aborta se diferirem.
- `render.mjs`: Puppeteer (SwiftShader) renderiza os 240 quadros numa pasta temporária e chama o `ffmpeg`.

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
