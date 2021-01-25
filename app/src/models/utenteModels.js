const dynamoose = require('dynamoose');
var Schema = dynamoose.Schema;

var UtentiSchema = new Schema({
  Account: {
    type: String,
    hashKey: true
	},
  Email: {
    type: String,
    required: 'An email is required'
  },
  Password: {
    type: String,
    required: 'A password is required'
  },
  Sale: {
    type: String,
    required: 'A salt is required'
  },
  Data: {
    type: String,
    default: ''+Date.now
  },
  Attivo: {
    type: Boolean,
    default: false
  },

}, {
  throughput:{read: 10, write:5}
});

module.exports = dynamoose.model('Utenti', UtentiSchema);
