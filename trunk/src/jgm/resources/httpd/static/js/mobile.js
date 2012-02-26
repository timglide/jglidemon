var jQT = new $.jQTouch({
    icon: false,
    statusBar: 'black-translucent',
    preloadImages: []
});

(function() {
	var updater_initOrig = updater.init;
	updater.init = function() {
		updater_initOrig();
		
		els.mainheader = $('#home');
		els.lastheader = null;
	};
})();

setHeader = function(newHeader) {
	return;
	
	if (els.lastheader == newHeader) {
		return;
	}
	
	var anim = 'slideup';
	
	if (els.mainheader == newHeader) {
		anim = 'slideup';
	}
	
	jQT.goTo('#' + newHeader.attr('id'), anim);
	
	els.lastheader = newHeader
}