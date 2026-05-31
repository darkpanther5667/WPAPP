# Claude Code CLI — WhatsApp Billing Pro
# Stitch MCP → Android App (Full Working, No Mock Data)

---

## STITCH PROJECT DETAILS

```
Title : WhatsApp Billing Pro
ID    : 5402780494813650166
```

---

## STEP 1: FETCH ALL SCREENS FROM STITCH MCP

Use the Stitch MCP tool to fetch images AND code for every screen below.
For each screen use `curl -L` to download the hosted URLs.

```
Screen 1 — Dashboard
  ID: 67acf6d93d8c461eb2aac56b2c9afb0f

Screen 2 — WhatsApp Preview
  ID: f5b20d9a129c4c6e8f52606a6ff0f771

Screen 3 — Invoice Detail
  ID: af3b5f5879534a8d88030322964ea229

Screen 4 — Create Invoice
  ID: b23f10ca911648af887b3c3a35cf1fc1

Screen 5 — Reports
  ID: a022477246eb43a6b096351050a8c06b

Screen 6 — Clients
  ID: eb65ea401b9c464386e5140504ccf328

Screen 7 — Settings
  ID: 630c8a8eb63e4c36ae8dd441b3dde2eb
```

For each screen:
1. Fetch the image (visual reference)
2. Fetch the generated code
3. Extract: colors, spacing, typography, components, layout
4. Note all interactive elements (buttons, inputs, navigation)
5. Note all data fields shown (what data is displayed where)

Do NOT write any code yet.

---

## STEP 2: AUDIT REPORT (show this before coding)

After reading all 7 screens, give me this report:

### Screens Found:
- [ ] Dashboard — layout, components, data fields
- [ ] WhatsApp Preview — layout, components, data fields
- [ ] Invoice Detail — layout, components, data fields
- [ ] Create Invoice — layout, components, data fields
- [ ] Reports — layout, components, data fields
- [ ] Clients — layout, components, data fields
- [ ] Settings — layout, components, data fields

### Design System Extracted:
- [ ] Color palette (exact hex values from Stitch)
- [ ] Typography scale (sizes + weights)
- [ ] Spacing system (padding/margin values)
- [ ] Component library (all reusable components found)
- [ ] Navigation flow (how screens connect)
- [ ] Icon set used

### API Data Mapping:
- [ ] What data does Dashboard need from API?
- [ ] What data does WhatsApp Preview need?
- [ ] What data does Invoice Detail need?
- [ ] What data does Create Invoice submit?
- [ ] What data does Reports need?
- [ ] What data does Clients need?
- [ ] What does Settings save/load?

> Wait for my "proceed" before writing ANY code.

---

## STEP 3: REPLICATE EXACTLY — ZERO DEVIATION

- Pixel-perfect match to Stitch — no creative changes
- EXACT colors (copy hex from Stitch, do not pick your own)
- EXACT padding, margin, corner radius from Stitch
- EXACT font sizes and weights from Stitch
- EXACT icon placement and sizes
- EXACT component order on every screen
- Do NOT add features not shown in design
- Do NOT simplify or combine screens

---

## STEP 4: NO MOCK DATA — EVER

```
❌ listOf("Customer 1", "Customer 2")
❌ val amount = 5000  // hardcoded
❌ Thread.sleep(2000) // fake loading
❌ "Lorem ipsum"
❌ placeholder images
❌ hardcoded invoice numbers
❌ fake transaction lists

✅ viewModel.customers.observe { adapter.submitList(it) }
✅ repository.getInvoices() // real Retrofit call
✅ ShimmerFrameLayout // real loading state
✅ emptyStateView.isVisible = list.isEmpty() // real empty state
✅ Snackbar.make(view, error.message) // real error handling
```

If an API endpoint is not ready → show empty state view.
Never invent data.

---

## STEP 5: TECH STACK

