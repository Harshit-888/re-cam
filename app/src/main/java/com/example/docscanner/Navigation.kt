package com.example.docscanner

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.docscanner.ui.camera.ScannerCameraScreen
import com.example.docscanner.ui.dashboard.DashboardScreen
import com.example.docscanner.ui.detail.DocumentDetailScreen
import com.example.docscanner.ui.editor.CropAndFilterScreen
import com.example.docscanner.ui.trash.TrashScreen
import com.example.docscanner.ui.viewer.PageViewerScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(DashboardNav)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<DashboardNav> {
                DashboardScreen(
                    onNavigateToCamera = { docId ->
                        backStack.add(ScannerCameraNav(docId))
                    },
                    onNavigateToDetail = { docId ->
                        backStack.add(DocumentDetailNav(docId))
                    },
                    onNavigateToTrash = {
                        backStack.add(TrashNav)
                    }
                )
            }

            entry<ScannerCameraNav> { key ->
                ScannerCameraScreen(
                    targetDocId = key.targetDocId,
                    onFinishScan = { docId ->
                        backStack.removeLastOrNull()
                        backStack.add(DocumentDetailNav(docId))
                    },
                    onClose = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<DocumentDetailNav> { key ->
                DocumentDetailScreen(
                    docId = key.docId,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToCrop = { pageId ->
                        backStack.add(CropAndFilterNav(key.docId, pageId))
                    },
                    onNavigateToViewer = { pageId ->
                        backStack.add(PageViewerNav(key.docId, pageId))
                    },
                    onAddPageCamera = { docId ->
                        backStack.add(ScannerCameraNav(docId))
                    }
                )
            }

            entry<CropAndFilterNav> { key ->
                CropAndFilterScreen(
                    docId = key.docId,
                    pageId = key.pageId,
                    onDone = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<PageViewerNav> { key ->
                PageViewerScreen(
                    docId = key.docId,
                    pageId = key.pageId,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    },
                    onNavigateToCrop = { pageId ->
                        backStack.add(CropAndFilterNav(key.docId, pageId))
                    }
                )
            }

            entry<TrashNav> {
                TrashScreen(
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}
