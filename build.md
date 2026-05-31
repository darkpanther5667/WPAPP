\# Android Shop Billing App — Complete Redesign Prompt

> For use in Windsurf / Cursor AI — Vyapar \& MyBillBook quality redesign



\---



\## PHASE 0: AUDIT FIRST (Do this before ANY changes)



Read every file. Then give me a report:



\### Code Audit Report:

\- \[ ] List all Activities and Fragments found

\- \[ ] List all API calls and endpoints used

\- \[ ] List all data models found

\- \[ ] List all crashes / null pointer risks found

\- \[ ] List all hardcoded strings / values found

\- \[ ] List all missing error handling

\- \[ ] List all incomplete screens or TODOs

\- \[ ] List what is working correctly (do not touch these)

\- \[ ] List what needs to be fixed

\- \[ ] List what needs to be built from scratch



> Wait for my approval before starting Phase 1.



\---



\## PHASE 1: PROJECT SETUP \& ARCHITECTURE



\### 1.1 — Upgrade build.gradle dependencies:



```groovy

// Core

implementation 'androidx.core:core-ktx:1.12.0'

implementation 'androidx.appcompat:appcompat:1.6.1'

implementation 'androidx.activity:activity-ktx:1.8.0'

implementation 'androidx.fragment:fragment-ktx:1.6.2'



// Material Design 3

implementation 'com.google.android.material:material:1.11.0'



// Navigation Component

implementation 'androidx.navigation:navigation-fragment-ktx:2.7.6'

implementation 'androidx.navigation:navigation-ui-ktx:2.7.6'



// ViewModel + LiveData + Coroutines

implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'

implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'

implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'



// Retrofit + OkHttp

implementation 'com.squareup.retrofit2:retrofit:2.9.0'

implementation 'com.squareup.retrofit2:converter-gson:2.9.0'

implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'



// UI

implementation 'com.facebook.shimmer:shimmer:0.5.0'

implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'

implementation 'io.coil-kt:coil:2.5.0'

implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'



// Storage

implementation 'androidx.security:security-crypto:1.1.0-alpha06'

implementation 'androidx.datastore:datastore-preferences:1.0.0'



// PDF Generation

implementation 'com.itextpdf:itext7-core:7.2.5'

```



\### 1.2 — Folder Structure (enforce this):



```

app/src/main/java/com/yourapp/

├── data/

│   ├── api/

│   │   ├── ApiService.kt         ← All Retrofit endpoints

│   │   ├── ApiClient.kt          ← OkHttp + Retrofit setup

│   │   └── AuthInterceptor.kt    ← JWT in every header

│   ├── models/

│   │   ├── Customer.kt

│   │   ├── Bill.kt

│   │   ├── Transaction.kt

│   │   ├── Staff.kt

│   │   └── ApiResponse.kt        ← Generic wrapper

│   └── repository/

│       ├── CustomerRepository.kt

│       ├── BillRepository.kt

│       └── AuthRepository.kt

├── ui/

│   ├── auth/

│   │   └── LoginActivity.kt

│   ├── main/

│   │   └── MainActivity.kt       ← BottomNav host

│   ├── home/

│   │   ├── HomeFragment.kt

│   │   └── HomeViewModel.kt

│   ├── customers/

│   │   ├── CustomerListFragment.kt

│   │   ├── CustomerDetailFragment.kt

│   │   └── CustomerViewModel.kt

│   ├── bills/

│   │   ├── BillListFragment.kt

│   │   ├── BillDetailFragment.kt

│   │   ├── NewBillFragment.kt

│   │   └── BillViewModel.kt

│   ├── reports/

│   │   ├── ReportsFragment.kt

│   │   └── ReportsViewModel.kt

│   └── common/

│       ├── BaseFragment.kt

│       ├── LoadingDialog.kt

│       └── adapters/

├── utils/

│   ├── Constants.kt

│   ├── NetworkUtils.kt

│   ├── CurrencyUtils.kt          ← ₹ Indian formatting

│   ├── DateUtils.kt              ← DD MMM YYYY

│   ├── PdfUtils.kt               ← Bill PDF generator

│   └── Extensions.kt            ← Kotlin extension functions

└── ShopApp.kt                    ← Application class

```



