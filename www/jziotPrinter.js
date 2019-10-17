var exec = require('cordova/exec');

var service = 'jziotPrinter';

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, service, 'coolMethod', [arg0]);
};

exports.turnOnPrinter = function(args, success, error) {
	exec(success, error, service, "turnOnPrinter", [args]);
};

exports.turnOffPrinter = function(args, success, error) {
	exec(success, error, service, "turnOffPrinter", [args]);
};

exports.printText = function(args, success, error) {
	exec(success, error, service, "printText", [args]);
};
