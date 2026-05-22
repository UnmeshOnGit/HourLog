package com.example.ui.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DateUtils
import com.example.data.HourLogEntry
import com.example.ui.theme.*
import com.example.ui.viewmodel.HourLogViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Helper color and text resolvers for Categories
fun getCategoryColor(category: String): Color {
    return when (category) {
        HourLogEntry.CATEGORY_PRODUCTIVE -> ColorProductive
        HourLogEntry.CATEGORY_SLEEP -> ColorSleep
        HourLogEntry.CATEGORY_WASTING -> ColorWasting
        HourLogEntry.CATEGORY_ESSENTIAL -> ColorEssential
        HourLogEntry.CATEGORY_COLLEGE -> ColorCollege
        else -> ColorNone
    }
}

fun getCategoryDisplayName(category: String): String {
    return when (category) {
        HourLogEntry.CATEGORY_PRODUCTIVE -> "Productive"
        HourLogEntry.CATEGORY_SLEEP -> "Sleep"
        HourLogEntry.CATEGORY_WASTING -> "Wasting"
        HourLogEntry.CATEGORY_ESSENTIAL -> "Essential"
        HourLogEntry.CATEGORY_COLLEGE -> "College"
        else -> "None"
    }
}

fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        HourLogEntry.CATEGORY_PRODUCTIVE -> Icons.Default.CheckCircle
        HourLogEntry.CATEGORY_SLEEP -> Icons.Default.Bedtime
        HourLogEntry.CATEGORY_WASTING -> Icons.Default.QueryBuilder
        HourLogEntry.CATEGORY_ESSENTIAL -> Icons.Default.Star
        HourLogEntry.CATEGORY_COLLEGE -> Icons.Default.School
        else -> Icons.Default.Circle
    }
}

