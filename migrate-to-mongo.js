const fs = require('fs');
const { MongoClient } = require('mongodb');

// Bypass SRV record due to local DNS block by using explicit direct replica set string
const uri = "mongodb://clashhmrri_db_user:KyDmI2m0Y50E3czc@ac-gesi8dm-shard-00-00.cmciyqa.mongodb.net:27017,ac-gesi8dm-shard-00-01.cmciyqa.mongodb.net:27017,ac-gesi8dm-shard-00-02.cmciyqa.mongodb.net:27017/?ssl=true&replicaSet=atlas-3ui4o6-shard-0&authSource=admin&retryWrites=true&w=majority&appName=Cluster0";
const client = new MongoClient(uri);
const dbName = "sharma_store";

async function migrate() {
  try {
    console.log("⏳ Connecting to MongoDB Atlas...");
    await client.connect();
    console.log("✅ Connected successfully!");

    const db = client.db(dbName);
    
    // Read local db.json
    console.log("⏳ Reading local db.json...");
    const rawData = fs.readFileSync('db.json', 'utf-8');
    const localDb = JSON.parse(rawData);

    // Prepare collections
    const collections = ['shop', 'customers', 'transactions', 'bills', 'staff'];
    
    for (const colName of collections) {
      if (localDb[colName]) {
        console.log(`⏳ Migrating collection: ${colName}...`);
        const collection = db.collection(colName);
        
        // Clear existing data in collection (optional, but good for clean slate)
        await collection.deleteMany({});
        
        let dataToInsert = localDb[colName];
        
        // Ensure dataToInsert is an array (shop might be an object)
        if (!Array.isArray(dataToInsert)) {
          dataToInsert = [dataToInsert];
        }
        
        if (dataToInsert.length > 0) {
          const result = await collection.insertMany(dataToInsert);
          console.log(`✅ Migrated ${result.insertedCount} documents into ${colName}`);
        } else {
          console.log(`ℹ️ Collection ${colName} is empty in local db.json.`);
        }
      }
    }

    console.log("🎉 Migration completed successfully!");

  } catch (error) {
    console.error("❌ Migration failed:", error);
  } finally {
    await client.close();
  }
}

migrate();
