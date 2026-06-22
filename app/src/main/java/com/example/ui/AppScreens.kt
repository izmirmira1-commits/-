package com.example.ui

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.data.*
import androidx.compose.foundation.lazy.LazyColumn
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationContainer(viewModel: AppViewModel) {
    val appState by viewModel.appState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Crossfade(targetState = appState, label = "ScreenTransition") { state ->
            when (state) {
                is AppState.Onboarding -> OnboardingScreen(viewModel)
                is AppState.Main -> MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun OnboardingScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var birthPlace by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    val coords by viewModel.currentCoordinates.collectAsState()
    val coordsName by viewModel.currentLocationName.collectAsState()

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            birthDate = "$year-${month + 1}-$dayOfMonth"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFCF5))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("onboarding_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE5DECF))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mystical Header Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFF4F1E8), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Cosmic Icon",
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "الساعة البونية",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5C4A31),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "تهيئة ميثاق طالع المريد وتعديل ميزان الفلك الروحاني",
                    fontSize = 12.sp,
                    color = Color(0xFF8C7A61),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المريد (الاسم الثنائي)", color = Color(0xFF8C7A61)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E293B),
                        unfocusedTextColor = Color(0xFF475569),
                        focusedBorderColor = Color(0xFFB45309),
                        unfocusedBorderColor = Color(0xFFD6CDBA),
                        focusedLabelColor = Color(0xFFB45309),
                        unfocusedLabelColor = Color(0xFF8C7A61)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("username_input")
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text("تاريخ الميلاد الشريف", color = Color(0xFF8C7A61)) },
                    readOnly = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E293B),
                        unfocusedTextColor = Color(0xFF475569),
                        focusedBorderColor = Color(0xFFB45309),
                        unfocusedBorderColor = Color(0xFFD6CDBA),
                        focusedLabelColor = Color(0xFFB45309),
                        unfocusedLabelColor = Color(0xFF8C7A61)
                    ),
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Filled.DateRange, contentDescription = "Calendar", tint = Color(0xFFB45309))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { datePickerDialog.show() }
                )

                OutlinedTextField(
                    value = birthPlace,
                    onValueChange = { birthPlace = it },
                    label = { Text("محل الولادة (المدينة)", color = Color(0xFF8C7A61)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E293B),
                        unfocusedTextColor = Color(0xFF475569),
                        focusedBorderColor = Color(0xFFB45309),
                        unfocusedBorderColor = Color(0xFFD6CDBA),
                        focusedLabelColor = Color(0xFFB45309),
                        unfocusedLabelColor = Color(0xFF8C7A61)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = country,
                    onValueChange = { country = it },
                    label = { Text("البلد الحالي", color = Color(0xFF8C7A61)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E293B),
                        unfocusedTextColor = Color(0xFF475569),
                        focusedBorderColor = Color(0xFFB45309),
                        unfocusedBorderColor = Color(0xFFD6CDBA),
                        focusedLabelColor = Color(0xFFB45309),
                        unfocusedLabelColor = Color(0xFF8C7A61)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                // Location GPS box during onboarding
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F1E8)),
                    border = BorderStroke(1.dp, Color(0xFFE5DECF))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = "GPS",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ميزان طالع إحداثياتك الحالي:",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = coordsName,
                                fontSize = 13.sp,
                                color = Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { viewModel.requestGpsLocation() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Retry GPS", tint = Color(0xFFB45309))
                        }
                    }
                }

                Button(
                    onClick = {
                        if (name.isNotEmpty() && birthDate.isNotEmpty() && birthPlace.isNotEmpty() && country.isNotEmpty()) {
                            viewModel.saveUserProfile(name, birthDate, birthPlace, country) {
                                // proceed
                            }
                        } else {
                            // prompt
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4A31)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_button")
                ) {
                    Text(
                        text = "كشف الطالع والولوج في الدائرة الروحية",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val currentProfile by viewModel.userProfile.collectAsState()
    val timeStr by viewModel.currentTimeStr.collectAsState()
    val sunriseStr by viewModel.sunriseTimeStr.collectAsState()
    val sunsetStr by viewModel.sunsetTimeStr.collectAsState()
    val locName by viewModel.currentLocationName.collectAsState()
    val coords by viewModel.currentCoordinates.collectAsState()

    val hourData by viewModel.hourData.collectAsState()
    val mansionData by viewModel.mansionData.collectAsState()
    val aiAdvice by viewModel.aiAdvice.collectAsState()
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsState()

    // Slide-out Drawer for settings
    val sheetState = rememberModalBottomSheetState()
    var showSettingsSheet by remember { mutableStateOf(false) }

    // Grab dynamic theme based on currently ruling planet hour
    val planetName = hourData?.planetArabic ?: "زحل"
    val planetGradient = getPlanetGradient(planetName)
    val planetAccent = getPlanetAccent(planetName)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFCF5)) // Warm sand/cream background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Elegant Cultural Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Settings/Profile button
                IconButton(onClick = { showSettingsSheet = true }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF5C4A31)
                    )
                }

                // App Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "الساعة البونية",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5C4A31)
                    )
                    Text(
                        text = "المدد الفلكي واللطائف الروحية",
                        fontSize = 11.sp,
                        color = Color(0xFF8C7A61)
                    )
                }

                // Share Button
                IconButton(onClick = {
                    shareCurrentAstroMoment(context, hourData, mansionData, timeStr, locName)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = Color(0xFF5C4A31)
                    )
                }
            }

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.0f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Greeting & Location Banner
                item {
                    LocationBanner(
                        userName = currentProfile?.name ?: "المريد",
                        locName = locName,
                        sunrise = sunriseStr,
                        sunset = sunsetStr,
                        timeStr = timeStr,
                        accentColor = planetAccent
                    )
                }

                // 1. Planetary Hour Card
                item {
                    PlanetaryHourCard(
                        data = hourData,
                        accentColor = planetAccent
                    )
                }

                // 2. Lunar Mansion Card
                item {
                    LunarMansionCard(
                        data = mansionData,
                        accentColor = planetAccent
                    )
                }

                // 3. Spiritual Correspondences Block (Agels, letters, incense)
                item {
                    SpiritualCorrespondencesBlock(
                        corr = hourData?.correspondence,
                        accentColor = planetAccent
                    )
                }

                // 4. Spiritual AI Advice Prose Card
                item {
                    SufiAIProseCard(
                        prose = aiAdvice,
                        isLoading = isGeneratingAi,
                        accentColor = planetAccent,
                        onRefresh = {
                            hourData?.let { h ->
                                mansionData?.let { m ->
                                    val now = Calendar.getInstance()
                                    val rawDay = now.get(Calendar.DAY_OF_WEEK)
                                    val dayOfWeek = (rawDay - 1).let { if (it < 0) 6 else it }
                                    viewModel.triggerAdviceRefresh(h.correspondence, viewModel.mansionData.value?.let { md ->
                                        // Fetch actual lunar mansion mapping
                                        LunarMansion(md.index, md.name, 0.0, 0.0, md.sign, md.element, md.nature, md.meaning, md.action)
                                    }, dayOfWeek)
                                }
                            }
                        }
                    )
                }
            }
        }

        // Location Drawer Sheet
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = Color(0xFFFDFCF5)
            ) {
                LocationSettingsSheetContent(
                    viewModel = viewModel,
                    onDismiss = { showSettingsSheet = false }
                )
            }
        }
    }
}

