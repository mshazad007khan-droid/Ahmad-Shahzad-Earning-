package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.EarningPlan
import com.example.data.TransactionRecord
import com.example.data.UserPlan
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppUi(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val currentScreen = viewModel.currentScreen
    val userBalance by viewModel.userBalance.collectAsStateWithLifecycle()
    val plans by viewModel.earningPlans.collectAsStateWithLifecycle()
    val userPlans by viewModel.userPlans.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    // Dialog state management
    var showAddPlanDialog by remember { mutableStateOf(false) }
    var showEditPlanDialog by remember { mutableStateOf<EarningPlan?>(null) }
    var showEasypaisaConfigDialog by remember { mutableStateOf(false) }

    // Admin Toggle State
    var isAdminMode by remember { mutableStateOf(false) }

    // Handle incoming Toast messages from flow
    LaunchedEffect(key1 = true) {
        viewModel.snackbarMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp)
                    ) {
                        Column {
                            Text(
                                "Ahmad Shahzad",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAdminMode) GoldMetallic else Color.White
                            )
                            Text(
                                if (isAdminMode) "⚡ ADMIN PANEL" else "💸 Earning App",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAdminMode) MintGreenAccent else GoldMetallic
                            )
                        }

                        // Compact Admin Mode Power Toggle
                        FilterChip(
                            selected = isAdminMode,
                            onClick = {
                                isAdminMode = !isAdminMode
                                if (isAdminMode) {
                                    viewModel.navigateTo(AppScreen.AdminDashboard)
                                } else {
                                    viewModel.navigateTo(AppScreen.Home)
                                }
                                Toast.makeText(
                                    context,
                                    if (isAdminMode) "Switched to Admin Mode" else "Switched to User Dashboard",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            label = {
                                Text(
                                    text = if (isAdminMode) "User Mode" else "Admin Panel",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isAdminMode) Icons.Default.Person else Icons.Default.Settings,
                                    contentDescription = "Admin toggle",
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFF9F1C),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E2822),
                                labelColor = GoldMetallic
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selected = isAdminMode,
                                enabled = true,
                                borderColor = PakistanGreenMedi,
                                selectedBorderColor = Color(0xFFFF9F1C)
                            ),
                            modifier = Modifier.testTag("admin_toggle_chip")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PakistanGreenDark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (!isAdminMode) {
                NavigationBar(
                    containerColor = PakistanGreenDark,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Home,
                        onClick = { viewModel.navigateTo(AppScreen.Home) },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                        label = { Text("Home", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PakistanGreenDark,
                            selectedTextColor = GoldMetallic,
                            indicatorColor = GoldMetallic,
                            unselectedIconColor = SoftGrey,
                            unselectedTextColor = SoftGrey
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.EarnPlans,
                        onClick = { viewModel.navigateTo(AppScreen.EarnPlans) },
                        icon = { Icon(Icons.Default.Star, contentDescription = "Plans") },
                        label = { Text("Plans", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PakistanGreenDark,
                            selectedTextColor = GoldMetallic,
                            indicatorColor = GoldMetallic,
                            unselectedIconColor = SoftGrey,
                            unselectedTextColor = SoftGrey
                        ),
                        modifier = Modifier.testTag("nav_plans")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Deposit,
                        onClick = { viewModel.navigateTo(AppScreen.Deposit) },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Deposit") },
                        label = { Text("Deposit", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PakistanGreenDark,
                            selectedTextColor = GoldMetallic,
                            indicatorColor = GoldMetallic,
                            unselectedIconColor = SoftGrey,
                            unselectedTextColor = SoftGrey
                        ),
                        modifier = Modifier.testTag("nav_deposit")
                    )
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.Withdraw,
                        onClick = { viewModel.navigateTo(AppScreen.Withdraw) },
                        icon = { Icon(Icons.Default.Send, contentDescription = "Withdraw") },
                        label = { Text("Withdraw", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PakistanGreenDark,
                            selectedTextColor = GoldMetallic,
                            indicatorColor = GoldMetallic,
                            unselectedIconColor = SoftGrey,
                            unselectedTextColor = SoftGrey
                        ),
                        modifier = Modifier.testTag("nav_withdraw")
                    )
                }
            } else {
                // Admin tab selector inside Admin scope
                NavigationBar(
                    containerColor = Color(0xFF161E1A),
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.AdminDashboard,
                        onClick = { viewModel.navigateTo(AppScreen.AdminDashboard) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Requests") },
                        label = { Text("Requests", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = GoldenOrange,
                            indicatorColor = GoldenOrange,
                            unselectedIconColor = SoftGrey,
                            unselectedTextColor = SoftGrey
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { showEasypaisaConfigDialog = true },
                        icon = { Icon(Icons.Default.Refresh, contentDescription = "Easypaisa Settings") },
                        label = { Text("Config Deposit", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = GoldenOrange,
                            indicatorColor = GoldenOrange,
                            unselectedIconColor = SoftGrey,
                            unselectedTextColor = SoftGrey
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { showAddPlanDialog = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = "Add Plan") },
                        label = { Text("Add Plan", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = GoldenOrange,
                            indicatorColor = GoldenOrange,
                            unselectedIconColor = SoftGrey,
                            unselectedTextColor = SoftGrey
                        )
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = DarkSurface
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(250)),
                exit = fadeOut(animationSpec = tween(250))
            ) {
                if (isAdminMode) {
                    AdminDashboardView(
                        viewModel = viewModel,
                        transactions = transactions,
                        plans = plans,
                        onEditPlan = { showEditPlanDialog = it }
                    )
                } else {
                    when (currentScreen) {
                        AppScreen.Home -> {
                            DashboardView(
                                viewModel = viewModel,
                                balance = userBalance,
                                activePlans = userPlans,
                                transactions = transactions,
                                availablePlans = plans
                            )
                        }
                        AppScreen.EarnPlans -> PlansMarketplaceView(
                            viewModel = viewModel,
                            plans = plans
                        )
                        AppScreen.Deposit -> DepositView(
                            viewModel = viewModel,
                            balance = userBalance
                        )
                        AppScreen.Withdraw -> WithdrawalView(
                            viewModel = viewModel,
                            balance = userBalance
                        )
                        AppScreen.AdminDashboard -> {
                            // Handled by isAdminMode check, but makes compile complete
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. ADD PLAN DIALOG
    if (showAddPlanDialog) {
        PlanDialogForm(
            titleStr = "Add New Investment Plan",
            onDismiss = { showAddPlanDialog = false },
            onConfirm = { pTitle, pCost, pDaily, pDays ->
                viewModel.addEarningPlan(pTitle, pCost, pDaily, pDays)
                showAddPlanDialog = false
            }
        )
    }

    // 2. EDIT PLAN DIALOG
    showEditPlanDialog?.let { currentPlan ->
        PlanDialogForm(
            titleStr = "Edit Investment Plan",
            initialPlan = currentPlan,
            onDismiss = { showEditPlanDialog = null },
            onConfirm = { pTitle, pCost, pDaily, pDays ->
                viewModel.updateEarningPlan(currentPlan.id, pTitle, pCost, pDaily, pDays)
                showEditPlanDialog = null
            }
        )
    }

    // 3. CONFIGURE EASYPAISA DEPOSIT CREDENTIALS
    if (showEasypaisaConfigDialog) {
        var num by remember { mutableStateOf(userBalance?.easypaisaNumber ?: "03123456789") }
        var name by remember { mutableStateOf(userBalance?.easypaisaName ?: "Ahmad Shahzad") }

        Dialog(onDismissRequest = { showEasypaisaConfigDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "Configure Easypaisa Deposit Info",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldMetallic
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your users will see this mobile number & name when they attempt to deposit PKRs.",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = num,
                        onValueChange = { num = it },
                        label = { Text("Easypaisa Account Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldMetallic,
                            unfocusedBorderColor = SoftGrey,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("config_number")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Account Title Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldMetallic,
                            unfocusedBorderColor = SoftGrey,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("config_name")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = { showEasypaisaConfigDialog = false }) {
                            Text("Cancel", color = SoftGrey)
                        }
                        Button(
                            onClick = {
                                viewModel.updateEasypaisaSettings(num, name)
                                showEasypaisaConfigDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldMetallic, contentColor = PakistanGreenDark)
                        ) {
                            Text("Save Configurations", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ================= USER PAGES =================

@Composable
fun DashboardView(
    viewModel: AppViewModel,
    balance: com.example.data.UserBalance?,
    activePlans: List<UserPlan>,
    transactions: List<TransactionRecord>,
    availablePlans: List<EarningPlan>
) {
    var filterType by remember { mutableStateOf("ALL") }
    var filterStatus by remember { mutableStateOf("ALL") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and balance card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PakistanGreenMedi),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(PakistanGreenMedi, PakistanGreenDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Welcome to Ahmad Shahzad Earning",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MintGreenAccent
                            )
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "VIP Mode",
                                tint = GoldMetallic,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "PKR ${String.format("%,.2f", balance?.balance ?: 0.0)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldMetallic
                        )
                        Text(
                            "Available Cash Margin Balance",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )

                        // Display dynamic active earning rate
                        val currentDailyEarn = activePlans.sumOf { it.dailyEarning }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active Yield",
                                tint = MintGreenAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Earnings Power: PKR ${String.format("%,.2f", currentDailyEarn)} / day",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MintGreenAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dynamic Row statistics
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    "₨ ${String.format("%,.0f", balance?.totalDeposit ?: 0.0)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text("Total Deposits", fontSize = 10.sp, color = SoftGrey)
                            }
                            Column {
                                Text(
                                    "₨ ${String.format("%,.0f", balance?.totalWithdraw ?: 0.0)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text("Withdrawn", fontSize = 10.sp, color = SoftGrey)
                            }
                            Column {
                                Text(
                                    "${activePlans.size} Active",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MintGreenAccent
                                )
                                Text("Active Packages", fontSize = 10.sp, color = SoftGrey)
                            }
                        }
                    }
                }
            }
        }

        // Quick action row
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(AppScreen.Deposit) }
                            .padding(8.dp)
                            .testTag("action_deposit")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(44.dp).background(PakistanGreenMedi, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Deposit Icon", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Add cash", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Spacer mimicking vertical divider
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(PakistanGreenLight))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.claimDailyEarnings() }
                            .padding(8.dp)
                            .testTag("action_claim")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(44.dp).background(Color(0xFFFF9F1C), CircleShape)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Daily Claim Icon", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Claim Daily", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Spacer mimicking vertical divider
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(PakistanGreenLight))

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { viewModel.navigateTo(AppScreen.Withdraw) }
                            .padding(8.dp)
                            .testTag("action_withdraw")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(44.dp).background(PakistanGreenLight, CircleShape)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Withdraw Icon", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Withdraw", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Daily income reminder card
        if (activePlans.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0x3BFF9F1C)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Income Notification",
                            tint = GoldenOrange,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Daily Income Claim is Available!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Press 'Claim Daily' above to retrieve income from your active investment plans.",
                                fontSize = 11.sp,
                                color = SoftGrey
                            )
                        }
                    }
                }
            }
        }

        // Available Earning Plans Section
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(
                    "🔥 High-Yield Earning Plans",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldMetallic
                )
                Text(
                    "All Plans",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintGreenAccent,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.EarnPlans) }
                )
            }
        }

        item {
            if (availablePlans.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "No packages available right now. Ask Admin to add plans!",
                        modifier = Modifier.padding(16.dp),
                        color = SoftGrey,
                        fontSize = 12.sp
                    )
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(availablePlans) { p ->
                        Card(
                            modifier = Modifier
                                .width(240.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                Brush.linearGradient(listOf(PakistanGreenLight, GoldMetallic))
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    p.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Price", fontSize = 9.sp, color = SoftGrey)
                                        Text("₨ ${p.cost}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = GoldMetallic)
                                    }
                                    Column {
                                        Text("Daily Earn", fontSize = 9.sp, color = SoftGrey)
                                        Text("₨ ${p.dailyEarning}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MintGreenAccent)
                                    }
                                    Column {
                                        Text("Validity", fontSize = 9.sp, color = SoftGrey)
                                        Text("${p.validityDays}d", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp), color = PakistanGreenLight.copy(alpha = 0.4f))
                                val totalProfit = p.dailyEarning * p.validityDays
                                val roi = (totalProfit / p.cost) * 100
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("Est. ROI", fontSize = 9.sp, color = SoftGrey)
                                        Text("${String.format("%.0f", roi)}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintGreenAccent)
                                    }
                                    Button(
                                        onClick = { viewModel.buyPlan(p) },
                                        colors = ButtonDefaults.buttonColors(containerColor = PakistanGreenMedi),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp).testTag("dash_buy_${p.id}")
                                    ) {
                                        Text("Purchase", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section active subscriptions
        item {
            Text(
                "My Investment Portfolio",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = GoldMetallic,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (activePlans.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Empty",
                            tint = SoftGrey,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "No active packages found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Browse our high yield plans in the package marketplace and click Buy to start earning daily!",
                            fontSize = 11.sp,
                            color = SoftGrey,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.navigateTo(AppScreen.EarnPlans) },
                            colors = ButtonDefaults.buttonColors(containerColor = PakistanGreenMedi)
                        ) {
                            Text("Explore Plans", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(activePlans) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(40.dp).background(PakistanGreenDark, CircleShape)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MintGreenAccent)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(p.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "Daily Income: PKR ${p.dailyEarning}",
                                fontSize = 11.sp,
                                color = MintGreenAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                            val formattedDate = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(p.purchaseDate))
                            Text("Purchased: $formattedDate", fontSize = 10.sp, color = SoftGrey)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ACTIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MintGreenAccent,
                            modifier = Modifier
                                .background(Color(0x1F2ECC71), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Section: Transaction History Header
        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Divider(color = PakistanGreenLight.copy(alpha = 0.3f), thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Ledger Icon",
                        tint = GoldMetallic,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Transaction History & Ledger",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldMetallic
                    )
                }
                Text(
                    "Monitor all past and pending deposits or withdrawals with your current status verified below.",
                    fontSize = 11.sp,
                    color = SoftGrey,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )
            }
        }

        // Segmented Filter Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PakistanGreenLight.copy(alpha = 0.15f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Filter by Type
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "TRANSACTION TYPE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftGrey.copy(alpha = 0.8f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val types = listOf(
                                "ALL" to "All Types 📋",
                                "DEPOSIT" to "Deposits 📥",
                                "WITHDRAWAL" to "Withdrawals 📤"
                            )
                            types.forEach { (typeVal, label) ->
                                val isSelected = filterType == typeVal
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PakistanGreenMedi else CardBackground)
                                        .border(
                                            1.dp,
                                            if (isSelected) GoldMetallic else PakistanGreenLight.copy(alpha = 0.3f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { filterType = typeVal }
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else SoftGrey
                                    )
                                }
                            }
                        }
                    }

                    // Filter by Status
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "TRANSACTION STATUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftGrey.copy(alpha = 0.8f)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val statuses = listOf(
                                "ALL" to "All Statuses",
                                "PENDING" to "🕒 Pending",
                                "APPROVED" to "✅ Approved",
                                "REJECTED" to "❌ Rejected"
                            )
                            statuses.forEach { (statusVal, label) ->
                                val isSelected = filterStatus == statusVal
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) PakistanGreenLight else CardBackground)
                                        .border(
                                            1.dp,
                                            if (isSelected) GoldMetallic.copy(alpha = 0.8f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { filterStatus = statusVal }
                                        .padding(horizontal = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else SoftGrey,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filtered transactions computed list
        val filteredTransactions = transactions.filter { t ->
            val matchesType = (filterType == "ALL" || t.type.uppercase() == filterType)
            val matchesStatus = (filterStatus == "ALL" || t.status.uppercase() == filterStatus)
            matchesType && matchesStatus
        }

        // Summary Statistics / Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredTransactions.size} items",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MintGreenAccent
                )

                if (filterType != "ALL" || filterStatus != "ALL") {
                    Text(
                        text = "Reset Filters",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldMetallic,
                        modifier = Modifier
                            .clickable {
                                filterType = "ALL"
                                filterStatus = "ALL"
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Empty History",
                            tint = SoftGrey.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (transactions.isEmpty()) "No transaction requests found!" else "No matching transactions found!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (transactions.isEmpty()) "All deposit and withdrawal records appear here once they are initiated." else "Reset filters or choose another selection to view matching orders.",
                            fontSize = 11.sp,
                            color = SoftGrey,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { t ->
                val localContext = LocalContext.current
                val clipboardManager = localContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                
                val isDeposit = t.type.uppercase() == "DEPOSIT"
                val typeColor = if (isDeposit) MintGreenAccent else GoldenOrange
                val statusColor = when (t.status.uppercase()) {
                    "APPROVED" -> MintGreenAccent
                    "REJECTED" -> ErrorRed
                    else -> GoldMetallic
                }

                val statusIcon = when (t.status.uppercase()) {
                    "APPROVED" -> Icons.Default.CheckCircle
                    "REJECTED" -> Icons.Default.Warning
                    else -> Icons.Default.Info
                }

                val signSymbol = if (isDeposit) "+" else "-"

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("transaction_item_${t.id}"),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, PakistanGreenLight.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(typeColor.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isDeposit) Icons.Default.Add else Icons.Default.Send,
                                        contentDescription = t.type,
                                        tint = typeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isDeposit) "Deposit Request" else "Withdrawal Order",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Payment Method: ${t.paymentMethod}",
                                        fontSize = 10.sp,
                                        color = SoftGrey
                                    )
                                }
                            }
                            Text(
                                text = "$signSymbol ₨ ${String.format("%,.2f", t.amount)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isDeposit) MintGreenAccent else GoldMetallic
                             )
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = PakistanGreenLight.copy(alpha = 0.2f))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Account detail row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isDeposit) "Sender Account Details" else "Beneficiary Details",
                                    fontSize = 10.sp,
                                    color = SoftGrey
                                )
                                Text(
                                    text = "${t.accountName} (${t.accountNumber})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    textAlign = TextAlign.End
                                )
                            }

                            // TRX / Reference row (especially for deposits)
                            if (t.referenceId.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TRX Transaction REF",
                                        fontSize = 10.sp,
                                        color = SoftGrey
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PakistanGreenDark)
                                            .clickable {
                                                val clip = ClipData.newPlainText("TRX ID", t.referenceId)
                                                clipboardManager.setPrimaryClip(clip)
                                                Toast.makeText(localContext, "Reference ID Copied", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = t.referenceId,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldMetallic
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Copy",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MintGreenAccent
                                        )
                                    }
                                }
                            }

                            // Date verification row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Submission Date",
                                    fontSize = 10.sp,
                                    color = SoftGrey
                                )
                                val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(t.timestamp))
                                Text(
                                    text = formattedDate,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            // Status badge row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Verification Status",
                                    fontSize = 10.sp,
                                    color = SoftGrey
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = t.status,
                                        tint = statusColor,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = t.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = statusColor
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

@Composable
fun PlansMarketplaceView(viewModel: AppViewModel, plans: List<EarningPlan>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "High-Yield Capital Plans",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldMetallic
                )
                Text(
                    "Invest in our secure plans to immediately activate your high premium yields daily. Managed directly by Ahmad Shahzad.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        if (plans.isEmpty()) {
            item {
                Text(
                    "No plans available at this moment. Ask the Administrator to share plans to begin.",
                    fontSize = 12.sp,
                    color = SoftGrey
                )
            }
        } else {
            items(plans) { p ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = CardDefaults.outlinedCardBorder(enabled = true).copy(
                        brush = Brush.linearGradient(listOf(PakistanGreenLight, GoldMetallic))
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                p.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFF9F1C), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "VIP Tier",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PakistanGreenDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Plan Price", fontSize = 11.sp, color = SoftGrey)
                                Text(
                                    "PKR ${p.cost}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldMetallic
                                )
                            }
                            Column {
                                Text("Daily Earning", fontSize = 11.sp, color = SoftGrey)
                                Text(
                                    "PKR ${p.dailyEarning}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MintGreenAccent
                                )
                            }
                            Column {
                                Text("Validity Info", fontSize = 11.sp, color = SoftGrey)
                                Text(
                                    "${p.validityDays} Days",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = PakistanGreenLight.copy(alpha = 0.5f))

                        val totalProfit = p.dailyEarning * p.validityDays
                        val roi = (totalProfit / p.cost) * 100
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    "Total Profit: PKR ${String.format("%,.0f", totalProfit)}",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "Return on Investment: ${String.format("%.0f", roi)}%",
                                    fontSize = 10.sp,
                                    color = MintGreenAccent
                                )
                            }

                            Button(
                                onClick = { viewModel.buyPlan(p) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PakistanGreenMedi,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("buy_plan_${p.id}")
                            ) {
                                Text("Purchase", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DepositView(viewModel: AppViewModel, balance: com.example.data.UserBalance?) {
    val context = LocalContext.current
    var inputAmount by remember { mutableStateOf("") }
    var inputAccountNum by remember { mutableStateOf("") }
    var inputAccountName by remember { mutableStateOf("") }
    var inputTrxId by remember { mutableStateOf("") }

    val receiverNumber = balance?.easypaisaNumber ?: "03123456789"
    val receiverName = balance?.easypaisaName ?: "Ahmad Shahzad"

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "Deposit Pakistani Rupees (PKR)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldMetallic
                )
                Text(
                    "Follow the simplified interactive instructions below to add balance via phone transaction.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Instructions Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Step 1: Send Funds to Admin Account",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldMetallic
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Open your Easypaisa Mobile App and send your desired deposit funds to:",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PakistanGreenDark, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Easypaisa Account Title", fontSize = 10.sp, color = SoftGrey)
                            Text(receiverName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Easypaisa Name", receiverName))
                                Toast.makeText(context, "Copied Name to Clipboard", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PakistanGreenMedi),
                            modifier = Modifier.sizeIn(minHeight = 32.dp, minWidth = 48.dp)
                        ) {
                            Text("Copy", fontSize = 10.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PakistanGreenDark, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Easypaisa Phone Number", fontSize = 10.sp, color = SoftGrey)
                            Text(receiverNumber, fontSize = 14.sp, fontWeight = FontWeight.Black, color = GoldMetallic)
                        }
                        Button(
                            onClick = {
                                val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                cb.setPrimaryClip(ClipData.newPlainText("Easypaisa Number", receiverNumber))
                                Toast.makeText(context, "Copied Number to Clipboard", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PakistanGreenMedi),
                            modifier = Modifier.sizeIn(minHeight = 32.dp, minWidth = 48.dp)
                        ) {
                            Text("Copy", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Submission Form Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Step 2: Submit Payment Credentials",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldMetallic
                    )
                    Text(
                        "After sending payments, input transaction records correctly to receive credits.",
                        fontSize = 11.sp,
                        color = SoftGrey
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("Deposit Amount (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldMetallic,
                            unfocusedBorderColor = PakistanGreenLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("dep_amount")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputAccountNum,
                        onValueChange = { inputAccountNum = it },
                        label = { Text("Sender Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldMetallic,
                            unfocusedBorderColor = PakistanGreenLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("dep_acc_number")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputAccountName,
                        onValueChange = { inputAccountName = it },
                        label = { Text("Your Sender Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldMetallic,
                            unfocusedBorderColor = PakistanGreenLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("dep_acc_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputTrxId,
                        onValueChange = { inputTrxId = it },
                        label = { Text("Easypaisa TRX ID (Transaction ID)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldMetallic,
                            unfocusedBorderColor = PakistanGreenLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("dep_trx")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val amt = inputAmount.toDoubleOrNull() ?: 0.0
                            viewModel.submitDeposit(amt, inputAccountNum, inputAccountName, inputTrxId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldMetallic, contentColor = PakistanGreenDark),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("dep_submit")
                    ) {
                        Text("Verify & Submit Deposit Link", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawalView(viewModel: AppViewModel, balance: com.example.data.UserBalance?) {
    var inputAmount by remember { mutableStateOf("") }
    var inputAccountNum by remember { mutableStateOf("") }
    var inputAccountName by remember { mutableStateOf("") }

    val limitAmount = 500.0

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    "Quick Easy Withdrawals",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldMetallic
                )
                Text(
                    "Cashout instantly to your private Easypaisa profile. Direct deposits completed by administrator within 15 minutes.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Limit warning Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PakistanGreenDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GoldMetallic,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Available to Cash-out: PKR ${String.format("%,.2f", balance?.balance ?: 0.0)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Minimum limits apply for withdraw payouts: PKR $limitAmount. Default gateway is Easypaisa.",
                            fontSize = 10.sp,
                            color = SoftGrey
                        )
                    }
                }
            }
        }

        // Withdrawal Form Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Input payout accounts credentials",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldMetallic
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { inputAmount = it },
                        label = { Text("How Much Amount to Cash-out (PKR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldMetallic,
                            unfocusedBorderColor = PakistanGreenLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_amount")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputAccountNum,
                        onValueChange = { inputAccountNum = it },
                        label = { Text("Your Easypaisa Mobile Account Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldMetallic,
                            unfocusedBorderColor = PakistanGreenLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_phone")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inputAccountName,
                        onValueChange = { inputAccountName = it },
                        label = { Text("Account Title Beneficiary Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldMetallic,
                            unfocusedBorderColor = PakistanGreenLight,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_name")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val amt = inputAmount.toDoubleOrNull() ?: 0.0
                            viewModel.submitWithdrawal(amt, inputAccountNum, inputAccountName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldMetallic, contentColor = PakistanGreenDark),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("withdraw_submit")
                    ) {
                        Text("Proceed Withdrawal Request", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ================= ADMIN VIEW COMPONENTS =================

@Composable
fun AdminDashboardView(
    viewModel: AppViewModel,
    transactions: List<TransactionRecord>,
    plans: List<EarningPlan>,
    onEditPlan: (EarningPlan) -> Unit
) {
    var adminTabSelected by remember { mutableStateOf(0) } // 0: Payment Requests, 1: Shared Packages

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "System Admin Panel",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = GoldMetallic
        )

        // Tab Row selectors
        TabRow(
            selectedTabIndex = adminTabSelected,
            containerColor = CardBackground,
            contentColor = GoldMetallic,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[adminTabSelected]),
                    color = GoldMetallic
                )
            }
        ) {
            Tab(
                selected = adminTabSelected == 0,
                onClick = { adminTabSelected = 0 },
                text = { Text("Requests", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = adminTabSelected == 1,
                onClick = { adminTabSelected = 1 },
                text = { Text("Manage Plans", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (adminTabSelected == 0) {
            // Manage user payment transactions (deposit verification, withdrawals validation)
            val pending = transactions.filter { it.status == "PENDING" }
            val completed = transactions.filter { it.status != "PENDING" }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Pending Active Requests (${pending.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (pending.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                "No pending verification deposits/withdrawals here.",
                                fontSize = 12.sp,
                                color = SoftGrey,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                } else {
                    items(pending) { r ->
                        val isDeposit = r.type == "DEPOSIT"
                        val styleColor = if (isDeposit) MintGreenAccent else GoldenOrange
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        r.type,
                                        color = styleColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "PKR ${r.amount}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Acc Holder: ${r.accountName}", fontSize = 11.sp, color = Color.White)
                                Text("Phone/Acc: ${r.accountNumber}", fontSize = 11.sp, color = Color.White)
                                if (r.referenceId.isNotEmpty()) {
                                    Text("TX Ref ID: ${r.referenceId}", fontSize = 11.sp, color = GoldMetallic, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { viewModel.approveTransaction(r.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MintGreenAccent),
                                        modifier = Modifier.weight(1f).testTag("approve_tx_${r.id}")
                                    ) {
                                        Text("APPROVE ✓", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                                    }
                                    Button(
                                        onClick = { viewModel.rejectTransaction(r.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                        modifier = Modifier.weight(1f).testTag("reject_tx_${r.id}")
                                    ) {
                                        Text("REJECT ✕", fontWeight = FontWeight.Black, fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Historic Log Archive (${completed.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftGrey
                    )
                }

                if (completed.isEmpty()) {
                    item {
                        Text("No resolved logs recorded yet.", fontSize = 11.sp, color = SoftGrey)
                    }
                } else {
                    items(completed) { c ->
                        val requestColor = if (c.type == "DEPOSIT") MintGreenAccent else GoldenOrange
                        val statusColor = if (c.status == "APPROVED") MintGreenAccent else ErrorRed
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161C18)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "${c.type} - ${c.accountName}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text("Phone: ${c.accountNumber}", fontSize = 11.sp, color = SoftGrey)
                                    if (c.referenceId.isNotEmpty()) {
                                        Text("Ref: ${c.referenceId}", fontSize = 10.sp, color = SoftGrey)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "PKR ${c.amount}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = requestColor
                                    )
                                    Text(
                                        c.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Manage custom investment offers (plans shared by Admin)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Manage Active Offers",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (plans.isEmpty()) {
                    item {
                        Text("No plans created yet. Use 'Add Plan' bottom action button to design a tier package.", fontSize = 12.sp, color = SoftGrey)
                    }
                } else {
                    items(plans) { p ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        p.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        "Cost: PKR ${p.cost} | Daily Yield: PKR ${p.dailyEarning}",
                                        fontSize = 12.sp,
                                        color = GoldMetallic
                                    )
                                    Text(
                                        "Validity Cooldown: ${p.validityDays} Days",
                                        fontSize = 11.sp,
                                        color = SoftGrey
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { onEditPlan(p) },
                                        modifier = Modifier.testTag("admin_edit_${p.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit package", tint = GoldMetallic)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteEarningPlan(p) },
                                        modifier = Modifier.testTag("admin_delete_${p.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete package", tint = ErrorRed)
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

// Custom reusable Composable Dialog Form to gather investment plan data (Adds / Edits)
@Composable
fun PlanDialogForm(
    titleStr: String,
    initialPlan: EarningPlan? = null,
    onDismiss: () -> Unit,
    onConfirm: (title: String, cost: Double, daily: Double, days: Int) -> Unit
) {
    var title by remember { mutableStateOf(initialPlan?.title ?: "") }
    var costStr by remember { mutableStateOf(initialPlan?.cost?.toString() ?: "") }
    var dailyStr by remember { mutableStateOf(initialPlan?.dailyEarning?.toString() ?: "") }
    var validityStr by remember { mutableStateOf(initialPlan?.validityDays?.toString() ?: "30") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    titleStr,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldMetallic
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Campaign Plan Title (e.g. Bronze Package)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldMetallic,
                        unfocusedBorderColor = SoftGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("plan_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    label = { Text("Campaign Purchase Fee (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldMetallic,
                        unfocusedBorderColor = SoftGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("plan_cost_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = dailyStr,
                    onValueChange = { dailyStr = it },
                    label = { Text("Daily Payout Profit (PKR)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldMetallic,
                        unfocusedBorderColor = SoftGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("plan_daily_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = validityStr,
                    onValueChange = { validityStr = it },
                    label = { Text("Validity Duration (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldMetallic,
                        unfocusedBorderColor = SoftGrey,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("plan_days_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = SoftGrey)
                    }
                    Button(
                        onClick = {
                            val cost = costStr.toDoubleOrNull() ?: 0.0
                            val daily = dailyStr.toDoubleOrNull() ?: 0.0
                            val days = validityStr.toIntOrNull() ?: 0
                            onConfirm(title, cost, daily, days)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldMetallic, contentColor = PakistanGreenDark)
                    ) {
                        Text("Submit Package", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
