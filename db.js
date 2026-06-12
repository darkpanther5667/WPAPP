const { MongoClient } = require('mongodb');
const fs = require('fs');
const path = require('path');

// MongoDB connection string must be supplied via environment variable to avoid committing secrets.
const MONGODB_URI = process.env.MONGODB_URI;
const DB_FILE = path.join(__dirname, 'db.json');

let client;
let db;
let useLocalFallback = false;

async function connectDB() {
  if (!MONGODB_URI) {
    useLocalFallback = true;
    console.warn('⚠️ MONGODB_URI not set. Using local db.json fallback.');
    return null;
  }
  
  if (!client) {
    client = new MongoClient(MONGODB_URI);
    await client.connect();
    db = client.db('sharma_store');
    console.log('✅ Connected to MongoDB Atlas');

    // Create indexes for query performance
    try {
      await Promise.all([
        db.collection('customers').createIndex({ store_id: 1, id: 1 }),
        db.collection('customers').createIndex({ phone: 1 }),
        db.collection('customers').createIndex({ store_id: 1, name: 1 }),
        db.collection('transactions').createIndex({ customer_id: 1 }),
        db.collection('transactions').createIndex({ store_id: 1, timestamp: -1 }),
        db.collection('bills').createIndex({ customer_id: 1, status: 1 }),
        db.collection('bills').createIndex({ store_id: 1 }),
        db.collection('bills').createIndex({ id: 1, store_id: 1 }),
        db.collection('staff').createIndex({ phone: 1, store_id: 1 }),
        db.collection('staff').createIndex({ store_id: 1, status: 1 }),
        db.collection('items').createIndex({ store_id: 1 }),
        db.collection('items').createIndex({ store_id: 1, name: 1 }),
        db.collection('sessions').createIndex({ token: 1 }),
        db.collection('sessions').createIndex({ expires_at: 1 }, { expireAfterSeconds: 0 }),
        db.collection('sessions').createIndex({ store_id: 1 }),
        db.collection('login_codes').createIndex({ phone: 1, expires_at: 1 }),
        db.collection('login_codes').createIndex({ store_id: 1 }),
        db.collection('login_codes').createIndex({ expires_at: 1 }, { expireAfterSeconds: 0 }),
      ]);
      console.log('✅ MongoDB indexes ensured');
    } catch (e) {
      console.warn('⚠️ Index creation failed (non-fatal):', e.message);
    }
  }
  return db;
}

// Helper to get backwards compatible full DB dump for dashboard.html and regex fallback
async function getFullDB() {
  // Fallback to local db.json when MONGODB_URI is not set or connectDB() returns null
  const localFallback = () => {
    try {
      const data = fs.readFileSync(DB_FILE, 'utf8');
      return JSON.parse(data);
    } catch (error) {
      console.error('Error reading local db.json:', error);
      return { shop: {}, customers: [], transactions: [], bills: [], staff: [], stores: [], items: [] };
    }
  };

  if (useLocalFallback) {
    return localFallback();
  }

  const database = await connectDB();
  if (!database) {
    useLocalFallback = true;
    return localFallback();
  }

  const shop = await database.collection('shop').findOne({});
  const customers = await database.collection('customers').find({}).toArray();
  const transactions = await database.collection('transactions').find({}).toArray();
  const bills = await database.collection('bills').find({}).toArray();
  const staff = await database.collection('staff').find({}).toArray();
  const stores = await database.collection('stores').find({}).toArray();
  const items = await database.collection('items').find({}).toArray();

  return { shop, customers, transactions, bills, staff, stores, items };
}

module.exports = {
  connectDB,
  getFullDB
};
