"""Build a tiny original EE/MIPS diagnostic ELF and an ISO9660 disc, without a PS2 SDK.

Only initializes NTSC scanout and slowly pulses the GS background. No game assets,
IOP modules, memory-card access or controller emulation are included.
"""
from pathlib import Path
import hashlib
import io
import struct
import pycdlib

OUT = Path(__file__).resolve().parent / "out"
BASE = 0x00100000
code = []
labels = {}
fixups = []


def emit(word):
    code.append(word)


def imm(op, rt, rs, value):
    emit((op << 26) | (rs << 21) | (rt << 16) | (value & 0xFFFF))


def li(rt, value):
    imm(0x0F, rt, 0, (value >> 16) & 0xFFFF)
    imm(0x0D, rt, rt, value & 0xFFFF)


def li64(rt, value):
    imm(0x0D, rt, 0, value >> 48)
    for shift in (32, 16, 0):
        emit((rt << 16) | (rt << 11) | (16 << 6) | 0x38)  # dsll rt,rt,16
        imm(0x0D, rt, rt, value >> shift)


def branch(op, rs, rt, label):
    fixups.append((len(code), op, rs, rt, label))
    emit(0)
    emit(0)  # branch delay slot


def label(name):
    labels[name] = len(code)


def build():
    # SetGsCrt(1, 2, 0): interlaced NTSC field mode; PS2 syscall number in v1.
    imm(9, 4, 0, 1)
    imm(9, 5, 0, 2)
    imm(9, 6, 0, 0)
    imm(9, 3, 0, 2)
    emit(0x0000000C)
    li(8, 0x12000000)  # GS privileged registers, t0
    li(9, 0x00001400)  # DISPFB1: FBW=10, 640 pixels, RGBA32
    imm(0x3F, 9, 8, 0x70)
    display = 636 | (50 << 12) | (3 << 23) | (2559 << 32) | (447 << 44)
    li64(9, display)
    imm(0x3F, 9, 8, 0x80)
    li(9, 0xA1)  # PMODE: circuit 1 enabled, fixed alpha=0, blend against BGCOLOR.
    imm(0x3F, 9, 8, 0)
    li(16, 0)
    li(17, 0)
    label("field")
    imm(0x23, 10, 8, 0x1000)  # GS CSR.FIELD toggles each field; no interrupt acknowledgement.
    imm(0x0C, 10, 10, 0x2000)
    branch(4, 10, 17, "field")
    emit((10 << 21) | (0 << 16) | (17 << 11) | 0x25)  # or s1,t2,zero
    imm(9, 16, 16, 1)
    imm(0x0C, 16, 16, 255)
    imm(0x0A, 11, 16, 128)
    branch(5, 11, 0, "rising")
    li(10, 255)
    emit((10 << 21) | (16 << 16) | (10 << 11) | 0x23)  # subu t2,t2,s0
    branch(4, 0, 0, "paint")
    label("rising")
    emit((16 << 21) | (0 << 16) | (10 << 11) | 0x25)
    label("paint")
    emit((10 << 16) | (10 << 11) | (1 << 6) | 0x02)  # srl t2,t2,1
    imm(9, 10, 10, 80)
    emit((10 << 16) | (10 << 11) | (16 << 6))  # sll t2,t2,16, blue channel
    imm(0x0D, 10, 10, 0x2206)
    imm(0x3F, 10, 8, 0xE0)  # BGCOLOR
    branch(4, 0, 0, "field")
    for at, op, rs, rt, target in fixups:
        offset = labels[target] - at - 1
        assert -32768 <= offset <= 32767
        code[at] = (op << 26) | (rs << 21) | (rt << 16) | (offset & 0xFFFF)
    payload = b"".join(struct.pack("<I", word) for word in code)
    payload += b"NEVOA ORIGINAL DIAGNOSTIC 1.0\0"
    ident = b"\x7fELF\x01\x01\x01" + bytes(9)
    header = ident + struct.pack("<HHIIIIIHHHHHH", 2, 8, 1, BASE, 52, 0, 0x20924001, 52, 32, 1, 0, 0, 0)
    segment = struct.pack("<IIIIIIII", 1, 0x1000, BASE, BASE, len(payload), len(payload), 5, 0x1000)
    elf = (header + segment).ljust(0x1000, b"\0") + payload
    cnf = b"BOOT2 = cdrom0:\\NEVO_000.01;1\r\nVER = 1.00\r\nVMODE = NTSC\r\n"
    OUT.mkdir(parents=True, exist_ok=True)
    (OUT / "NEVO_000.01").write_bytes(elf)
    (OUT / "SYSTEM.CNF").write_bytes(cnf)
    iso = pycdlib.PyCdlib()
    iso.new(interchange_level=1, vol_ident="NEVOA_DIAGNOSTIC")
    iso.add_fp(io.BytesIO(cnf), len(cnf), iso_path="/SYSTEM.CNF;1")
    iso.add_fp(io.BytesIO(elf), len(elf), iso_path="/NEVO_000.01;1")
    path = OUT / "Nevoa_Diagnostico.iso"
    iso.write(str(path))
    iso.close()
    verify = pycdlib.PyCdlib()
    verify.open(str(path))
    recovered = io.BytesIO()
    verify.get_file_from_iso_fp(recovered, iso_path="/NEVO_000.01;1")
    verify.close()
    assert recovered.getvalue() == elf
    print(f"{path}: {path.stat().st_size} bytes, ELF entry {BASE:08X}, SHA256 {hashlib.sha256(path.read_bytes()).hexdigest()}")


if __name__ == "__main__":
    build()
