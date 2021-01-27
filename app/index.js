var express = require('express');
var bodyParser = require('body-parser');
//var mongoose = require('mongoose');
var AWS = require("aws-sdk");
var favicon = require('serve-favicon');
var path = require('path');
var Utenti = require('./src/models/utenteModels');
//var UserMessages = require('./src/models/userMessageModels');
//var EmailVerifications = require('./src/models/emailVerificationModels');
//var Qr = require('./src/models/qrModels');
//Creo istanza di express (web server)
var app = express();

//importo parser per leggere i parametri passati in POST
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

//Preparazione sessione
const passport = require('passport');
app.use(passport.initialize());
app.use(passport.session());
app.use(favicon(path.join(__dirname, 'public', 'images', 'favicons', 'favicon.gif')));

const session = require("express-session");

app.use(session({ secret: "cats", resave: false, saveUninitialized: false }));
app.use(express.urlencoded({ extended: true })); // express body-parser
app.use(passport.initialize());
app.use(passport.session());


// aspetto che il container di mongo sia
function pausecomp(millis)
{
    var date = new Date();
    var curDate = null;
    do { curDate = new Date(); }
    while(curDate-date < millis);
}
console.log('Taking a break...');
pausecomp(10000);
console.log('Ten seconds later, ...'); //connessione al db mongoose.set('useFindAndModify', false);

//MongoDB

/*
mongoose.set('connectTimeoutMS', 30); mongoose .connect(
	'mongodb://mongodb:27017/dbcoffee',
	//'mongodb://localhost:27017/dbcoffee',
	//'mongodb://localhost/dbcoffee',
	// 'mongodb://asw_mongodb_1.asw_interna:27017/dbsa', ANDAVA BENE
	{ useNewUrlParser: true }) .then(() => console.log('MongoDB Connected')) .catch((err) => console.log(err));
// OK  mongoose.connect('mongodb://mongodb/dbsa', { useNewUrlParser: true, useFindAndModify: false });
//mongoose.connect('mongodb://username:password@host:port', { useNewUrlParser: true, useFindAndModify: false });
*/

//DynamoDB
//AWS


AWS.config.update({
  region: "eu-central-1",
  endpoint: "https://dynamodb.eu-central-1.amazonaws.com"
//  endpoint: "http://localhost:8000"
});

//Local

//TODO PUT HERE CONNECTION

var dynamodb = new AWS.DynamoDB();


// Routes
var routes = require('./src/routes/routes');
routes(app);

//Path globale root
var path = require('path');
global.appRoot = path.resolve(__dirname);
app.use('/static', express.static(__dirname + '/public'));

//metto in ascolto il web server
app.listen(3000, function () {
  console.log('Node API server started on port 3000!');
});