\---



\## PHASE 2: DESIGN SYSTEM



Build a complete design system FIRST before touching any screen.



\### 2.1 — colors.xml:



```xml

<!-- Primary Brand -->

<color name="primary">#1A56DB</color>

<color name="primary\_dark">#1046B8</color>

<color name="primary\_light">#EEF2FF</color>

<color name="primary\_container">#DBEAFE</color>



<!-- Semantic Colors -->

<color name="success">#16A34A</color>

<color name="success\_light">#DCFCE7</color>

<color name="danger">#DC2626</color>

<color name="danger\_light">#FEE2E2</color>

<color name="warning">#D97706</color>

<color name="warning\_light">#FEF3C7</color>



<!-- Neutrals -->

<color name="gray\_900">#111827</color>

<color name="gray\_700">#374151</color>

<color name="gray\_500">#6B7280</color>

<color name="gray\_300">#D1D5DB</color>

<color name="gray\_100">#F3F4F6</color>

<color name="white">#FFFFFF</color>



<!-- Background -->

<color name="bg\_primary">#F8FAFC</color>

<color name="bg\_card">#FFFFFF</color>

<color name="bg\_bottom\_nav">#FFFFFF</color>

```



\### 2.2 — typography.xml (Material Type Scale):



```xml

<style name="Text.Display" parent="TextAppearance.Material3.DisplaySmall">

&#x20;   <item name="fontFamily">@font/inter\_semibold</item>

&#x20;   <item name="android:textSize">32sp</item>

&#x20;   <item name="android:textColor">@color/gray\_900</item>

</style>

<style name="Text.Headline" parent="TextAppearance.Material3.HeadlineMedium">

&#x20;   <item name="fontFamily">@font/inter\_semibold</item>

&#x20;   <item name="android:textSize">20sp</item>

</style>

<style name="Text.Title" parent="TextAppearance.Material3.TitleMedium">

&#x20;   <item name="fontFamily">@font/inter\_medium</item>

&#x20;   <item name="android:textSize">16sp</item>

</style>

<style name="Text.Body" parent="TextAppearance.Material3.BodyMedium">

&#x20;   <item name="fontFamily">@font/inter\_regular</item>

&#x20;   <item name="android:textSize">14sp</item>

</style>

<style name="Text.Label" parent="TextAppearance.Material3.LabelSmall">

&#x20;   <item name="fontFamily">@font/inter\_regular</item>

&#x20;   <item name="android:textSize">12sp</item>

&#x20;   <item name="android:textColor">@color/gray\_500</item>

</style>

<style name="Text.Amount" parent="TextAppearance.Material3.HeadlineSmall">

&#x20;   <item name="fontFamily">@font/inter\_semibold</item>

&#x20;   <item name="android:textSize">24sp</item>

</style>

```



\### 2.3 — Theme (themes.xml):



```xml

<style name="Theme.ShopApp" parent="Theme.Material3.Light.NoActionBar">

&#x20;   <item name="colorPrimary">@color/primary</item>

&#x20;   <item name="colorPrimaryContainer">@color/primary\_container</item>

&#x20;   <item name="colorSurface">@color/bg\_card</item>

&#x20;   <item name="colorBackground">@color/bg\_primary</item>

&#x20;   <item name="colorError">@color/danger</item>

&#x20;   <item name="android:windowBackground">@color/bg\_primary</item>

&#x20;   <item name="android:statusBarColor">@color/white</item>

&#x20;   <item name="android:windowLightStatusBar">true</item>

</style>

```



\### 2.4 — Reusable Component Styles:



