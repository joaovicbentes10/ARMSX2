# Som de iniciar jogo

`python tools/nevoa-sfx/generate_launch.py` gera `res/raw/nevoa_game_launch.wav`.

Versão Cristal V2 aprovada pelo usuário: acorde curto com síntese FM,
corpo grave estável, brilho suave e reverberação estéreo com reflexões amortecidas.
Não contém amostras externas ou áudio da Sony. Saída determinística com semente
fixa: PCM16 estéreo, 48 kHz, 880 ms, pico 0,78 e RMS aproximado de -19,09 dBFS.
Ataque e saída suaves evitam estalos; termina 20 ms antes da passagem de 900 ms
da home para o runtime. A abertura V6 é independente.

O evento `MenuSfx.Event.LAUNCH` usa o volume e a opção de efeitos de interface.
Pacotes próprios podem substituir apenas este efeito com `game_launch.wav`,
`game_launch.ogg` ou `game_launch.mp3`. A importação conserva as regras existentes.
