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
 * Dedicated non-exported activity for executing performance benchmark audits.
 *
 * Round 70.3 (Arena H004 follow-up): isolates the benchmark runner from the normal
 * user launcher flow and prevents third-party apps from triggering expensive CPU/ML
 * work. Declared in AndroidManifest.xml with `android:exported="false"`.
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

