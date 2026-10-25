# Diagnóstico original de PS2

```
python -m pip install -r tools/nevoa-diagnostic/requirements.txt
python tools/nevoa-diagnostic/build_iso.py
```

Cria `out/Nevoa_Diagnostico.iso`, um disco ISO9660 com `SYSTEM.CNF` e um ELF
EE/MIPS escrito neste projeto. Inicializa uma saída NTSC com um fundo azul
que pulsa lentamente. Não acessa cartões de memória, rede ou módulos IOP.
Não inclui BIOS, bibliotecas, ROMs, imagens, áudio ou código de jogos comerciais.

Serve para verificar leitura de ISO, identidade de um segundo disco, boot,
apresentação de quadros e ciclo de estados. Não é teste de compatibilidade com
um segundo jogo comercial nem de botões, gamepad físico, IOP, VU, áudio ou gameplay.
O resultado em hardware real não foi certificado.

Referências de interface, sem incorporar código delas:
[syscall SetGsCrt no PS2SDK](https://github.com/ps2dev/ps2sdk/blob/master/ee/kernel/include/syscallnr.h),
[convenção do syscall](https://github.com/ps2dev/ps2sdk/blob/master/ee/kernel/src/kernel.S)
e definições de registradores em `pcsx2/GS/GSRegs.h` deste repositório.
