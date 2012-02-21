var jQT = new $.jQTouch({
    icon: false,
    statusBar: 'black-translucent',
    preloadImages: []
});

updater._initOrig = updater.init;
updater.init = function() {
	updater._initOrig();
	
	els.screenshot = $('#screenshot div.ssHolder img');
	els.mainheader = $('#home');
	els.lastheader = null;
};

setHeader = function(newHeader) {
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