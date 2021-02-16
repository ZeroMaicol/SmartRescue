var dynamoose = require('dynamoose');

var Utenti = dynamoose.model("Utenti");
var Buildings = dynamoose.model("buildings")

const { validationResult } = require('express-validator');
var config = require('./config');
var Crypto = require('crypto');
const passport = require('passport');

var https = require('https');
var aws4 = require('aws4');

//--------------------------------

const RETRY_ERRS = ['EADDRINFO', 'ETIMEDOUT', 'ECONNRESET', 'ESOCKETTIMEDOUT', 'ENOTFOUND', 'EMFILE']

//Richiesta sulla base delle opzioni che vengono passate. Ritorna il risultato della richiesta.
var request = async function request(options) {
  options.retries = options.retries || 0
  return new Promise((resolve, reject) => {
    const onError = err => {
      if (RETRY_ERRS.includes(err.code) && options.retries < 5) {
        options.retries++
        return request(options).then(resolve).catch(reject)
      }
      reject(err)
    }
    var req = https.request(options, res => {
      let bufs = []
      res.on('error', onError)
      res.on('data', bufs.push.bind(bufs))
      res.on('end', () => {
        resolve({
          statusCode: res.statusCode,
          headers: res.headers,
          body: Buffer.concat(bufs).toString('utf8'),
        })
      })
    }).on('error', onError).end(options.body)
		return {
			result: req
		};
  })
}


//Effettua la signature aws4 e richiede i dati della shadow con una HTTP GET.
exports.get_shadow = async function(req, res) {
	try {
		var result = aws4.sign({
  		service: 'iotdata',
			host: config.host,
  		region: config.region,
  		method: 'GET',
  		path: '/things/'+req.body.thingName+'/shadow?name='+req.body.shadowName,
  		headers: {
    		'Content-Type': 'application/x-amz-json-1.0',
  		},
  			body: '{}'
		}, {
			secretAccessKey: config.secretAccessKey,
			accessKeyId: config.accessKeyId
		});
		const ret = await request(result);
		console.log(ret.body);
		res.status(201).json({body: ret.body, shadowName: req.body.shadowName, thingName: req.body.thingName});
	} catch (error) {
		console.log(error);
		res.status(501).json({errors: [error]});
	}
}

//--------------------------------

exports.home = (req, res) => {
	res.sendFile(appRoot + '/www/index.html');
}

exports.who_logged = (req, res) => {
	res.json({user:req.user._id});
};

//Ritorna le informazioni utili all'utente autenticato.
exports.get_home_data = async (req, res) => {
	const now = new Date();
	const isLoggedIn = req.isAuthenticated();
	res.json({
		isLoggedIn: isLoggedIn,
		username: isLoggedIn ? req.user.Account : "",
		buildingID: isLoggedIn ? config.buildingID : ""
	});
}

exports.show_adminPage = function(req, res) {
	res.sendFile(appRoot + '/www/map.html');
};

exports.show_mazemap = function(req, res) {
	res.sendFile(appRoot + '/www/map.html');
};

exports.show_device = function(req, res) {
	res.sendFile(appRoot + '/www/device.html');
};

exports.show_login = function(req, res) {
	res.sendFile(appRoot + '/www/login.html');
};


//Esporta things di un building
exports.list_things = async function(req, res) {
	try{
		const things = await Buildings.query("id").eq(config.buildingID).exec();
		console.log(things[0]);
		res.status(201).json(things[0].things);
	} catch (error) {
		console.log(error);
		res.status(501).json({errors: [error]});
	}
};

//Esegue una POST sulla shadow dell'allarme per impostarlo a false.
exports.set_alarm = async function(req, res) {
  try {
    console.log(req.body.code);

    const building = await Buildings.query("id").eq(config.buildingID).exec();
    console.log("Building = "+building[0].id);

    var hashedCode = sha512(req.body.code, building[0].sale);
    if (hashedCode.passwordHash == building[0].password) {
      var result = aws4.sign({
      	service: 'iotdata',
    		host: config.host,
      	region: config.region,
      	method: 'POST',
      	path: '/things/'+config.alarmThing+'/shadow?name=',
      	headers: {
      		'Content-Type': 'application/x-amz-json-1.0',
      	},
      		body: '{"state": {"desired": {"alarm": false}}}'
    	}, {
    		secretAccessKey: config.secretAccessKey,
    		accessKeyId: config.accessKeyId
    	});
    	const ret = await request(result);
    	console.log(ret.body);
    	res.status(201).json({msg: "Codice Corretto!"});
    } else {
      res.status(501).json({msg: "Codice Errato!"});
    }
  } catch (error) {
	   console.log(error);
	   res.status(501).json({errors: [error]});
    }
}

