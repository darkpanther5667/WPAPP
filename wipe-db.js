require('dotenv').config();
const { MongoClient } = require('mongodb');

async function wipeDatabase() {
  if (!process.env.MONGODB_URI) {
    console.error('Missing MONGODB_URI');
    process.exit(1);
  }

  const client = new MongoClient(process.env.MONGODB_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
  });

  try {
    await client.connect();
    const db = client.db('invoicing');
    console.log('Connected to MongoDB. Wiping collections...');

    const collections = ['staff', 'stores', 'customers', 'transactions', 'bills', 'sessions', 'login_codes'];
    
    for (const col of collections) {
      await db.collection(col).deleteMany({});
      console.log(`Cleared ${col} collection.`);
    }

    console.log('Database wiped completely.');
  } catch (e) {
    console.error(e);
  } finally {
    await client.close();
  }
}

wipeDatabase();
