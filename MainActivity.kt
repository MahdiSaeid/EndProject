package com.example.loginapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.loginapp.ui.theme.LoginAppTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    // هنگام ساخت Activity، محتوای اصلی اپلیکیشن را تنظیم می‌کنیم
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()  // فعال کردن نمایش در لبه‌های صفحه (بدون حاشیه‌ها)
        setContent {
            // ست کردن تم و نمایش محتوا
            LoginAppTheme {
                AppContent()  // نمایش محتوای اپلیکیشن
            }
        }
    }
}

// محتوای اصلی اپلیکیشن که بر اساس وضعیت کاربر تغییر می‌کند
@Composable
fun AppContent() {
    var loggedIn by remember { mutableStateOf(false) } // وضعیت ورود کاربر (آیا وارد شده است یا نه)
    var loginTime by remember { mutableStateOf("") }  // زمانی که کاربر وارد شده را ذخیره می‌کنیم

    // اگر کاربر وارد شده باشد، صفحه اصلی را نشان می‌دهیم
    if (loggedIn) {
        HomeScreen(loginTime) {
            loggedIn = false // اگر کاربر خارج شود، وضعیت ورود را به false تغییر می‌دهیم
        }
    } else {
        // اگر کاربر وارد نشده باشد، صفحه ورود را نشان می‌دهیم
        LoginScreen(onLogin = {
            loginTime = getCurrentTime()  // زمان ورود را می‌گیریم
            loggedIn = true  // وضعیت ورود را به true تغییر می‌دهیم
        })
    }
}

// صفحه ورود (Login)
@Composable
fun LoginScreen(onLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }  // نام کاربری وارد شده
    var password by remember { mutableStateOf("") }  // کلمه عبور وارد شده

    // طراحی صفحه ورود با استفاده از Column برای چیدمان عمودی
    Column(
        modifier = Modifier
            .fillMaxSize()  // صفحه را به طور کامل پر می‌کند
            .padding(24.dp),  // فاصله اطراف صفحه
        horizontalAlignment = Alignment.CenterHorizontally,  // متن‌ها در وسط صفحه تراز می‌شوند
        verticalArrangement = Arrangement.Center  // چیدمان عمودی در وسط صفحه
    ) {
        // فیلد نام کاربری
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },  // وقتی کاربر چیزی وارد می‌کند، مقدار آن را ذخیره می‌کنیم
            label = { Text("Username") },  // برچسب فیلد
            modifier = Modifier.fillMaxWidth()  // عرض فیلد به اندازه صفحه
        )
        Spacer(modifier = Modifier.height(16.dp))  // فاصله بین فیلدها
        // فیلد کلمه عبور
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },  // وقتی کاربر کلمه عبور وارد می‌کند، مقدار آن را ذخیره می‌کنیم
            label = { Text("Password") },  // برچسب فیلد
            modifier = Modifier.fillMaxWidth()  // عرض فیلد به اندازه صفحه
        )
        Spacer(modifier = Modifier.height(24.dp))  // فاصله بیشتر بین فیلدها و دکمه
        // دکمه ورود
        Button(
            onClick = onLogin,  // وقتی دکمه زده شد، تابع onLogin اجرا می‌شود
            modifier = Modifier.fillMaxWidth()  // دکمه به اندازه صفحه عرض می‌شود
        ) {
            Text("Login")  // متن روی دکمه
        }
    }
}

// صفحه اصلی بعد از ورود
@Composable
fun HomeScreen(loginTime: String, onLogout: () -> Unit) {
    // طراحی صفحه اصلی با استفاده از Column
    Column(
        modifier = Modifier
            .fillMaxSize()  // صفحه را به طور کامل پر می‌کند
            .padding(24.dp),  // فاصله اطراف صفحه
        verticalArrangement = Arrangement.Center,  // چیدمان عمودی در وسط صفحه
        horizontalAlignment = Alignment.CenterHorizontally  // متن‌ها در وسط صفحه تراز می‌شوند
    ) {
        // نمایش زمان ورود
        Text("You logged in at:")
        Text(loginTime, style = MaterialTheme.typography.headlineSmall)  // زمان ورود به صورت بزرگ‌تر
        Spacer(modifier = Modifier.height(24.dp))  // فاصله بعد از زمان ورود
        // دکمه خروج
        Button(onClick = onLogout) {
            Text("Back to Login")  // متن روی دکمه
        }
    }
}

// تابعی برای دریافت زمان جاری
fun getCurrentTime(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())  // فرمت تاریخ و زمان
    return sdf.format(Date())  // تاریخ و زمان کنونی را برمی‌گرداند
}

// پیش‌نمایش برای بررسی طراحی
@Preview(showBackground = true)
@Composable
fun PreviewApp() {
    LoginAppTheme {
        AppContent()  // محتوای اپلیکیشن را نمایش می‌دهد
    }
}
