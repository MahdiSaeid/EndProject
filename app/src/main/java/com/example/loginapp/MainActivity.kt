@Composable
fun ProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)

    // بارگذاری داده‌ها از SharedPreferences هنگام بارگذاری کامپوز
    var name by remember { mutableStateOf(sharedPref.getString("name", "") ?: "") }
    var family by remember { mutableStateOf(sharedPref.getString("family", "") ?: "") }
    var imageUriString by remember { mutableStateOf(sharedPref.getString("imageUri", null)) }
    val imageUri = imageUriString?.let { Uri.parse(it) }

    // راه‌انداز انتخاب تصویر از گالری
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUriString = uri.toString() // ذخیره آدرس تصویر انتخاب شده به صورت رشته
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Spacer(modifier = Modifier.height(24.dp)) // فاصله عمودی بالای صفحه

        if (imageUri != null) {
            // نمایش تصویر انتخاب شده در یک دایره با قابلیت کلیک برای انتخاب تصویر جدید
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .clickable {
                        launcher.launch("image/*") // باز کردن گالری برای انتخاب تصویر جدید
                    },
                contentScale = ContentScale.Crop
            )
        } else {
            // اگر تصویری انتخاب نشده، نمایش دایره خاکستری کلیک‌پذیر برای انتخاب تصویر
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { launcher.launch("image/*") }, // باز کردن گالری
                contentAlignment = Alignment.Center
            ) {
                Text("Select Image", color = Color.White) // متن داخل دایره
            }
        }
        Spacer(modifier = Modifier.height(24.dp)) // فاصله عمودی بعد از تصویر

        OutlinedTextField(
            value = name,          // مقدار ورودی نام کاربر
            onValueChange = { name = it }, // به‌روزرسانی نام با هر تغییر
            label = { Text("Name") },       // برچسب فیلد نام
            modifier = Modifier.fillMaxWidth() // پر کردن کل عرض صفحه
        )
        Spacer(modifier = Modifier.height(8.dp)) // فاصله عمودی بین فیلدها

        OutlinedTextField(
            value = family,          // مقدار ورودی فامیلی کاربر
            onValueChange = { family = it }, // به‌روزرسانی فامیلی با هر تغییر
            label = { Text("Family") },       // برچسب فیلد فامیلی
            modifier = Modifier.fillMaxWidth() // پر کردن کل عرض صفحه
        )
        Spacer(modifier = Modifier.height(24.dp)) // فاصله عمودی قبل از دکمه‌ها

        Button(
            onClick = {
                // ذخیره داده‌ها در SharedPreferences
                with(sharedPref.edit()) {
                    putString("name", name)
                    putString("family", family)
                    putString("imageUri", imageUriString)
                    apply()
                }
                Toast.makeText(context, "Profile saved", Toast.LENGTH_SHORT).show() // نمایش پیام ذخیره موفق
            },
            modifier = Modifier.fillMaxWidth() // دکمه پر عرض
        ) {
            Text("Save Profile") // متن دکمه ذخیره پروفایل
        }
        Spacer(modifier = Modifier.height(12.dp)) // فاصله عمودی قبل از دکمه بازگشت

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back") // دکمه بازگشت به صفحه قبلی
        }
    }
}

