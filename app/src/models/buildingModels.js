//Modello di un edificio: sarà definito da un id, più altri dati dinamici che saranno presi grazie al parametro saveUnknown.
const dynamoose = require('dynamoose');
var Schema = dynamoose.Schema;


var BuildingSchema = new Schema({
  id: {
    type: String,
    hashKey: true
  }
}, {
  throughput:{read: 10, write:5},
  "saveUnknown":true
});

module.exports = dynamoose.model('buildings', BuildingSchema);