```xml

<!-- Card -->

<style name="Card.Default">

&#x20;   <item name="cardBackgroundColor">@color/bg\_card</item>

&#x20;   <item name="cardCornerRadius">12dp</item>

&#x20;   <item name="cardElevation">0dp</item>

&#x20;   <item name="strokeColor">@color/gray\_300</item>

&#x20;   <item name="strokeWidth">1dp</item>

&#x20;   <item name="contentPadding">16dp</item>

</style>



<!-- Primary Button -->

<style name="Button.Primary" parent="Widget.Material3.Button">

&#x20;   <item name="backgroundTint">@color/primary</item>

&#x20;   <item name="android:textColor">@color/white</item>

&#x20;   <item name="cornerRadius">8dp</item>

&#x20;   <item name="android:paddingTop">14dp</item>

&#x20;   <item name="android:paddingBottom">14dp</item>

&#x20;   <item name="android:textSize">15sp</item>

&#x20;   <item name="android:fontFamily">@font/inter\_semibold</item>

</style>



<!-- Outlined Button -->

<style name="Button.Outlined" parent="Widget.Material3.Button.OutlinedButton">

&#x20;   <item name="strokeColor">@color/primary</item>

&#x20;   <item name="android:textColor">@color/primary</item>

&#x20;   <item name="cornerRadius">8dp</item>

</style>



<!-- Status Badge — Paid -->

<style name="Badge.Paid">

&#x20;   <item name="android:background">@drawable/bg\_badge\_green</item>

&#x20;   <item name="android:textColor">@color/success</item>

&#x20;   <item name="android:textSize">11sp</item>

&#x20;   <item name="android:paddingStart">8dp</item>

&#x20;   <item name="android:paddingEnd">8dp</item>

&#x20;   <item name="android:paddingTop">3dp</item>

&#x20;   <item name="android:paddingBottom">3dp</item>

</style>

```



\---



\## PHASE 3: SCREEN-BY-SCREEN REDESIGN



\### SCREEN 1: LOGIN

```

Layout: Full screen, white background

Top 35%:  App logo (vector) + App name + tagline

&#x20;         "Apni Dukaan, Apna Hisaab"

Middle:   Login card (rounded 16dp, subtle shadow)

&#x20; - "Phone Number" TextInputLayout (outlined)

&#x20;   Prefix: +91 | Input type: phone

&#x20; - "PIN" TextInputLayout (outlined)

&#x20;   4-digit PIN with show/hide toggle

&#x20; - "Login" Button (full width, primary style)

&#x20; - "Forgot PIN?" text button (centered)

Bottom:   Version number in gray



Validations:

&#x20; - Phone: exactly 10 digits, starts with 6-9

&#x20; - PIN: exactly 4 digits

&#x20; - Show inline error under each field

&#x20; - Disable login button while loading

&#x20; - Show CircularProgressIndicator inside button

```



\### SCREEN 2: HOME / DASHBOARD

```

Top Bar:

&#x20; - Shop name (left, bold)

&#x20; - Notification bell icon (right)

&#x20; - Staff avatar with initials (right)



Summary Cards (2x2 grid, MaterialCardView):

&#x20; Card 1 — Total Outstanding  (red icon)

&#x20; Card 2 — Today's Collection (green icon)

&#x20; Card 3 — Pending Bills      (amber icon)

&#x20; Card 4 — Active Customers   (blue icon)

&#x20; Each card: icon + label (12sp gray) + value (28sp bold)



Recent Transactions (RecyclerView):

&#x20; Section header: "Recent Transactions" + "See All" link

&#x20; Each row: avatar initials | customer name | amount

&#x20;           (green = credit, red = debit) | time ago

&#x20; Empty state: illustration + "No transactions yet"

&#x20; Pull to refresh: SwipeRefreshLayout

&#x20; Loading state: ShimmerFrameLayout (3 placeholder rows)

```



\### SCREEN 3: KHATA / CUSTOMERS

