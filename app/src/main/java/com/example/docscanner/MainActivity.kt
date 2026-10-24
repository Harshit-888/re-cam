package com.example.docscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.theme.DocScannerTheme

class MainActivity : ComponentActivity() {

    /**
     * Owned by the Activity, not a global singleton (see `AGENTS.md` §2.6).
     * `applicationContext` is used so the repository and its persistence scope
     * cannot outlive a leaked Activity reference.
     */
    private val repository: ScannerRepository by lazy {
        ScannerRepository(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DocScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(repository = repository)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Configuration changes recreate the Activity but must not tear down the
        // repository mid-write; only a real finish ends its scope.
        if (isFinishing) {
            repository.close()
        }
    }
}
