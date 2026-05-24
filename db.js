const { MongoClient } = require('mongodb');

// MongoDB connection string must be supplied via environment variable to avoid committing secrets.
const MONGODB_URI = process.env.MONGODB_URI;
if (!MONGODB_URI) {
  console.warn('⚠️ MONGODB_URI not set. Set it in .env or environment variables.');
}

let client;
let db;

async function connectDB() {
  if (!client) {
    client = new MongoClient(MONGODB_URI);
    await client.connect();
    db = client.db('sharma_store');
    console.log('✅ Connected to MongoDB Atlas');
  }
  return db;
}

// Helper to get backwards compatible full DB dump for dashboard.html and regex fallback
async function getFullDB() {
  const database = await connectDB();
  const shop = await database.collection('shop').findOne({});
  const customers = await database.collection('customers').find({}).toArray();
  const transactions = await database.collection('transactions').find({}).toArray();
  const bills = await database.collection('bills').find({}).toArray();
  const staff = await database.collection('staff').find({}).toArray();

  return { shop, customers, transactions, bills, staff };
}

module.exports = {
  connectDB,
  getFullDB
};
