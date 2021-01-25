var mongoose = require('mongoose');
var Schema = mongoose.Schema;


var EmailVerificationSchema = new Schema({
    _id: {
        type: String,
        required: "Userid is required"
    },
    hash: {
        type: String,
        required: 'hash is required'
    },
}, {collection: 'emailVerifications'});
module.exports = mongoose.model('EmailVerifications', EmailVerificationSchema);