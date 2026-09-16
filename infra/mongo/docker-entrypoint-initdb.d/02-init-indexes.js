// --- USER SERVICE ---
db = db.getSiblingDB("userdb");
db.users.createIndex({ email: 1 }, { unique: true }); // unique emails
db.users.createIndex({ username: 1 }); // quick search by username

// --- PRODUCT SERVICE ---
db = db.getSiblingDB("productdb");
db.products.createIndex({ name: 1 }); // quick search by name
db.products.createIndex({ category: 1 }); // for filters

// --- ORDER SERVICE ---
db = db.getSiblingDB("orderdb");
db.orders.createIndex({ userId: 1 }); // search orders by userId
db.orders.createIndex({ status: 1 }); // quick search by status
db.orders.createIndex({ createdAt: -1 }); // for sorting

// --- NOTIF SERVICE ---
// user_projections uses userId as _id — unique by default, no extra index needed