```

Top: SearchView (real-time filter)

&#x20;    Filter chips: All | High Balance | Cleared



Customer Grid (RecyclerView — vertical list):

&#x20; Each card (MaterialCardView):

&#x20;   - Avatar circle: colored bg + initials (2 letters)

&#x20;   - Name (16sp bold) + Phone (13sp gray)

&#x20;   - Outstanding: large amount

&#x20;     RED (#DC2626) if balance > 0

&#x20;     GREEN (#16A34A) if balance = 0

&#x20;   - Last transaction date (12sp gray)

&#x20;   - Quick actions: \[View Khata] \[Add Entry]



FAB (bottom right): + Add Customer

&#x20; Opens BottomSheetDialog:

&#x20;   - Name field (required)

&#x20;   - Phone field (required, 10 digits)

&#x20;   - Opening balance field (optional)

&#x20;   - Save button



Empty state: illustration + "Koi customer nahi mila"

Loading: Shimmer cards (3 placeholders)

```



\### SCREEN 4: CUSTOMER DETAIL

```

Header (colored bg — primary color):

&#x20; - Back arrow

&#x20; - Customer name (20sp white bold)

&#x20; - Phone number (14sp white)

&#x20; - Outstanding amount (32sp white bold)

&#x20; - "BAAKI HAI" or "CLEAR" badge



Action Buttons (horizontal row below header):

&#x20; \[+ Credit]  \[- Debit]  \[New Bill]  \[WhatsApp]



TabLayout (2 tabs):

&#x20; TAB 1 — LEDGER:

&#x20;   RecyclerView of all transactions

&#x20;   Each row: date | type badge | amount | note | staff name

&#x20;   Credit rows: green amount

&#x20;   Debit rows: red amount



&#x20; TAB 2 — BILLS:

&#x20;   RecyclerView of all bills

&#x20;   Each row: Bill# | date | amount | status badge

&#x20;   Status: Paid (green) | Unpaid (red) | Partial (amber)

&#x20;   Click → Bill Detail screen

```



\### SCREEN 5: BILLS LIST

```

Top: 3 mini stat chips

&#x20; Total Bills | Paid Amount | Unpaid Amount



Filter Tabs: All | Paid | Unpaid | This Month



RecyclerView:

&#x20; Each bill card:

&#x20;   - Bill # (bold) + Customer name

&#x20;   - Date (DD MMM YYYY)

&#x20;   - Item count ("3 items")

&#x20;   - Total amount (bold)

&#x20;   - Status badge (colored pill)

&#x20;   - Actions: View (eye) | Mark Paid (check) | Share (share)



Empty state per filter tab

Loading: Shimmer placeholders

FAB: + New Bill

```



\### SCREEN 6: BILL DETAIL / INVOICE

```

Toolbar: "Invoice #B-001" + Share + Print icons



Invoice Card (printable layout):

&#x20; ┌─────────────────────────────┐

&#x20; │  \[SHOP LOGO]                │

&#x20; │  Sharma General Store       │

&#x20; │  Main Bazaar, Farrukhabad   │

&#x20; │  Ph: 9999999999             │

&#x20; │─────────────────────────────│

&#x20; │  INVOICE          #B-001    │

&#x20; │  Date: 24 May 2025          │

&#x20; │─────────────────────────────│

&#x20; │  Bill To:                   │

&#x20; │  Ramesh Kumar               │

&#x20; │  Ph: 9876543210             │

&#x20; │─────────────────────────────│

&#x20; │  Item      Qty  Rate   Amt  │

&#x20; │  Atta 5kg   2   250   500  │

&#x20; │  Sugar 1kg  1   55     55  │

&#x20; │─────────────────────────────│

&#x20; │  Subtotal:         ₹555    │

&#x20; │  Tax (0%):           ₹0    │

&#x20; │  TOTAL:            ₹555    │

&#x20; │─────────────────────────────│

&#x20; │  Status: ● UNPAID           │

&#x20; └─────────────────────────────┘



Bottom Buttons (if unpaid):

&#x20; \[Mark as Paid]  \[Share PDF]

```



\### SCREEN 7: NEW BILL

