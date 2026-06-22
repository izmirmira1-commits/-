package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.AstronomyUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

sealed interface AppState {
    object Onboarding : AppState
    object Main : AppState
}

data class CurrentHourData(
    val hourNumber: Int,
    val isDay: Boolean,
    val startHourDecimal: Double,
    val endHourDecimal: Double,
    val elapsedFraction: Float,
    val remainingTimeStr: String,
    val planetArabic: String,
    val planetSpiritual: String,
    val correspondence: HourCorrespondence?,
    val formattedStart: String,
    val formattedEnd: String
)

data class CurrentMansionData(
    val index: Int,
    val name: String,
    val sign: String,
    val element: String,
    val nature: String,
    val meaning: String,
    val action: String,
    val hoursRemaining: Double,
    val progressFraction: Float,
    val formattedRemaining: String
)

class AppViewModel(
    private val context: Context,
    private val repository: AppRepository
) : ViewModel() {

    // Navigation and Profile
    private val _appState = MutableStateFlow<AppState>(AppState.Onboarding)
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    // Astro Data & Tickers
    private val _currentTimeStr = MutableStateFlow("")
    val currentTimeStr: StateFlow<String> = _currentTimeStr.asStateFlow()

    private val _sunriseTimeStr = MutableStateFlow("06:00")
    val sunriseTimeStr: StateFlow<String> = _sunriseTimeStr.asStateFlow()

    private val _sunsetTimeStr = MutableStateFlow("18:00")
    val sunsetTimeStr: StateFlow<String> = _sunsetTimeStr.asStateFlow()

    private val _currentLocationName = MutableStateFlow("جاري جلب الموقع...")
    val currentLocationName: StateFlow<String> = _currentLocationName.asStateFlow()

    private val _currentCoordinates = MutableStateFlow<Pair<Double, Double>?>(null)
    val currentCoordinates: StateFlow<Pair<Double, Double>?> = _currentCoordinates.asStateFlow()

    private val _hourData = MutableStateFlow<CurrentHourData?>(null)
    val hourData: StateFlow<CurrentHourData?> = _hourData.asStateFlow()

    private val _mansionData = MutableStateFlow<CurrentMansionData?>(null)
    val mansionData: StateFlow<CurrentMansionData?> = _mansionData.asStateFlow()

    private val _aiAdvice = MutableStateFlow<String>("جاري استلهام أسرار اللحظة...")
    val aiAdvice: StateFlow<String> = _aiAdvice.asStateFlow()

    private val _isGeneratingAi = MutableStateFlow(false)
    val isGeneratingAi: StateFlow<Boolean> = _isGeneratingAi.asStateFlow()

    private val _allLunarMansions = MutableStateFlow<List<LunarMansion>>(emptyList())
    val allLunarMansions: StateFlow<List<LunarMansion>> = _allLunarMansions.asStateFlow()

    // Internal Calculations Cache
    private var localSunriseHours = 6.0
    private var localSunsetHours = 18.0
    private var lastGeneratedAdviceDetails: String = ""

    private var tickerJob: Job? = null

    init {
        // Observe profile and guide navigation
        viewModelScope.launch {
            repository.profileFlow.collect { profile ->
                _userProfile.value = profile
                if (profile != null) {
                    _appState.value = AppState.Main
                    // Restore profile coords if any
                    if (profile.latitude != 0.0 && profile.longitude != 0.0) {
                        _currentCoordinates.value = Pair(profile.latitude, profile.longitude)
                        resolveLocationName(profile.latitude, profile.longitude)
                        calculateSunTimes(profile.latitude, profile.longitude)
                    } else {
                        // Request GPS location
                        requestGpsLocation()
                    }
                } else {
                    _appState.value = AppState.Onboarding
                }
            }
        }

        // Fetch all lunar mansions once for educational browser or side charts
        viewModelScope.launch {
            repository.getAllMansionsFlow().collect { list ->
                _allLunarMansions.value = list
            }
        }

        // Start Clock & Logic Ticker
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                val now = Calendar.getInstance()
                
                // Formatted Current Time
                val sdf = SimpleDateFormat("hh:mm:ss a (EEEE)", Locale("ar"))
                _currentTimeStr.value = sdf.format(now.time)

                // Current Local Decimal Hour (0.0 to 24.0)
                val decimalHour = now.get(Calendar.HOUR_OF_DAY) +
                        (now.get(Calendar.MINUTE) / 60.0) +
                        (now.get(Calendar.SECOND) / 3600.0)

                // 0 = Sunday, 1 = Monday ... 6 = Saturday
                // java Calendar has SUNDAY = 1, MONDAY = 2... SATURDAY = 7
                val rawDay = now.get(Calendar.DAY_OF_WEEK)
                val dayOfWeek = (rawDay - 1).let { if (it < 0) 6 else it } // sunday -> 0, monday -> 1 ... saturday -> 6

                computeCalculatedAstro(decimalHour, dayOfWeek)

                delay(1000)
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private suspend fun computeCalculatedAstro(decimalHour: Double, dayOfWeek: Int) {
        val sunrise = localSunriseHours
        val sunset = localSunsetHours

        // 1. Compute biological planetary hour
        val isDay = decimalHour >= sunrise && decimalHour < sunset
        val dayHourLength = (sunset - sunrise) / 12.0
        val nightHourLength = (24.0 - (sunset - sunrise)) / 12.0

        val hourNum: Int
        val startHour: Double
        val endHour: Double
        
        if (isDay) {
            val idx = ((decimalHour - sunrise) / dayHourLength).toInt().coerceIn(0, 11)
            hourNum = idx + 1
            startHour = sunrise + idx * dayHourLength
            endHour = sunrise + (idx + 1) * dayHourLength
        } else {
            if (decimalHour >= sunset) {
                val idx = ((decimalHour - sunset) / nightHourLength).toInt().coerceIn(0, 11)
                hourNum = 13 + idx
                startHour = sunset + idx * nightHourLength
                endHour = sunset + (idx + 1) * nightHourLength
            } else {
                val idxFromSunrise = ((sunrise - decimalHour) / nightHourLength).toInt().coerceIn(0, 11)
                hourNum = 24 - idxFromSunrise
                startHour = sunrise - (idxFromSunrise + 1) * nightHourLength
                endHour = sunrise - idxFromSunrise * nightHourLength
            }
        }

        // Remaining time in current hour
        val remainingHours = endHour - decimalHour
        val remCal = (if (remainingHours < 0) remainingHours + 24.0 else remainingHours)
        val remMinTotal = (remCal * 60).toInt()
        val remSec = ((remCal * 3600) % 60).toInt()
        val remainingStr = String.format("%02d دقيقة و %02d ثانية", remMinTotal, remSec)

        val progressFrac = (1f - (remainingHours / (if (isDay) dayHourLength else nightHourLength)).toFloat()).coerceIn(0f, 1f)

        // Query Database correspondence
        val corr = repository.getCorrespondence(dayOfWeek, hourNum)
        val finalPlanet = corr?.planetArabic ?: when (dayOfWeek) {
            0 -> "شمس"
            1 -> "قمر"
            2 -> "مريخ"
            3 -> "عطارد"
            4 -> "مشتري"
            5 -> "زهرة"
            else -> "زحل"
        }
        val finalSpiritual = corr?.planetSpiritual ?: "أسرار الفلك"

        _hourData.value = CurrentHourData(
            hourNumber = hourNum,
            isDay = isDay,
            startHourDecimal = startHour,
            endHourDecimal = endHour,
            elapsedFraction = progressFrac,
            remainingTimeStr = remainingStr,
            planetArabic = finalPlanet,
            planetSpiritual = finalSpiritual,
            correspondence = corr,
            formattedStart = formatDecimalToTime(startHour),
            formattedEnd = formatDecimalToTime(endHour)
        )

        // 2. Compute Lunar Longitude and Mansion
        val nowMs = System.currentTimeMillis()
        val lunarLon = AstronomyUtils.getLunarLongitude(nowMs)
        val mansionIdx = AstronomyUtils.getMansionIndex(lunarLon)
        val mansionRemainingHours = AstronomyUtils.getHoursRemainingInMansion(lunarLon)

        val lunarDb = repository.getLunarMansion(mansionIdx)
        if (lunarDb != null) {
            val totalMansionDuration = 360.0 / 28.0 / 0.549 // ~23.4 hours
            val elapsedHours = totalMansionDuration - mansionRemainingHours
            val progressMansionFrac = (elapsedHours / totalMansionDuration).toFloat().coerceIn(0f, 1f)
            
            val remMansionSec = (mansionRemainingHours * 3600).toLong()
            val remMHours = remMansionSec / 3600
            val remMMin = (remMansionSec % 3600) / 60
            val formattedMansionRemStr = String.format("%02d ساعة و %02d دقيقة", remMHours, remMMin)

            _mansionData.value = CurrentMansionData(
                index = mansionIdx,
                name = lunarDb.name,
                sign = lunarDb.zodiacSign,
                element = lunarDb.element,
                nature = lunarDb.nature,
                meaning = lunarDb.meanings,
                action = lunarDb.actions,
                hoursRemaining = mansionRemainingHours,
                progressFraction = progressMansionFrac,
                formattedRemaining = formattedMansionRemStr
            )
        }

        // 3. Dynamic Advice generation trigger
        // Re-generate if key variables changed or if nothing is generated yet
        val cacheKey = "$finalPlanet|$mansionIdx|$dayOfWeek"
        if (cacheKey != lastGeneratedAdviceDetails) {
            lastGeneratedAdviceDetails = cacheKey
            triggerAdviceRefresh(corr, lunarDb, dayOfWeek)
        }
    }

    fun triggerAdviceRefresh(corr: HourCorrespondence?, mansion: LunarMansion?, dayOfWeek: Int) {
        viewModelScope.launch {
            _isGeneratingAi.value = true
            
            val dayPlanetName = when (dayOfWeek) {
                0 -> "الشمس"
                1 -> "القمر"
                2 -> "المريخ"
                3 -> "عطارد"
                4 -> "المشتري"
                5 -> "الزهرة"
                else -> "زحل"
            }

            if (corr != null && mansion != null) {
                try {
                    // Try to generate live with Gemini
                    val liveAdvice = withContext(Dispatchers.IO) {
                        GeminiApiClient.generateSpiritualSummary(
                            planet = corr.planetArabic,
                            spiritualPlanetName = corr.planetSpiritual,
                            dayPlanet = dayPlanetName,
                            mansion = mansion.name,
                            angel = corr.angelName,
                            divineName = corr.divineName,
                            letters = corr.letters
                        )
                    }
                    _aiAdvice.value = liveAdvice
                } catch (e: Exception) {
                    // Fail gracefully to beautiful offline template
                    _aiAdvice.value = generateOfflineSpiritualAdvice(corr, mansion, dayPlanetName)
                }
            } else {
                _aiAdvice.value = "جاري تجميع تنزلات أسرار الفاتحة وتدشين الجداول الروحية..."
            }
            _isGeneratingAi.value = false
        }
    }

    private fun generateOfflineSpiritualAdvice(corr: HourCorrespondence, mansion: LunarMansion, dayPlanet: String): String {
        return """
            اعلم أيها السالك الحريص على أسرار التنزلات الغيبية، أن هذه الساعة ومحكوماتها طالعة بنور الله وجلال عظمته. 
            فهي ساعة مباركة ينبض طالعها ببركات كوكب ${corr.planetArabic} وهو ${corr.planetSpiritual}، وتحت حِقبة يوم يحكمه روحاني كوكب $dayPlanet.
            
            وفي هذا الوقت، يستقر القمر الشريف ببهائه في منزلة (${mansion.name} - طالع ${mansion.zodiacSign})، مجرياً فضيلتها في فلك طبع الحرف ومقتضى الطبيعة (${corr.temperament}) ومتوجهاً بالأفق نحو اتجاه الـ (${corr.direction}).
            
            الملك الموكل القائم بتدبير الملكوت الأرضي والسماوي في هذا الميقات هو السيد المَلَك الجليل (${corr.angelName})، والسر الرازق الممد لهذا الوقت يسري من الاسم الأعظم وسر التجلي الباطن (${corr.divineName})، وجوهر حرفها المكتوم بالأسرار هو (${corr.letters}).
            
            📌 أبواب الفتوح وصلاح الأعمال:
            ينفتح في هذه الساعة من اللطائف الإلهية طاقات عظيمة؛ وتصلح هذه الدقيقة تحديداً لـ: ${corr.positiveWorks} وقبائل الحوائج، وتعمها إشارات منزلة ${mansion.name} في: ${mansion.actions}.
            
            ⚠️ المحاذير الإعجازية:
            احذر رعاك الله وحماك من مغبّات تغالب الطبع ومصادمة النحس الموانع الكامنة، وتجنب بجهدك: ${corr.caution} لئلا تفسد نيتك وتصدم الفيوضات الخادمة.
            
            فالزم الذكر والمجاهدة، واجعل اسم الله الأعظم (${corr.divineName}) وِرد طالعك وكرِّر الحرف المرقوم (${corr.letters}) تبلغ الفتح الأكبر بجاه رسول الله صلى الله عليه وسلم وبمدد من العارفين كالبوني وابن عربي الفاتح.
        """.trimIndent()
    }

    @SuppressLint("DefaultLocale")
    private fun formatDecimalToTime(decimal: Double): String {
        val h = decimal.toInt()
        val m = ((decimal - h) * 60).toInt()
        val suffix = if (h >= 12) "م" else "ص"
        val displayH = if (h == 0) 12 else if (h > 12) h - 12 else h
        return String.format("%d:%02d %s", displayH, m, suffix)
    }

    // Manual profile save/update
    fun saveUserProfile(name: String, birthDate: String, birthPlace: String, country: String, callback: () -> Unit) {
        viewModelScope.launch {
            val coords = _currentCoordinates.value
            val profile = UserProfile(
                name = name,
                birthDate = birthDate,
                birthPlace = birthPlace,
                country = country,
                latitude = coords?.first ?: 0.0,
                longitude = coords?.second ?: 0.0
            )
            repository.insertOrUpdateProfile(profile)
            callback()
        }
    }

    // Manual coords update to enforce custom city/location
    fun updateManualCoordinates(lat: Double, lng: Double) {
        viewModelScope.launch {
            _currentCoordinates.value = Pair(lat, lng)
            resolveLocationName(lat, lng)
            calculateSunTimes(lat, lng)
            
            // Save inside database if user profile is already created
            _userProfile.value?.let { profile ->
                val updated = profile.copy(latitude = lat, longitude = lng)
                repository.insertOrUpdateProfile(updated)
            }
        }
    }

    // Reverse Geocoding
    private fun resolveLocationName(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale("ar"))
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: ""
                    val country = addr.countryName ?: ""
                    val name = if (city.isNotEmpty()) "$city، $country" else country
                    _currentLocationName.value = name
                } else {
                    _currentLocationName.value = "إحداثيات: \n" + String.format("%.3f , %.3f", lat, lng)
                }
            } catch (e: Exception) {
                _currentLocationName.value = "إحداثيات: \n" + String.format("%.3f , %.3f", lat, lng)
            }
        }
    }

    // Fetch live SunriseSunset API or fallback to AstronomyUtils formulas
    private suspend fun calculateSunTimes(lat: Double, lng: Double) {
        withContext(Dispatchers.IO) {
            try {
                val response = SunriseSunsetApi.service.getTimes(lat, lng)
                if (response.status == "OK" && response.results != null) {
                    val localTz = TimeZone.getDefault()
                    val sunriseDecimal = parseUtcToLocalDecimal(response.results.sunrise, localTz)
                    val sunsetDecimal = parseUtcToLocalDecimal(response.results.sunset, localTz)
                    
                    localSunriseHours = sunriseDecimal
                    localSunsetHours = sunsetDecimal

                    _sunriseTimeStr.value = formatDecimalToTime(sunriseDecimal)
                    _sunsetTimeStr.value = formatDecimalToTime(sunsetDecimal)
                } else {
                    useOfflineAstronomy(lat, lng)
                }
            } catch (e: Exception) {
                // Catch all block -> fallback to offline calculations
                useOfflineAstronomy(lat, lng)
            }
        }
    }

    private fun useOfflineAstronomy(lat: Double, lng: Double) {
        val backupTimes = AstronomyUtils.getBackupSunriseSunset(lat, lng, Calendar.getInstance())
        localSunriseHours = backupTimes.first
        localSunsetHours = backupTimes.second

        _sunriseTimeStr.value = formatDecimalToTime(backupTimes.first)
        _sunsetTimeStr.value = formatDecimalToTime(backupTimes.second)
    }

    private fun parseUtcToLocalDecimal(utcTimeStr: String, localTz: TimeZone): Double {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            val date = format.parse(utcTimeStr) ?: return 6.0
            
            val calendar = Calendar.getInstance(localTz)
            calendar.time = date
            
            val h = calendar.get(Calendar.HOUR_OF_DAY)
            val m = calendar.get(Calendar.MINUTE)
            val s = calendar.get(Calendar.SECOND)
            h + (m / 60.0) + (s / 3600.0)
        } catch (e: Exception) {
            6.0
        }
    }

    // Location Service Trigger
    @SuppressLint("MissingPermission")
    fun requestGpsLocation() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
            .addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    updateManualCoordinates(loc.latitude, loc.longitude)
                } else {
                    fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        if (lastLoc != null) {
                            updateManualCoordinates(lastLoc.latitude, lastLoc.longitude)
                        } else {
                            // Cairo, Egypt as a spiritual default
                            updateManualCoordinates(30.0444, 31.2357) 
                        }
                    }
                }
            }
            .addOnFailureListener {
                // Cairo, Egypt fallback on prompt failure
                updateManualCoordinates(30.0444, 31.2357) 
            }
    }

    fun resetProfile() {
        viewModelScope.launch {
            repository.deleteProfile()
            _userProfile.value = null
            _appState.value = AppState.Onboarding
        }
    }
}
