# ARMSX2 Névoa — manutenção

Versão pessoal do ARMSX2 com o visual "Névoa da BIOS" (abertura própria, biblioteca estilo PS5, fundo animado).
O emulador em si é o do ARMSX2, sem mudanças.

## Onde está cada coisa

- Código novo: `platforms/android/app/src/main/java/com/armsx2/nevoa/`
- Testes: `platforms/android/app/src/test/java/com/armsx2/nevoa/`
- Abertura (gerador do vídeo): `tools/nevoa-intro/` (veja o README de lá)
- Build automático: `.github/workflows/nevoa-android.yml` (roda a cada push na branch `nevoa`)
- Spec e plano: `docs/superpowers/`

## Arquivos originais do ARMSX2 que foram alterados

São os únicos lugares onde uma atualização do original pode dar conflito:

| Arquivo | O que mudou |
|---|---|
| `AndroidManifest.xml` | abertura e tela principal travadas em horizontal; tema `Theme.ARMSX2.Nevoa` na tela principal |
| `BootSplashActivity.kt` | vídeo usa a própria duração; som da abertura; troca para a tela principal sem animação |
| `Pasx2Application.kt` | 1 linha: cores da paleta Névoa |
| `navigation/AppNavigation.kt` | rota Home abre `NevoaHome` em vez de `HomeScreen` |
| `navigation/NavigationDrawer.kt` | menu lateral compacto com ícones consistentes e seções recolhíveis |
| `ui/theme/Theme.kt` | esquema de cores/tipografia Névoa; horizontal como padrão |
| `ui/settings/AppTab.kt` | linha "Som da abertura" |
| `ui/common/ArmsComponents.kt` | fundo Névoa calmo e componentes de vidro compartilhados |
| `ui/common/SettingControls.kt` | linha de opção com vidro Névoa |
| `ui/settings/SettingsWidgets.kt` | linhas de opções compactas; descrições abertas pelo ícone de informação |
| `ui/settingshub/SettingsScreen.kt` | painel de configurações translúcido; grupos e seções em navegação tipográfica |
| `res/values/themes.xml` | estilo `Theme.ARMSX2.Nevoa` |
| `res/layout/activity_boot_splash.xml` | `FillVideoView` no lugar de `VideoView` |
| `res/raw/boot_intro.mp4` | abertura própria |

## Puxar as melhorias do ARMSX2 original

1. No GitHub, abra o seu fork (`joaovicbentes10/ARMSX2`) e clique em **Sync fork → Update branch** (isso atualiza a `master`).
2. No PC, dentro de `C:\Projetos\armsx2-nevoa`:

   ```bash
   git checkout nevoa
   git fetch upstream
   git merge upstream/master
   ```

3. Se aparecer conflito, ele será num dos arquivos da tabela acima: mantenha a mudança do original e reaplique a linha da Névoa.
4. `git push origin nevoa` — o GitHub gera o APK novo em ~40 min (aba **Actions → Nevoa Android → Artifacts**).
5. Se o build quebrar depois de uma atualização, volte para o último commit que funcionava e espere o original corrigir.

## Instalar no celular

Baixe `armsx2-nevoa-android` em **Actions → (última execução verde) → Artifacts**, extraia o `.apk` e instale **por cima** — a chave de assinatura é fixa, então BIOS, saves e configurações continuam.

## Chave de assinatura

Fica em `C:\Projetos\nevoa-keystore` (fora do repositório) e nos secrets `NEVOA_*` do GitHub.
**Mantenha um backup dessa pasta.** Sem ela não dá para instalar atualizações por cima.

## Regerar a abertura

Veja `tools/nevoa-intro/README.md`. Depois de mudar `intro.html`, rode `node tools/nevoa-intro/render.mjs`
e copie `out/boot_intro.mp4` para `res/raw/` e `out/nevoa_last_frame.webp` para `res/drawable-nodpi/`.
Se mudar o desenho das torres, mude igual em `nevoa/background/NevoaShader.kt` — o script recusa gerar o
vídeo se o último quadro não bater com o fundo da biblioteca.
