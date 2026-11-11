#!/usr/bin/env python3
"""Offline APK / DEX / ELF inspection for the DocScanner reverse-engineering work.

Why this exists: the heavy RE stack (Ghidra, apktool, jadx) is a JVM application
plus release-archive downloads. In a sandbox that can reach PyPI but not
`release-assets.githubusercontent.com` or any Maven mirror, none of it installs.
This script does the part of the job that needs no JVM, using three pure-wheel
dependencies:

    pip install --break-system-packages androguard capstone pyelftools

Verified working on this repository's own artifacts:

    tools/re/re_probe.py apk  adobe-scan/base.apk
    tools/re/re_probe.py so   indymobile/native_libs/libimageproc.so
    tools/re/re_probe.py dex  docs/original_apk_binaries/classes.dex

What it gives you that a text dump does not: the binary AndroidManifest parsed
properly, real JNI export names (the `Java_...` symbols), and actual ARM64
instructions at any address. That is enough to answer "what does this library
export and what does the call site do" without Ghidra.

What it does NOT do, and no amount of scripting fixes: it does not produce
Ghidra-quality decompiled C. Capstone gives instructions, not expressions. For
that, `tools/re/GHIDRA_NOTES.md` says what to install where.
"""
from __future__ import annotations

import argparse
import collections
import sys
from pathlib import Path

# androguard logs every parser step at DEBUG; silence it before importing the API.
try:  # pragma: no cover - display only
    import loguru

    loguru.logger.remove()
except Exception:  # pragma: no cover
    pass

INTERESTING = ("http://", "https://", "api.", "/v1/", "/v2/", "secret", "token", "key=", "bearer")


def _need(module: str, pip_name: str):
    try:
        return __import__(module, fromlist=["*"])
    except ImportError:
        sys.exit(f"missing dependency: pip install --break-system-packages {pip_name}")


# ---------------------------------------------------------------- APK / AXML


def cmd_apk(args: argparse.Namespace) -> int:
    _need("androguard", "androguard")
    from androguard.core.apk import APK

    apk = APK(str(Path(args.file)))
    print(f"file        : {args.file}")
    print(f"package     : {apk.get_package()}")
    print(f"versionName : {apk.get_androidversion_name()}")
    print(f"versionCode : {apk.get_androidversion_code()}")
    print(f"minSdk      : {apk.get_min_sdk_version()}   targetSdk: {apk.get_target_sdk_version()}")

    perms = sorted(apk.get_permissions())
    print(f"permissions : {len(perms)}")
    for p in perms:
        print(f"   {p}")

    activities = sorted(apk.get_activities())
    services = sorted(apk.get_services())
    receivers = sorted(apk.get_receivers())
    providers = sorted(apk.get_providers())
    print(f"activities  : {len(activities)}  services: {len(services)}  "
          f"receivers: {len(receivers)}  providers: {len(providers)}")

    if args.grep:
        needle = args.grep.lower()
        hits = [c for group, items in
                (("activity", activities), ("service", services),
                 ("receiver", receivers), ("provider", providers))
                for c in items if needle in c.lower()]
        print(f"\n--grep '{args.grep}': {len(hits)} match(es)")
        for c in hits:
            print(f"   {c}")
        # A declared-but-permission-guarded export is the interesting case: report
        # the guard too, or an absence claim is unverifiable.
        return 0 if hits else 1

    for c in activities[:args.limit]:
        print(f"   activity {c}")
    return 0


# ---------------------------------------------------------------------- ELF


