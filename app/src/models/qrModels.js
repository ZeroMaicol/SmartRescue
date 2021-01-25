var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var QrSchema = new Schema({
  _id: {
    type: String
	},
  life: {
    type: Number
  }
}, {collection: 'qr'});

module.exports = mongoose.model('Qr', QrSchema);