//Effettua una get sulla shadow dell'allarme e torna la risposta.
exports.get_alarm = async function(req, res) {
  try {
    var result = aws4.sign({
      service: 'iotdata',
    	host: config.host,
    	region: config.region,
    	method: 'GET',
    	path: '/things/'+config.alarmThing+'/shadow?name=',
    	headers: {
      	'Content-Type': 'application/x-amz-json-1.0',
      },
      	body: '{}'
    },{
    	secretAccessKey: config.secretAccessKey,
    	accessKeyId: config.accessKeyId
    });
    const ret = await request(result);
    console.log(ret.body);
    res.status(201).json({body: ret.body});
  } catch (error) {
	   console.log(error);
	   res.status(501).json({errors: [error]});
    }
}

//Variabile utilizzata per l'algoritmo di criptazione SHA512
var sha512 = function(password, salt){
	if (!salt) {
		const length = 32;
		salt = Crypto.randomBytes(Math.ceil(length/2))
			.toString('hex') /** convert to hexadecimal format */
			.slice(0,length);
	}
	var hash = Crypto.createHmac('sha512', salt); /** Hashing algorithm sha512 */
	hash.update(password);
	var value = hash.digest('hex');
	return {
		salt:salt,
		passwordHash:value
	};
};

//Creazione di un nuovo utente
exports.new_utente = async function(req, res) {
	const errors = validationResult(req);
	console.log(errors);
	if (!errors.isEmpty()) {
		return res.json({errors: errors.array()});
	}
	var hashedPass = sha512(req.body.password);
	var new_user = {
		Account: req.body._id,
		Email: req.body.email,
		Password: hashedPass.passwordHash,
		Sale: hashedPass.salt,
		Attivo: true
	}
	try {
		var utente = new Utenti(new_user);
		await utente.save();
		console.log("Utente "+utente+" aggiunto con successo!");
		res.status(201).json({msg: "Sei registrato!"});
	} catch (error) {
		console.log(error);
		res.status(501).json({errors: [error]});
	}
};

//Gestione della sessione con passport
passport.serializeUser(function(user, cb) {
  	cb(null, user.Account);
});

passport.deserializeUser(function(id, cb) {
	Utenti.get({"Account":id}, (err, user) => {
		cb(err, user);
	});
});

const LocalStrategy = require('passport-local').Strategy;

passport.use(new LocalStrategy(
  function(username, password, done) {
	  console.log("login attempt by " + username);
		Utenti.get({"Account":username}, (err, utente) => {
			if (err) {
				return done(err);
			}
			console.log("Utente = "+utente);

			if (typeof utente === 'undefined') {
				return done(null, false, {message: "User not found"});
			}
			var hashedPass = sha512(password, utente.Sale);
			if (hashedPass.passwordHash == utente.Password) {
				if (utente.Attivo) {
					return done(null, utente, {message: "Login Verified"});
				} else {
					return done(null, false, {message: "Email not verified"});
				}
			} else {
				return done(null, false, {message: "Invalid credential"});
			}
		});
  }
));

//Controlla se il login è stato effettuato e ridirezione in base a esso.
exports.check_login = (req, res, next) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.json({errors: errors.array()});
	}
	passport.authenticate('local', function(err, user, info) {
		if (err) {
			return next(err);
		}
		if (!user) {
			return res.json({
				errors: [{msg: info.message}]
			});
		}
		req.logIn(user, function(err) {
			if (err) { return next(err); }
			if (user.Account == "admin") {
				return res.json({path: "/admin"});
			} else {
				return res.json({path: "/"});
			}
		});
	})(req, res, next);
}
