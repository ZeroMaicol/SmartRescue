const dynamoose = require('dynamoose');
var Schema = dynamoose.Schema;

var DeviceSchema = new Schema({
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
  }
}, {
  throughput:{read: 10, write:5}
});


var BuildingSchema = new Schema({

  device: [DeviceSchema]

};

module.exports = dynamoose.model('building', BuildingSchema);