@Composable
fun HourLogAppContent(viewModel: HourLogViewModel) {
    val currentScreenState by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDarkState by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val fontScaleState by viewModel.fontSizeScale.collectAsStateWithLifecycle()

    MyApplicationTheme(darkTheme = isDarkState) {
        val currentDensity = androidx.compose.ui.platform.LocalDensity.current
        val customDensity = remember(currentDensity, fontScaleState) {
            object : androidx.compose.ui.unit.Density by currentDensity {
                override val fontScale: Float
                    get() = currentDensity.fontScale * fontScaleState
            }
        }
        CompositionLocalProvider(
            androidx.compose.ui.platform.LocalDensity provides customDensity
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AnimatedContent(
                    targetState = currentScreenState,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        "splash" -> SplashScreen(viewModel)
                        "dashboard" -> DashboardScreen(viewModel)
                        "log" -> DailyLogScreen(viewModel)
                        "calendar" -> CalendarScreen(viewModel)
                        "search" -> SearchScreen(viewModel)
                        "statistics" -> StatisticsScreen(viewModel)
                        "settings" -> SettingsScreen(viewModel)
                        "backup" -> BackupScreen(viewModel)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 1: SPLASH SCREEN
// ----------------------------------------------------
@Composable
fun SplashScreen(viewModel: HourLogViewModel) {
    LaunchedEffect(Unit) {
        delay(1200) // Brief aesthetic splash delay
        viewModel.currentScreen.value = "dashboard"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "HourLog",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Track your time, master your day",
                fontSize = 14.sp,
                color = TextSecondaryMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ----------------------------------------------------
// SCREEN 2: DASHBOARD
// ----------------------------------------------------
@Composable
fun DashboardScreen(viewModel: HourLogViewModel) {
    val todayStr = remember { DateUtils.getTodayDateString() }
    val selectedMonthDaysWithLogs by viewModel.daysWithLogs.collectAsStateWithLifecycle()
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()

    val todayLogs = remember(allLogs) {
        allLogs.filter { it.date == todayStr }
    }

    // Stats calculations
    val loggedHoursCount = todayLogs.filter { it.activity.trim().isNotEmpty() }.size
    val productiveHoursCount = todayLogs.filter { it.category == HourLogEntry.CATEGORY_PRODUCTIVE }.size
    val wastingHoursCount = todayLogs.filter { it.category == HourLogEntry.CATEGORY_WASTING }.size
    val sleepHoursCount = todayLogs.filter { it.category == HourLogEntry.CATEGORY_SLEEP }.size

    val productivityRatio = if (loggedHoursCount > 0) {
        productiveHoursCount.toFloat() / loggedHoursCount.toFloat()
    } else 0f

    Scaffold(
        bottomBar = { DashboardBottomNavigation("dashboard", viewModel) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                // Welcome Text Row
                Column {
                    Text(
                        text = DateUtils.formatToShortDisplay(todayStr).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Today",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(SpecialTagBg)
                                .border(BorderStroke(1.dp, SpecialTagBorder), RoundedCornerShape(100.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = DateUtils.getDayName(todayStr),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = SpecialTagText
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Stat Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ActiveBorderGray)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Today's Summary",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Circular Indicator
                            Box(
                                modifier = Modifier.size(80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Background Circle
                                    drawCircle(
                                        color = ActiveBorderGray,
                                        style = Stroke(width = 8.dp.toPx())
                                    )
                                    // Progress Ring
                                    drawArc(
                                        color = AccentNeonMint,
                                        startAngle = -90f,
                                        sweepAngle = productivityRatio * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(productivityRatio * 100).toInt()}%",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Productive",
                                        fontSize = 8.sp,
                                        color = TextSecondaryMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(20.dp))

                            // Numerical Stats
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DashboardMiniStatRaw("Total Logged", "$loggedHoursCount / 24 hrs", Icons.Default.AccessTime, ColorSleep)
                                DashboardMiniStatRaw("Productive", "$productiveHoursCount hrs", Icons.Default.CheckCircle, ColorProductive)
                                DashboardMiniStatRaw("Wasting time", "$wastingHoursCount hrs", Icons.Default.Cancel, ColorWasting)
                            }
                        }
                    }
                }
            }

            // Quick Open Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.selectedDate.value = DateUtils.getTodayDateString()
                            viewModel.currentScreen.value = "log"
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, ActiveBorderGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AmbientGlowColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    tint = AccentNeonMint
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Quick Log Today",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Tap to update hour logs",
                                    fontSize = 12.sp,
                                    color = TextSecondaryMuted
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = TextSecondaryMuted
                        )
                    }
                }
            }

            // Quick Grid Shortcuts
            item {
                Text(
                    text = "Discover",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardShortcutCard(
                        title = "Calendar",
                        subtitle = "View History",
                        icon = Icons.Default.CalendarMonth,
                        color = ColorSleep,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.currentScreen.value = "calendar"
                    }

                    DashboardShortcutCard(
                        title = "Insights",
                        subtitle = "Productive Hours",
                        icon = Icons.Outlined.Analytics,
                        color = ColorProductive,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.currentScreen.value = "statistics"
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardShortcutCard(
                        title = "Search History",
                        subtitle = "Lookup Logs",
                        icon = Icons.Default.Search,
                        color = ColorEssential,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.currentScreen.value = "search"
                    }

                    DashboardShortcutCard(
                        title = "Backup/Restore",
                        subtitle = "Safeguard",
                        icon = Icons.Default.CloudSync,
                        color = ColorCollege,
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.currentScreen.value = "backup"
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun DashboardMiniStatRaw(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ",
            fontSize = 13.sp,
            color = TextSecondaryMuted,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DashboardShortcutCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, ActiveBorderGray)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondaryMuted
            )
        }
    }
}

// ----------------------------------------------------
// SCREEN 3: DAILY TIMELINE LOG
// ----------------------------------------------------
@Composable
fun DailyLogScreen(viewModel: HourLogViewModel) {
    val activeDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val fullDayLogs by viewModel.logsForSelectedDate.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 28.dp, bottom = 12.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.currentScreen.value = "dashboard" }) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Return Home",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Timeline Journal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = DateUtils.getDayName(activeDate),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.navigatePreviousDay() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Day"
                            )
                        }
                        Text(
                            text = DateUtils.formatToShortDisplay(activeDate),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(onClick = { viewModel.navigateNextDay() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Day"
                            )
                        }
                    }
                }
            }
        },
        bottomBar = { DashboardBottomNavigation("log", viewModel) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(fullDayLogs, key = { it.hourIndex }) { entry ->
                    HourLogCard(
                        entry = entry,
                        onSave = { updatedText, updatedCategory, updatedMood ->
                            viewModel.saveHourLog(
                                hourIndex = entry.hourIndex,
                                activity = updatedText,
                                category = updatedCategory,
                                mood = updatedMood
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HourLogCard(
    entry: HourLogEntry,
    onSave: (String, String, String) -> Unit
) {
    var textValue by remember(entry.activity) { mutableStateOf(entry.activity) }
    var selectedCategory by remember(entry.category) { mutableStateOf(entry.category) }
    var selectedMood by remember(entry.mood) { mutableStateOf(entry.mood) }
    
    val scope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    val categoryColor = getCategoryColor(selectedCategory)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (selectedCategory != HourLogEntry.CATEGORY_NONE) 1.5.dp else 1.dp,
            color = if (selectedCategory != HourLogEntry.CATEGORY_NONE) categoryColor.copy(alpha = 0.5f) else ActiveBorderGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Title Hour bar & Mood Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DateUtils.getHourLabel(entry.hourIndex),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Mood Buttons in single row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("😊", "😐", "😔", "🥱", "🌟").forEach { mood ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (selectedMood == mood) ActiveBorderGray else Color.Transparent)
                                .clickable {
                                    selectedMood = mood
                                    onSave(textValue, selectedCategory, mood)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = mood, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Editable description field
            OutlinedTextField(
                value = textValue,
                onValueChange = { newStr ->
                    textValue = newStr
                    debounceJob?.cancel()
                    debounceJob = scope.launch {
                        delay(600) // Debounce autosaver perfectly
                        onSave(newStr, selectedCategory, selectedMood)
                    }
                },
                placeholder = {
                    Text(
                        text = "What did you do during this hour?",
                        fontSize = 13.sp,
                        color = TextSecondaryMuted
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ActiveBorderGray,
                    focusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Categories horizontal selection list
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HourLogEntry.ALL_CATEGORIES.filter { it != HourLogEntry.CATEGORY_NONE }.forEach { cat ->
                    val color = getCategoryColor(cat)
                    val isSelected = selectedCategory == cat
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.background)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) color else ActiveBorderGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                val targetCat = if (isSelected) HourLogEntry.CATEGORY_NONE else cat
                                selectedCategory = targetCat
                                onSave(textValue, targetCat, selectedMood)
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = null,
                                tint = if (isSelected) color else TextTertiaryDark,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = getCategoryDisplayName(cat),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onBackground else TextSecondaryMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 4: CALENDAR HISTORY VIEW
// ----------------------------------------------------
@Composable
fun CalendarScreen(viewModel: HourLogViewModel) {
    val loggedDaysList by viewModel.daysWithLogs.collectAsStateWithLifecycle()
    val calendarView = remember { mutableStateOf(Calendar.getInstance()) }
    val currentMonthLabel = remember(calendarView.value) {
        DateUtils.formatMonthYear(calendarView.value)
    }

    // Days Grid computation
    val cells = remember(calendarView.value) {
        val firstDayCal = calendarView.value.clone() as Calendar
        firstDayCal.set(Calendar.DAY_OF_MONTH, 1)
        val dayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 2 = Mon ...
        val blanks = dayOfWeek - 1
        val totalDays = calendarView.value.getActualMaximum(Calendar.DAY_OF_MONTH)
        (1..blanks).map { null } + (1..totalDays).map { it }
    }

    Scaffold(
        bottomBar = { DashboardBottomNavigation("calendar", viewModel) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "History Navigator",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Select Past Days",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Month Selector Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    val cloned = calendarView.value.clone() as Calendar
                    cloned.add(Calendar.MONTH, -1)
                    calendarView.value = cloned
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Month")
                }

                Text(
                    text = currentMonthLabel,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = {
                    val cloned = calendarView.value.clone() as Calendar
                    cloned.add(Calendar.MONTH, 1)
                    calendarView.value = cloned
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Week List Headers
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiaryDark,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Calendar Day Grid
            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(cells.size) { i ->
                        val dayNum = cells[i]
                        if (dayNum == null) {
                            Box(modifier = Modifier.aspectRatio(1f))
                        } else {
                            val cellCal = calendarView.value.clone() as Calendar
                            cellCal.set(Calendar.DAY_OF_MONTH, dayNum)
                            val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val cellDateStr = dbDateFormat.format(cellCal.time)
                            val hasLogs = loggedDaysList.contains(cellDateStr)
                            
                            val isToday = cellDateStr == DateUtils.getTodayDateString()

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isToday) MaterialTheme.colorScheme.primary else ActiveBorderGray,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        viewModel.selectDate(cellDateStr)
                                        viewModel.currentScreen.value = "log"
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                        fontSize = 15.sp
                                    )
                                    if (hasLogs) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(AccentNeonMint)
                                        )
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

// ----------------------------------------------------
// SCREEN 5: SEARCH WORK Timeline LOGS
// ----------------------------------------------------
@Composable
fun SearchScreen(viewModel: HourLogViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        bottomBar = { DashboardBottomNavigation("search", viewModel) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Search Database",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Track Keywords",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search textfield
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search activity text (e.g. college, gym)", color = TextSecondaryMuted) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = ActiveBorderGray
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (searchQuery.trim().isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ContentPasteSearch,
                            contentDescription = null,
                            tint = TextTertiaryDark,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Type keywords to search activities",
                            fontSize = 14.sp,
                            color = TextSecondaryMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No log results match this keyword.",
                        fontSize = 14.sp,
                        color = TextSecondaryMuted,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(searchResults) { entry ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    keyboardController?.hide()
                                    viewModel.selectedDate.value = entry.date
                                    viewModel.currentScreen.value = "log"
                                },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, ActiveBorderGray),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = getCategoryIcon(entry.category),
                                            contentDescription = null,
                                            tint = getCategoryColor(entry.category),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = DateUtils.formatToShortDisplay(entry.date),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(getCategoryColor(entry.category).copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = getCategoryDisplayName(entry.category),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = getCategoryColor(entry.category)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = DateUtils.getHourLabel(entry.hourIndex),
                                    fontSize = 11.sp,
                                    color = TextSecondaryMuted,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = entry.activity,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// SCREEN 6: STATISTICS & PIE INSIGHTS
// ----------------------------------------------------
@Composable
fun StatisticsScreen(viewModel: HourLogViewModel) {
    val allLogs by viewModel.allLogs.collectAsStateWithLifecycle()

    // Aggregate statistics
    val nonNoneLogs = remember(allLogs) {
        allLogs.filter { it.activity.trim().isNotEmpty() }
    }

    val totalHours = nonNoneLogs.size

    val categoryCounts = remember(nonNoneLogs) {
        val countMap = mutableMapOf<String, Int>()
        HourLogEntry.ALL_CATEGORIES.forEach { countMap[it] = 0 }
        nonNoneLogs.forEach { log ->
            countMap[log.category] = (countMap[log.category] ?: 0) + 1
        }
        countMap
    }

    val productiveCount = categoryCounts[HourLogEntry.CATEGORY_PRODUCTIVE] ?: 0
    val sleepCount = categoryCounts[HourLogEntry.CATEGORY_SLEEP] ?: 0
    val wastingCount = categoryCounts[HourLogEntry.CATEGORY_WASTING] ?: 0
    val essentialCount = categoryCounts[HourLogEntry.CATEGORY_ESSENTIAL] ?: 0
    val collegeCount = categoryCounts[HourLogEntry.CATEGORY_COLLEGE] ?: 0

    // Hourly density distribution (which hours of day are logged most)
    val hourCounts = remember(nonNoneLogs) {
        val distribution = IntArray(24)
        nonNoneLogs.forEach { log ->
            if (log.hourIndex in 0..23) {
                distribution[log.hourIndex]++
            }
        }
        distribution
    }

    val peakHourIndex = remember(hourCounts) {
        var maxIdx = -1
        var maxVal = 0
        for (i in 0..23) {
            if (hourCounts[i] > maxVal) {
                maxVal = hourCounts[i]
                maxIdx = i
            }
        }
        maxIdx
    }

    Scaffold(
        bottomBar = { DashboardBottomNavigation("statistics", viewModel) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Analytics Panel",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Insights & Distribution",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Summary Info cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ActiveBorderGray),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Total Logged", fontSize = 11.sp, color = TextSecondaryMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "$totalHours hrs", fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, ActiveBorderGray),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Peak Hour", fontSize = 11.sp, color = TextSecondaryMuted)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (peakHourIndex != -1) "${if (peakHourIndex > 12) peakHourIndex - 12 else peakHourIndex} ${if (peakHourIndex >= 12) "PM" else "AM"}" else "--",
                                fontSize = 20.sp, 
                                fontWeight = FontWeight.Black, 
                                color = ColorSleep
                            )
                        }
                    }
                }
            }

            // Custom Painted Category Distribution Chart
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ActiveBorderGray),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Category Breakdown",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        if (totalHours == 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No data logged yet", color = TextSecondaryMuted, fontSize = 13.sp)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Doughnut Ring
                                Box(
                                    modifier = Modifier.size(110.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        var currentAngle = -90f
                                        val categories = listOf(
                                            HourLogEntry.CATEGORY_PRODUCTIVE,
                                            HourLogEntry.CATEGORY_SLEEP,
                                            HourLogEntry.CATEGORY_WASTING,
                                            HourLogEntry.CATEGORY_ESSENTIAL,
                                            HourLogEntry.CATEGORY_COLLEGE,
                                            HourLogEntry.CATEGORY_NONE
                                        )

                                        categories.forEach { cat ->
                                            val count = categoryCounts[cat] ?: 0
                                            if (count > 0) {
                                                val sweep = (count.toFloat() / totalHours.toFloat()) * 360f
                                                drawArc(
                                                    color = getCategoryColor(cat),
                                                    startAngle = currentAngle,
                                                    sweepAngle = sweep,
                                                    useCenter = false,
                                                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                                                )
                                                currentAngle += sweep
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(18.dp))

                                // Legend labels
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    HourLogEntry.ALL_CATEGORIES.forEach { cat ->
                                        val count = categoryCounts[cat] ?: 0
                                        val pct = if (totalHours > 0) (count * 100) / totalHours else 0
                                        
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(getCategoryColor(cat))
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = getCategoryDisplayName(cat),
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                            Text(
                                                text = "$pct%",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextSecondaryMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Timeline Heatmap Block Diagram
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ActiveBorderGray),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Daily Hourly Activity Density",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            for (i in 0..23) {
                                val logsAtThisHour = hourCounts[i]
                                val saturation = when {
                                    logsAtThisHour > 5 -> 1f
                                    logsAtThisHour > 3 -> 0.7f
                                    logsAtThisHour > 1 -> 0.4f
                                    logsAtThisHour > 0 -> 0.2f
                                    else -> 0f
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (saturation > 0f) MaterialTheme.colorScheme.primary.copy(alpha = saturation)
                                            else ActiveBorderGray
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("12 AM", fontSize = 10.sp, color = TextSecondaryMuted, fontWeight = FontWeight.Bold)
                            Text("12 PM", fontSize = 10.sp, color = TextSecondaryMuted, fontWeight = FontWeight.Bold)
                            Text("11 PM", fontSize = 10.sp, color = TextSecondaryMuted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

// ----------------------------------------------------
// SCREEN 7: SETTINGS & PREFERENCES
// ----------------------------------------------------
@Composable
fun SettingsScreen(viewModel: HourLogViewModel) {
    val context = LocalContext.current
    val isDarkState by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val remindersState by viewModel.remindersEnabled.collectAsStateWithLifecycle()
    val reminderHourState by viewModel.reminderHour.collectAsStateWithLifecycle()
    val reminderMinuteState by viewModel.reminderMinute.collectAsStateWithLifecycle()
    val fontSizeState by viewModel.fontSizeScale.collectAsStateWithLifecycle()

    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = { DashboardBottomNavigation("settings", viewModel) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Preferences",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Settings Panel",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Dark Mode Preferences Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ActiveBorderGray),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = AccentNeonMint)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "Dark Mode Canvas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Switch(
                        checked = isDarkState,
                        onCheckedChange = { viewModel.setThemeMode(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reminders Preferences Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ActiveBorderGray),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = ColorSleep)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(text = "Evening Reminder Bell", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Switch(
                            checked = remindersState,
                            onCheckedChange = { viewModel.setRemindersEnabled(it) }
                        )
                    }

                    if (remindersState) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .clickable {
                                    val timePicker = TimePickerDialog(
                                        context,
                                        { _, hour, minute ->
                                            viewModel.setReminderTime(hour, minute)
                                        },
                                        reminderHourState,
                                        reminderMinuteState,
                                        false
                                    )
                                    timePicker.show()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Trigger Time", fontSize = 13.sp, color = TextSecondaryMuted)
                            Text(
                                text = String.format("%02d:%02d %s", 
                                    if (reminderHourState > 12) reminderHourState - 12 else if (reminderHourState == 0) 12 else reminderHourState,
                                    reminderMinuteState, 
                                    if (reminderHourState >= 12) "PM" else "AM"
                                ),
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Font Scaling adjustment slider
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ActiveBorderGray),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FormatSize, contentDescription = null, tint = ColorEssential)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(text = "UI Text Scaling multiplier", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("0.8x", fontSize = 11.sp, color = TextSecondaryMuted)
                        Slider(
                            value = fontSizeState,
                            onValueChange = { viewModel.setFontSizeScale(it) },
                            valueRange = 0.8f..1.4f,
                            steps = 3,
                            modifier = Modifier.weight(1f)
                        )
                        Text("1.4x", fontSize = 11.sp, color = TextSecondaryMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Danger Factory reset
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showResetDialog = true },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, ColorWasting.copy(alpha = 0.4f)),
                colors = CardDefaults.cardColors(containerColor = ColorWasting.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = ColorWasting)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Clear All App Data", color = ColorWasting, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Erase all logged hours and configurations.", color = TextSecondaryMuted, fontSize = 11.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Erase All Database Record?") },
            text = { Text("This will permanently clear every hourly productivity logging entries you created. This operation is irreversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetAllData {
                            Toast.makeText(context, "All logs cleared successfully", Toast.LENGTH_SHORT).show()
                            showResetDialog = false
                            viewModel.currentScreen.value = "dashboard"
                        }
                    }
                ) {
                    Text("Clear Everything", color = ColorWasting, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ----------------------------------------------------
// SCREEN 8: BACKUP & PORTABLE SYNC
// ----------------------------------------------------
@Composable
fun BackupScreen(viewModel: HourLogViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var manualBackupCode by remember { mutableStateOf("") }

    // File System Exports using standard platform Storage Access Framework
    val exportFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                val os = context.contentResolver.openOutputStream(it)
                if (os != null) {
                    val ok = viewModel.exportToFile(os)
                    if (ok) {
                        Toast.makeText(context, "Logs backed up to file!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Exporting file failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    val importFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                val inputStream = context.contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val ok = viewModel.importFromFile(inputStream)
                    if (ok) {
                        Toast.makeText(context, "Full history restored from backup file!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to parse file template", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = { DashboardBottomNavigation("backup", viewModel) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Sync Hub",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Backup & Restore",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // High priority Local files card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ActiveBorderGray),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "A. Backup Local Files",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Save database records directly into any destination folder or Google Drive securely.",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { exportFileLauncher.launch("HourLog_Backup_${System.currentTimeMillis()}.json") },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorSleep),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export JSON", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { importFileLauncher.launch(arrayOf("application/json")) },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorProductive),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Import File", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Copy-paste text system
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, ActiveBorderGray),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "B. Text Code Backup Portal",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Copy/paste raw text logs directly to transition easily between phones without file limits.",
                            fontSize = 11.sp,
                            color = TextSecondaryMuted,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val code = viewModel.getBackupKeyString()
                                if (code.isNotEmpty()) {
                                    clipboardManager.setText(AnnotatedString(code))
                                    Toast.makeText(context, "Text code backed up! Copied to clipboard.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No backup points exist yet", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CopyAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate & Copy raw code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = manualBackupCode,
                            onValueChange = { manualBackupCode = it },
                            placeholder = { Text("Paste clipboard backup code here...", color = TextSecondaryMuted, fontSize = 12.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = ActiveBorderGray
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (manualBackupCode.trim().isNotEmpty()) {
                                    val ok = viewModel.restoreFromBackupString(manualBackupCode.trim())
                                    if (ok) {
                                        Toast.makeText(context, "Restore complete!", Toast.LENGTH_SHORT).show()
                                        manualBackupCode = ""
                                    } else {
                                        Toast.makeText(context, "Invalid key formatting error", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Paste non-empty data first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorEssential),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = manualBackupCode.isNotEmpty()
                        ) {
                            Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Restore from Pasted Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(30.dp)) }
        }
    }
}

// ----------------------------------------------------
// UTILS: DOCK NAVIGATION NAVIGATION BAR
// ----------------------------------------------------
@Composable
fun DashboardBottomNavigation(currentSelected: String, viewModel: HourLogViewModel) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.drawBehind {
            // Draw a neat top border line
            drawLine(
                color = ActiveBorderGray,
                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        }
    ) {
        val navItemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = ActiveCapsuleText,
            selectedTextColor = AccentNeonMint,
            unselectedIconColor = TextPrimaryLight.copy(alpha = 0.6f),
            unselectedTextColor = TextPrimaryLight.copy(alpha = 0.6f),
            indicatorColor = ActiveCapsuleBg
        )

        NavigationBarItem(
            selected = currentSelected == "dashboard",
            onClick = { viewModel.currentScreen.value = "dashboard" },
            icon = {
                Icon(
                    imageVector = if (currentSelected == "dashboard") Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                    contentDescription = "Console"
                )
            },
            label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = navItemColors
        )

        NavigationBarItem(
            selected = currentSelected == "log",
            onClick = { viewModel.currentScreen.value = "log" },
            icon = {
                Icon(
                    imageVector = if (currentSelected == "log") Icons.Default.EditCalendar else Icons.Outlined.EditCalendar,
                    contentDescription = "Hourly logs"
                )
            },
            label = { Text("Log", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = navItemColors
        )

        NavigationBarItem(
            selected = currentSelected == "calendar",
            onClick = { viewModel.currentScreen.value = "calendar" },
            icon = {
                Icon(
                    imageVector = if (currentSelected == "calendar") Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "History navigator"
                )
            },
            label = { Text("History", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = navItemColors
        )

        NavigationBarItem(
            selected = currentSelected == "statistics",
            onClick = { viewModel.currentScreen.value = "statistics" },
            icon = {
                Icon(
                    imageVector = if (currentSelected == "statistics") Icons.Default.Analytics else Icons.Outlined.Analytics,
                    contentDescription = "Insights"
                )
            },
            label = { Text("Insights", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = navItemColors
        )

        NavigationBarItem(
            selected = currentSelected == "settings",
            onClick = { viewModel.currentScreen.value = "settings" },
            icon = {
                Icon(
                    imageVector = if (currentSelected == "settings") Icons.Default.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            colors = navItemColors
        )
    }
}
