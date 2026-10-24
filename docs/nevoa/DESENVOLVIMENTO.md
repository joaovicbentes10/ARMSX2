# Desenvolvimento e manutenção

[Índice do Névoa](../NEVOA.md)

## Preparar uma sessão

Leia os guias afetados e a [passagem de contexto](../CODEX-HANDOFF.md). Antes de editar:

```powershell
Set-Location C:/Projetos/armsx2-nevoa
git status --short
git branch --show-current
git log -5 --oneline
```

Preservar alterações locais existentes. Trabalhar em uma função por vez, ou duas quando uma for simples. Limitar a leitura e a inspeção do celular ao alcance da mudança.

## Ambiente

| Item | Referência atual |
|---|---|
| Módulo | `platforms/android/app` |
| Gradle | Wrapper em `platforms/android` |
| JDK no CI | 17 |
| Compile/target SDK | 37 |
| Min SDK padrão | 26 |
| NDK do CI | `29.0.14206865` |
| CMake do CI | `3.31.6` |
| ABI principal | ARM64 |
| Flavors | `github` e `play` |

As fontes de verdade são `app/build.gradle.kts`, `gradle/libs.versions.toml`, o wrapper e o workflow. Não alterar versões apenas para coincidir com esta tabela depois que esses arquivos evoluírem.

No computador de referência, o SDK está em `C:\Users\joaov\AppData\Local\Android\Sdk`. Na validação de 24/10, o NDK 29 local estava incompleto; o NDK `30.0.16248370` e o JDK 21 estavam disponíveis. Isso não mudou as versões do CI.

## Build oficial pelo GitHub Actions

O [workflow Névoa](../../.github/workflows/nevoa-android.yml) roda por push em `nevoa` ou manualmente por `workflow_dispatch`. Uma nova execução na mesma referência cancela a anterior conforme sua configuração de concorrência.

O job faz checkout com submódulos, prepara JDK/SDK/NDK/CMake, busca dependências do shaderc, instala o target Rust Android, configura assinatura e executa:

```bash
cd platforms/android
./gradlew :app:testGithubDebugUnitTest :app:assembleGithubRelease \
  -Parmsx2.applicationId=com.armsx2.nevoa \
  '-Parmsx2.appLabel=ARMSX2 Névoa'
```

O núcleo nativo torna o build completo demorado: o histórico registra cerca de 40 minutos, sem garantia de duração. O artefato `armsx2-nevoa-android` contém os APKs de `app/build/outputs/apk/`.

Em falhas, o workflow tenta publicar trechos do erro Gradle como annotations. Consultar o erro concreto e os logs antes de repetir o build ou alterar código.

## Compilação e testes locais de Kotlin

Com as ferramentas instaladas:

```powershell
Set-Location C:/Projetos/armsx2-nevoa/platforms/android
$env:ANDROID_HOME = 'C:/Users/joaov/AppData/Local/Android/Sdk'
./gradlew.bat :app:compileGithubDebugKotlin `
  '-Parmsx2.applicationId=com.armsx2.nevoa' --console=plain
./gradlew.bat :app:testGithubDebugUnitTest `
  --tests 'com.armsx2.nevoa.*' `
  '-Parmsx2.applicationId=com.armsx2.nevoa' --console=plain
