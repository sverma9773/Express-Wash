package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
class CarWashViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CarWashRepository(AppDatabase.getDatabase(application).carWashDao())
    val userEmail = "sverma9773@gmail.com" // Preserving user context
    private val prefs = application.getSharedPreferences("xpress_carwash_prefs", Context.MODE_PRIVATE)
    private val _searchQuery = MutableStateFlow("")

    // Admin login states
    var isAdminLoggedIn by mutableStateOf(false)
    var adminUsernameState by mutableStateOf("")
    var adminPasswordState by mutableStateOf("")
    var adminLoginError by mutableStateOf("")

    // User login states
    var signInEmail by mutableStateOf("")
    var signInPassword by mutableStateOf("")
    var signInError by mutableStateOf("")

    // User signup states
    var signUpName by mutableStateOf("")
    var signUpEmail by mutableStateOf("")
    var signUpPhone by mutableStateOf("")
    var signUpPassword by mutableStateOf("")
    var signUpError by mutableStateOf("")

    // BOOKING FORM INPUTS & VALIDATION
    var fullName by mutableStateOf("")
    var mobileNumber by mutableStateOf("")
    var emailAddress by mutableStateOf("")
    var vehicleType by mutableStateOf("Sedan") // Hatchback, Sedan, SUV, Luxury Car
    val selectedServices = mutableStateOf<Set<String>>(emptySet())
    var bookingDate by mutableStateOf("")
    var bookingTimeSlot by mutableStateOf("09:00 AM")

    // Submission states
    var hasSubmissionError by mutableStateOf(false)
    var lastErrorMsg by mutableStateOf("")
    var successfullyBookedRecord by mutableStateOf<Booking?>(null)

    // Active session states
    var isUserLoggedIn by mutableStateOf(false)
    var loggedInUserEmail by mutableStateOf("")
    var loggedInUserName by mutableStateOf("")
    var loggedInUserPhone by mutableStateOf("")

    // EXTRA PREMIUM STATE
    var isDarkMode by mutableStateOf(true) // Start Dark Mode by default for luxurious vibes
    var isRefreshing by mutableStateOf(false)
        private set

    // Booking search state
    var searchMobileQuery by mutableStateOf("")

    init {
        // Load persistable sessions
        isUserLoggedIn = prefs.getBoolean("is_user_logged_in", false)
        loggedInUserEmail = prefs.getString("logged_in_user_email", "") ?: ""
        loggedInUserName = prefs.getString("logged_in_user_name", "") ?: ""
        loggedInUserPhone = prefs.getString("logged_in_user_phone", "") ?: ""
        isAdminLoggedIn = prefs.getBoolean("is_admin_logged_in", false)
        
        if (isUserLoggedIn) {
            fullName = loggedInUserName
            mobileNumber = loggedInUserPhone
            emailAddress = loggedInUserEmail
            onSearchQueryChanged(loggedInUserPhone)
        }

        // Seed default sandbox user sverma9773@gmail.com so it's ready out-of-the-box
        if (prefs.getString("usr_pwd_$userEmail", "").isNullOrEmpty()) {
            prefs.edit()
                .putString("usr_pwd_$userEmail", "xpress123")
                .putString("usr_name_$userEmail", "Sanjay Verma")
                .putString("usr_phone_$userEmail", "+91 97731 23456")
                .apply()
        }
    }

    // ==========================================
    // REACTIVE STREAMS FROM DATABASE
    // ==========================================
    val allBookings = repository.allBookings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allOffers = repository.allOffers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allGalleryItems = repository.allGalleryItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allReviews = repository.allReviews.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dynamic search flow
    val searchResults: StateFlow<List<Booking>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                allBookings
            } else {
                repository.searchBookings(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        searchMobileQuery = newQuery
    }

    // Refresh simulator
    fun triggerPullToRefresh() {
        viewModelScope.launch {
            isRefreshing = true
            delay(1500) // Realistic server sync simulation
            isRefreshing = false
        }
    }

    // ==========================================
    // BOOKING FORM INPUTS & VALIDATION
    // ==========================================

    // Validations
    fun toggleServiceSelection(service: String) {
        val current = selectedServices.value
        if (current.contains(service)) {
            selectedServices.value = current - service
        } else {
            selectedServices.value = current + service
        }
    }

    fun submitBooking(onSuccess: () -> Unit) {
        // Simple client-side checks
        if (fullName.isBlank()) {
            hasSubmissionError = true
            lastErrorMsg = "Please enter your full name."
            return
        }
        if (mobileNumber.length < 10) {
            hasSubmissionError = true
            lastErrorMsg = "Please enter a valid 10-digit mobile number."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress).matches()) {
            hasSubmissionError = true
            lastErrorMsg = "Please enter a valid email address."
            return
        }
        if (selectedServices.value.isEmpty()) {
            hasSubmissionError = true
            lastErrorMsg = "Please select at least one premium service."
            return
        }
        if (bookingDate.isBlank()) {
            hasSubmissionError = true
            lastErrorMsg = "Please select a booking date."
            return
        }

        hasSubmissionError = false
        
        // Formulate a beautiful booking ID (e.g., RX-Lucknow-12459)
        val rand = (10000..99999).random()
        val customId = "RX-$rand"

        val serviceSummaryString = selectedServices.value.joinToString(", ")

        // Prevention of exact duplicate booking check (Mobile + Date + Slot)
        val duplicate = allBookings.value.any {
            it.mobile == mobileNumber && it.dateString == bookingDate && it.timeSlot == bookingTimeSlot && it.status != "Cancelled"
        }

        if (duplicate) {
            hasSubmissionError = true
            lastErrorMsg = "A wash appointment is already scheduled for this mobile number on this slot/day."
            return
        }

        val newBooking = Booking(
            fullName = fullName,
            mobile = mobileNumber,
            email = emailAddress,
            vehicleType = vehicleType,
            services = serviceSummaryString,
            dateString = bookingDate,
            timeSlot = bookingTimeSlot,
            status = "Confirmed", // Instant confirmation as premium feature!
            bookingId = customId
        )

        viewModelScope.launch {
            repository.bookAppointment(newBooking)
            successfullyBookedRecord = newBooking
            // Reset fields
            fullName = ""
            mobileNumber = ""
            emailAddress = ""
            selectedServices.value = emptySet()
            bookingDate = ""
            onSuccess()
        }
    }

    // Custom pricing database map for package summary and financial statements
    val servicePrices = mapOf(
        "Automatic Wash" to 500,
        "Foam Wash" to 300,
        "Wax Wash" to 400,
        "Detailing" to 1500,
        "Teflon Coating" to 2200,
        "Polishing" to 800,
        "Interior Cleaning" to 600,
        "Automatic Car Wash" to 500,
        "Foam & High-Pressure Jet Wash" to 300,
        "Wax Wash & Paint Protection" to 400,
        "Interior Dry Cleaning" to 600,
        "Alloy Wheel Deep Cleaning" to 900,
        "Rubbing & Polishing" to 1000,
        "Interior & Exterior Detailing" to 3500
    )

    fun calculateBookingPrice(servicesString: String): Int {
        var total = 0
        val items = servicesString.split(", ")
        for (item in items) {
            total += servicePrices[item.trim()] ?: 450 // Default backup price for packages
        }
        return total
    }

    // ==========================================
    // BACKEND USER & ADMIN OPERATIONS
    // ==========================================
    fun performAdminLogin(): Boolean {
        adminLoginError = ""
        if (adminUsernameState.lowercase() == "admin" && adminPasswordState == "xpress123") {
            isAdminLoggedIn = true
            isUserLoggedIn = false // mutually exclusive
            prefs.edit()
                .putBoolean("is_admin_logged_in", true)
                .putBoolean("is_user_logged_in", false)
                .apply()
            return true
        } else {
            adminLoginError = "Invalid admin username or password credentials."
            return false
        }
    }

    fun performAdminLogout() {
        isAdminLoggedIn = false
        adminUsernameState = ""
        adminPasswordState = ""
        prefs.edit()
            .putBoolean("is_admin_logged_in", false)
            .apply()
    }

    fun performUserLogin(): Boolean {
        signInError = ""
        val email = signInEmail.trim()
        val password = signInPassword.trim()

        if (email.isEmpty() || password.isEmpty()) {
            signInError = "Please fill in all credentials."
            return false
        }

        val storedPass = prefs.getString("usr_pwd_$email", "")
        if (storedPass.isNullOrEmpty()) {
            signInError = "No account found for this email. Please sign up first!"
            return false
        }

        if (storedPass != password) {
            signInError = "Incorrect password. Please try again."
            return false
        }

        // Success! Load details
        isUserLoggedIn = true
        isAdminLoggedIn = false
        loggedInUserEmail = email
        loggedInUserName = prefs.getString("usr_name_$email", "Valued Client") ?: "Valued Client"
        loggedInUserPhone = prefs.getString("usr_phone_$email", "") ?: ""

        fullName = loggedInUserName
        mobileNumber = loggedInUserPhone
        emailAddress = loggedInUserEmail
        onSearchQueryChanged(loggedInUserPhone)

        prefs.edit()
            .putBoolean("is_user_logged_in", true)
            .putBoolean("is_admin_logged_in", false)
            .putString("logged_in_user_email", loggedInUserEmail)
            .putString("logged_in_user_name", loggedInUserName)
            .putString("logged_in_user_phone", loggedInUserPhone)
            .apply()
        return true
    }

    fun performUserSignUp(): Boolean {
        signUpError = ""
        val name = signUpName.trim()
        val email = signUpEmail.trim()
        val phone = signUpPhone.trim()
        val password = signUpPassword.trim()

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            signUpError = "All fields are required to register."
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signUpError = "Please enter a valid email address."
            return false
        }

        if (phone.length < 10) {
            signUpError = "Please enter a valid mobile number."
            return false
        }

        if (password.length < 6) {
            signUpError = "Password must be at least 6 characters."
            return false
        }

        // Save new user in SharedPreferences
        prefs.edit()
            .putString("usr_pwd_$email", password)
            .putString("usr_name_$email", name)
            .putString("usr_phone_$email", phone)
            .apply()

        // Automatically log them in as User
        isUserLoggedIn = true
        isAdminLoggedIn = false
        loggedInUserEmail = email
        loggedInUserName = name
        loggedInUserPhone = phone

        fullName = name
        mobileNumber = phone
        emailAddress = email
        onSearchQueryChanged(phone)

        prefs.edit()
            .putBoolean("is_user_logged_in", true)
            .putBoolean("is_admin_logged_in", false)
            .putString("logged_in_user_email", loggedInUserEmail)
            .putString("logged_in_user_name", loggedInUserName)
            .putString("logged_in_user_phone", loggedInUserPhone)
            .apply()

        // Clear forms
        signUpName = ""
        signUpEmail = ""
        signUpPhone = ""
        signUpPassword = ""

        return true
    }

    fun performUserLogout() {
        isUserLoggedIn = false
        loggedInUserEmail = ""
        loggedInUserName = ""
        loggedInUserPhone = ""
        
        fullName = ""
        mobileNumber = ""
        emailAddress = ""
        onSearchQueryChanged("")

        prefs.edit()
            .putBoolean("is_user_logged_in", false)
            .putString("logged_in_user_email", "")
            .putString("logged_in_user_name", "")
            .putString("logged_in_user_phone", "")
            .apply()
    }

    fun changeBookingStatus(bookingId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, newStatus)
        }
    }

    fun deleteBooking(bookingId: Int) {
        viewModelScope.launch {
            repository.deleteBooking(bookingId)
        }
    }

    // Dynamic Admin Dashboard Aggregates
    val adminStatsFlow = allBookings.map { list ->
        val total = list.size
        val pendingCount = list.count { it.status == "Pending" || it.status == "Confirmed" }
        val completedCount = list.count { it.status == "Completed" }
        var totalRevenue = 15400 // Nice baseline starting income
        list.forEach {
            if (it.status != "Cancelled") {
                totalRevenue += calculateBookingPrice(it.services)
            }
        }
        AdminStats(
            totalBookings = total,
            todaysBookings = pendingCount,
            completedBookings = completedCount,
            totalRevenue = totalRevenue
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdminStats(0, 0, 0, 0)
    )

    data class AdminStats(
        val totalBookings: Int,
        val todaysBookings: Int,
        val completedBookings: Int,
        val totalRevenue: Int
    )

    // Dynamic Offer CRUD from dashboard
    var newOfferTitle by mutableStateOf("")
    var newOfferDesc by mutableStateOf("")
    var newOfferDiscount by mutableStateOf("15% OFF")
    var newOfferCode by mutableStateOf("")

    fun adminCreateOffer() {
        if (newOfferTitle.isNotBlank() && newOfferDesc.isNotBlank()) {
            val offer = Offer(
                title = newOfferTitle,
                description = newOfferDesc,
                discountText = newOfferDiscount,
                promoCode = newOfferCode.uppercase()
            )
            viewModelScope.launch {
                repository.addOffer(offer)
                newOfferTitle = ""
                newOfferDesc = ""
                newOfferDiscount = "15% OFF"
                newOfferCode = ""
            }
        }
    }

    fun adminDeleteOffer(offerId: Int) {
        viewModelScope.launch {
            repository.removeOffer(offerId)
        }
    }

    // Dynamic Gallery CRUD
    var newGalleryCategory by mutableStateOf("Automatic Wash")
    var newGalleryTitle by mutableStateOf("")
    var newGalleryDesc by mutableStateOf("")

    fun adminCreateGalleryItem() {
        if (newGalleryTitle.isNotBlank() && newGalleryDesc.isNotBlank()) {
            val icons = listOf("ic_launcher_foreground", "ic_washer", "ic_car_shine", "ic_custom_clean")
            val randIcon = icons.random()
            val newItem = GalleryItem(
                category = newGalleryCategory,
                title = newGalleryTitle,
                description = newGalleryDesc,
                drawableResId = randIcon
            )
            viewModelScope.launch {
                repository.addGalleryItem(newItem)
                newGalleryTitle = ""
                newGalleryDesc = ""
            }
        }
    }

    fun adminDeleteGallery(itemId: Int) {
        viewModelScope.launch {
            repository.removeGalleryItem(itemId)
        }
    }

    // Reviews addition
    var customerReviewerName by mutableStateOf("")
    var customerRating by mutableStateOf(5)
    var customerReviewComment by mutableStateOf("")
    var customerVehicleModel by mutableStateOf("")

    fun submitCustomerReview() {
        if (customerReviewerName.isNotBlank() && customerReviewComment.isNotBlank()) {
            val newRev = Review(
                reviewerName = customerReviewerName,
                rating = customerRating,
                comment = customerReviewComment,
                vehicleModel = if (customerVehicleModel.isBlank()) "Sedan" else customerVehicleModel
            )
            viewModelScope.launch {
                repository.addReview(newRev)
                customerReviewerName = ""
                customerRating = 5
                customerReviewComment = ""
                customerVehicleModel = ""
            }
        }
    }
}