@Composable
fun LocationBanner(
    userName: String,
    locName: String,
    sunrise: String,
    sunset: String,
    timeStr: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5DECF))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مرحباً بك يا سائل الميقات القويم،",
                        fontSize = 11.sp,
                        color = Color(0xFF8C7A61)
                    )
                    Text(
                        text = userName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5C4A31)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "طالع متصل", color = Color(0xFFB45309), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Color(0xFFE5DECF), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "موقعك الحالي:", fontSize = 10.sp, color = Color(0xFF8C7A61))
                    }
                    Text(text = locName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "الشروق: $sunrise", fontSize = 12.sp, color = Color(0xFF5C4A31))
                    Text(text = "الغروب: $sunset", fontSize = 12.sp, color = Color(0xFF5C4A31))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4F1E8), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = timeStr,
                    color = Color(0xFF1E293B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PlanetaryHourCard(
    data: CurrentHourData?,
    accentColor: Color
) {
    if (data == null) {
        Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = accentColor)
        }
        return
    }

    val planetGradient = getPlanetGradient(data.planetArabic)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(planetGradient))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (data.isDay) Color(0xFFFBBF24) else Color(0xFF38BDF8), RoundedCornerShape(5.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الساعة الكوكبية: ${data.hourNumber}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (data.isDay) "ساعة نهارية" else "ساعة ليلية",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "كوكب الساعة: ${data.planetArabic}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = data.planetSpiritual,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Timeline graphics showing elapsed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = data.formattedStart, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    LinearProgressIndicator(
                        progress = data.elapsedFraction,
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Text(text = data.formattedEnd, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مرور طاقة الكوكب بالساعة:",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "ينتهي الكشف خلال: ${data.remainingTimeStr}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun LunarMansionCard(
    data: CurrentMansionData?,
    accentColor: Color
) {
    if (data == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5DECF))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "منزلة القمر الغالبة",
                    fontSize = 14.sp,
                    color = Color(0xFF8C7A61)
                )
                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFFEF3C7),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = data.nature,
                        color = Color(0xFFB45309),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${data.index}: منزلة ${data.name}",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF5C4A31)
            )

            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "برج المنزلة: ${data.sign}", fontSize = 12.sp, color = Color(0xFF5C4A31), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "طبع المنزلة: ${data.element}", fontSize = 12.sp, color = Color(0xFF64748B))
            }

            Text(
                text = data.meaning,
                fontSize = 12.sp,
                color = Color(0xFF334155),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4F1E8), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "الأعمال الملائمة بالمنزلة:", fontSize = 11.sp, color = Color(0xFFB45309), fontWeight = FontWeight.Bold)
                    Text(text = data.action, fontSize = 12.sp, color = Color(0xFF1E293B))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الانتقال للمنزلة التالية:",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = data.formattedRemaining,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SpiritualCorrespondencesBlock(
    corr: HourCorrespondence?,
    accentColor: Color
) {
    if (corr == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5DECF))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "اللطائف الإلهية والملائكية للساعة",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5C4A31),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Grid of Correspondences
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CorrespondenceItem(label = "الملك الموكل بالساعة", value = corr.angelName, icon = Icons.Filled.Person)
                CorrespondenceItem(label = "الرسول المرتبط بالساعة", value = corr.prophetName, icon = Icons.Filled.Person)
                CorrespondenceItem(label = "الاسم الأعظم الصوفي للذكر", value = corr.divineName, icon = Icons.Filled.Check, valueColor = Color(0xFFB45309))
                CorrespondenceItem(label = "أسرار حروف المدد للساعة", value = corr.letters, icon = Icons.Filled.Edit, valueColor = Color(0xFFB45309))
                CorrespondenceItem(label = "البخور الروحاني المطهر", value = corr.incense, icon = Icons.Filled.Info)
                CorrespondenceItem(label = "الاتجاه الروحاني الكوني", value = corr.direction, icon = Icons.Filled.LocationOn)
                CorrespondenceItem(label = "طبع الساعة ومزاجها", value = corr.temperament, icon = Icons.Filled.Info)
            }
        }
    }
}

