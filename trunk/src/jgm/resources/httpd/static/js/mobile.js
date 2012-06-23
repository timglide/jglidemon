(function() {
	var updater_initOrig = updater.init;
	updater.init = function() {
		updater_initOrig();
		
		els.headers    = $('#headers');
		els.mainheader = $('#home');
	};
})();

setHeader = function(newHeader) {
	if (newHeader == els.ajaxerror ||
		newHeader == els.discheader ||
		newHeader == els.notattached) {
		
		els.headers.children().hide();
		newHeader.show();
		els.headers.show();
	} else {
		els.headers.hide();
	}
}