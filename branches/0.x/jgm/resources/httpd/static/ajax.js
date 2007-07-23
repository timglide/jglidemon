function refreshNifty() {
	Nifty("#main,#connected");
	Nifty("div#content,div#nav","same-height");
	Nifty("#loadingheader,#discheader,#mainheader");
}

function hideAll(except) {
	if (except != 'loadingheader') $('loadingheader').hide();
	if (except != 'discheader')	$('discheader').hide(); 
	if (except != 'connected') $('connected').hide();

	// need this in case it's the first time making this one visible
	if (except != null)
		$(except).show();
}

var updater = {
	update: function() {
		new Ajax.Request(updater.url, {
			method: 'get',
			asynchronous: true,
			onSuccess: updater.handle,
			onFailure: updater.fail
		});
	},

	url: '/ajax/status',
	fail: function(t) {
	    alert('AJAX Error: ' + t.status + ' -- ' + t.statusText);
	},

	handle: function(t) {
		var xml = (new XMLDoc(t.responseText)).docNode;

		var status = xml.selectNodeText('/status');
		if (status != 'success') {
			var message = xml.selectNodeText('/message');
			alert(status + ((message != '') ? "\n" + message : ""));
			return;
		}

		var mainTitle = '';
		var jgmTitle = 'JGlideMon ' + xml.selectNodeText('/jgm/version');
		var connected = xml.selectNodeText('/jgm/connected') == 'true';

		var showMe = null;

		if (!connected) {
			mainTitle = 'Disconnected';
			showMe = 'discheader';
		} else {
			mainTitle = 'Name: Level ## Class';
			// update everything

			showMe = 'connected';
		}

		if (mainTitle != '') mainTitle += ' - ';

		document.title = mainTitle + jgmTitle;

		refreshNifty();
		hideAll(showMe);
	}
};
