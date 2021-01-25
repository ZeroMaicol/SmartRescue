const {param, body} = require("express-validator");
//var mongoose = require('mongoose');
var dynamoose = require('dynamoose');

var Utenti = dynamoose.model("Utenti");

const passwordMinLength = 8;
const usernameMinLength = 4;

/*
exports.new_qr_check = [
  body("life")
    .isInt({min:1, max:5}).withMessage("Must be a number between 1-5")
];
*/
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

exports.reset_password = [
	body("password")
		.exists().withMessage("Password is missing")
		.isLength({min: passwordMinLength}).withMessage("Password must be at least " + passwordMinLength + " characters long"),

	body("confirm_password")
		.trim()
		.exists().withMessage("Confirmation password is missing")
		.custom((value, {req}) => {
			if (value !== req.body.password) {
				throw new Error();
			}
			return true;
		}).withMessage("Confirmation password doesn't match password"),

	param("token")
		.exists().withMessage("Reset token is missing")
		.custom(async value => {
			if (! await Utenti.findOne({
				resetPasswordToken: value,
				resetPasswordExpires: { $gt: Date.now() }
			})) {
				return Promise.reject();
			}
		}).withMessage("Password reset token is invalid or has expired")
];

/*
exports.request_reset_email = [
	body("username")
		.exists().withMessage("Username is missing")
		.not().isEmpty().withMessage("Username is missing")
		.custom(async value => {
			if (! await Utenti.findOne({_id: value})) {
				return Promise.reject();
			}
		}).withMessage("User not found")
];
*/

exports.api_contact_us = [
	body("fullName")
		.exists().withMessage("Full name is required")
		.not().isEmpty().withMessage("Full name is required"),

	body("email")
		.exists().withMessage("Email is required")
		.not().isEmpty().withMessage("Email is required")
		.normalizeEmail()
		.isEmail().withMessage("Invalid email"),

	body("message")
		.exists().withMessage("Message is required")
		.not().isEmpty().withMessage("Message is required")
];
