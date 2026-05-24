# Sharma Store Billing & Khata Management System

A high-fidelity shop keeping application containing a **WhatsApp Webhook Bot API** and a **Web Dashboard** to easily track credit (उधार), receipts (जमा), and invoices (बिल).

---

## Technical Architecture

1. **WhatsApp Bot Logic (Node.js)**: Built on Express and Axios. Connects directly to Meta's WhatsApp Cloud API. Translates voice/text commands in Hinglish/English style and updates the shared ledger.
2. **Interactive Commercial Web Dashboard (Single File)**: A client-rendered Material-inspired web console styled in Tailwind CSS & Chart.js. Allows stores to visualize daily collections, manage Khata accounts, mark invoice statuses, and test messages using a real-time active **WhatsApp Simulator**.

---

## 📂 Deliverables Included

- `server.js` — Node.js WhatsApp webhook router & bot logic
- `dashboard.html` — Standalone, high-fidelity browser console with embedded simulation
- `db.json` — Shared local JSON database template initialized with sample data
- `/app` — Fully prepared Android environment loading the web console locally inside a viewport container.

---

## PART 1: WhatsApp Bot Setup & Run

### 1. Register with Meta Developer Portal
To connect your physical phone/number to the Meta WhatsApp Cloud API:
1. Navigate to the **[Meta Developers Portal](https://developers.facebook.com/)** and log in.
2. Click **Create App**, select **Other** type, and choose **Business** portfolio.
3. Under the dashboard services, locate **WhatsApp** and click **Set Up**.
4. Inside the WhatsApp sidebar, click **API Setup** to obtain:
   - **Temporary Access Token** (or configure a Permanent System User Token inside Business Settings)
   - **Phone Number ID** (a 15-digit unique ID)

### 2. Configure Environment Secrets
Create a `.env` file in your server folder (or use environment variables) to match the credentials of your Meta dashboard:
```bash
WHATSAPP_TOKEN=your_meta_system_user_token
PHONE_NUMBER_ID=your_whatsapp_phone_number_id
VERIFY_TOKEN=sharma_store_token   # Match this with your Webhook webhook portal settings
PORT=3000
```

### 3. Expose Your Local Webhook Port via Ngrok
The WhatsApp Cloud Webhook API requires a public HTTPS absolute URL destination to push incoming receipts to:
1. Install [ngrok](https://ngrok.com/) on your host computer.
2. Run this command in your command console to expose the local server:
   ```bash
   ngrok http 3000
   ```
3. Copy the secure forwarding address provided (e.g., `https://xxxx-xx-xx.ngrok-free.app`).

### 4. Link Webhook in Meta developer Portal
1. Under Meta WhatsApp sidebar navigation, click **Configuration**.
2. Click **Callback URL Edit**, enter:
   - **Callback URL**: `https://xxxx-xx-xx.ngrok-free.app/webhook`
   - **Verify Token**: `sharma_store_token` (as configured in step #2)
3. Click **Verify and Save**.
4. Under **Webhook Fields**, press **Manage** and subscribe to **`messages`** notifications to receive real-time updates.

---

## PART 2: How to Run the WhatsApp Bot & Web Console

### 1. Launching the WhatsApp Webhook server:
From your local folder:
```bash
# Initialize node dependencies
npm install express axios dotenv

# Start the webhook server
node server.js
```
The server will boot up and wait for real-time WhatsApp Cloud messages on port `3000`.

### 2. Opening the Web Dashboard:
No complex server is required to use the beautiful management dashboard.
1. Simply double-click on `dashboard.html` directly in any web browser!
2. All additions, deletions, updates, invoice markings, and WhatsApp Simulator inputs will store safely inside your local browser `localStorage` to give a robust developer integration trial experience!

### 3. Running as an Android App:
For your convenience, the dashboard has been packaged and configured inside our ready-to-run Android App framework (under `/app` folder assets loading edge-to-edge inside `/app/src/main/java/com/example/MainActivity.kt`'s WebView viewport). You can deploy, build, or preview the APK instantly on Android devices or Android Cloud Emulator stream!

---

## 💬 Supported Bot Conversational Commands (Hindi / English)

Our advanced keyword matcher supports rich mix-phrasing:
- **Outstanding Check**: _"Ramesh ka kitna baaki hai"_ or _"Ramesh outstanding check"_
- **Add Ledger Credit**: _"Ramesh ka khata mein 100 daal do"_ or _"add 100 to Ramesh khata"_
- **Fast Invoice Create**: _"Suresh ka 500 ka bill banao"_
- **Add Line Items**: _"Ramesh ka 50 mein Chini add karo"_ or _"Suresh ka soap 180 add karo"_
- **Check Store Collections**: _"Aaj ki sale kitni thi"_ or _"today total sale"_
- **Send Invoice Sheet**: _"Ramesh ka bill bhej do"_

---

## 🇮🇳 India Format Standards Applied

- All values are automatically parsed and displayed in **Indian Rupees (₹)**.
- Calendar selectors, receipts, and table column data adhere strictly to the Indian Standard date formatting (**DD/MM/YYYY**).
- Full compatibility with unicode characters ensuring names, memo notes, and descriptions in **Hindi / English** render clean and professional.
