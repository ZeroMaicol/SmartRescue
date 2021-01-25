var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var CorsiSchema = new Schema({
  _id: {
    type: String
	},
  description: {
    type: String
  },
  total: {
    type: Number
  }
}, {collection: 'corsi'});

module.exports = mongoose.model('Corsi', CorsiSchema);
