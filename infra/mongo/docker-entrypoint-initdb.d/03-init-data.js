// --- PRODUCT SERVICE ---
db = db.getSiblingDB("productdb");
db.products.insertMany([
  { uuid: "2875bef3-d47c-4498-a15a-4d0c6c3f0e06", name: "Laptop", category: "Electronics", price: 1200, stock:10 },
  { uuid: "bf6160a1-eb6c-4307-800a-0669f0aa4d2f", name: "Phone", category: "Electronics", price: 800, stock:10 }
]);

// --- ORDER SERVICE ---
db = db.getSiblingDB("orderdb");
db.orders.insertMany([
  { uuid: "dee4f548-29ec-4c96-8beb-c4a445e3c6fe", userId: "1", productId: "1", quantity: 1, status: "CREATED", createdAt: new Date() },
  { uuid: "7fa9f6e1-a13a-4a58-9963-d844bb893cda", userId: "2", productId: "2", quantity: 2, status: "CREATED", createdAt: new Date() }
]);