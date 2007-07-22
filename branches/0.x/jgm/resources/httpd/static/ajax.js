function refreshNifty() {
	Nifty("#loading,#disc,#main");
	Nifty("div#content,div#nav","same-height");
	Nifty("#loadingheader,#discheader,#mainheader");
}

function hideAll() {
	$('loading').hide();
	$('disc').hide();
	$('main').hide();
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
			showMe = $('disc');
		} else {
			mainTitle = 'Name: Level ## Class';
			// update everything

			showMe = $('main');
		}

		if (mainTitle != '') mainTitle += ' - ';

		document.title = mainTitle + jgmTitle;

//		refreshNifty();
		hideAll();
		showMe.show();
	}
};
