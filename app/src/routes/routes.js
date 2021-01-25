module.exports = function(app) {
	var controller = require('../controllers/controller');
	var validator = require("../controllers/validator");
  	var passport = require('passport');

	app.get('/', isUser, controller.home);
	app.get('/login', isNotLoggedIn, controller.show_login);

	//app.get("/email/confirm/:hash", controller.confirm_email);
	//app.get("/reset/:token", controller.send_reset_password);
	//app.post("/reset/:token", validator.reset_password, controller.reset_password);
	//app.post("/api/sendForgotEmail", validator.request_reset_email, controller.send_forgot_email);
	app.get("/api/homeData", controller.get_home_data);
	app.post("/api/contactUs", validator.api_contact_us, controller.contact_us);
	app.get("/api/contactUs", isAdminLoggedIn, controller.get_contact_us);
	app.get("/admin/contactUs", isAdminLoggedIn, controller.show_contact_us);

	app.get('/api/log', controller.who_logged);

	app.route('/api/utenti')
		.get(isAdminLoggedIn, controller.list_utenti)
		.post(validator.new_user, controller.new_utente);

/*	app.route('/api/tickets')
		.get(isAdminLoggedIn, controller.list_tickets);

	app.route('/api/userTicketsTotal')
		.get(controller.list_userTicketsTotal);
*/
	//app.get('/api/userTickets', isLoggedIn, controller.list_userTickets);
	//app.delete('/api/userTickets/:id', isAdminLoggedIn, controller.delete_ticket);

	app.get('/map', isLoggedIn, controller.show_mazemap);
	app.get('/device', isLoggedIn, controller.show_device);

	app.get('/admin/main', isAdminLoggedIn, controller.show_adminPage);

/*
  	//GESTIONE TICKET DELL'ADMIN
	app.get('/api/admin/userTickets', isAdminLoggedIn, controller.list_adminUserTicketsTotal);



	app.get('/admin/qr', isAdminLoggedIn, controller.show_admin_qr);

	app.get('/api/admin/qr', isAdminLoggedIn, controller.list_qr);
	app.post('/api/admin/qr', isAdminLoggedIn, validator.new_qr_check, controller.new_qr);
	app.delete('/api/admin/qr/:id', controller.delete_qr_add_life);
*/

	//LOGIN, LOGOUT
	app.post('/login', validator.login, controller.check_login);

	app.get('/logout', (request, response) => {
		  request.logout();
		  response.redirect('/');
	});

	function isLoggedIn(request, response, next) {
    // passport adds this to the request object
  	if (request.isAuthenticated()) {
        return next();
    }
    	response.redirect('/login');
	}

	function isNotLoggedIn(request, response, next) {
    // passport adds this to the request object
  	if (!request.isAuthenticated()) {
        return next();
    }
    	response.redirect('/');
	}

	function isUser(request, response, next) {
    // passport adds this to the request object
  	if (!request.isAuthenticated()) {
        return next();
    } else {
			if (request.isAuthenticated() && request.user._id == "admin") {
				response.redirect('/admin/main')
			} else {
					return next();
			}
		}
	}


	function isAdminLoggedIn(request, response, next) {
    // passport adds this to the request object
  	if (request.isAuthenticated() && request.user._id == "admin") {
        return next();
    }
    response.redirect('/login');
	}
}
