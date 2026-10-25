# Original APK Binary Artifacts

These ground-truth binaries were extracted directly from the original production APK package (`com.indymobileapp.document.scanner`):

| File | Size | Source | Purpose |
|:---|:---|:---|:---|
| [`libimageproc.so`](libimageproc.so) | 138,664 bytes | `split_config.arm64_v8a.apk` (`lib/arm64-v8a/`) | Native C/C++ image processing library. Contains all 7 JNI functions (`enhancePhoto`, `enhanceDocument`, `enhanceSignature`, `signatureFilter`, `detectPageBorder`, `perspectiveTransformRect`, `calculateIOU`). |
| [`classes.dex`](classes.dex) | 10,059,336 bytes | `base.apk` | Primary Dalvik executable code. |
| [`classes2.dex`](classes2.dex) | 9,215,892 bytes | `base.apk` | Secondary Dalvik executable code (contains pipeline models and utilities). |
| [`classes3.dex`](classes3.dex) | 7,067,980 bytes | `base.apk` | Third Dalvik split. |
| [`classes4.dex`](classes4.dex) | 7,052,964 bytes | `base.apk` | Fourth Dalvik split. |

### Tooling & Inspection Notes for Arena.ai
- **Native Disassembly**: Disassembly, dynamic symbols, and C decompilations are documented in [`docs/ORIGINAL_APP_FILTER_SPEC.md`](../ORIGINAL_APP_FILTER_SPEC.md).
- **Bytecode Inspection**: Smali and JADX decompilations for the key pipeline classes (`fa1`, `wv3`, `tw3`, `lu3`, `qx3`, `qt3`, `bx`, `bx3`, `PSOpenCV`, etc.) are also available inside [`docs/original_apk_pipeline_artifacts.zip`](../original_apk_pipeline_artifacts.zip).
