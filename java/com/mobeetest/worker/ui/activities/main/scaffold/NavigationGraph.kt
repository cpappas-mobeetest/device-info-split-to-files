package com.mobeetest.worker.ui.activities.main.scaffold

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.lifecycle.AndroidViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.layout.layout

import com.mobeetest.worker.ui.theme.mainDrawerSheetBottomPadding
import com.mobeetest.worker.ui.theme.mainDrawerSheetTopPadding
import com.mobeetest.worker.ui.theme.mainDrawerWidth
import com.mobeetest.worker.ui.activities.main.pages.screens.DeviceInfoScreen

// CompositionLocal για να αλλάζουμε δυναμικά τα gestures του Drawer από όπου θέλουμε
val LocalSetDrawerGesturesEnabled =
    staticCompositionLocalOf<(Boolean) -> Unit> { { _ -> } }

@Stable
class ScreenCacheController {
    private val _activeRoute = mutableStateOf("")

    val activeRoute: String get() = _activeRoute.value

    fun navigateTo(route: String) {
        _activeRoute.value = route
    }
}

@Composable
fun CachedNavigationApp(
    viewModels: List<AndroidViewModel>,
    onRecheckPermissions: () -> Unit
) {
    val controller = remember { ScreenCacheController() }

    @Suppress("DEPRECATION") val systemUiController = rememberSystemUiController()

    // Stabilize system UI values to avoid unnecessary SideEffect calls
    val useDarkIcons = remember { true }
    val backgroundColor = MaterialTheme.colorScheme.background

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = backgroundColor,
            darkIcons = useDarkIcons
        )
    }

    // Hoist immutable structures to avoid re-allocation every recomposition
    val routeToScreenNameMap = remember {
        mapOf(
            "NetworkScreen" to "Network",
            "Neighbours" to "Neighbours",
            "Graphs" to "Graphs",
            "Google Map" to "Google Map",
            "SpeedTest" to "Speed Test",
            "Cellular" to "Cellular",
            "Driver Statistics" to "Driver Statistics",
            "Wi-Fi" to "Wi-Fi",
            "DeviceInfo" to "Device Information",   // ⬅ νέο
        )

    }

    val screenNameToRouteMap = remember(routeToScreenNameMap) {
        routeToScreenNameMap.entries.associate { (k, v) -> v to k }
    }

    // NEW: state για το αν επιτρέπονται τα gestures του Drawer
    val drawerGesturesEnabled = remember { mutableStateOf(true) }


    // Lazy screen factories - screens are not composed until first navigation
    val screenFactories = remember {
        mapOf<String, @Composable () -> Unit>(
            "NetworkScreen" to { /*NetworkScreen(viewModels)*/ },
            "Neighbours" to { /*NeighborsScreen(viewModels)*/ },
            "SpeedTest" to { /*NetworkScreenCapture(viewModels, captureRef, 1)*/ },
            "Graphs" to { /*GraphsScreen(viewModels)*/ },
            "Wi-Fi" to { /*WifiScreen(emptyList())*/ },
            "Cellular" to { /*CellularScreen(emptyList())*/ },
            "Driver Statistics" to { /*NetworkDriveScreen(emptyList())*/ },
            "Google Map" to { /*MapScreen(viewModels, drawerGesturesEnabled = drawerGesturesEnabled)*/ },
            "DeviceInfo" to { DeviceInfoScreen(viewModels) },   // ⬅ placeholder οθόνη
        )
    }

    // Cache for built screens - only visited screens exist here
    val builtScreens = remember { mutableStateMapOf<String, @Composable () -> Unit>() }

    // Pre-seed NetworkScreen to keep current behavior
    LaunchedEffect(Unit) {
        if ("NetworkScreen" !in builtScreens) {
            builtScreens["NetworkScreen"] = screenFactories["NetworkScreen"]!!
        }
        if (controller.activeRoute.isEmpty()) controller.navigateTo("NetworkScreen")
    }

    // Build screen on first navigation
    LaunchedEffect(controller.activeRoute) {
        val route = controller.activeRoute
        if (route.isNotEmpty() && route !in builtScreens && route in screenFactories) {
            builtScreens[route] = screenFactories[route]!!
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Stabilize callbacks to avoid recomposition churn in TopBar and BottomNav
    val onScreenSelected = remember(controller) { { newScreen: String ->
        screenNameToRouteMap[newScreen]?.let { controller.navigateTo(it) }
        Unit
    }}

    CompositionLocalProvider(
        // NEW: δίνουμε setter σε όλο το subtree
        LocalSetDrawerGesturesEnabled provides { enabled -> drawerGesturesEnabled.value = enabled }
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerGesturesEnabled.value || drawerState.isOpen, // ✅ allow-close-when-open
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier
                        .width(mainDrawerWidth)
                        .systemBarsPadding()
                        .padding(
                            top = mainDrawerSheetTopPadding,
                            bottom = mainDrawerSheetBottomPadding
                        ),
                    drawerShape = RectangleShape
                ) {
                    val currentScreenName = routeToScreenNameMap[controller.activeRoute] ?: controller.activeRoute
                    MobeetestDrawer(pageName = currentScreenName)
                }

            }
        ) {
            Scaffold(
                topBar = {
                    TopBar(
                        currentScreen = routeToScreenNameMap[controller.activeRoute] ?: controller.activeRoute,
                        onScreenSelected = onScreenSelected,
                        viewModels = emptyList(),
                        drawerState = drawerState,
                        coroutineScope = coroutineScope,
                        onRecheckPermissions = onRecheckPermissions
                    )
                },
                bottomBar = {
                    BottomNavigationBar(
                        currentScreen = routeToScreenNameMap[controller.activeRoute] ?: controller.activeRoute,
                        onScreenSelected = onScreenSelected
                    )
                }
            ) { padding ->
                // Stabilize modifier to avoid allocation
                val contentModifier = remember(padding) { Modifier.padding(padding) }
                CachedScreenSwitcher(
                    controller = controller,
                    screens = builtScreens,
                    modifier = contentModifier,
                )
            }
        }
    }
}

@Composable
fun CachedScreenSwitcher(
    controller: ScreenCacheController,
    screens: Map<String, @Composable () -> Unit>,
    modifier: Modifier = Modifier
) {
    val active = controller.activeRoute
    val stateHolder = rememberSaveableStateHolder()

    // κρατάμε μόνιμα τη NetworkScreen
    val keepAliveRoutes = remember { setOf("NetworkScreen") }

    Box(modifier = modifier.fillMaxSize()) {
        screens.forEach { (route, screen) ->
            key(route) {
                stateHolder.SaveableStateProvider(route) {
                    val visible = route == active
                    if (route in keepAliveRoutes) {
                        // ⬇️ κρατάμε τη σύνθεση ζωντανή αλλά την "βγάζουμε" από layout όταν δεν είναι ενεργή
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .offstage(!visible)   // custom modifier παρακάτω
                        ) {
                            screen()
                        }
                    } else {
                        // οι υπόλοιπες όπως πριν: μόνο η ενεργή συντίθεται
                        if (visible) screen()
                    }
                }
            }
        }
    }
}

// Helper: "Offstage" = μετριέται/τοποθετείται σε 0x0 όταν είναι κρυφή,
// άρα κρατάς τη σύνθεση στη μνήμη χωρίς κόστος layout/draw.
private fun Modifier.offstage(offstage: Boolean): Modifier = this.then(
    if (!offstage) Modifier else Modifier.layout { _, _ ->
        // δεν μετράμε/δεν ζωγραφίζουμε τίποτα
        layout(0, 0) { /* no place */ }
    }
)
