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