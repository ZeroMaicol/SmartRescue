const {param, body} = require("express-validator");
var dynamoose = require('dynamoose');

var Utenti = dynamoose.model("Utenti");

const passwordMinLength = 8;
const usernameMinLength = 4;

exports.new_user = [
    body("._id")
		.trim()
		.isLength({min: usernameMinLength}).withMessage("Username must be at least " + usernameMinLength + " characters long")
		.isAlphanumeric().withMessage("Username can only contain letters and numbers")
		.custom(async value => {
      const count = await Utenti.query("Account").eq(value).exec();
			if (count.count != 0) {
        return Promise.reject();
      }
    }).withMessage("Username already in use"),

	body("email")
		.normalizeEmail()
		.isEmail().withMessage("Invalid email")
		.custom(async value => {
      console.log("mail: "+value);
      const countEmail = await Utenti.scan("Email").eq(value).exec();
      console.log(countEmail.count);
      if (countEmail.count != 0) {
        return Promise.reject();
      }
    }).withMessage("Email already in use"),

	body("password")
        .isLength({min: passwordMinLength}).withMessage("Password must be at least " + passwordMinLength + " characters long")
];

exports.login = [
	body("username")
		.trim()
		.exists().withMessage("Username is missing")
		.not().isEmpty().withMessage("Username is missing"),
	body("password")
		.exists().withMessage("Password is missing")
		.not().isEmpty().withMessage("Password is missing"),
]
