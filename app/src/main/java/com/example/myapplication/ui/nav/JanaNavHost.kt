package com.example.myapplication.ui.nav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.Direction
import com.example.myapplication.data.MainTab
import com.example.myapplication.ui.JanaViewModel
import com.example.myapplication.ui.auth.CodeScreen
import com.example.myapplication.ui.auth.PasswordScreen
import com.example.myapplication.ui.auth.TermsScreen
import com.example.myapplication.ui.auth.WelcomeScreen
import com.example.myapplication.ui.components.JanaBottomBar
import com.example.myapplication.ui.home.HomeScreen
import com.example.myapplication.ui.i18n.stringsFor
import com.example.myapplication.ui.messages.ChatScreen
import com.example.myapplication.ui.messages.MessagesScreen
import com.example.myapplication.ui.misc.ReferralScreen
import com.example.myapplication.ui.misc.ShopScreen
import com.example.myapplication.ui.misc.TutorialScreen
import com.example.myapplication.ui.misc.WalletScreen
import com.example.myapplication.ui.orders.LostItemScreen
import com.example.myapplication.ui.orders.OrderListScreen
import com.example.myapplication.ui.orders.OrdersScreen
import com.example.myapplication.ui.pickup.PickupScreen
import com.example.myapplication.ui.misc.TextPage
import com.example.myapplication.ui.profile.AddressScreen
import com.example.myapplication.ui.profile.DeleteAccountScreen
import com.example.myapplication.ui.profile.ChangePasswordScreen
import com.example.myapplication.ui.profile.ProfileScreen
import com.example.myapplication.ui.profile.PromoScreen
import com.example.myapplication.ui.profile.SettingsScreen
import com.example.myapplication.ui.theme.JanaColors

private object Routes {
    const val WELCOME = "welcome"
    const val CODE = "code/{phone}"
    const val PASSWORD = "password/{phone}"
    const val TERMS = "terms"
    const val MAIN = "main"
    const val ORDERS = "orders/{direction}/{status}"
    const val LOST = "lost"
    const val CHAT = "chat"
    const val TUTORIAL = "tutorial"
    const val WALLET = "wallet"
    const val REFERRAL = "referral"
    const val MARKET = "market/{id}"
    const val SETTINGS = "settings"
    const val ADDRESS = "address"
    const val COLLECTION = "collection"
    const val PROMO = "promo"
    const val TARIFFS = "tariffs"
    const val FAQ = "faq"
    const val PROHIBITED = "prohibited"
    const val CONTACT = "contact"
    const val ABOUT = "about"
    const val CHANGE_PASSWORD = "password-change"
    const val DELETE = "delete-account"

    fun code(dial: String, national: String) = "code/${dial.removePrefix("+")}-$national"
    fun password(dial: String, national: String) = "password/${dial.removePrefix("+")}-$national"
    fun orders(direction: Direction, status: String) = "orders/${direction.name}/$status"
    fun market(id: String) = "market/$id"
}

private fun androidx.navigation.NavController.open(route: String) {
    navigate(route) { launchSingleTop = true }
}

