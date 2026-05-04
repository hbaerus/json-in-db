const http = require('http')
const express = require('express')
var bodyParser = require('body-parser');
var cors = require('cors')
var crypto = require('crypto')
var oracledb = require('oracledb')
const muserver = require('../exports/server')
const muhealth = require('./health')
const mustory = require('../core/stories')
const db = require('../core/db')

const app = express()

const port = muserver.port
const apiToken = requiredEnv('MUSPRINT_API_TOKEN')
const allowedOrigins = requiredEnv('MUSPRINT_ALLOWED_ORIGINS').split(',')
  .map(function(origin) { return origin.trim() })
  .filter(function(origin) { return origin.length > 0 })

// Enable JSON body parser
app.use(bodyParser.json())

app.use(cors({ origin: allowedOrigins }))

// Setup route handlers
app.use('/stories/health', muhealth)
app.use('/stories', authenticate, mustory)

// Setup default server error handler
app.use(function (err, request, response, next) {
  if (response.headersSent) {
    return next(err); // Not if already sent
  }
  // Error is due to a server issue, so send code 500
  console.log('> Internal server error, responding with HTTP code 500.')
  response.status(500).json({"message":"An internal error occurred in stories server"})
})

// Start listening
var server = app.listen(port, async function () {
  try {
    // If instant client directory specified, use it
    if (process.platform === 'darwin' && muserver.instantClientDir)
      oracledb.initOracleClient({libDir: muserver.instantClientDir});
    await db.initialize();
    await db.ping();
    console.log("> MuSprint stories service listening at http://localhost:%s/stories/", port);
  }
  catch (err) {
    console.error(err);
    process.kill(process.pid, 'SIGTERM');
  }
});

// Close database connection pool and service
async function terminateApp() {
  try {
    await db.terminate();
    server.close();
    console.log("> MuSprint stories service shutdown");
  }
  catch (err) {
    // Catch error and issue SIGTERM
    console.error(err);
  }
}

// Handle signals for graceful shutdown
process
  .once('SIGTERM', terminateApp)
  .once('SIGINT',  terminateApp);

function requiredEnv(name) {
  var value = process.env[name]
  if (!value) {
    throw new Error(name + ' must be set')
  }
  return value
}

function authenticate(request, response, next) {
  var auth = request.get('authorization') || ''
  var token = null
  if (auth.indexOf('Bearer ') === 0) {
    token = auth.slice(7)
  } else {
    token = request.get('x-api-token')
  }

  if (!token || !safeEquals(token, apiToken)) {
    response.status(401).json({"message":"Authentication required"})
    return
  }

  next()
}

function safeEquals(left, right) {
  var leftBuffer = Buffer.from(left)
  var rightBuffer = Buffer.from(right)
  if (leftBuffer.length !== rightBuffer.length) {
    return false
  }
  return crypto.timingSafeEqual(leftBuffer, rightBuffer)
}
