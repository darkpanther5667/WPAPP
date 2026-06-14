export interface Customer {
  id: string;
  name: string;
  phone?: string;
  created_at?: string;
}

export interface Shop {
  name?: string;
  owner?: string;
  address?: string;
}

export interface Transaction {
  id: string;
  customer_id: string;
  type: "payment" | "credit";
  amount: number;
  note?: string;
  staff_phone?: string;
  timestamp: string;
  payment_mode?: string;
}

export interface BillItem {
  name: string;
  qty: number;
  price: number;
  hsn_code?: string;
  gst_rate?: number;
  taxable?: number;
  cgst?: number;
  sgst?: number;
  igst?: number;
  total_with_tax?: number;
}

export interface Bill {
  id: string;
  customer_id: string;
  items: BillItem[];
  total: number;
  status: string;
  created_at: string;
  paid_at?: string;
  gst_type?: string;
  gst_rate?: number;
  taxable_amount?: number;
  total_cgst?: number;
  total_sgst?: number;
  total_igst?: number;
  grand_total?: number;
  invoice_number?: string;
}

export interface Staff {
  id: string;
  name: string;
  phone: string;
}

export interface Store {
  id: string;
  store_name: string;
  owner_name: string;
  phone: string;
  email: string;
  business_type: string;
  plan: string;
  address?: string;
  created_at: string;
  status: string;
  invoice_template?: string;
  upi_id?: string;
  gstin?: string;
}

export interface StoredItem {
  id: string;
  name: string;
  price: number;
  stock: number;
  gst_rate?: number;
  sku?: string;
  hsn?: string;
  unit?: string;
  description?: string;
  purchase_price?: number;
  created_at: string;
}

export interface FullDatabase {
  shop?: Shop;
  customers: Customer[];
  transactions: Transaction[];
  bills: Bill[];
  staff: Staff[];
  items?: StoredItem[];
  expenses?: Expense[];
  purchases?: Purchase[];
}

export interface Expense {
  id: string;
  title: string;
  amount: number;
  category: string;
  note?: string;
  created_at: string;
}

export interface PurchaseItem {
  name: string;
  qty: number;
  price: number;
  amount: number;
}

export interface Purchase {
  id: string;
  supplierName: string;
  supplierPhone?: string;
  totalAmount: number;
  paidAmount: number;
  status: "paid" | "unpaid" | "partial";
  items: PurchaseItem[];
  notes?: string;
  created_at: string;
  updated_at?: string;
}

export interface DailyReport {
  date: string;
  billsTotal: number;
  paymentTotal: number;
  billsCount: number;
  outstanding?: { name: string; phone?: string; balance: number }[];
}

export interface LoginResponse {
  success: boolean;
  token: string;
  store: Store;
  message?: string;
}

export interface RegisterResponse {
  status: string;
  store_id: string;
  message: string;
}

export interface ApiError {
  success: false;
  message: string;
}