```

Toolbar: "New Bill" + Save (checkmark icon)



Customer selector (AutoCompleteTextView):

&#x20; Type to search → dropdown shows matching customers



Items section:

&#x20; Header: "Items" + \[+ Add Item] button

&#x20; Each item row:

&#x20;   - Item name (EditText)

&#x20;   - Qty (number input)

&#x20;   - Rate ₹ (number input)

&#x20;   - Amount (auto-calculated, read-only)

&#x20;   - Delete (X) icon

&#x20; Total: auto-updates as items are added/changed



Notes field (optional, multiline)



Generate Bill button (full width, primary)

&#x20; Loading state while API call is in progress

```



\### SCREEN 8: REPORTS

```

Date Range Picker:

&#x20; \[From Date]  →  \[To Date]  \[Apply]



Charts (MPAndroidChart):

&#x20; 1. Bar Chart: Daily collections (last 7 days)

&#x20; 2. Donut Chart: Paid vs Unpaid bills

&#x20; 3. Line Chart: Customer growth over time



Summary Table:

&#x20; Top 10 customers by outstanding

&#x20; Columns: Rank | Customer | Billed | Paid | Outstanding



Export button: "Export CSV" (shares via Android intent)

```



\---



\## PHASE 4: ANDROID-SPECIFIC REQUIREMENTS



\### 4.1 — Network Layer:

```kotlin

// AuthInterceptor — adds JWT to every request

class AuthInterceptor(private val prefs: EncryptedSharedPreferences) : Interceptor {

&#x20;   override fun intercept(chain: Interceptor.Chain): Response {

&#x20;       val token = prefs.getString("jwt\_token", null)

&#x20;       val request = chain.request().newBuilder()

&#x20;           .addHeader("Authorization", "Bearer $token")

&#x20;           .build()

&#x20;       return chain.proceed(request)

&#x20;   }

}



// NetworkUtils — check before every API call

object NetworkUtils {

&#x20;   fun isConnected(context: Context): Boolean {

&#x20;       val cm = context.getSystemService(Context.CONNECTIVITY\_SERVICE) as ConnectivityManager

&#x20;       return cm.activeNetwork != null

&#x20;   }

}

```



\### 4.2 — BaseFragment (all fragments extend this):

```kotlin

abstract class BaseFragment : Fragment() {

&#x20;   fun showLoading() { /\* show LoadingDialog \*/ }

&#x20;   fun hideLoading() { /\* hide LoadingDialog \*/ }

&#x20;   fun showSuccess(msg: String) { Snackbar.make(requireView(), msg, Snackbar.LENGTH\_SHORT)

&#x20;       .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.success)).show() }

&#x20;   fun showError(msg: String) { Snackbar.make(requireView(), msg, Snackbar.LENGTH\_LONG)

&#x20;       .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.danger)).show() }

&#x20;   fun checkNetwork(): Boolean {

&#x20;       if (!NetworkUtils.isConnected(requireContext())) {

&#x20;           showError("Internet connection nahi hai")

&#x20;           return false

&#x20;       }

&#x20;       return true

&#x20;   }

}

```



\### 4.3 — CurrencyUtils:

```kotlin

object CurrencyUtils {

&#x20;   fun format(amount: Double): String {

&#x20;       val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

&#x20;       return formatter.format(amount) // outputs ₹1,00,000.00

&#x20;   }

&#x20;   fun formatShort(amount: Double): String {

&#x20;       return when {

&#x20;           amount >= 100000 -> "₹${String.format("%.1f", amount/100000)}L"

&#x20;           amount >= 1000   -> "₹${String.format("%.1f", amount/1000)}K"

&#x20;           else             -> "₹${amount.toInt()}"

&#x20;       }

&#x20;   }

}

```



\### 4.4 — WhatsApp Quick Send:

