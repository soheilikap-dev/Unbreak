package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONArray
import org.json.JSONObject

// CP Package Model
data class CpPackage(
    val id: Int,
    val amount: Int,
    val priceTomans: Int,
    val isSpecial: Boolean = false,
    val formattedAmount: String = "${DecimalFormat("#,###").format(amount)} CP"
)

// Past Order Log Model
data class OrderItem(
    val id: String,
    val packageName: String,
    val price: String,
    val uid: String,
    val timestamp: Long,
    val status: String = "در انتظار پرداخت و ارسال رسید"
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = CodDarkBg
                    ) { innerPadding ->
                        StoreScreen(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("sultan_cp_prefs", Context.MODE_PRIVATE) }

    // Saved Account Coordinates
    var savedUid by remember { mutableStateOf(sharedPrefs.getString("saved_uid", "") ?: "") }
    var savedPlayerName by remember { mutableStateOf(sharedPrefs.getString("saved_player_name", "") ?: "") }
    var savedPhone by remember { mutableStateOf(sharedPrefs.getString("saved_phone", "") ?: "") }

    // Past Orders List
    val orderListState = remember { mutableStateListOf<OrderItem>() }

    // Load past orders
    LaunchedEffect(Unit) {
        val jsonString = sharedPrefs.getString("order_history", "[]") ?: "[]"
        try {
            val jsonArray = JSONArray(jsonString)
            orderListState.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                orderListState.add(
                    OrderItem(
                        id = obj.optString("id", UUID.randomUUID().toString().take(6).uppercase()),
                        packageName = obj.optString("packageName", ""),
                        price = obj.optString("price", ""),
                        uid = obj.optString("uid", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        status = obj.optString("status", "در انتظار پرداخت")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Active Checkout Package
    var selectedPackage by remember { mutableStateOf<CpPackage?>(null) }

    // Store Packages definition
    val cpPackages = remember {
        listOf(
            CpPackage(1, 80, 154000),
            CpPackage(2, 160, 308000),
            CpPackage(3, 240, 462000),
            CpPackage(4, 320, 616000),
            CpPackage(5, 400, 770000),
            CpPackage(6, 480, 924000),
            CpPackage(7, 560, 1078000),
            CpPackage(8, 640, 1232000),
            CpPackage(9, 720, 1386000),
            CpPackage(10, 800, 1540000),
            CpPackage(11, 880, 1694000),
            CpPackage(12, 960, 1848000),
            CpPackage(13, 1040, 2002000),
            CpPackage(14, 1120, 2156000),
            CpPackage(15, 1200, 2310000),
            CpPackage(16, 1280, 2464000),
            CpPackage(17, 1360, 2618000, isSpecial = true, formattedAmount = "1,360 CP (آفر ویژه)")
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // App header & Hero banner
        item {
            HeroHeaderSection()
        }

        // Feature benefits tickers
        item {
            StoreFeaturesBanner()
        }

        // Shop Title
        item {
            Text(
                text = "لیست قیمت سی‌پی شگفت‌انگیز 💎",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CodAccentGold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }

        // Custom styled grid implementation (rows of 2 items)
        val chunkedPackages = cpPackages.chunked(2)
        items(chunkedPackages) { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { cpPack ->
                    Box(modifier = Modifier.weight(1f)) {
                        PackageCard(
                            cpPackage = cpPack,
                            onClick = { selectedPackage = cpPack }
                        )
                    }
                }
                // Handle odd count alignment
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Orders List Profile
        if (orderListState.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                RecentOrdersSection(
                    orders = orderListState,
                    onClearHistory = {
                        sharedPrefs.edit().putString("order_history", "[]").apply()
                        orderListState.clear()
                        Toast.makeText(context, "تاریخچه تراکنش‌ها پاک شد", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // Shop support channel links
        item {
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = CodDivider, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(16.dp))
            StoreSupportSection()
        }
    }

    // Interactive Checkout Drawer / Dialog
    selectedPackage?.let { pack ->
        CheckoutDialog(
            cpPackage = pack,
            initialUid = savedUid,
            initialPlayerName = savedPlayerName,
            initialPhone = savedPhone,
            onDismiss = { selectedPackage = null },
            onConfirmOrder = { uid, playerName, phone ->
                // Save coordinates locally
                savedUid = uid
                savedPlayerName = playerName
                savedPhone = phone
                sharedPrefs.edit()
                    .putString("saved_uid", uid)
                    .putString("saved_player_name", playerName)
                    .putString("saved_phone", phone)
                    .apply()

                // Generate and record purchase log
                val newOrder = OrderItem(
                    id = UUID.randomUUID().toString().take(6).uppercase(),
                    packageName = pack.formattedAmount,
                    price = formatPriceTomans(pack.priceTomans),
                    uid = uid,
                    timestamp = System.currentTimeMillis()
                )

                orderListState.add(0, newOrder)
                // Save JSON list
                val jsonArray = JSONArray()
                orderListState.forEach {
                    val obj = JSONObject()
                    obj.put("id", it.id)
                    obj.put("packageName", it.packageName)
                    obj.put("price", it.price)
                    obj.put("uid", it.uid)
                    obj.put("timestamp", it.timestamp)
                    obj.put("status", it.status)
                    jsonArray.put(obj)
                }
                sharedPrefs.edit().putString("order_history", jsonArray.toString()).apply()

                // Trigger Telegram Link Support flow
                val orderMsg = """
                    💎 سفارش سی‌پی از اپلیکیشن سلطان سی‌پی 💎
                    📦 محصول خریداری شده: ${pack.formattedAmount}
                    💰 مبلغ خرید: ${formatPriceTomans(pack.priceTomans)}
                    🆔 شناسه کاربری (UID): $uid
                    👤 نام شما در بازی: $playerName
                    📞 شماره تماس: $phone
                    💳 شماره کارت مقصد: 5859-8312-5060-3718 (به نام سهیل محمدی)
                    ⏱ تاریخ: ${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(newOrder.timestamp))}
                    
                    * مپ اسکرین‌شات رسید پرداخت را ضمیمه کنید *
                """.trimIndent()

                // Copy to clipboard
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Order Details", orderMsg)
                clipboard.setPrimaryClip(clip)

                Toast.makeText(context, "جزئیات سفارش در حافظه موبایل کپی شد!", Toast.LENGTH_LONG).show()

                // Open Telegram
                val tgUsername = "UNBREAKYT"
                val appUri = "tg://resolve?domain=$tgUsername"
                val webUri = "https://t.me/$tgUsername"
                launchSocialIntent(context, appUri, webUri)

                selectedPackage = null
            }
        )
    }
}

@Composable
fun HeroHeaderSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    ) {
        // Hero Background picture
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner),
            contentDescription = "سلطان سیپی بنر",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dim Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.4f),
                            CodDarkBg
                        )
                    )
                )
        )

        // Overlay Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(CodAccentRed, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "فروش ویژه و فوری 🔴",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = "💎 سلطان سیپی کالاف دیوتی",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black
                )
            )

            Text(
                text = "خرید ایمن و ارزان سیپی با پشتیبانی شبانه‌روزی و گارانتی بازگشت وجه کامل",
                style = TextStyle(
                    color = CodTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

@Composable
fun StoreFeaturesBanner() {
    val features = remember {
        listOf(
            Triple("تحویل فوری و خودکار", "سیپی‌ها کمتر از ۱۵ دقیقه شارژ می‌شوند", Icons.Default.CheckCircle),
            Triple("پشتیبانی ۲۴ ساعته", "خدمت‌رسانی حتی در روزهای تعطیل", Icons.Default.Phone),
            Triple("کمترین قیمت بازار", "تخفیف‌های استثنایی و رقابتی", Icons.Default.Star),
            Triple("امنیت ۱۰۰٪ تضمینی", "پرداخت ایمن و حفظ حریم خصوصی", Icons.Default.Lock),
            Triple("ضمانت بازگشت وجه", "در صورت بروز کوچکترین مشکل در خرید", Icons.Default.ShoppingCart)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        features.forEach { item ->
            val title = item.first
            val subtitle = item.second
            val icon = item.third
            Card(
                colors = CardDefaults.cardColors(containerColor = CodSurface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CodDivider),
                modifier = Modifier
                    .width(180.dp)
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = CodAccentGold
                    )
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = subtitle,
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = CodTextSecondary
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageCard(
    cpPackage: CpPackage,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = CodSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (cpPackage.isSpecial) 2.dp else 1.dp,
            color = if (cpPackage.isSpecial) CodAccentGold else CodDivider
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (cpPackage.isSpecial) 4.dp else 0.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("package_card_${cpPackage.id}")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (cpPackage.isSpecial) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(listOf(CodAccentRed, CodAccentGold)),
                            RoundedCornerShape(bottomStart = 8.dp, topEnd = 14.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "آفر ویژه 🔥",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Gold CP Coin block
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(CodAccentGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CP",
                        style = TextStyle(
                            color = Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    )
                }

                Text(
                    text = cpPackage.formattedAmount,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = formatPriceTomans(cpPackage.priceTomans),
                    style = TextStyle(
                        color = CodAccentGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                // Trigger item buy action
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (cpPackage.isSpecial) CodAccentRed else CodAccentGold,
                        contentColor = if (cpPackage.isSpecial) Color.White else Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("buy_button_${cpPackage.id}")
                ) {
                    Text(
                        text = "خرید",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RecentOrdersSection(
    orders: List<OrderItem>,
    onClearHistory: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "سفارشات اخیر شما 📜",
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            TextButton(onClick = onClearHistory) {
                Text(
                    text = "پاک کردن تاریخچه",
                    color = CodAccentRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        orders.take(5).forEach { order ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CodSurface),
                border = BorderStroke(1.dp, CodDivider),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "بسته: ${order.packageName}",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "کد رهگیری: ${order.id} | قیمت: ${order.price}",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = CodTextSecondary
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(CodAccentGold.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .border(1.dp, CodAccentGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "منتظر رسید",
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CodAccentGold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreSupportSection() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "راه‌های ارتباطی و پشتیبانی 💬",
            style = TextStyle(
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Text(
            text = "در صورت بروز هرگونه ابهام یا سوال پیش از خرید با ما ارتباط برقرار کنید:",
            style = TextStyle(
                fontSize = 12.sp,
                color = CodTextSecondary
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Telegram Button
            Button(
                onClick = {
                    launchSocialIntent(context, "tg://resolve?domain=unbreak_yt", "https://t.me/unbreak_yt")
                },
                colors = ButtonDefaults.buttonColors(containerColor = BlueTelegram),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Telegram",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تلگرام",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Rubika Button
            Button(
                onClick = {
                    launchSocialIntent(context, "", "https://rubika.ir/unbreak_gaming")
                },
                colors = ButtonDefaults.buttonColors(containerColor = CodAccentGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Rubika",
                    modifier = Modifier.size(16.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "روبیکا",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Instagram Button
            Button(
                onClick = {
                    launchSocialIntent(context, "instagram://user?username=UNBREAK_YT", "https://instagram.com/UNBREAK_YT")
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurpleInstagram),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Instagram",
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "اینستاگرام",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Direct Call Button
            Button(
                onClick = {
                    try {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:09118876440"))
                        context.startActivity(dialIntent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "امکان برقراری تماس یافت نشد", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CodSurfaceLight),
                border = BorderStroke(1.dp, CodDivider),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Call",
                    modifier = Modifier.size(16.dp),
                    tint = CodAccentGold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تماس تلفنی",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    cpPackage: CpPackage,
    initialUid: String,
    initialPlayerName: String,
    initialPhone: String,
    onDismiss: () -> Unit,
    onConfirmOrder: (uid: String, name: String, phone: String) -> Unit
) {
    var uidState by remember { mutableStateOf(initialUid) }
    var playerNameState by remember { mutableStateOf(initialPlayerName) }
    var phoneState by remember { mutableStateOf(initialPhone) }

    val context = LocalContext.current

    val isFormValid = remember(uidState, playerNameState, phoneState) {
        uidState.isNotBlank() && playerNameState.isNotBlank() && phoneState.isNotBlank()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(enabled = true, onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Elegant RTL Checkout Slideup Board
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 680.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .clickable(enabled = false) {},  // Avoid click propagation
                    colors = CardDefaults.cardColors(containerColor = CodSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                    border = BorderStroke(1.dp, CodDivider)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title Drawer notch handles
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(5.dp)
                                .background(CodDivider, CircleShape)
                                .align(Alignment.CenterHorizontally)
                        )

                        // Package info callout section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CodSurfaceLight, RoundedCornerShape(12.dp))
                                .border(1.dp, CodDivider, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = cpPackage.formattedAmount,
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "بسته انتخابی شما",
                                    fontSize = 11.sp,
                                    color = CodTextSecondary
                                )
                            }

                            Text(
                                text = formatPriceTomans(cpPackage.priceTomans),
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CodAccentGold
                                )
                            )
                        }

                        // Form Section 1: User coordinates inputs
                        Text(
                            text = "۱. مشخصات اکانت بازی:",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = uidState,
                            onValueChange = { uidState = it },
                            label = { Text("یو‌آیدی اکانت (UID)") },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Left),
                            placeholder = { Text("مانند: 712283944719220138", color = CodTextSecondary.copy(alpha = 0.5f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CodAccentGold,
                                unfocusedBorderColor = CodDivider,
                                cursorColor = CodAccentGold
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("uid_input")
                        )

                        OutlinedTextField(
                            value = playerNameState,
                            onValueChange = { playerNameState = it },
                            label = { Text("نام مستعار در بازی (IGN)") },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                            placeholder = { Text("مانند: Captain_Price", color = CodTextSecondary.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CodAccentGold,
                                unfocusedBorderColor = CodDivider,
                                cursorColor = CodAccentGold
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("player_name_input")
                        )

                        OutlinedTextField(
                            value = phoneState,
                            onValueChange = { phoneState = it },
                            label = { Text("شماره همراه فعال") },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Left),
                            placeholder = { Text("مانند: 09118876440", color = CodTextSecondary.copy(alpha = 0.5f)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CodAccentGold,
                                unfocusedBorderColor = CodDivider,
                                cursorColor = CodAccentGold
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input")
                        )

                        // Form Section 2: Cards details and copy button
                        Text(
                            text = "۲. پرداخت کارت به کارت:",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        // Bank Card Graphic Board
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2130)),
                            border = BorderStroke(1.dp, CodAccentGold.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "بانک تجارت 💳",
                                        style = TextStyle(
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp
                                        )
                                    )
                                    Text(
                                        text = "عضو شتاب",
                                        style = TextStyle(
                                            color = CodTextSecondary,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                Text(
                                    text = "5859 - 8312 - 5060 - 3718",
                                    style = TextStyle(
                                        color = CodAccentGold,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 19.sp,
                                        letterSpacing = 1.5.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "صاحب کارت:",
                                            color = CodTextSecondary,
                                            fontSize = 9.sp
                                        )
                                        Text(
                                            text = "سهیل محمدی",
                                            style = TextStyle(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "مبلغ قابل انتقال:",
                                            color = CodTextSecondary,
                                            fontSize = 9.sp
                                        )
                                        Text(
                                            text = formatPriceTomans(cpPackage.priceTomans),
                                            style = TextStyle(
                                                color = CodAccentGold,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        // Hot keys copy buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Card Number", "5859831250603718")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "شماره کارت کپی شد 📋", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CodSurfaceLight),
                                border = BorderStroke(1.dp, CodDivider),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Copy Card",
                                    modifier = Modifier.size(14.dp),
                                    tint = CodAccentGold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "کپی کارت", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Price", cpPackage.priceTomans.toString())
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "مبلغ دقیق کپی شد 📋", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CodSurfaceLight),
                                border = BorderStroke(1.dp, CodDivider),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Copy Price",
                                    modifier = Modifier.size(14.dp),
                                    tint = CodAccentGold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "کپی مبلغ", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        // Note on support
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "توضیح",
                                modifier = Modifier.size(16.dp),
                                tint = CodAccentGold
                            )
                            Text(
                                text = "پس از واریز، دکمه زیر را زده و عکس رسید بانکی را بفرستید.",
                                fontSize = 11.sp,
                                color = CodTextSecondary
                            )
                        }

                        // Bottom Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cancel
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = CodDivider),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .weight(1f)
                            ) {
                                Text(text = "انصراف", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            // Confirm & Send Telegram
                            Button(
                                onClick = {
                                    if (isFormValid) {
                                        onConfirmOrder(uidState, playerNameState, phoneState)
                                    } else {
                                        Toast.makeText(context, "لطفاً تمام مشخصات بازی را وارد کنید", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = isFormValid,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFormValid) CodAccentGold else CodDivider,
                                    contentColor = if (isFormValid) Color.Black else CodTextSecondary
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .weight(2f)
                                    .testTag("submit_order_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Telegram",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "پرداخت و ارسال جزئیات",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Utility formatting pricing
fun formatPriceTomans(priceInTomans: Int): String {
    val formatter = DecimalFormat("#,###")
    return "${formatter.format(priceInTomans)} تومان"
}

// Safe launching Intent to Social apps
fun launchSocialIntent(context: Context, appUriSchema: String, webFallbackUrl: String) {
    if (appUriSchema.isNotBlank()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(appUriSchema))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    try {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webFallbackUrl))
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(webIntent)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "خطا در باز کردن لینک پشتیبان", Toast.LENGTH_SHORT).show()
    }
}