def cmd_so(args: argparse.Namespace) -> int:
    _need("elftools", "pyelftools")
    from elftools.elf.elffile import ELFFile

    path = Path(args.file)
    with path.open("rb") as fh:
        elf = ELFFile(fh)
        print(f"file        : {path}")
        print(f"machine     : {elf['e_machine']}  class: ELF{elf.elfclass}")

        dyn = elf.get_section_by_name(".dynsym")
        syms = list(dyn.iter_symbols()) if dyn else []
        jni = sorted(s.name for s in syms if s.name.startswith("Java_"))
        print(f"dynamic syms: {len(syms)}   JNI exports: {len(jni)}")
        for name in jni:
            # Java_com_example_Class_method -> com.example.Class.method
            parts = name.split("_")[1:]
            if len(parts) >= 2:
                print(f"   {'.'.join(parts[:-1])} :: {parts[-1]}")
            else:
                print(f"   {name}")

        imports = sorted({s.name for s in syms if s.name and s['st_info']['type'] == 'STT_FUNC'
                          and s['st_shndx'] == 'SHN_UNDEF'})
        if imports:
            print(f"imports (undefined funcs): {len(imports)}")
            for name in imports[:args.limit]:
                print(f"   {name}")

        text = elf.get_section_by_name(".text")
        if text:
            print(f".text       : {len(text.data())} bytes @ 0x{text['sh_addr']:x}")

        if args.disasm:
            _need("capstone", "capstone")
            import capstone

            md = capstone.Cs(capstone.CS_ARCH_ARM64, capstone.CS_MODE_ARM)
            code = text.data()
            base = text["sh_addr"]
            if args.address is not None:
                offset = args.address - base
                if offset < 0 or offset >= len(code):
                    sys.exit(f"0x{args.address:x} is outside .text "
                             f"(0x{base:x}-0x{base + len(code):x})")
                print(f"\ndisassembly @ 0x{args.address:x}:")
                window = code[offset:offset + args.count * 4]
                origin = args.address
            else:
                print(f"\ndisassembly @ 0x{base:x} (start of .text):")
                window = code[:args.count * 4]
                origin = base
            for ins in md.disasm(window, origin):
                print(f"   0x{ins.address:x}: {ins.mnemonic:<9} {ins.op_str}")
    return 0


# ---------------------------------------------------------------------- DEX


def cmd_dex(args: argparse.Namespace) -> int:
    _need("androguard", "androguard")
    from androguard.core.dex import DEX

    path = Path(args.file)
    dex = DEX(path.read_bytes())
    classes = sorted(c.get_name() for c in dex.get_classes())
    print(f"file        : {path}  ({path.stat().st_size:,} bytes)")
    print(f"classes     : {len(classes)}")

    packages = collections.Counter()
    for name in classes:
        # La/b/c; -> a/b
        body = name[1:-1] if name.startswith("L") and name.endswith(";") else name
        parts = body.split("/")
        packages["/".join(parts[:2]) if len(parts) > 1 else "(default)"] += 1

    print("top packages:")
    for pkg, count in packages.most_common(args.limit):
        print(f"   {count:>5}  {pkg}")

    if args.grep:
        needle = args.grep.lower()
        hits = [c for c in classes if needle in c.lower()]
        print(f"\n--grep '{args.grep}': {len(hits)} class(es)")
        for name in hits[:args.limit]:
            print(f"   {name}")
        return 0 if hits else 1
    return 0


# ------------------------------------------------------------------ strings


def cmd_strings(args: argparse.Namespace) -> int:
    """Cheap but effective: URLs / keys / endpoints in any binary."""
    import re

    data = Path(args.file).read_bytes()
    found: set[str] = set()
    for match in re.finditer(rb"[\x20-\x7e]{8,120}", data):
        text = match.group().decode("ascii", "ignore")
        if any(marker in text.lower() for marker in INTERESTING):
            found.add(text.strip())
        if len(found) >= args.limit:
            break
    print(f"interesting strings: {len(found)}")
    for text in sorted(found)[:args.limit]:
        print(f"   {text}")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    subs = parser.add_subparsers(dest="cmd", required=True)

    apk = subs.add_parser("apk", help="parse an APK's binary AndroidManifest")
    apk.add_argument("file")
    apk.add_argument("--grep", help="print components matching this substring; exit 1 if none")
    apk.add_argument("--limit", type=int, default=15)
    apk.set_defaults(func=cmd_apk)

    so = subs.add_parser("so", help="ELF metadata, JNI exports, disassembly")
    so.add_argument("file")
    so.add_argument("--disasm", action="store_true", help="disassemble ARM64 .text")
    so.add_argument("--address", type=lambda v: int(v, 0), help="start address (hex ok)")
    so.add_argument("--count", type=int, default=24, help="instructions to show")
    so.add_argument("--limit", type=int, default=25)
    so.set_defaults(func=cmd_so)

    dex = subs.add_parser("dex", help="class inventory for a .dex")
    dex.add_argument("file")
    dex.add_argument("--grep", help="print classes matching this substring; exit 1 if none")
    dex.add_argument("--limit", type=int, default=20)
    dex.set_defaults(func=cmd_dex)

    strings = subs.add_parser("strings", help="pull URLs/keys/endpoints out of any binary")
    strings.add_argument("file")
    strings.add_argument("--limit", type=int, default=40)
    strings.set_defaults(func=cmd_strings)

    args = parser.parse_args()
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
