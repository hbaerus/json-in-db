

var oracledb = require('oracledb');

function requiredEnv(name) {
  var value = process.env[name];
  if (!value) {
    throw new Error(name + ' must be set');
  }
  return value;
}

var config = {
  user: requiredEnv('NODE_ORACLEDB_USER'),
  password: requiredEnv('NODE_ORACLEDB_PASSWORD'),
  connectString : requiredEnv('NODE_ORACLEDB_CONNECTIONSTRING'),
  poolMin: 10,
  poolMax: 10,
  poolIncrement: 0
}

var ALLOWED_QBE_FIELDS = {
  name: true,
  notes: true,
  price: true,
  region: true,
  type: true
};

function parseQbe(qbe) {
  if (qbe == null || qbe === '') {
    return null;
  }

  var parsed;
  try {
    parsed = JSON.parse(qbe);
  } catch (err) {
    err.statusCode = 400;
    throw err;
  }

  validateQbe(parsed);
  return parsed;
}

function validateQbe(qbe) {
  if (qbe == null || Array.isArray(qbe) || typeof qbe !== 'object') {
    var err = new Error('QBE filter must be an object');
    err.statusCode = 400;
    throw err;
  }

  Object.keys(qbe).forEach(function(key) {
    if (key.charAt(0) === '$' || !ALLOWED_QBE_FIELDS[key]) {
      var err = new Error('QBE filter contains an unsupported field');
      err.statusCode = 400;
      throw err;
    }
    if (qbe[key] != null && typeof qbe[key] === 'object') {
      var err = new Error('QBE filter values must be scalar');
      err.statusCode = 400;
      throw err;
    }
  });
}

async function initialize() {
  var conn;
  oracledb.autoCommit = true;
  await oracledb.createPool(config);
  try {
    conn = await oracledb.getConnection();
    var soda = conn.getSodaDatabase();
    var collection = await soda.createCollection('wines');
    await collection.createIndex({ "name" : "WINE_IDX" });
  } finally {
    if (conn) {
      await conn.close();
    }
  }
}

async function close() {
  await oracledb.getPool().close();
}

async function get(qbe) {
  var conn;
  try {
    conn = await oracledb.getConnection();
    var collection = await getCollection(conn);
    var builder = collection.find();
    var parsedQbe = parseQbe(qbe);
    if (parsedQbe != null) {
      builder.filter(parsedQbe);
    }
    var docs = await builder.getDocuments();
    return toJSON(docs);
  } finally {
    if (conn) {
      await conn.close();
    }
  }
}

async function update(id, review) {
  delete review.id;
  var conn;
  try {
    conn = await oracledb.getConnection();
    var collection = await getCollection(conn);
    return await collection.find().key(id).replaceOne(review);
  } finally {
    if (conn) {
      await conn.close();
    }
  }
}

async function create(review) {
  var conn;
  try {
    conn = await oracledb.getConnection();
    var collection = await getCollection(conn);
    var result = await collection.insertOneAndGet(review);
    return result.key;
  } finally {
    if (conn) {
      await conn.close();
    }
  }
}

async function remove(id) {
  var conn;
  try {
    conn = await oracledb.getConnection();
    var collection = await getCollection(conn);
    return await collection.find().key(id).remove();
  } finally {
    if (conn) {
      await conn.close();
    }
  }
}

function code() {
  str = `    var wine = request.body;
    var id = wine.id;
    var soda = conn.getSodaDatabase();
    var collection = await soda.openCollection("wines");
    await collection.find().key(id).replaceOne(wine);
`;
  return JSON.stringify({"value":str});
}

async function getCollection(conn) {
  var soda = conn.getSodaDatabase();
  return await soda.openCollection('wines');
}

function toJSON(documents) {
  var result = [];
  for (let i = 0; i < documents.length; i++) {
    var doc = documents[i];  // the document (with key, metadata, etc)
    var key = doc.key;     
    content = doc.getContent();
    content.id = key;        // inject key into content 
    result.push(content);
  }
  return result;
}

module.exports.initialize = initialize;
module.exports.close = close;
module.exports.get = get;
module.exports.update = update;
module.exports.create = create;
module.exports.remove = remove;
module.exports.code = code;