| Layer | Library | Version |
|---|---|---|
| Language | Kotlin | Latest stable |
| UI | Material Design 3 | 1.11.0 |
| Architecture | MVVM + Repository | — |
| Navigation | Navigation Component | 2.7.6 |
| Network | Retrofit2 + OkHttp | 2.9.0 |
| Async | Coroutines + Flow | 1.7.3 |
| Loading | Facebook Shimmer | 0.5.0 |
| Images | Coil | 2.5.0 |
| Charts | MPAndroidChart | 3.1.0 |
| PDF/Share | Android Share Intent | — |
| Storage | EncryptedSharedPreferences | 1.1.0-alpha06 |

### build.gradle (add all of these):
```groovy
implementation 'androidx.core:core-ktx:1.12.0'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.navigation:navigation-fragment-ktx:2.7.6'
implementation 'androidx.navigation:navigation-ui-ktx:2.7.6'
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'
implementation 'com.facebook.shimmer:shimmer:0.5.0'
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
implementation 'io.coil-kt:coil:2.5.0'
implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
implementation 'androidx.security:security-crypto:1.1.0-alpha06'
implementation 'androidx.datastore:datastore-preferences:1.0.0'
```

---

## STEP 6: FOLDER STRUCTURE

```
app/src/main/java/com/whatsappbillingpro/
├── data/
│   ├── api/
│   │   ├── ApiService.kt
│   │   ├── ApiClient.kt
│   │   └── AuthInterceptor.kt
│   ├── models/
│   │   ├── Client.kt
│   │   ├── Invoice.kt
│   │   ├── InvoiceItem.kt
│   │   ├── Transaction.kt
│   │   ├── Report.kt
│   │   ├── WhatsAppMessage.kt
│   │   └── ApiResponse.kt
│   └── repository/
│       ├── ClientRepository.kt
│       ├── InvoiceRepository.kt
│       ├── ReportRepository.kt
│       └── AuthRepository.kt
├── ui/
│   ├── auth/
│   │   └── LoginActivity.kt
│   ├── main/
│   │   └── MainActivity.kt
│   ├── dashboard/
│   │   ├── DashboardFragment.kt
│   │   └── DashboardViewModel.kt
│   ├── whatsapp/
│   │   ├── WhatsAppPreviewFragment.kt
│   │   └── WhatsAppViewModel.kt
│   ├── invoice/
│   │   ├── InvoiceDetailFragment.kt
│   │   ├── CreateInvoiceFragment.kt
│   │   └── InvoiceViewModel.kt
│   ├── reports/
│   │   ├── ReportsFragment.kt
│   │   └── ReportsViewModel.kt
│   ├── clients/
│   │   ├── ClientsFragment.kt
│   │   ├── ClientDetailFragment.kt
│   │   └── ClientViewModel.kt
│   ├── settings/
│   │   ├── SettingsFragment.kt
│   │   └── SettingsViewModel.kt
│   └── common/
│       ├── BaseFragment.kt
│       ├── LoadingDialog.kt
│       └── adapters/
│           ├── ClientAdapter.kt
│           ├── InvoiceAdapter.kt
│           └── TransactionAdapter.kt
├── utils/
│   ├── Constants.kt
│   ├── NetworkUtils.kt
│   ├── CurrencyUtils.kt
│   ├── DateUtils.kt
│   ├── WhatsAppUtils.kt
│   └── Extensions.kt
└── App.kt
```

---

## STEP 7: CONSTANTS

```kotlin
object Constants {
    // ⚠️ Replace with your actual Render.com URL
    const val BASE_URL = "https://your-app.onrender.com/api/"

    // Shared Preferences keys
    const val PREF_TOKEN      = "jwt_token"
    const val PREF_STAFF_ID   = "staff_id"
    const val PREF_STAFF_NAME = "staff_name"
    const val PREF_SHOP_NAME  = "shop_name"

    // Formatting
    const val CURRENCY_SYMBOL = "₹"
    const val DATE_FORMAT     = "dd MMM yyyy"
    const val DATE_TIME_FORMAT = "dd MMM yyyy, hh:mm a"
}
```

