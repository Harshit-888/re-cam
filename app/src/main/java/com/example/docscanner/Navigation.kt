package com.example.docscanner

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.ui.camera.ScannerCameraScreen
import com.example.docscanner.ui.dashboard.DashboardScreen
import com.example.docscanner.ui.detail.DocumentDetailScreen
import com.example.docscanner.ui.editor.CropAndFilterScreen
import com.example.docscanner.ui.trash.TrashScreen
import com.example.docscanner.ui.viewer.PageViewerScreen

/**
 * Navigation 3 graph.
 *
 * The [ScannerRepository] is threaded through explicitly from
 * [MainActivity] into every screen — the single dependency-injection mechanism in
 * this app. Do not replace it with a static accessor.
 */
@Composable
fun MainNavigation(repository: ScannerRepository) {
    val backStack = rememberNavBackStack(DashboardNav)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<DashboardNav> {
                DashboardScreen(
                    repository = repository,
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
                    repository = repository,
                    onFinishScan = { docId ->
                        // Replace the camera so Back returns to where the scan
                        // started rather than reopening the viewfinder.
                        backStack.removeLastOrNull()
                        val firstPageId = repository.getDocumentById(docId)
                            ?.pages?.firstOrNull()?.id
                        if (firstPageId != null) {
                            // Post-capture review: auto-detected borders + filters,
                            // page by page — then on to the document.
                            backStack.add(CropAndFilterNav(docId, firstPageId, review = true))
                        } else {
                            addDetailIfNeeded(backStack, docId)
                        }
                    },
                    onClose = {
                        backStack.removeLastOrNull()
                    }
                )
            }

            entry<DocumentDetailNav> { key ->
                DocumentDetailScreen(
                    docId = key.docId,
                    repository = repository,
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
                    repository = repository,
                    reviewMode = key.review,
                    onDone = {
                        backStack.removeLastOrNull()
                        if (key.review) addDetailIfNeeded(backStack, key.docId)
                    }
                )
            }

            entry<PageViewerNav> { key ->
                PageViewerScreen(
                    docId = key.docId,
                    pageId = key.pageId,
                    repository = repository,
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
                    repository = repository,
                    onNavigateBack = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}

/**
 * Pushes a [DocumentDetailNav] unless that document's detail screen is already on
 * top. Adding a second entry with the same [androidx.navigation3.runtime.NavKey]
 * would give Navigation 3 two entries with identical keys.
 */
private fun addDetailIfNeeded(
    backStack: MutableList<androidx.navigation3.runtime.NavKey>,
    docId: String
) {
    val top = backStack.lastOrNull()
    if (top is DocumentDetailNav && top.docId == docId) return
    backStack.add(DocumentDetailNav(docId))
}
