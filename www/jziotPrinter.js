var exec = require('cordova/exec');

var service = 'jziotPrinter';

exports.testMethod = function (arg0, success, error) {
    exec(success, error, service, 'testMethod', [arg0]);
};

exports.turnOnPrinter = function(args, success, error) {
	exec(success, error, service, "turnOnPrinter", [args]);
};

exports.turnOffPrinter = function(args, success, error) {
	exec(success, error, service, "turnOffPrinter", [args]);
};

exports.printBulkData = function(args, success, error) {
	exec(success, error, service, "printBulkData", [args]);
};