@Composable
fun JanaNavHost(vm: JanaViewModel = viewModel()) {
    if (!vm.ready) {
        Box(Modifier.fillMaxSize().background(JanaColors.Background))
        return
    }
    val nav = rememberNavController()
    val strings = stringsFor(vm.language)
    val start = if (vm.session != null) Routes.MAIN else Routes.WELCOME

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.WELCOME) {
            WelcomeScreen(
                vm = vm,
                strings = strings,
                onContinue = { dial, national -> nav.open(Routes.code(dial, national)) },
                onTerms = { nav.open(Routes.TERMS) },
            )
        }
        composable(
            Routes.CODE,
            arguments = listOf(navArgument("phone") { type = NavType.StringType }),
        ) { entry ->
            val phone = entry.arguments?.getString("phone").orEmpty()
            CodeScreen(
                vm = vm,
                strings = strings,
                routePhone = phone,
                onBack = { nav.popBackStack() },
                onVerified = { nav.open(Routes.password(phone.substringBefore("-").let { "+$it" }, phone.substringAfter("-"))) },
            )
        }
        composable(
            Routes.PASSWORD,
            arguments = listOf(navArgument("phone") { type = NavType.StringType }),
        ) { entry ->
            val phone = entry.arguments?.getString("phone").orEmpty()
            PasswordScreen(
                vm = vm,
                strings = strings,
                routePhone = phone,
                onBack = { nav.popBackStack() },
                onLoggedIn = {
                    nav.navigate(Routes.MAIN) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.TERMS) {
            TermsScreen(strings) { nav.popBackStack() }
        }
        composable(Routes.MAIN) {
            MainShell(vm, strings, nav)
        }
        composable(
            Routes.ORDERS,
            arguments = listOf(
                navArgument("direction") { type = NavType.StringType },
                navArgument("status") { type = NavType.StringType },
            ),
        ) { entry ->
            val direction = Direction.valueOf(entry.arguments?.getString("direction") ?: Direction.RECEIVE.name)
            val status = entry.arguments?.getString("status").orEmpty()
            OrderListScreen(vm, strings, vm.language, direction, status) { nav.popBackStack() }
        }
        composable(Routes.LOST) { LostItemScreen(strings) { nav.popBackStack() } }
        composable(Routes.CHAT) { ChatScreen(vm, strings) { nav.popBackStack() } }
        composable(Routes.TUTORIAL) { TutorialScreen(strings) { nav.popBackStack() } }
        composable(Routes.WALLET) { WalletScreen(strings) { nav.popBackStack() } }
        composable(Routes.REFERRAL) { ReferralScreen(vm, strings) { nav.popBackStack() } }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                vm = vm,
                strings = strings,
                onBack = { nav.popBackStack() },
                onChangeAccount = {
                    vm.logout {
                        nav.navigate(Routes.WELCOME) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                    }
                },
                onProhibited = { nav.open(Routes.PROHIBITED) },
                onContact = { nav.open(Routes.CONTACT) },
                onPassword = { nav.open(Routes.CHANGE_PASSWORD) },
                onAbout = { nav.open(Routes.ABOUT) },
                onDelete = { nav.open(Routes.DELETE) },
            )
        }
        composable(Routes.ADDRESS) {
            AddressScreen(vm, strings, vm.language.resolved() == com.example.myapplication.data.AppLang.EN) {
                nav.popBackStack()
            }
        }
        composable(Routes.COLLECTION) { TextPage(strings.collection, strings.collectionBody, strings.back) { nav.popBackStack() } }
        composable(Routes.PROMO) { PromoScreen(strings) { nav.popBackStack() } }
        composable(Routes.TARIFFS) { TextPage(strings.tariffs, strings.tariffsBody, strings.back) { nav.popBackStack() } }
        composable(Routes.FAQ) { TextPage(strings.faq, strings.faqBody, strings.back) { nav.popBackStack() } }
        composable(Routes.PROHIBITED) { TextPage(strings.prohibited, strings.prohibitedBody, strings.back) { nav.popBackStack() } }
        composable(Routes.CONTACT) { TextPage(strings.contact, strings.contactBody, strings.back) { nav.popBackStack() } }
        composable(Routes.ABOUT) { TextPage(strings.about, strings.aboutBody, strings.back) { nav.popBackStack() } }
        composable(Routes.CHANGE_PASSWORD) { ChangePasswordScreen(vm, strings) { nav.popBackStack() } }
        composable(Routes.DELETE) {
            DeleteAccountScreen(
                vm = vm,
                strings = strings,
                onBack = { nav.popBackStack() },
                onDeleted = {
                    nav.navigate(Routes.WELCOME) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                },
            )
        }
        composable(
            Routes.MARKET,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            ShopScreen(entry.arguments?.getString("id").orEmpty(), strings) { nav.popBackStack() }
        }
    }
}

@Composable
private fun MainShell(
    vm: JanaViewModel,
    strings: com.example.myapplication.ui.i18n.Strings,
    nav: androidx.navigation.NavHostController,
) {
    Scaffold(
        containerColor = JanaColors.Background,
        bottomBar = {
            JanaBottomBar(vm.tab, strings) { tab ->
                vm.selectTab(tab)
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (vm.tab) {
                MainTab.HOME -> HomeScreen(
                    vm = vm,
                    strings = strings,
                    language = vm.language,
                    onWallet = { nav.open(Routes.WALLET) },
                    onTutorial = { nav.open(Routes.TUTORIAL) },
                    onReferral = { nav.open(Routes.REFERRAL) },
                    onMarket = { nav.open(Routes.market(it)) },
                )
                MainTab.ORDER -> OrdersScreen(
                    vm = vm,
                    strings = strings,
                    onWallet = { nav.open(Routes.WALLET) },
                    onStatus = { direction, status -> nav.open(Routes.orders(direction, status)) },
                    onLost = { nav.open(Routes.LOST) },
                )
                MainTab.PICKUP -> PickupScreen(vm, strings)
                MainTab.MESSAGE -> MessagesScreen(strings) { nav.open(Routes.CHAT) }
                MainTab.PROFILE -> ProfileScreen(
                    vm = vm,
                    strings = strings,
                    onSettings = { nav.open(Routes.SETTINGS) },
                    onWallet = { nav.open(Routes.WALLET) },
                    onAddress = { nav.open(Routes.ADDRESS) },
                    onCollection = { nav.open(Routes.COLLECTION) },
                    onInvite = { nav.open(Routes.REFERRAL) },
                    onPromo = { nav.open(Routes.PROMO) },
                    onTariffs = { nav.open(Routes.TARIFFS) },
                    onLessons = { nav.open(Routes.TUTORIAL) },
                    onFaq = { nav.open(Routes.FAQ) },
                )
            }
        }
    }
}