```kotlin

fun sendWhatsAppMessage(phone: String, message: String) {

&#x20;   val url = "https://wa.me/91$phone?text=${Uri.encode(message)}"

&#x20;   val intent = Intent(Intent.ACTION\_VIEW, Uri.parse(url))

&#x20;   startActivity(intent)

}

// Usage on Customer Detail screen:

sendWhatsAppMessage(

&#x20;   customer.phone,

&#x20;   "Namaste ${customer.name} ji 🙏\\nAapka baaki: ${CurrencyUtils.format(customer.outstanding)}\\n- Sharma General Store"

)

```



\### 4.5 — Shimmer Loading (example for customer list):

```xml

<com.facebook.shimmer.ShimmerFrameLayout

&#x20;   android:id="@+id/shimmerLayout"

&#x20;   android:layout\_width="match\_parent"

&#x20;   android:layout\_height="wrap\_content"

&#x20;   app:shimmer\_duration="1000">

&#x20;   <!-- repeat placeholder card 3 times -->

&#x20;   <include layout="@layout/item\_customer\_placeholder"/>

&#x20;   <include layout="@layout/item\_customer\_placeholder"/>

&#x20;   <include layout="@layout/item\_customer\_placeholder"/>

</com.facebook.shimmer.ShimmerFrameLayout>

```



\---



\## PHASE 5: BACKEND — DO NOT BREAK



\- All existing API endpoints on Render.com must keep working

\- BASE\_URL must be in `Constants.kt` only — not hardcoded anywhere else

\- If new endpoints are needed, list them — I will add to backend

\- Backend already handles: auth, customers, transactions, bills



```kotlin

object Constants {

&#x20;   const val BASE\_URL = "https://your-app.onrender.com/api/"

&#x20;   const val PREF\_TOKEN = "jwt\_token"

&#x20;   const val PREF\_STAFF\_ID = "staff\_id"

&#x20;   const val PREF\_STAFF\_NAME = "staff\_name"

}

```



\---



\## PHASE 6: NEW FEATURES TO ADD



\### 6.1 — Staff Login with PIN

\- Phone + 4-digit PIN screen

\- POST `/api/auth/login` → receive JWT token

\- Save token in `EncryptedSharedPreferences`

\- Auto-login if token valid (check on splash screen)



\### 6.2 — Global Search

\- Search icon in HomeFragment toolbar

\- SearchView expands full width

\- Searches customers AND bills simultaneously

\- Results in 2 sections: Customers | Bills



\### 6.3 — PDF Bill Generation + Share

\- Generate PDF invoice using iText7

\- Save to app's external cache directory

\- Share via Android share intent (WhatsApp, email, etc.)



\### 6.4 — Offline Indicator

\- Persistent banner at top when no internet

\- "Aap offline hain — data refresh nahi hoga"

\- Banner auto-hides when connection restored



\### 6.5 — Pull to Refresh

\- All list screens must support SwipeRefreshLayout

\- Refresh color: primary blue

\- Show last updated time below toolbar



\---



\## HOW TO WORK WITH ME



1\. First show me the audit report (Phase 0)

2\. Ask: "Kahan se shuru karein?" before any changes

3\. Make ONE screen change at a time

4\. After each screen, tell me exactly what to test on device

5\. Never delete working code — comment with `// OLD:` if replacing

6\. If anything is unclear — ASK, do not assume

7\. Keep all existing API calls intact



\---



\## TECH STACK SUMMARY



| Layer | Technology |

|---|---|

| Language | Kotlin |

| UI | Material Design 3 (Material You) |

| Architecture | MVVM + Repository pattern |

| Navigation | Navigation Component (single Activity) |

| Network | Retrofit2 + OkHttp + Coroutines |

| Charts | MPAndroidChart |

| Loading | Facebook Shimmer |

| PDF | iText7 |

| Storage | EncryptedSharedPreferences + DataStore |

| Backend | Already deployed on Render.com |

| Min SDK | 24 (Android 7.0) |

| Target SDK | 34 (Android 14) |



\---



\*Inspired by Vyapar \& MyBillBook — built for Indian shop owners\*

