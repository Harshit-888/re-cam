#!/usr/bin/env python3
"""Assert that BenchmarkAuditActivity is exported only behind android.permission.DUMP.

Why this is a check and not a comment: the activity is `android:exported="true"`
in `app/src/main/AndroidManifest.xml`, in *every* build type, because it exists to
be started via `adb shell am start`. The `android.permission.DUMP` guard is the
only thing between it and any app on the device that can request that permission.
A refactor that drops the attribute still compiles, still passes every unit test,
and still ships — so it needs a gate.

Run against merged manifests inside APKs (what actually ships):

    tools/ci/check_audit_guard.py --aapt2 "$ANDROID_SDK_ROOT/build-tools/36.0.0/aapt2" \
        app/build/outputs/apk/*/*.apk

or against saved `aapt2 dump xmltree` output, which is how the tests run here
without an SDK:

    tools/ci/check_audit_guard.py fixtures/debug-manifest.txt
"""
from __future__ import annotations

import argparse
import re
import subprocess
import sys

ACTIVITY = "BenchmarkAuditActivity"


def blocks(lines: list[str]) -> list[list[str]]:
    """Split an `aapt2 dump xmltree` listing into per-node blocks.

    Each element starts with `E: <tag>`; attributes follow at greater indentation
    until the next `E:`. Splitting this way is the whole point: an earlier version
    of this check cut the block at the line *naming* the activity, which silently
    excluded the `android:permission` attribute one line below and made the gate
    impossible to satisfy.
    """
    out: list[list[str]] = []
    cur: list[str] | None = None
    for line in lines:
        if line.lstrip().startswith("E: "):
            if cur:
                out.append(cur)
            cur = [line]
        elif cur is not None:
            cur.append(line)
    if cur:
        out.append(cur)
    return out


def dump_manifest(apk: str, aapt2: str) -> list[str]:
    proc = subprocess.run(
        [aapt2, "dump", "xmltree", "--file", "AndroidManifest.xml", apk],
        capture_output=True, text=True,
    )
    if proc.returncode != 0:
        sys.exit(f"aapt2 failed on {apk}:\n{proc.stderr.strip()}")
    return proc.stdout.splitlines()


def check(label: str, lines: list[str], permission: str) -> bool:
    hits = [b for b in blocks(lines) if any(ACTIVITY in line for line in b)]
    if not hits:
        print(f"  {label}: audit activity absent — nothing to guard")
        return True

    ok = True
    for block in hits:
        text = "\n".join(block)
        exported = re.search(r'android:exported\([^)]*\)=\(type [^)]*\)(0x[0-9a-f]+)', text)
        granted = permission.split(".")[-1]
        guarded = "android:permission" in text and granted in text
        print(f"  {label}: audit activity present")
        for line in block:
            print(f"      {line.strip()}")
        if exported and int(exported.group(1), 16) == 0:
            print(f"  {label}: OK (exported=false)")
            continue
        if guarded:
            print(f"  {label}: OK (exported, guarded by {permission})")
        else:
            print(f"  {label}: FAIL — exported without {permission}", file=sys.stderr)
            ok = False
    return ok


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("paths", nargs="+", help="APKs, or saved xmltree dumps")
    parser.add_argument("--aapt2", help="aapt2 binary; required when given APKs")
    parser.add_argument("--permission", default="android.permission.DUMP")
    args = parser.parse_args()

    failures = 0
    for path in args.paths:
        if path.endswith(".apk"):
            if not args.aapt2:
                sys.exit("--aapt2 is required to read an APK")
            lines = dump_manifest(path, args.aapt2)
        else:
            with open(path, encoding="utf-8") as fh:
                lines = fh.read().splitlines()
        if not check(path, lines, args.permission):
            failures += 1

    if failures:
        print(f"{failures} manifest(s) failed the export guard", file=sys.stderr)
        return 1
    print("all manifests OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
