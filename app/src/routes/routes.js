module.exports = function(app) {
	var controller = require('../controllers/controller');
	var validator = require("../controllers/validator");
  	var passport = require('passport');

	app.get('/', isUser, controller.home);
	app.get('/login', isNotLoggedIn, controller.show_login);
	app.get("/api/homeData", controller.get_home_data);
	app.get('/api/log', controller.who_logged);

	app.route('/api/utenti')
		.post(validator.new_user, controller.new_utente);

	app.route('/api/getshadow')
		.post(controller.get_shadow);

	app.route('/api/alarm')
		.get(controller.get_alarm)
		.post(controller.set_alarm);

	app.route('/api/things')
		.get(isLoggedIn, controller.list_things);

	app.get('/map', isLoggedIn, controller.show_mazemap);
	app.get('/device', isLoggedIn, controller.show_device);

	app.get('/admin/main', isAdminLoggedIn, controller.show_adminPage);

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
