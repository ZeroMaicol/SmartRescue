var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var UserMessagesSchema = new Schema({
  fullName: {
    type: String,
    required: "A name is required"
  },
  email: {
    type: String,
    required: 'An email is required'
  },
  contact: {
    type: String
  },
  company: {
    type: String
  },
  message: {
    type: String
  }
}, {collection: 'userMessages'});

module.exports = mongoose.model('UserMessages', UserMessagesSchema);