@Composable
fun ChatScreen(onBack: () -> Unit) {
    var messages by remember { mutableStateOf(mutableListOf<Message>()) } // لیست پیام‌ها در حالت قابل تغییر
    var inputText by remember { mutableStateOf("") }                    // متن ورودی پیام جدید
    val context = LocalContext.current                                 // دسترسی به کانتکست برای نمایش توست
    val coroutineScope = rememberCoroutineScope()                     // اسکوپ اجرای coroutineها

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Chat with AI") },          // عنوان نوار ابزار بالا
            navigationIcon = {
                IconButton(onClick = onBack) {         // دکمه بازگشت در نوار ابزار
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f).padding(8.dp), // لیست پیام‌ها با فضای داخلی و وزن بالا
            reverseLayout = true                           // نمایش پیام‌های جدید در پایین صفحه
        ) {
            items(messages.asReversed()) { message ->
                MessageItem(message) // نمایش هر پیام در لیست
            }
        }

        Row(modifier = Modifier.padding(8.dp)) {
            TextField(
                value = inputText,                  // مقدار متن ورودی
                onValueChange = { inputText = it }, // به‌روزرسانی متن ورودی هنگام تایپ
                modifier = Modifier.weight(1f),    // فیلد متنی فضای بیشتری می‌گیرد
                placeholder = { Text("Type your message...") } // متن راهنما
            )
            Spacer(modifier = Modifier.width(8.dp)) // فاصله افقی بین فیلد و دکمه ارسال
            Button(onClick = {
                if (inputText.isNotBlank()) {
                    val userMessage = Message(content = inputText, isUser = true) // ساخت پیام کاربر
                    messages.add(userMessage) // اضافه کردن پیام به لیست
                    val currentInput = inputText // ذخیره متن فعلی برای ارسال
                    inputText = "" // پاک کردن فیلد ورودی

                    // اجرای همزمان ارسال پیام به API و دریافت پاسخ هوش مصنوعی
                    coroutineScope.launch {
                        val aiResponse = callOpenAI(currentInput) // فراخوانی API OpenAI
                        messages.add(Message(content = aiResponse, isUser = false)) // اضافه کردن پاسخ AI به لیست
                    }
                }
            }) {
                Text("Send") // متن دکمه ارسال پیام
            }
        }
    }
}

data class Message(val content: String, val isUser: Boolean) // مدل داده‌ای پیام، محتوا و فرستنده

@Composable
fun MessageItem(message: Message) {
    // تعیین رنگ پس‌زمینه پیام بر اساس فرستنده
    val backgroundColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    // تعیین جهت چیدمان پیام (راست برای کاربر، چپ برای AI)
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    // تعیین رنگ متن برای خوانایی بهتر
    val textColor = if (message.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp), // فاصله اطراف پیام
        contentAlignment = alignment // تعیین جهت قرارگیری پیام
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp), // گوشه‌های گرد پیام
            color = backgroundColor,          // رنگ پس‌زمینه پیام
            shadowElevation = 2.dp,           // سایه زیر پیام
            modifier = Modifier.padding(4.dp) // فاصله داخلی اطراف پیام
        ) {
            Text(
                text = message.content,       // نمایش متن پیام
                color = textColor,            // رنگ متن پیام
                modifier = Modifier.padding(8.dp) // فاصله داخلی متن از لبه‌ها
            )
        }
    }
}

suspend fun callOpenAI(prompt: String): String {
    // ایجاد کلاینت HTTP با Ktor
    val client = HttpClient(CIO) {
        // فعال‌سازی تبدیل JSON خودکار
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true // نادیده گرفتن کلیدهای اضافی در پاسخ JSON
            })
        }
        // تنظیم محدودیت زمان درخواست
        install(HttpTimeout) {
            requestTimeoutMillis = 30000 // 30 ثانیه
        }
    }

    // ساختار درخواست JSON با مدل و پیام کاربر
    val requestBody = Json.encodeToJsonElement(
        mapOf(
            "model" to "gpt-3.5-turbo",          // تعیین مدل هوش مصنوعی
            "messages" to listOf(
                mapOf("role" to "user", "content" to prompt) // پیام کاربر
            )
        )
    ).toString()

    try {
        // ارسال درخواست POST به API OpenAI
        val response = client.post("https://api.openai.com/v1/chat/completions") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $OPENAI_API_KEY") // اضافه کردن کلید API به هدر
                append(HttpHeaders.ContentType, "application/json")         // تعیین نوع محتوا به JSON
            }
            setBody(requestBody) // تعیین بدنه درخواست
        }

        // دریافت پاسخ به صورت متن خام
        val responseBody = response.bodyAsText()

        // تبدیل پاسخ به JSON
        val jsonElement = Json.parseToJsonElement(responseBody)

        // استخراج بخش choices که شامل پاسخ‌ها است
        val choices = jsonElement.jsonObject["choices"]?.jsonArray

        // گرفتن محتوای پیام پاسخ اولین گزینه
        val content = choices?.get(0)?.jsonObject?.get("message")?.jsonObject?.get("content")?.jsonPrimitive?.content

        return content ?: "No response from AI." // در صورت نبود پاسخ، پیام پیش‌فرض برگردانده شود
    } catch (e: Exception) {
        e.printStackTrace() // چاپ خطا در لاگ
        return "Error: ${e.message}" // برگرداندن پیام خطا
    } finally {
        client.close() // بستن کلاینت HTTP بعد از انجام درخواست
    }
}