```

No PowerShell, colocar os argumentos `-P...` com pontos e valores entre aspas. Usar tarefas com flavor, como `testGithubDebugUnitTest`; os testes unitários desse fluxo usam debug, não uma tarefa release.

Para configurar o projeto com o NDK íntegro disponível, foi usada localmente a propriedade `'-Parmsx2.ndkVersion=30.0.16248370'`. Isso serviu à verificação de Kotlin e ao empacotamento com núcleo já compilado; não comprova equivalência de uma recompilação nativa usando outro NDK.

Relatórios: `app/build/reports/tests/testGithubDebugUnitTest/`. XMLs: `app/build/test-results/testGithubDebugUnitTest/`. Há limitações da suíte completa no Windows descritas no [registro de validação](VALIDACAO-E-PENDENCIAS.md).

## Iteração de interface com núcleo já compilado

Sem alterações nativas, pode-se validar a interface com as bibliotecas do último APK confiável do mesmo projeto, evitando recompilar o emulador:

1. Registrar o APK-base e seu commit verde.
2. Extrair `lib/arm64-v8a/*.so` para uma pasta local fora do código versionado.
3. Usar um init script Gradle temporário para desabilitar o caminho CMake apenas nessa execução e apontar `sourceSets.main.jniLibs` para essa pasta.
4. Resolver duplicatas idênticas também fornecidas por dependências; não substituir silenciosamente bibliotecas diferentes.
5. Gerar `assembleGithubRelease` com application ID, rótulo e chave estável do Névoa.
6. Conferir o SHA-256 de `libemucore_4k.so` dentro do APK final contra a origem antes de instalar.

Na sessão de referência, o init script está em `%TEMP%\nevoa-validation-search\prebuilt.init.gradle`, e as bibliotecas na subpasta `jni`. São recursos locais não versionados; sua existência não é garantida em outra máquina. Atualizar a origem se houver mudanças nativas ou de dependências.

Não versionar um desvio permanente que desabilite a compilação nativa do CI. Esse método comprova a interface e seu empacotamento com aquele núcleo, não uma recompilação das fontes nativas atuais.

## Assinatura e dados

A chave estável fica fora do repositório, em `C:\Projetos\nevoa-keystore`. Secrets do GitHub:

- `NEVOA_KEYSTORE_B64`
- `NEVOA_KEYSTORE_PASSWORD`
- `NEVOA_KEY_ALIAS`
- `NEVOA_KEY_PASSWORD`

O workflow escreve as propriedades esperadas pelo Gradle. Localmente, `platforms/android/armsx2_keystore.properties` é ignorado pelo Git e referencia a chave externa. Campos: `storeFile`, `storePassword`, `keyAlias` e `keyPassword`. Nunca registrar seus valores em documentação, comandos exibidos ou logs.

Sem essa configuração, a variante release pode recorrer à chave debug e não substituir a instalação assinada do Névoa. Investigar a assinatura antes de considerar desinstalar o aplicativo.

Manter backup protegido da chave. Sem a mesma assinatura, não é possível atualizar normalmente a instalação existente. Remover propriedades temporárias contendo senhas ao encerrar a validação.

## ADB e inspeção pontual

O executável não estava no PATH da sessão de referência:

```powershell
$nevoaAdb = 'C:/Users/joaov/AppData/Local/Android/Sdk/platform-tools/adb.exe'
& $nevoaAdb devices -l
& $nevoaAdb install -r 'C:/caminho/app-github-release.apk'
& $nevoaAdb shell am start -n com.armsx2.nevoa/com.armsx2.BootSplashActivity
```

Com vários dispositivos, usar `-s SERIAL`. O aparelho deve aparecer como `device`; `unauthorized` exige autorização no Android.

```powershell
& $nevoaAdb shell screencap -p /sdcard/nevoa-check.png
& $nevoaAdb pull /sdcard/nevoa-check.png 'C:/caminho/nevoa-check.png'
```

Para transições, preferir um trecho curto de `screenrecord` a várias capturas repetidas. Filtrar `logcat` pelo problema. Não tratar abertura da BIOS como teste de jogo real.

## Regerar a abertura

Requer Node.js/npm, Puppeteer com navegador disponível e `ffmpeg` no PATH:

```powershell
Set-Location C:/Projetos/armsx2-nevoa/tools/nevoa-intro
npm ci
node render.mjs
node render.mjs --still 3.4,5.5
```

O render normal gera vídeo e imagem final; `--still` produz apenas quadros. `--crf 23` ajusta a qualidade do vídeo, cujo padrão no script é CRF 20.

O gerador usa WebGL2/SwiftShader, `window.renderFrame(t)` determinístico e `window.selfCheck()`. A falha da comparação interrompe o render. Após conferir o resultado, copiar os dois arquivos:

| Saída em `tools/nevoa-intro/out/` | Destino relativo à raiz |
|---|---|
| `boot_intro.mp4` | `platforms/android/app/src/main/res/raw/boot_intro.mp4` |
| `nevoa_last_frame.webp` | `platforms/android/app/src/main/res/drawable-nodpi/nevoa_last_frame.webp` |

Manter `intro.html`, `NevoaShader.kt` e `NevoaPalette.kt` sincronizados. Conferir preenchimento, tema inicial e passagem no aparelho.

## Incorporar o upstream

Com o trabalho local salvo e revisado:

```powershell
git switch nevoa
git fetch upstream
git log --oneline HEAD..upstream/master
git merge upstream/master
```

Resolver conflitos preservando a lógica atualizada do upstream e reaplicando a apresentação Névoa. Não escolher um lado inteiro automaticamente. A [arquitetura](ARQUITETURA.md) lista as principais integrações alteradas.

Executar os checks relevantes, gerar APK e conferir as áreas atingidas antes de `git push origin nevoa`. Se uma atualização quebrar o app, identificar a causa e preparar uma reversão revisável; não apagar alterações locais nem reescrever histórico publicado como atalho.

## Entregar um incremento

1. Revisar o diff e `git diff --check`.
2. Compilar e executar testes apropriados.
3. Instalar e conferir os estados modificados, quando houver interface.
4. Registrar commit, origem do APK, evidências e limites.
5. Atualizar os guias afetados e a passagem de contexto.
6. Commit com a identidade do responsável e envio à branch de trabalho quando a entrega incluir publicação no repositório.

Push aceito não significa CI verde. Evidências locais e remotas são distintas. Alterações apenas documentais podem usar `[skip ci]`, como no histórico, evitando uma recompilação nativa desnecessária.
