const { MongoClient } = require('mongodb');

// Bypass SRV record due to local DNS block by using explicit direct replica set string
const MONGODB_URI = "mongodb://clashhmrri_db_user:KyDmI2m0Y50E3czc@ac-gesi8dm-shard-00-00.cmciyqa.mongodb.net:27017,ac-gesi8dm-shard-00-01.cmciyqa.mongodb.net:27017,ac-gesi8dm-shard-00-02.cmciyqa.mongodb.net:27017/?ssl=true&replicaSet=atlas-3ui4o6-shard-0&authSource=admin&retryWrites=true&w=majority&appName=Cluster0";

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
