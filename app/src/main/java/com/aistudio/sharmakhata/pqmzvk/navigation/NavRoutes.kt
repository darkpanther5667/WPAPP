package com.aistudio.sharmakhata.pqmzvk.navigation

object NavRoutes {
    // Auth
    const val LOGIN = "login"
    const val REGISTER_STORE = "register_store"
    const val LOADING = "loading"

    // Home
    const val HOME = "home"

    // Customers
    const val CUSTOMERS = "customers"
    const val CUSTOMER_DETAIL = "customer_detail/{customerId}"
    const val CUSTOMER_LEDGER = "customer_ledger/{customerId}"
    const val ADD_CUSTOMER = "add_customer"
    const val SEARCH = "search"

    // Bills
    const val BILLS = "bills"
    const val BILL_DETAIL = "bill_detail/{billId}"
    const val CREATE_BILL = "create_bill?customerId={customerId}&quickMode={quickMode}"
    const val PDF_VIEWER = "pdf_viewer/{billId}"

    // Inventory
    const val INVENTORY = "inventory"
    const val ADD_EDIT_ITEM = "add_edit_item?itemId={itemId}"
    const val EDIT_ITEM = "edit_item/{itemId}"
    const val PURCHASES = "purchases"
    const val ADD_PURCHASE = "add_purchase"
    const val PURCHASE_DETAIL = "purchase_detail/{id}"

    // Reports
    const val REPORTS = "reports"
    const val ADD_EXPENSE = "add_expense"

    // Web / PDF
    const val WEBVIEW = "webview"
    const val WEBVIEW_PDF = "webview_pdf/{billId}"

    // Quick bill (legacy)
    const val QUICK_BILL = "quick_bill"

    // Settings & Admin
    const val STAFF = "staff"
    const val INVOICE_TEMPLATES = "invoice_templates"

    fun customerDetail(customerId: String) = "customer_detail/$customerId"
    fun customerLedger(customerId: String) = "customer_ledger/$customerId"
    fun billDetail(billId: String) = "bill_detail/$billId"
    fun createBill(customerId: String? = null, quickMode: Boolean = false) =
        "create_bill?customerId=${customerId ?: ""}&quickMode=$quickMode"
    fun pdfViewer(billId: String) = "pdf_viewer/$billId"
    fun editItem(itemId: Long) = "edit_item/$itemId"
    fun purchaseDetail(id: Long) = "purchase_detail/$id"
    fun webviewPdf(billId: String) = "webview_pdf/$billId"
    fun addEditItem(itemId: Long? = null) =
        "add_edit_item?itemId=${itemId ?: ""}"
}
