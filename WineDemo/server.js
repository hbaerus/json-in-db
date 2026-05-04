

var express  = require('express');
var app      = express();
var parser   = require('body-parser');
var crypto   = require('crypto');
var dbconfig = require('./db/dbconfig.js');
var db       = require('./db/wines-' + dbconfig.dbname + '.js');

var port = process.env.WINE_PORT || 3000;
var apiToken = requiredEnv('WINE_API_TOKEN');

app.use(parser.json());
app.use('/', express.static(__dirname + '/web'));

app.use(function(req, res, next) {
  res.setHeader('Content-Type', 'application/json');
  next();
});

app.use(['/wines', '/wines-reset', '/code'], authenticate);

var server = app.listen(port, async function () 
{
  var host = server.address().address
  var port = server.address().port
  try
  {
    await db.initialize();
    console.log("Winestore listening at http://%s:%s", host, port);
  }
  catch (err)
  {
    console.error(err);
  }
});

app.get('/wines', async function (request, response)  
{
  try
  {
    var qbe = request.query.qbe;
    var result = await db.get(qbe);
    response.send(result);     
  }
  catch (err)
  {
    handle(err, response);
  }
});

app.post('/wines', async function (request, response)
{
  var review = request.body;
  var id = review.id;
  try
  {
    if (id) {
      await db.update(id, review);
      response.send({'updated':id});
    } else {
      var key = await db.create(review);
      response.send({'generatedKey':key});
    }
  } catch (err) {
    handle(err, response);
  }
});

app.delete('/wines/:id', async function (request, response)
{
  try {
    var id = request.params.id;
    await db.remove(id);
    response.send({'status':'removed document'});
  } catch(err) {
    handle(err, response);
  }
});

app.post('/wines-reset', async function (request, response)
{
  try {
    var result = await db.get('{}');
    for (let i = 0; i < result.length; i++) {
      var id = result[i].id;
      console.log('removing ' + id);
      await db.remove(id);
    }

    for (let i = 0; i < dbconfig.wines.length; i++) {
      await db.create(dbconfig.wines[i]);
    }
    response.send({'status':'reset'});
  } catch(err) {
    handle(err, response);
  }
});


// for about page
app.use('/about.png', express.static(__dirname + '/web/css/images/about-' + dbconfig.dbname + '.png'));
app.get('/code', async function (request, response)  
{
  response.send(db.code());
});

function handle(err, response) {
   var statusCode = err.statusCode || 500;
   if (statusCode >= 500) {
     console.error(err);
   }
   response.status(statusCode).send({"error": statusCode >= 500 ? "Internal server error" : "Invalid request"});
}

function requiredEnv(name) {
  var value = process.env[name];
  if (!value) {
    throw new Error(name + ' must be set');
  }
  return value;
}

function authenticate(request, response, next) {
  var auth = request.get('authorization') || '';
  var token = null;
  if (auth.indexOf('Bearer ') === 0) {
    token = auth.slice(7);
  } else {
    token = request.get('x-api-token');
  }

  if (!token || !safeEquals(token, apiToken)) {
    response.status(401).send({"error":"Authentication required"});
    return;
  }

  next();
}

function safeEquals(left, right) {
  var leftBuffer = Buffer.from(left);
  var rightBuffer = Buffer.from(right);
  if (leftBuffer.length !== rightBuffer.length) {
    return false;
  }
  return crypto.timingSafeEqual(leftBuffer, rightBuffer);
}
