var express = require('express');
var bodyParser = require('body-parser');

var AWS = require("aws-sdk");
var favicon = require('serve-favicon');
var path = require('path');
var Utenti = require('./src/models/utenteModels');
var Buildings = require('./src/models/buildingModels');

var config = require('./src/controllers/config');

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


function pausecomp(millis)
{
    var date = new Date();
    var curDate = null;
    do { curDate = new Date(); }
    while(curDate-date < millis);
}
console.log('Taking a break...');
pausecomp(10000);
console.log('Ten seconds later, ...');

//DynamoDB
//AWS

AWS.config.update({
  region: "eu-central-1",
  endpoint: "https://dynamodb.eu-central-1.amazonaws.com",
//  accessKeyId: config.accessKeyId,
//  secretAccessKey: config.secretAccessKey
});

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
