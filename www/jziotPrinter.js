var exec = require('cordova/exec');

exports.coolMethod = function (args, success, error) {
    exec(success, error, 'jziotPrinter', 'coolMethod', [arg0]);
};

exports.turnOnPrinter = function(args, success, error) {
	exec(success, error, service, "turnOnPrinter", [args]);
};
