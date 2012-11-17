var jQT = new $.jQTouch({
    icon: '/static/img/stitch-57.png',
    icon4: '/static/img/stitch-114.png',
    statusBar: 'black-translucent',
    preloadImages: []
});

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