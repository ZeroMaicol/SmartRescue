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
  Attivo: {
    type: Boolean,
    default: false
  },
  ThingID: {
    type: String,
    default: ""
  }
}, {
  throughput:{read: 10, write:5}
});

module.exports = dynamoose.model('Utenti', UtentiSchema);