---

## STEP 8: SCREEN-BY-SCREEN BUILD INSTRUCTIONS

Build in this exact order. ONE screen at a time.
Wait for my approval after each screen.

---

### SCREEN 1 — DASHBOARD
**Stitch ID:** `67acf6d93d8c461eb2aac56b2c9afb0f`

Read from Stitch, then build:

**API calls needed:**
```kotlin
// GET /api/dashboard/summary
data class DashboardSummary(
    val totalOutstanding: Double,
    val todayCollection: Double,
    val pendingInvoices: Int,
    val totalClients: Int,
    val recentTransactions: List<Transaction>
)
```

**Requirements:**
- Fetch real summary data on fragment start
- Shimmer loading while fetching
- Pull to refresh (SwipeRefreshLayout)
- Empty state if no transactions
- All amounts formatted as ₹ Indian format
- Match Stitch layout exactly (cards, colors, spacing)

---

### SCREEN 2 — WHATSAPP PREVIEW
**Stitch ID:** `f5b20d9a129c4c6e8f52606a6ff0f771`

Read from Stitch, then build:

**API calls needed:**
```kotlin
// GET /api/whatsapp/messages
// POST /api/whatsapp/send
data class WhatsAppMessage(
    val id: String,
    val customerName: String,
    val phone: String,
    val message: String,
    val timestamp: String,
    val type: String // "sent" | "received"
)
```

**Requirements:**
- Load real WhatsApp message history from API
- Chat bubble UI matching Stitch exactly
- Send button calls real API
- WhatsApp deep link: `wa.me/91{phone}?text={message}`
- Shimmer loading for message list
- Real-time feel (auto-refresh every 30s if design shows it)

---

### SCREEN 3 — INVOICE DETAIL
**Stitch ID:** `af3b5f5879534a8d88030322964ea229`

Read from Stitch, then build:

**API calls needed:**
```kotlin
// GET /api/invoices/{id}
data class Invoice(
    val id: String,
    val invoiceNumber: String,
    val client: Client,
    val items: List<InvoiceItem>,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val status: String, // "paid" | "unpaid" | "partial"
    val createdAt: String,
    val paidAt: String?
)
```

**Requirements:**
- Load invoice by ID passed via Navigation args
- "Mark as Paid" button → PATCH /api/invoices/{id}/pay
- "Share" button → Android share intent (PDF or image)
- Print layout with proper invoice format
- Status badge color matches Stitch exactly

---

### SCREEN 4 — CREATE INVOICE
**Stitch ID:** `b23f10ca911648af887b3c3a35cf1fc1`

Read from Stitch, then build:

**API calls needed:**
```kotlin
// GET /api/clients (for client selector autocomplete)
// POST /api/invoices (create new invoice)
data class CreateInvoiceRequest(
    val clientId: String,
    val items: List<InvoiceItem>,
    val notes: String?,
    val tax: Double?
)
```

**Requirements:**
- Client selector: AutoCompleteTextView with real client list from API
- Add/remove items dynamically (match Stitch layout)
- Auto-calculate total as items are added/changed
- All fields validated before submit
- Submit button shows CircularProgressIndicator while loading
- On success: navigate to Invoice Detail screen with new invoice ID
- On error: Snackbar with server error message
- Match Stitch form layout exactly

---

### SCREEN 5 — REPORTS
**Stitch ID:** `a022477246eb43a6b096351050a8c06b`

Read from Stitch, then build:

**API calls needed:**
```kotlin
// GET /api/reports?from={date}&to={date}
data class ReportData(
    val totalSales: Double,
    val totalCollected: Double,
    val totalPending: Double,
    val dailySales: List<DailySale>,
    val topClients: List<ClientSummary>
)
```

