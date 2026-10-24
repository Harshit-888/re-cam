package com.example.docscanner

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.docscanner.data.ScannerRepository
import com.example.docscanner.ui.camera.ScannerCameraScreen
import com.example.docscanner.ui.dashboard.DashboardScreen
import com.example.docscanner.ui.detail.DocumentDetailScreen
import com.example.docscanner.ui.editor.CropAndFilterScreen
import com.example.docscanner.ui.settings.SettingsScreen
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
        // ---- screen transitions -------------------------------------------
        // Shared-axis X: the screen being opened slides in from the end while the
        // one underneath drifts a quarter of the way out, so the two read as one
        // sheet moving rather than two panels swapping. Going back plays the same
        // motion in reverse. Only top-level animation builders are used here, so
        // this does not depend on the type of scope NavDisplay hands the spec.
        transitionSpec = { navForwardTransition() },
        popTransitionSpec = { navBackTransition() },
        // Predictive back (edge swipe) gets the pop motion, so the live preview of
        // the gesture matches where letting go actually lands.
        predictivePopTransitionSpec = { navBackTransition() },
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
                    },
                    onNavigateToSettings = {
                        backStack.add(SettingsNav)
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

            entry<SettingsNav> {
                val settings by repository.settings.collectAsState()
                SettingsScreen(
                    settings = settings,
                    onSettingsChange = { transform -> repository.updateSettings(transform) },
                    onNavigateBack = {
                        backStack.removeLastOrNull()
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

/**
 * Forward navigation: the incoming screen enters from the trailing edge, the
 * outgoing screen moves a quarter of the way towards the leading edge.
 *
 * The outgoing screen deliberately travels less than the incoming one — that
 * parallax is what makes a change of screen read as depth instead of a cut.
 */
private fun navForwardTransition(): ContentTransform =
    slideInHorizontally(
        animationSpec = tween(durationMillis = NAV_TRANSITION_MS, easing = FastOutSlowInEasing),
        initialOffsetX = { width -> width }
    ).togetherWith(
        slideOutHorizontally(
            animationSpec = tween(durationMillis = NAV_TRANSITION_MS, easing = FastOutSlowInEasing),
            targetOffsetX = { width -> -width / NAV_PARALLAX_DIVISOR }
        )
    )

/**
 * Back navigation: the exact reverse of [navForwardTransition], so the screen you
 * came from settles back where you left it.
 */
private fun navBackTransition(): ContentTransform =
    slideInHorizontally(
        animationSpec = tween(durationMillis = NAV_TRANSITION_MS, easing = FastOutSlowInEasing),
        initialOffsetX = { width -> -width / NAV_PARALLAX_DIVISOR }
    ).togetherWith(
        slideOutHorizontally(
            animationSpec = tween(durationMillis = NAV_TRANSITION_MS, easing = FastOutSlowInEasing),
            targetOffsetX = { width -> width }
        )
    )

/** Long enough to read as motion, short enough to stay out of the way. */
private const val NAV_TRANSITION_MS = 320

/** How far the outgoing screen travels, as a fraction of its own width. */
private const val NAV_PARALLAX_DIVISOR = 4
