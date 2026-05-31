package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.CarWashViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: CarWashViewModel = viewModel()
            MyApplicationTheme(darkTheme = vm.isDarkMode) {
                MainAppContainer(vm)
            }
        }
    }
}

// ==========================================
// CENTRAL NAVIGATION COMPONENT
// ==========================================

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object Home : Screen("home")
    object Services : Screen("services")
    object Booking : Screen("booking")
    object Status : Screen("status")
    object More : Screen("more")
    object Gallery : Screen("gallery")
    object Offers : Screen("offers")
    object AboutUs : Screen("about")
    object Contact : Screen("contact")
    object AdminDashboard : Screen("admin_dash")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(vm: CarWashViewModel) {
    var currentScreen by remember { mutableStateOf<String>(Screen.Splash.route) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Observe DB lists
    val bookings by vm.allBookings.collectAsStateWithLifecycle()
    val offers by vm.allOffers.collectAsStateWithLifecycle()
    val galleryItems by vm.allGalleryItems.collectAsStateWithLifecycle()
    val reviews by vm.allReviews.collectAsStateWithLifecycle()
    val searchResults by vm.searchResults.collectAsStateWithLifecycle()
    val adminStats by vm.adminStatsFlow.collectAsStateWithLifecycle()

    // Edge to edge padding handling
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        topBar = {
            if (currentScreen != Screen.Splash.route && currentScreen != Screen.Auth.route && currentScreen != Screen.AdminDashboard.route) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CarWashLogoSmall()
                            Column {
                                Text(
                                    text = "ROBOTIC XPRESS",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "CAR WASH • LUCKNOW",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    actions = {
                        // Light/Dark Toggle
                        IconButton(
                            onClick = { vm.isDarkMode = !vm.isDarkMode },
                            modifier = Modifier.testTag("dark_mode_toggle")
                        ) {
                            Icon(
                                imageVector = if (vm.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle theme mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Info/Offers Indicator Badge
                        IconButton(onClick = { currentScreen = Screen.More.route }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "More Menu",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        bottomBar = {
            if (currentScreen != Screen.Splash.route && currentScreen != Screen.Auth.route && currentScreen != Screen.AdminDashboard.route) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

                    NavigationBarItem(
                        selected = currentScreen == Screen.Home.route,
                        onClick = { currentScreen = Screen.Home.route },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home",
                                tint = if (currentScreen == Screen.Home.route) activeColor else inactiveColor
                            )
                        },
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Services.route,
                        onClick = { currentScreen = Screen.Services.route },
                        label = { Text("Services", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Services",
                                tint = if (currentScreen == Screen.Services.route) activeColor else inactiveColor
                            )
                        },
                        modifier = Modifier.testTag("nav_services")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Booking.route,
                        onClick = { currentScreen = Screen.Booking.route },
                        label = { Text("Book", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Book",
                                tint = if (currentScreen == Screen.Booking.route) activeColor else inactiveColor
                            )
                        },
                        modifier = Modifier.testTag("nav_book")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.Status.route,
                        onClick = { currentScreen = Screen.Status.route },
                        label = { Text("Status", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Status",
                                tint = if (currentScreen == Screen.Status.route) activeColor else inactiveColor
                            )
                        },
                        modifier = Modifier.testTag("nav_status")
                    )
                    NavigationBarItem(
                        selected = currentScreen == Screen.More.route || currentScreen == Screen.Gallery.route || currentScreen == Screen.Offers.route || currentScreen == Screen.AboutUs.route || currentScreen == Screen.Contact.route || currentScreen == Screen.AdminDashboard.route,
                        onClick = { currentScreen = Screen.More.route },
                        label = { Text("More", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "More Screen Options",
                                tint = if (currentScreen == Screen.More.route) activeColor else inactiveColor
                            )
                        },
                        modifier = Modifier.testTag("nav_more")
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentScreen != Screen.Splash.route && currentScreen != Screen.Booking.route && currentScreen != Screen.AdminDashboard.route && currentScreen != Screen.Auth.route) {
                ExtendedFloatingActionButton(
                    text = { Text("BOOK NOW", fontWeight = FontWeight.Bold, color = Color.White) },
                    icon = { Icon(Icons.Default.DirectionsCar, "Instant Scheduling Car Wash", tint = Color.White) },
                    onClick = { currentScreen = Screen.Booking.route },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .testTag("floating_book_btn")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentScreen) {
                Screen.Splash.route -> SplashScreen {
                    currentScreen = when {
                        vm.isAdminLoggedIn -> Screen.AdminDashboard.route
                        vm.isUserLoggedIn -> Screen.Home.route
                        else -> Screen.Auth.route
                    }
                }
                Screen.Auth.route -> AuthScreen(
                    vm = vm,
                    onLoginSuccess = { isAdm ->
                        currentScreen = if (isAdm) Screen.AdminDashboard.route else Screen.Home.route
                    }
                )
                Screen.Home.route -> HomeScreen(
                    vm = vm,
                    offers = offers,
                    reviews = reviews,
                    onNavigateToBooking = { currentScreen = Screen.Booking.route },
                    onNavigateToServices = { currentScreen = Screen.Services.route },
                    onNavigateToGallery = { currentScreen = Screen.Gallery.route },
                    onNavigateToContact = { currentScreen = Screen.Contact.route }
                )
                Screen.Services.route -> ServicesScreen(
                    vm = vm,
                    onNavigateToBooking = { serviceName ->
                        vm.selectedServices.value = setOf(serviceName)
                        currentScreen = Screen.Booking.route
                    }
                )
                Screen.Booking.route -> BookingScreen(
                    vm = vm,
                    onBookingSavedSuccess = {
                        // Triggers receipt screen state inside booking flow
                    }
                )
                Screen.Status.route -> StatusScreen(
                    vm = vm,
                    searchResults = searchResults
                )
                Screen.More.route -> MoreScreen(
                    vm = vm,
                    onNavigateScreen = { route -> currentScreen = route }
                )
                Screen.Gallery.route -> GalleryScreen(
                    vm = vm,
                    galleryList = galleryItems
                )
                Screen.Offers.route -> OffersScreen(
                    offers = offers,
                    onOfferClaimed = { offer ->
                        Toast.makeText(context, "Promo Applied: ${offer.promoCode}!", Toast.LENGTH_SHORT).show()
                        currentScreen = Screen.Booking.route
                    }
                )
                Screen.AboutUs.route -> AboutUsScreen()
                Screen.Contact.route -> ContactScreen()
                Screen.AdminDashboard.route -> AdminDashboardScreen(
                    vm = vm,
                    bookingsList = bookings,
                    offersList = offers,
                    galleryList = galleryItems,
                    stats = adminStats,
                    onLogoutSuccess = {
                        currentScreen = Screen.Auth.route
                    }
                )
            }
        }
    }
}

// ==========================================
// SUBCOMPOSABLE SECTIONS
// ==========================================

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "LaserGlow")
    val glowColor by infiniteTransition.animateColor(
        initialValue = Color(0xFF1E0204),
        targetValue = Color(0xFFE30613),
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowColor"
    )

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
        delay(2200)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // High Gloss Futuristic Laser Grid Canvas Background
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .scale(scale.value)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(glowColor.copy(alpha = 0.5f), Color.Transparent),
                                center = center,
                                radius = size.minDimension * 0.8f
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                CarWashLogoBig()
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "ROBOTIC XPRESS",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
            Text(
                text = "CAR WASH",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE30613),
                textAlign = TextAlign.Center,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Drive In Dirty — Drive Out Shining",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = Color(0xFFE30613),
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = " लखनऊ's First Advanced Automatic Clean Center",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.4f),
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
        }
    }
}

// 1.5. STANDALONE AUTHENTICATION GATEWAY
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    vm: com.example.ui.CarWashViewModel,
    onLoginSuccess: (Boolean) -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var showAdminInputs by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF141125), // Cosmic Premium Luxury Palette
                        Color(0xFF08060F)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Glowing central automotive badge overlay of Lucknow automatic wash center
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFE30613).copy(alpha = 0.22f), Color.Transparent),
                                center = center,
                                radius = size.minDimension * 0.75f
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                CarWashLogoBig()
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "ROBOTIC XPRESS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 2.sp
            )
            Text(
                text = "LUCKNOW CAR WASH & DETAILING STUDIO",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(26.dp))

            if (!showAdminInputs) {
                // Tab switcher for Sign In / Sign Up
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.06f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { isSignUpMode = false },
                            color = if (!isSignUpMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "SIGN IN",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isSignUpMode) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { isSignUpMode = true },
                            color = if (isSignUpMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "SIGN UP",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSignUpMode) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                if (!isSignUpMode) {
                    // Sign In Screen Input Layout
                    Text(
                        "Welcome Back! Log in to detail your vehicle",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = vm.signInEmail,
                        onValueChange = { vm.signInEmail = it },
                        label = { Text("Email Address", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Email, "Email Input Lead", tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_signin_email_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = vm.signInPassword,
                        onValueChange = { vm.signInPassword = it },
                        label = { Text("Password", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Lock, "Password Input Lead", tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_signin_pass_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (vm.signInError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(vm.signInError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (vm.performUserLogin()) {
                                Toast.makeText(context, "Sign In Successful!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(false)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_signin_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SIGN IN", fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Sign Up Screen Input Layout
                    Text(
                        "Create clean-detailing customer profile",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = vm.signUpName,
                        onValueChange = { vm.signUpName = it },
                        label = { Text("Full Name", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Person, "Name Input Lead", tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_signup_name_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = vm.signUpEmail,
                        onValueChange = { vm.signUpEmail = it },
                        label = { Text("Email Address", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Email, "Email Input Lead", tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_signup_email_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = vm.signUpPhone,
                        onValueChange = { vm.signUpPhone = it },
                        label = { Text("Mobile Contact Number", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Phone, "Phone Input Lead", tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_signup_phone_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = vm.signUpPassword,
                        onValueChange = { vm.signUpPassword = it },
                        label = { Text("Create password (min 6 char)", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Lock, "Password Input Lead", tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_signup_pass_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (vm.signUpError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(vm.signUpError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (vm.performUserSignUp()) {
                                Toast.makeText(context, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(false)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_signup_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SIGN UP", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // Admin Dashboard Login Mode
                Text(
                    "Authorize Administrative Detailing Monitor Station",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = vm.adminUsernameState,
                    onValueChange = { vm.adminUsernameState = it },
                    label = { Text("Administrator ID", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Person, "Admin Id Lead", tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_admin_user_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = vm.adminPasswordState,
                    onValueChange = { vm.adminPasswordState = it },
                    label = { Text("Secret Password Key", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Lock, "Admin Secret Lead", tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_admin_pass_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                if (vm.adminLoginError.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(vm.adminLoginError, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (vm.performAdminLogin()) {
                            Toast.makeText(context, "Admin Authorization Granted!", Toast.LENGTH_SHORT).show()
                            onLoginSuccess(true)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_admin_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("AUTHORIZE STATION", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(16.dp))

            // Testing pre-seed cards
            Text(
                "FAST DEV TESTING ACTIONS & ROLES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // User Pre-seed
                if (!showAdminInputs) {
                    Button(
                        onClick = {
                            vm.signInEmail = vm.userEmail
                            vm.signInPassword = "xpress123"
                            if (vm.performUserLogin()) {
                                Toast.makeText(context, "Sandbox User Access Bypass granted!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(false)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("bypass_user_login"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DirectionsCar, "Bypass Car Wash Icon", modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("USER PORTAL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    // Admin Pre-seed
                    Button(
                        onClick = {
                            vm.adminUsernameState = "admin"
                            vm.adminPasswordState = "xpress123"
                            if (vm.performAdminLogin()) {
                                Toast.makeText(context, "Sandbox Admin desk opened!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(true)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("bypass_admin_login"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.25f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, "Bypass Admin Icon", modifier = Modifier.size(16.dp), tint = Color.Red)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("BYPASS ADMIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Switch between standard customer authentication and administrative gateway!
                Button(
                    onClick = {
                        showAdminInputs = !showAdminInputs
                        vm.signInError = ""
                        vm.signUpError = ""
                        vm.adminLoginError = ""
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("toggle_admin_user_portal"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAdminInputs) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (showAdminInputs) Icons.Default.Person else Icons.Default.AdminPanelSettings,
                        contentDescription = "Switch Modes Link Icon",
                        modifier = Modifier.size(16.dp),
                        tint = if (showAdminInputs) MaterialTheme.colorScheme.primary else Color.Red
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showAdminInputs) "USER LOGIN" else "DEV ADMIN GATE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 2. HOME SCREEN (MAIN)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    vm: CarWashViewModel,
    offers: List<Offer>,
    reviews: List<Review>,
    onNavigateToBooking: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToContact: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Slide Carousel State
    val banners = listOf(
        Pair("Fully Automatic Tunnel Wash", "Scratch-free robotic conveyor technology"),
        Pair("Active Premium Foam Jet Wash", "Lather wash removing all micro dirt molecules"),
        Pair("Dual Coating Paint Protect", "Premium wax gloss glaze shield armor"),
        Pair("Alloy Wheel Deep Detail Shine", "Intense metallic brake-dust sweep"),
        Pair("Sanitized Vacuum Cabin Polish", "Pristine dustless console dashboard cleaning")
    )
    var activeSlideIndex by remember { mutableStateOf(0) }

    // Auto sliding effect (every 4 seconds)
    LaunchedEffect(key1 = activeSlideIndex) {
        delay(4000)
        activeSlideIndex = (activeSlideIndex + 1) % banners.size
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // A. AUTO-SLIDER CAROUSEL SECTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color.Black)
            ) {
                // Background artistic gradient block mimicking high-end detailing studio lights
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE30613).copy(alpha = 0.25f),
                                        Color.Black
                                    )
                                )
                            )
                        }
                )

                // Carousel Content Display with nice slide animations
                AnimatedContent(
                    targetState = activeSlideIndex,
                    transitionSpec = {
                        slideInHorizontally { width -> width / 2 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width / 2 } + fadeOut()
                    },
                    modifier = Modifier.fillMaxSize()
                ) { targetIndex ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Surface(
                            color = Color(0xFFE30613).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFE30613)),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = " PREMIUM SLIDER ${targetIndex + 1}/5 ",
                                fontSize = 9.sp,
                                color = Color(0xFFFF4D4D),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = banners[targetIndex].first,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = banners[targetIndex].second,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.LightGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Dot Pagination Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    banners.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == activeSlideIndex) 14.dp else 7.dp, 7.dp)
                                .clip(CircleShape)
                                .background(if (idx == activeSlideIndex) Color(0xFFE30613) else Color.White.copy(alpha = 0.5f))
                                .clickable { activeSlideIndex = idx }
                        )
                    }
                }
            }

            // B. WELCOME INTRODUCTION SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Welcome to Lucknow's First Advanced Automatic Robotic Car Caring Center",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(20.dp))

                // C. THE QUICK ACTIONS CARDS
                Text(
                    text = "QUICK ACTIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionCard(
                        title = "Book Wash",
                        subtitle = "Instant appointment",
                        icon = Icons.Default.CalendarMonth,
                        onClick = onNavigateToBooking,
                        modifier = Modifier.weight(1f).testTag("action_book")
                    )
                    QuickActionCard(
                        title = "Services",
                        subtitle = "Exploration pricing",
                        icon = Icons.Default.DirectionsCar,
                        onClick = onNavigateToServices,
                        modifier = Modifier.weight(1f).testTag("action_services")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // D. INSTANT OFFERS TEASER SLIDER
                if (offers.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LATEST BRAND PROMOTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE30613).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFE30613),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = " ${offers.first().discountText} ",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = offers.first().title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = offers.first().description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Code: ${offers.first().promoCode}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Tap More to see all >",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // E. CUSTOMER REVIEWS FEEDBACK SLIDER
                Text(
                    text = "CUSTOMER DETAILED REVIEW RATINGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 12.dp)
                ) {
                    items(reviews) { r ->
                        Card(
                            modifier = Modifier
                                .width(280.dp)
                                .height(130.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(r.reviewerName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text(r.vehicleModel, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Row {
                                        repeat(r.rating) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "*",
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = r.comment,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // F. END OF CONTENT SPACER
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = "", tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// 3. SERVICES SCREEN
@Composable
fun ServicesScreen(
    vm: CarWashViewModel,
    onNavigateToBooking: (String) -> Unit
) {
    val services = listOf(
        Pair("Automatic Car Wash", "Advanced robotic wash system for fast and efficient exterior clean. Process completes within minutes, including dual high-intensity air dryers."),
        Pair("Foam & High-Pressure Jet Wash", "Deep cleaning with premium active snow foam bath and intense 350-bar water pressure Sweeper to lift stubborn mud."),
        Pair("Wax Wash & Paint Protection", "Enhance glazes, seals, and defends paintwork from Uttar Pradesh ultraviolet rays and high-traffic road impurities."),
        Pair("Interior Dry Cleaning", "Complete interior restoration: detailing active seats, side dashboard panels, custom console treatment, and premium cabin vacuuming."),
        Pair("Teflon Coating", "Long-lasting surface paint detailing with advanced composite sealants. Imparts severe water bead-off glow finish."),
        Pair("Alloy Wheel Deep Cleaning", "Intriguing brake dust dissolver wash, high pressure tire scrub, and intense metal alloy polish brush shine."),
        Pair("Rubbing & Polishing", "Eliminate surface scratches, swirls, oxidation, and restore the vehicle to a deep showroom glaze look."),
        Pair("Interior & Exterior Detailing", "Comprehensive bumper-to-bumper car spa wash, internal detailing, chassis clean, engine gloss shield, and full protect wax polish.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "OUR PREMIUM CAR CARING SERVICES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Select a Premium Detailing Treatment",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(services) { s ->
                val basePrice = vm.servicePrices[s.first] ?: 500
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("service_card_${s.first.replace(" ", "_")}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = s.first,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = " ₹$basePrice ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = s.second,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { onNavigateToBooking(s.first) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("BOOK THIS WASH APPOINTMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

// 4. BOOKING SCREEN
@Composable
fun BookingScreen(
    vm: CarWashViewModel,
    onBookingSavedSuccess: () -> Unit
) {
    var stepState by remember { mutableStateOf(1) } // 1: Input details, 2: Receipt screen
    val context = LocalContext.current

    if (stepState == 1) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "SCHEDULING PORTAL",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Secure Your Detailing Slot",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Form Fields
            OutlinedTextField(
                value = vm.fullName,
                onValueChange = { vm.fullName = it },
                label = { Text("Full Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_name_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                leadingIcon = { Icon(Icons.Default.Person, "Client Name Icon") }
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = vm.mobileNumber,
                onValueChange = { vm.mobileNumber = it },
                label = { Text("10-Digit Mobile Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_phone_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Phone, "Mobile Contact Icon") }
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = vm.emailAddress,
                onValueChange = { vm.emailAddress = it },
                label = { Text("Email Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_email_input"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, "Email Address Icon") }
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Vehicle Type Multi-Choice Grid (2x2 layout is extremely robust against text wrap on tight screens)
            Text("Vehicle Frame Profile:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            val types = listOf("Hatchback", "Sedan", "SUV", "Luxury Car")
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    types.take(2).forEach { t ->
                        val isSel = vm.vehicleType == t
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { vm.vehicleType = t },
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = t,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    types.drop(2).forEach { t ->
                        val isSel = vm.vehicleType == t
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { vm.vehicleType = t },
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = t,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Date picker simulated text trigger
            Text("Select Detailing Date:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Pre set dates representing relative calendar
                val sdf = SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH)
                val cal = Calendar.getInstance()
                repeat(4) { idx ->
                    val dStr = sdf.format(cal.time)
                    val isSel = vm.bookingDate == dStr
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { vm.bookingDate = dStr },
                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dStr.split("-")[0],
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = dStr.split("-")[1],
                                fontSize = 10.sp,
                                color = if (isSel) Color.White.copy(alpha = 0.8f) else Color.Gray
                            )
                        }
                    }
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Time Slots Rows
            Text("Available Machinery Hours (Lucknow timezone):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            val slots = listOf(
                "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
                "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"
            )
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                slots.forEach { slot ->
                    val isSel = vm.bookingTimeSlot == slot
                    Surface(
                        modifier = Modifier
                            .clickable { vm.bookingTimeSlot = slot },
                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = slot,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Premium Multi Select Checklist Cards with price tags
            Text("Select Treatments Packages (Multi-Select):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            val treatments = listOf(
                "Automatic Wash", "Foam Wash", "Wax Wash", "Detailing",
                "Teflon Coating", "Polishing", "Interior Cleaning"
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                treatments.forEach { tr ->
                    val isChecked = vm.selectedServices.value.contains(tr)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { vm.toggleServiceSelection(tr) },
                        color = if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isChecked) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selection indicator checkmark",
                                    tint = if (isChecked) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tr,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            val price = vm.servicePrices[tr] ?: 500
                            Text(
                                text = "₹$price",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isChecked) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            }

            // Error display
            if (vm.hasSubmissionError) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = vm.lastErrorMsg,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    vm.submitBooking {
                        stepState = 2
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .testTag("booking_submit_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PROCEED AND CONFIRM BOOKING", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    } else {
        // RECEIPT COMPOSABLE SCREEN
        val booking = vm.successfullyBookedRecord
        if (booking != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "YOUR CAR WASH APPOINTMENT HAS BEEN SUCCESSFULLY BOOKED",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Beautiful Bill Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("BOOKING REFERENCE:", fontSize = 11.sp, color = Color.Gray)
                            Text(booking.bookingId, fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        ReceiptRow(label = "Full Name", value = booking.fullName)
                        ReceiptRow(label = "Registered Mobile", value = booking.mobile)
                        ReceiptRow(label = "Registered Email", value = booking.email)
                        ReceiptRow(label = "Vehicle Profile", value = booking.vehicleType)
                        ReceiptRow(label = "Detailing Date", value = booking.dateString)
                        ReceiptRow(label = "Time Slot Slot", value = booking.timeSlot)
                        ReceiptRow(label = "Active Status", value = booking.status)

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                        Text("SERVICES INCLUDED:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(booking.services, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ESTIMATED TOTAL:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("₹${vm.calculateBookingPrice(booking.services)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        stepState = 1
                        vm.successfullyBookedRecord = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("BOOK ANOTHER CAR WASH")
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        // Share intent triggered
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Robotic Xpress Appointment")
                            putExtra(Intent.EXTRA_TEXT, "Scheduled my car ${booking.vehicleType} wash for ${booking.dateString} ${booking.timeSlot} under booking id ${booking.bookingId}!")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("SHARE BOOKING SLIP")
                }
                Spacer(modifier = Modifier.height(50.dp))
            }
        }
    }
}

@Composable
fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

// 5. STATUS / MY BOOKINGS SCREEN
@Composable
fun StatusScreen(
    vm: CarWashViewModel,
    searchResults: List<Booking>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "REGISTRY TRACKING",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Track Scheduled Wash",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = vm.searchMobileQuery,
            onValueChange = { vm.onSearchQueryChanged(it) },
            label = { Text("Search by Registered Mobile Number") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("status_search_bar"),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Phone, "") },
            suffix = {
                if (vm.searchMobileQuery.isNotEmpty()) {
                    IconButton(onClick = { vm.onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, "Clear search text")
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (searchResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AirportShuttle,
                        contentDescription = "",
                        tint = Color.Gray.copy(alpha = 0.5f),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Wash Schedules Found",
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "Enter a valid registered mobile number above to find your live status.",
                        fontSize = 11.sp,
                        color = Color.Gray.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(searchResults) { b ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(b.bookingId, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                StatusTag(b.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Name: ${b.fullName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Vehicle: ${b.vehicleType}", fontSize = 11.sp)
                            Text("Date/Slot: ${b.dateString} • ${b.timeSlot}", fontSize = 11.sp)
                            Text("Services: ${b.services}", fontSize = 11.sp, color = Color.Gray)

                            if (b.status != "Cancelled" && b.status != "Completed") {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { vm.changeBookingStatus(b.id, "Cancelled") },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red),
                                    modifier = Modifier.align(Alignment.Start)
                                ) {
                                    Text("CANCEL APPOINTMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

@Composable
fun StatusTag(status: String) {
    val col = when (status) {
        "Confirmed" -> Color(0xFF4CAF50)
        "In Progress" -> Color(0xFF2196F3)
        "Completed" -> Color(0xFF9C27B0)
        "Cancelled" -> Color(0xFFE30613)
        else -> Color(0xFFFF9800) // Pending
    }
    Surface(
        color = col.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = " $status ",
            color = col,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp)
        )
    }
}

// 6. MORE SCREEN (GATEWAY TO SECONDARY SHEETS)
@Composable
fun MoreScreen(
    vm: CarWashViewModel,
    onNavigateScreen: (String) -> Unit
) {
    var adminLoginDialogVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Premium Client Account Profile Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Avatar Account icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (vm.loggedInUserName.isNotBlank()) vm.loggedInUserName.uppercase() else "GUEST CUSTOMER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = vm.loggedInUserEmail.ifBlank { "sverma9773@gmail.com" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (vm.loggedInUserPhone.isNotBlank()) {
                        Text(
                            text = vm.loggedInUserPhone,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        vm.performUserLogout()
                        onNavigateScreen(Screen.Auth.route)
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("more_logout_btn")
                ) {
                    Text("LOGOUT", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ADDITIONAL TILES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Explore App Sections",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        MoreMenuTile(
            title = "Promo Coupons & Offers",
            desc = "Redeem vouchers and special discounts in Lucknow",
            icon = Icons.Default.LocalOffer,
            onClick = { onNavigateScreen(Screen.Offers.route) },
            tag = "more_offers_btn"
        )
        MoreMenuTile(
            title = "Robot Center Gallery",
            desc = "View live high gloss detailing finished results",
            icon = Icons.Default.Collections,
            onClick = { onNavigateScreen(Screen.Gallery.route) },
            tag = "more_gallery_btn"
        )
        MoreMenuTile(
            title = "About Our Clean Standards",
            desc = "Lucknow's first conveyor belt intelligent machine detailing",
            icon = Icons.Default.Info,
            onClick = { onNavigateScreen(Screen.AboutUs.route) },
            tag = "more_about_btn"
        )
        MoreMenuTile(
            title = "Get In Touch",
            desc = "Address, support numbers, and navigator mappings",
            icon = Icons.Default.Map,
            onClick = { onNavigateScreen(Screen.Contact.route) },
            tag = "more_contact_btn"
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Review Feedback Submissions Tile
        Text("WRITE A RECENT REVIEW:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = vm.customerReviewerName,
                    onValueChange = { vm.customerReviewerName = it },
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = vm.customerVehicleModel,
                    onValueChange = { vm.customerVehicleModel = it },
                    label = { Text("Your Car Model (e.g. Amaze)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = vm.customerReviewComment,
                    onValueChange = { vm.customerReviewComment = it },
                    label = { Text("Write your detailing feedback") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rating: ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        repeat(5) { i ->
                            val r = i + 1
                            val isSel = vm.customerRating >= r
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "",
                                tint = if (isSel) Color(0xFFFFB300) else Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { vm.customerRating = r }
                            )
                        }
                    }
                    Button(
                        onClick = {
                            if (vm.customerReviewerName.isNotBlank() && vm.customerReviewComment.isNotBlank()) {
                                vm.submitCustomerReview()
                                Toast.makeText(vm.getApplication(), "Review posted successfully!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.heightIn(min = 36.dp)
                    ) {
                        Text("POST", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Admin Access Section
        Text(text = "MANAGEMENT WORKSTATION", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (vm.isAdminLoggedIn) {
            Button(
                onClick = { onNavigateScreen(Screen.AdminDashboard.route) },
                modifier = Modifier.fillMaxWidth().testTag("enter_admin_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("ENTER ADMIN CONTROL DESK", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { 
                    vm.performAdminLogout()
                    onNavigateScreen(Screen.Auth.route)
                },
                modifier = Modifier.fillMaxWidth().testTag("logout_admin_btn")
            ) {
                Text("LOGOUT AS ADMIN")
            }
        } else {
            Button(
                onClick = { onNavigateScreen(Screen.Auth.route) },
                modifier = Modifier.fillMaxWidth().testTag("login_admin_dialog_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SIGN IN AS ADMINISTRATOR", color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun MoreMenuTile(
    title: String,
    desc: String,
    icon: ImageVector,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag(tag),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Text(desc, fontSize = 10.sp, color = Color.Gray)
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "",
                tint = Color.Gray
            )
        }
    }
}

// 7. GALLERY SCREEN
@Composable
fun GalleryScreen(
    vm: CarWashViewModel,
    galleryList: List<GalleryItem>
) {
    var activeCategoryTab by remember { mutableStateOf("Automatic Wash") }
    var selectedItemForZoom by remember { mutableStateOf<GalleryItem?>(null) }
    val categories = listOf("Automatic Wash", "Foam Wash", "Detailing", "Interior Cleaning")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "MEDIA PORTFOLIO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Robot Center Showcase Gallery",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Custom categories scroll tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSel = activeCategoryTab == cat
                Surface(
                    modifier = Modifier.clickable { activeCategoryTab = cat },
                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Grid layout
        val filteredItems = galleryList.filter { it.category == activeCategoryTab }
        if (filteredItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No showcase images in this category yet.", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredItems) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedItemForZoom = item }
                            .testTag("gallery_grid_item_${item.title.replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            // High fidelity stylized canvas mock picture representing action
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                CanvasGraphicMock(item.id)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                item.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                item.description,
                                fontSize = 9.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    // FULL SCREEN IMAGE VIEWER DIALOG WITH ZOOM
    if (selectedItemForZoom != null) {
        val zoom = selectedItemForZoom!!
        Dialog(onDismissRequest = { selectedItemForZoom = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(zoom.category.uppercase(), color = Color(0xFFFF4D4D), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { selectedItemForZoom = null }) {
                            Icon(Icons.Default.Close, "Dismiss image zoom", tint = Color.White)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(Color.DarkGray.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CanvasGraphicMock(zoom.id)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(zoom.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(zoom.description, color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

// 8. OFFERS SCREEN
@Composable
fun OffersScreen(
    offers: List<Offer>,
    onOfferClaimed: (Offer) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "PROMOTIONS DESK",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Available Lucknow Detailing Offers & Coupons",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (offers.isEmpty()) {
            Text("No active promotions or discount coupons available at this time.", color = Color.Gray)
        } else {
            offers.forEach { offer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("offer_coupon_${offer.promoCode}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = offer.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                color = Color(0xFFE30613),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "  ${offer.discountText}  ",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = offer.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = " USE PROMO: ${offer.promoCode} ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Button(
                                onClick = { onOfferClaimed(offer) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("CLAIM", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

// 9. ABOUT US SCREEN
@Composable
fun AboutUsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CarWashLogoBig(modifier = Modifier.scale(0.85f))
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Lucknow's First Advanced Automatic Car Wash Experience",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Experience next-generation automobile care with advanced automatic washing technology, premium cleaning solutions, and professional service standards designed for a faster, smarter, and scratch-free finish.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "WHY CHOOSE ROBOTIC XPRESS CAR WASH?",
            fontWeight = FontWeight.Black,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        val bulletPoints = listOf(
            "Advanced Automatic Machine Conveyor Technology — Ultra-precision robotics sweep.",
            "Safe & Scratch-Free Active Cleaning — Eliminates human friction paint swirling.",
            "Premium Professional-Grade Chemicals — Safe on automotive clearcoat glazing.",
            "Surface-Specific Chemical Protect & Armor — Tailored SUV or sedan shielding.",
            "Fast Robotic Delivery Swipes — Wash done within minutes under high timers.",
            "Modern Equipment Detailing Station — High 40HP storm dry blowers."
        )

        bulletPoints.forEach { point ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "✅ ",
                    fontSize = 12.sp
                )
                Text(
                    text = point,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨ DRIVE IN DIRTY — DRIVE OUT SHINING ✨",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

// 10. CONTACT SCREEN
@Composable
fun ContactScreen() {
    val context = LocalContext.current
    val address = "Metro Station Area, Near LDA Colony, Kanpur Rd, Lucknow, Uttar Pradesh 226012"
    val phoneNumber = "+919773000000" // Styled representation
    val whatsappUrl = "https://wa.me/919773000000"
    val emailAddress = "support@xpressroboticcarwash.com"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "COMMUNICATION SHEETS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Headquarters Location & Active Care Lines",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(14.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("BUSINESS NAME:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text("ROBOTIC XPRESS CAR WASH LUCKNOW", fontSize = 14.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(8.dp))
                Text("DETAILED HQ ADDRESS:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(address, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(8.dp))
                Text("OFFICIAL EMAIL:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(emailAddress, fontSize = 12.sp, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(8.dp))
                Text("OPERATIONAL DETAILED TIMINGS:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text("Monday - Sunday : 08:00 AM - 09:00 PM (IST)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Actions Group
        Button(
            onClick = {
                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                context.startActivity(callIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("contact_call_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Phone, "Call Now Support Care", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("CALL HOTLINE SUPPORT NOW", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                context.startActivity(waIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("contact_whatsapp_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)) // WhatsApp iconic color
        ) {
            Icon(Icons.Default.DirectionsCar, "WhatsApp Quick Booking Chat Info", tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("WHATSAPP DETAILED CHAT APPOINTMENT", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                // Map navigation trigger
                val mapUri = Uri.parse("geo:26.7909,80.8920?q=" + Uri.encode("Robotic Car Wash Lucknow"))
                val mapIntent = Intent(Intent.ACTION_VIEW, mapUri).apply {
                    setPackage("com.google.android.apps.maps")
                }
                context.startActivity(mapIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("contact_maps_btn")
        ) {
            Icon(Icons.Default.Map, "Google Map Pointer Navigator Location")
            Spacer(modifier = Modifier.width(8.dp))
            Text("SECURE MAP NAVIGATION POINTER", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = {
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$emailAddress"))
                context.startActivity(emailIntent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .testTag("contact_email_btn")
        ) {
            Icon(Icons.Default.Email, "Compose detailing message email")
            Spacer(modifier = Modifier.width(8.dp))
            Text("COMPOSE OFFICIAL DETAILED EMAIL", fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(60.dp))
    }
}

// 11. ADMIN DASHBOARD WORKSTATION
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    vm: CarWashViewModel,
    bookingsList: List<Booking>,
    offersList: List<Offer>,
    galleryList: List<GalleryItem>,
    stats: CarWashViewModel.AdminStats,
    onLogoutSuccess: () -> Unit
) {
    var activeAdminTab by remember { mutableStateOf(0) } // 0: Booking management, 1: Offer management, 2: Gallery items
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "ADMIN STATION", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                Text(text = "Control Dashboard", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = " AUTHORIZED ",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(4.dp)
                    )
                }
                IconButton(
                    onClick = {
                        vm.performAdminLogout()
                        onLogoutSuccess()
                    },
                    modifier = Modifier.testTag("admin_header_logout_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Logout current administrator",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Live analytical quick widgets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminMetricCard(title = "Total Bookings", value = "${stats.totalBookings}", modifier = Modifier.weight(1f))
            AdminMetricCard(title = "Today Active", value = "${stats.todaysBookings}", modifier = Modifier.weight(1f))
            AdminMetricCard(title = "Total Revenue", value = "₹${stats.totalRevenue}", modifier = Modifier.weight(1.2f))
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Main Tab Switches
        TabRow(selectedTabIndex = activeAdminTab, containerColor = MaterialTheme.colorScheme.surface, modifier = Modifier.clip(RoundedCornerShape(8.dp))) {
            Tab(selected = activeAdminTab == 0, onClick = { activeAdminTab = 0 }) {
                Text("Bookings", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
            }
            Tab(selected = activeAdminTab == 1, onClick = { activeAdminTab = 1 }) {
                Text("Vouchers CRUD", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
            }
            Tab(selected = activeAdminTab == 2, onClick = { activeAdminTab = 2 }) {
                Text("Gallery ADD", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        when (activeAdminTab) {
            0 -> {
                // Bookings Management
                if (bookingsList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No customer wash schedules in local database yet.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(bookingsList) { bk ->
                            var expandedStatusSetter by remember { mutableStateOf(false) }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_booking_item_${bk.bookingId}"),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(bk.bookingId, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                            Text(bk.fullName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                        StatusTag(status = bk.status)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Contact Phone: ${bk.mobile}", fontSize = 11.sp)
                                    Text("Vehicle Frame: ${bk.vehicleType}", fontSize = 11.sp)
                                    Text("Date & Slot: ${bk.dateString} at ${bk.timeSlot}", fontSize = 11.sp)
                                    Text("Premium Packages: ${bk.services}", fontSize = 11.sp, color = Color.Gray)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val statusList = listOf("Pending", "Confirmed", "In Progress", "Completed", "Cancelled")
                                        statusList.forEach { st ->
                                            if (bk.status != st) {
                                                Button(
                                                    onClick = { vm.changeBookingStatus(bk.id, st) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(26.dp)
                                                ) {
                                                    Text(st.uppercase(), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Black)
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    IconButton(
                                        onClick = { vm.deleteBooking(bk.id) },
                                        modifier = Modifier.align(Alignment.End).size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Delete Booking slip permanently from DB", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Offers CRUD
                Column(modifier = Modifier.fillMaxSize()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("ADD A COOPERATIVE DISPATCH VOUCHER:", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = vm.newOfferTitle,
                                onValueChange = { vm.newOfferTitle = it },
                                label = { Text("Offer Theme (e.g. Foam Blast)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = vm.newOfferDesc,
                                onValueChange = { vm.newOfferDesc = it },
                                label = { Text("Brief description of benefits") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = vm.newOfferDiscount,
                                    onValueChange = { vm.newOfferDiscount = it },
                                    label = { Text("Disp Code Text") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = vm.newOfferCode,
                                    onValueChange = { vm.newOfferCode = it },
                                    label = { Text("Promo String Code") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { vm.adminCreateOffer() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("INSERT VALUE INTO ROOM DATABASE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ACTIVE DISPATCH LISTS:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(offersList) { off ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(off.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Discount: ${off.discountText} • Promo: ${off.promoCode}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { vm.adminDeleteOffer(off.id) }) {
                                        Icon(Icons.Default.Delete, "Delete Offer Coupon code", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Gallery Add Section
                Column(modifier = Modifier.fillMaxSize()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("APPEND HIGH GLOSS PORTFOLIO SCREEN:", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))

                            // Category dropdown representation
                            val cats = listOf("Automatic Wash", "Foam Wash", "Detailing", "Interior Cleaning")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                cats.forEach { c ->
                                    val isSel = vm.newGalleryCategory == c
                                    Surface(
                                        modifier = Modifier.clickable { vm.newGalleryCategory = c },
                                        color = if (isSel) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "  $c  ",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = vm.newGalleryTitle,
                                onValueChange = { vm.newGalleryTitle = it },
                                label = { Text("Showcase Picture Highlight Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = vm.newGalleryDesc,
                                onValueChange = { vm.newGalleryDesc = it },
                                label = { Text("Details of polish finish achieved") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { vm.adminCreateGalleryItem() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("SAVE APPLIED ATTACHMENT TO DB", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ACTIVE PORTFOLIO SPECIMENS:", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(galleryList) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Category: ${item.category}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                    IconButton(onClick = { vm.adminDeleteGallery(item.id) }) {
                                        Icon(Icons.Default.Delete, "Delete portfolio image record", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 9.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// ==========================================
// CUSTOM VECTOR ART DRAWINGS & LOGOS
// ==========================================

@Composable
fun CarWashLogoSmall(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(34.dp)) {
        // Draw deep black carbon outer ring base
        drawCircle(color = Color(0xFF0D0D0D), radius = size.minDimension * 0.48f)
        // Red luxury dynamic robotic hex shield
        val shieldPath = Path().apply {
            val rad = size.minDimension * 0.45f
            addArc(
                oval = androidx.compose.ui.geometry.Rect(
                    center = center,
                    radius = rad
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 360f
            )
        }
        drawPath(shieldPath, color = Color(0xFFE30613), style = Stroke(width = 3f))
        // High speed laser cleaning streaks
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.25f, size.height * 0.4f),
            end = Offset(size.width * 0.75f, size.height * 0.4f),
            strokeWidth = 2.5f
        )
        drawLine(
            color = Color.White,
            start = Offset(size.width * 0.35f, size.height * 0.55f),
            end = Offset(size.width * 0.65f, size.height * 0.55f),
            strokeWidth = 2.5f
        )
        // Shimmering center star
        drawCircle(color = Color.White, radius = 3f, center = Offset(size.width * 0.5f, size.height * 0.7f))
    }
}

@Composable
fun CarWashLogoBig(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(100.dp)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFE30613).copy(alpha = 0.15f), Color.Transparent)
            ),
            radius = size.minDimension * 0.5f
        )
        // Hex Shield Vector Overlay
        val hexPath = Path().apply {
            moveTo(size.width * 0.5f, size.height * 0.1f)
            lineTo(size.width * 0.85f, size.height * 0.28f)
            lineTo(size.width * 0.85f, size.height * 0.72f)
            lineTo(size.width * 0.5f, size.height * 0.9f)
            lineTo(size.width * 0.15f, size.height * 0.72f)
            lineTo(size.width * 0.15f, size.height * 0.28f)
            close()
        }
        drawPath(path = hexPath, color = Color(0xFFE30613), style = Stroke(width = 4.5f))

        // Center sport car silhouette
        val carBodyPath = Path().apply {
            moveTo(size.width * 0.28f, size.height * 0.62f)
            cubicTo(
                size.width * 0.28f, size.height * 0.58f,
                size.width * 0.35f, size.height * 0.55f,
                size.width * 0.4f, size.height * 0.55f
            )
            lineTo(size.width * 0.6f, size.height * 0.55f)
            cubicTo(
                size.width * 0.65f, size.height * 0.55f,
                size.width * 0.72f, size.height * 0.58f,
                size.width * 0.72f, size.height * 0.62f
            )
            lineTo(size.width * 0.75f, size.height * 0.68f)
            lineTo(size.width * 0.25f, size.height * 0.68f)
            close()
        }
        drawPath(path = carBodyPath, color = Color(0xFFE30613))

        // Windshield shine
        val glassPath = Path().apply {
            moveTo(size.width * 0.42f, size.height * 0.56f)
            lineTo(size.width * 0.58f, size.height * 0.56f)
            lineTo(size.width * 0.55f, size.height * 0.61f)
            lineTo(size.width * 0.45f, size.height * 0.61f)
            close()
        }
        drawPath(path = glassPath, color = Color.White)

        // Shimmer sparkles
        drawCircle(color = Color.White, radius = 5f, center = Offset(size.width * 0.5f, size.height * 0.22f))
        drawCircle(color = Color.White, radius = 4f, center = Offset(size.width * 0.22f, size.height * 0.22f))
        drawCircle(color = Color.White, radius = 4f, center = Offset(size.width * 0.78f, size.height * 0.22f))
    }
}

@Composable
fun CanvasGraphicMock(id: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Linear robotic neon sweep backdrop
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF0D0D0D),
                    Color(0xFFE30613).copy(alpha = 0.2f),
                    Color(0xFF161616)
                )
            )
        )
        val center = Offset(size.width / 2f, size.height / 2f)

        when (id % 4) {
            0 -> {
                // Drawing 1: Conveyor Tunnel Wash & water rays
                drawCircle(color = Color(0xFFE30613).copy(alpha = 0.3f), radius = size.minDimension * 0.3f, center = center)
                repeat(8) { i ->
                    val angle = (i * 45) * (Math.PI / 180f)
                    val edgePoint = Offset(
                        (center.x + Math.cos(angle) * size.width * 0.2f).toFloat(),
                        (center.y + Math.sin(angle) * size.height * 0.3f).toFloat()
                    )
                    drawLine(color = Color.White.copy(alpha = 0.6f), start = center, end = edgePoint, strokeWidth = 3f)
                }
                drawCircle(color = Color.White, radius = size.minDimension * 0.08f, center = center)
            }
            1 -> {
                // Drawing 2: Deep snow rich lather bubbles
                drawCircle(color = Color(0xFFE30613), radius = size.minDimension * 0.25f, center = center, style = Stroke(width = 3f))
                repeat(6) { idx ->
                    val offset = Offset(idx * 40f + 20f, size.height * 0.4f)
                    drawCircle(color = Color.White.copy(alpha = 0.8f), radius = 18f, center = offset)
                    drawCircle(color = Color.White.copy(alpha = 0.9f), radius = 10f, center = offset + Offset(4f, -4f))
                }
            }
            2 -> {
                // Drawing 3: Teflon ceramic coat glaze star sparkles
                val shieldPath = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.2f)
                    lineTo(size.width * 0.7f, size.height * 0.35f)
                    lineTo(size.width * 0.7f, size.height * 0.65f)
                    lineTo(size.width * 0.5f, size.height * 0.8f)
                    lineTo(size.width * 0.3f, size.height * 0.65f)
                    lineTo(size.width * 0.3f, size.height * 0.35f)
                    close()
                }
                drawPath(shieldPath, color = Color(0xFFE30613))
                // Sparkle shining diamond inside
                val starPath = Path().apply {
                    moveTo(size.width * 0.5f, size.height * 0.38f)
                    lineTo(size.width * 0.54f, size.height * 0.5f)
                    lineTo(size.width * 0.66f, size.height * 0.5f)
                    lineTo(size.width * 0.56f, size.height * 0.58f)
                    lineTo(size.width * 0.6f, size.height * 0.7f)
                    lineTo(size.width * 0.5f, size.height * 0.62f)
                    lineTo(size.width * 0.4f, size.height * 0.7f)
                    lineTo(size.width * 0.44f, size.height * 0.58f)
                    lineTo(size.width * 0.34f, size.height * 0.5f)
                    lineTo(size.width * 0.46f, size.height * 0.5f)
                    close()
                }
                drawPath(starPath, color = Color.White)
            }
            else -> {
                // Drawing 4: Alloy tire sweeper outline
                drawCircle(color = Color.White, radius = size.minDimension * 0.28f, center = center, style = Stroke(width = 5f))
                drawCircle(color = Color(0xFFE30613), radius = size.minDimension * 0.12f, center = center)
                repeat(6) { i ->
                    val angle = (i * 60) * (Math.PI / 180f)
                    val rimPoint = Offset(
                        (center.x + Math.cos(angle) * size.width * 0.25f).toFloat(),
                        (center.y + Math.sin(angle) * size.height * 0.25f).toFloat()
                    )
                    drawLine(color = Color.White, start = center, end = rimPoint, strokeWidth = 4f)
                }
            }
        }
    }
}

@Composable
fun FAQItem(q: String, a: String) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = q,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.9f),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "",
                    tint = Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = a,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

// FlowRow Custom Layout Helper fallback
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val childConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val placeables = measurables.map { it.measure(childConstraints) }
        var yPosition = 0
        var xPosition = 0
        var maxRowHeight = 0
        val layoutWidth = constraints.maxWidth
        val placedCoordinates = mutableListOf<Triple<androidx.compose.ui.layout.Placeable, Int, Int>>()

        for (placeable in placeables) {
            if (xPosition + placeable.width > layoutWidth) {
                xPosition = 0
                yPosition += maxRowHeight + 8.dp.roundToPx() // Add line vertical spacing offset
                maxRowHeight = 0
            }
            placedCoordinates.add(Triple(placeable, xPosition, yPosition))
            xPosition += placeable.width + 8.dp.roundToPx() // Row horizontal gap offset
            maxRowHeight = maxOf(maxRowHeight, placeable.height)
        }

        val totalHeight = yPosition + maxRowHeight
        layout(layoutWidth, totalHeight) {
            for ((p, x, y) in placedCoordinates) {
                p.placeRelative(x, y)
            }
        }
    }
}