**Requirements:**
- Date range picker → calls API with real dates
- MPAndroidChart graphs with real API data
- Chart colors match Stitch exactly
- Export CSV → Android share intent
- Shimmer loading while fetching
- Empty state if no data for selected range

---

### SCREEN 6 — CLIENTS
**Stitch ID:** `eb65ea401b9c464386e5140504ccf328`

Read from Stitch, then build:

**API calls needed:**
```kotlin
// GET /api/clients
// POST /api/clients (add new client)
// GET /api/clients/{id} (client detail)
data class Client(
    val id: String,
    val name: String,
    val phone: String,
    val outstanding: Double,
    val totalBilled: Double,
    val lastTransactionAt: String?
)
```

**Requirements:**
- Real client list from API
- SearchView filters list in real time (local filter on fetched data)
- Outstanding amount: RED if > 0, GREEN if = 0
- Add Client → BottomSheetDialog with form → POST /api/clients
- Click client → ClientDetailFragment (pass client ID via nav args)
- Shimmer loading (match number of shimmer cards to Stitch layout)
- Empty state with message if no clients

---

### SCREEN 7 — SETTINGS
**Stitch ID:** `630c8a8eb63e4c36ae8dd441b3dde2eb`

Read from Stitch, then build:

**API calls needed:**
```kotlin
// GET /api/settings (load current settings)
// PUT /api/settings (save settings)
// POST /api/auth/logout
```

**Requirements:**
- Load real settings from API on fragment start
- Save button → PUT /api/settings with real data
- Logout → clears EncryptedSharedPreferences → navigates to Login
- Match Stitch layout exactly (every field, every toggle, every section)
- Show success Snackbar after save
- Confirm dialog before logout

---

## STEP 9: SHARED REQUIREMENTS (all screens)

### Loading:
```kotlin
// Show shimmer → fetch → hide shimmer → show content/error/empty
shimmerLayout.startShimmer()
shimmerLayout.isVisible = true
recyclerView.isVisible = false

viewModel.data.observe(viewLifecycleOwner) { result ->
    shimmerLayout.stopShimmer()
    shimmerLayout.isVisible = false
    when (result) {
        is Success -> {
            recyclerView.isVisible = true
            adapter.submitList(result.data)
        }
        is Error -> showError(result.message)
        is Empty -> emptyStateView.isVisible = true
    }
}
```

### Network check before every API call:
```kotlin
if (!NetworkUtils.isConnected(requireContext())) {
    showError("Internet connection nahi hai")
    return
}
```

### Currency formatting:
```kotlin
object CurrencyUtils {
    fun format(amount: Double): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return formatter.format(amount)
    }
}
```

### WhatsApp deep link:
```kotlin
object WhatsAppUtils {
    fun open(context: Context, phone: String, message: String) {
        val url = "https://wa.me/91$phone?text=${Uri.encode(message)}"
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
```

---

## STEP 10: HOW TO WORK

1. Fetch all 7 Stitch screens via MCP (`curl -L` for hosted URLs)
2. Show me the audit report → wait for "proceed"
3. Build ONE screen at a time in the order above
4. After each screen: `"Test karo — [Screen Name]"`
5. Wait for my confirmation before next screen
6. Ask if anything in Stitch design is unclear — never guess
7. Never add mock data under any circumstance

---

## CRITICAL RULES

| ❌ Never | ✅ Always |
|---|---|
| Mock / dummy data | Real API calls only |
| Hardcoded strings | `Constants.kt` or `strings.xml` |
| Hardcoded amounts | From API response |
| Guess design details | Read from Stitch MCP |
| Skip to next screen | Wait for approval |
| Direct API in Fragment | Through ViewModel → Repository |
| Fake loading delays | Real Shimmer + API |

---

*WhatsApp Billing Pro — Stitch MCP + Claude Code CLI + Render.com*