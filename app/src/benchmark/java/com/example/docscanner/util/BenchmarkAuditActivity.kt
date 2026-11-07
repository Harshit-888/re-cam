package com.example.docscanner.util

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Dedicated activity for executing performance benchmark audits.
 *
 * Round 70.3 corrective fix (Arena H004 review): isolates the benchmark runner from the normal
 * user launcher flow and prevents third-party apps from triggering expensive CPU/ML
 * work. Declared only in the benchmark manifest with `android:exported="true"` and protected by
 * `android:permission="android.permission.DUMP"`. On Android 12+, `exported="false"` blocks
 * adb shell starts on non-debuggable benchmark builds because run-as is disabled; `DUMP`
 * provides a strict permission boundary that grants access to adb shell (UID 2000) while
 * expected to deny unprivileged callers. This boundary still requires a separate-app device test.
 *
 * Invoked via adb:
 * `adb shell am start -n com.example.docscanner/.util.BenchmarkAuditActivity`
 */
class BenchmarkAuditActivity : Activity() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val statusView = TextView(this).apply {
            text = "DocScanner Benchmark Running... Check logcat: DocScannerPerf"
            setPadding(32, 32, 32, 32)
        }
        setContentView(statusView)

        Log.i("DocScannerPerf", "[benchmark] BenchmarkAuditActivity started")

        scope.launch(Dispatchers.IO) {
            try {
                PerfAuditRunner.runAudit(applicationContext)
                Log.i("DocScannerPerf", "[benchmark] BenchmarkAuditActivity completed successfully")
                launch(Dispatchers.Main) {
                    statusView.text = "Benchmark Completed. Check logcat: DocScannerPerf"
                }
            } catch (t: Throwable) {
                Log.e("DocScannerPerf", "[benchmark] BenchmarkAuditActivity failed", t)
                launch(Dispatchers.Main) {
                    statusView.text = "Benchmark Failed: ${t.message}"
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}

