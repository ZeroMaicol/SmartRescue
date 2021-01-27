//var mongoose = require('mongoose');
var dynamoose = require('dynamoose');
var Utenti = dynamoose.model("Utenti");
//var UserMessages = mongoose.model("UserMessages");
//var EmailVerifications = mongoose.model("EmailVerifications");
//var Qr = mongoose.model("Qr");
//var nodemailer = require("nodemailer");
const { validationResult } = require('express-validator');

var Crypto = require('crypto');
const passport = require('passport');

exports.home = (req, res) => {
	res.sendFile(appRoot + '/www/index.html');
}

exports.who_logged = (req, res) => {
	res.json({user:req.user._id});
};

exports.get_home_data = async (req, res) => {
	const now = new Date();
	/*const data = await get_rankings(now.getFullYear(), now.getMonth() + 1);
	if (data.error) {
		console.log(data.error);
		res.json(data.error);
		return;
	}*/
	const isLoggedIn = req.isAuthenticated();
	res.json({
		isLoggedIn: isLoggedIn,
		username: isLoggedIn ? req.user.Account : "",
		thingID: isLoggedIn ? req.user.ThingID : ""
		//rankings: data.rankings,
		//userRankings: data.userRankings
	});
}

exports.get_thing_data = async (req, res) => {
	const isLoggedIn = req.isAuthenticated();
	res.json({
		isLoggedIn: isLoggedIn,
		username: isLoggedIn ? req.user.Account : ""
		//rankings: data.rankings,
		//userRankings: data.userRankings
	});
}

exports.show_contact_us = function(req, res) {
	res.sendFile(appRoot + '/www/contactUs.html');
};

exports.get_contact_us = function(req, res) {
	UserMessages.find({}, function(err, messages) {
		if (err)
			res.send(err);
		res.json(messages);
	});
};

exports.contact_us = async (req, res) => {
	const errors = validationResult(req);
	if (!errors.isEmpty()) {
		return res.json({errors: errors.array()});
	}
	const data = {
		fullName: req.body.fullName,
		email: req.body.email,
		contact: req.body.contact,
		company: req.body.company,
		message: req.body.message,
	}
	console.log(data);
	const newMessage = new UserMessages(data);
	await newMessage.save();
	mailOptions = {
		to: process.env.GMAIL_HOST,
		subject: 'ASW-Cafeteria - Contacted by ' + data.fullName,
		text: 'From: ' + data.email + '\n'
			+ 'Company: ' + data.company + '\n'
			+ 'Contact: ' + data.contact + '\n\n'
			+ 'Message:\n' + data.message
	};
	smtpTransport.sendMail(mailOptions);
	res.json(data);
};

exports.show_mazemap = function(req, res) {
	res.sendFile(appRoot + '/www/map.html');
};

exports.show_device = function(req, res) {
	res.sendFile(appRoot + '/www/device.html');
};

exports.show_adminPage = function(req, res) {
	res.sendFile(appRoot + '/www/map.html');
};


//Login route
exports.show_login = function(req, res) {
	res.sendFile(appRoot + '/www/login.html');
};

//Esporta gli utenti
exports.list_utenti = function(req, res) {
	Utenti.all({}, function(err, utenti) {
		if (err)
			res.send(err);
		res.json(utenti);
	});
};


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

exports.set_thingID = async function(req, res) {
	try {
		await dynamoose.transaction([
			Utenti.transaction.update({"Account":req.body.account}, {"$SET":{"ThingID":req.body.thingID}})
		]);
		console.log("Utente "+req.body.account+" aggiornato con successo con thingID "+req.body.thingID);
		res.status(201).json({msg: "Update riuscito!"})
	} catch (error) {
		console.log(error);
		res.status(501).json({errors: [error]});
	}
}

//sessione
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