@Composable
fun CorrespondenceItem(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = Color(0xFF1E293B)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAF8F0), RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF8C7A61), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 11.sp, color = Color(0xFF64748B))
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
fun SufiAIProseCard(
    prose: String,
    isLoading: Boolean,
    accentColor: Color,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
        border = BorderStroke(1.dp, Color(0xFFD6CDBA))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الفيوضات والأبواب الروحية والنبأ",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )

                IconButton(
                    onClick = onRefresh,
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFFB45309), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh Advice", tint = Color(0xFFB45309))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = prose,
                fontSize = 13.sp,
                lineHeight = 22.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.Justify,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Brand Stamp
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "منقول عن الكشف الأكبر للبوني وابن عربي",
                    fontSize = 10.sp,
                    color = Color(0xFF8C7A61)
                )
            }
        }
    }
}

@Composable
fun LocationSettingsSheetContent(
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var latText by remember { mutableStateOf("") }
    var lngText by remember { mutableStateOf("") }
    val currentCoords by viewModel.currentCoordinates.collectAsState()
    val currentLocationStr by viewModel.currentLocationName.collectAsState()

    LaunchedEffect(currentCoords) {
        currentCoords?.let {
            latText = it.first.toString()
            lngText = it.second.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Text(
            text = "ميزان الموقع الجغرافي وقاعدة البيانات",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5C4A31),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "تعديل إحداثيات طالعك الحالي لتحديث تواقيت الشروق والغروب الكوكبية.",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Text(
            text = "موقعك الحالي: $currentLocationStr",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB45309),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = latText,
            onValueChange = { latText = it },
            label = { Text("خط العرض (Latitude)", color = Color(0xFF8C7A61)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1E293B),
                unfocusedTextColor = Color(0xFF475569),
                focusedBorderColor = Color(0xFFB45309),
                unfocusedBorderColor = Color(0xFFD6CDBA),
                focusedLabelColor = Color(0xFFB45309),
                unfocusedLabelColor = Color(0xFF8C7A61)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = lngText,
            onValueChange = { lngText = it },
            label = { Text("خط الطول (Longitude)", color = Color(0xFF8C7A61)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFF1E293B),
                unfocusedTextColor = Color(0xFF475569),
                focusedBorderColor = Color(0xFFB45309),
                unfocusedBorderColor = Color(0xFFD6CDBA),
                focusedLabelColor = Color(0xFFB45309),
                unfocusedLabelColor = Color(0xFF8C7A61)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    viewModel.requestGpsLocation()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C7A61)),
                modifier = Modifier.weight(1.0f)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تحديد تلقائي بـ GPS", color = Color.White, fontSize = 12.sp)
            }

            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lng = lngText.toDoubleOrNull()
                    if (lat != null && lng != null) {
                        viewModel.updateManualCoordinates(lat, lng)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C4A31)),
                modifier = Modifier.weight(1.0f)
            ) {
                Text("حفظ وتعديل الطالع", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFE5DECF), thickness = 1.dp)
        Spacer(modifier = Modifier.height(16.dp))

        // Profile Reset Option
        Button(
            onClick = {
                viewModel.resetProfile()
                onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("إعادة ضبط ميثاق الملف الشخصي والأوراد والخطوات", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Share helper utilizing Android Intent
private fun shareCurrentAstroMoment(
    context: Context,
    hour: CurrentHourData?,
    mansion: CurrentMansionData?,
    timeStr: String,
    loc: String
) {
    if (hour == null || mansion == null) return

    val text = """
        ✨ الساعة البونية - كشف اللحظة الروحانية الفلكية ✨
        
        📅 التوقيت الحالي الشريف: $timeStr
        📍 مرصد الموقع: $loc
        
        🪐 كوكب الساعة الكوكبية الحالية: ${hour.planetArabic}
        📖 الوصف الروحاني: ${hour.planetSpiritual}
        🔢 الساعة رقم: ${hour.hourNumber} (${if (hour.isDay) "ساعة نهارية" else "ساعة ليلية"})
        🕒 الميقات: من ${hour.formattedStart} إلى ${hour.formattedEnd}
        
        🌙 منزلة القمر الحالية: منزلة ${mansion.name} (${mansion.index}/28)
        🌟 البرج والطبيعة: برج ${mansion.sign} (${mansion.element})
        ✨ طبع الساعة الكوني: ${mansion.nature}
        
        👼 السيد الملك الموكل بالتصريف: ${hour.correspondence?.angelName}
        🕌 اسم الله الأعظم للرياضة: ${hour.correspondence?.divineName}
        ✏️ حروف المدد النورانية: ${hour.correspondence?.letters}
        
        📚 الفيوضات والأبواب الروحية المفتوحة:
        "${hour.correspondence?.positiveWorks}"
        
        مدد من شيوخ الطريقة صوفي محتسب!
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة طالع اللحظة"))
}

fun getPlanetGradient(planet: String): List<Color> {
    return when (planet) {
        "زحل" -> listOf(ColorSaturnStart, ColorSaturnEnd)
        "المشتري" -> listOf(ColorJupiterStart, ColorJupiterEnd)
        "المريخ" -> listOf(ColorMarsStart, ColorMarsEnd)
        "الشمس" -> listOf(ColorSunStart, ColorSunEnd)
        "الزهرة" -> listOf(ColorVenusStart, ColorVenusEnd)
        "عطارد" -> listOf(ColorMercuryStart, ColorMercuryEnd)
        "القمر" -> listOf(ColorMoonStart, ColorMoonEnd)
        else -> listOf(ColorSaturnStart, ColorSaturnEnd)
    }
}

fun getPlanetAccent(planet: String): Color {
    return when (planet) {
        "زحل" -> ColorSaturnAccent
        "المشتري" -> ColorJupiterAccent
        "المريخ" -> ColorMarsAccent
        "الشمس" -> ColorSunAccent
        "الزهرة" -> ColorVenusAccent
        "عطارد" -> ColorMercuryAccent
        "القمر" -> ColorMoonAccent
        else -> ColorSaturnAccent
    }
}
