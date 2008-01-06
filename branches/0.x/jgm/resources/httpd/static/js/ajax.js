// these property lists are so that we only allocate memory once
// as well as to only use $(...) once per element in order to reduce 
// the browser's memory usage

var els = null;

var vars = {
	connected: false,
	attached: false,

	name: null,
	level: null,
	clazz: null,
	lcclazz: null,
	health: null,
	mana: null,
	manaName: null,
	xp: null,
	nextXp: null,
	xpPercent: null,
	xpPerHour: null,
	ttl: null,
	mode: null,
	profile: null,
	fullProfile: null,
	kills: null,
	loots: null,
	deaths: null,

	targetName: null,
	targetLevel: null,
	targetHealth: null
};


// this is responsible for the bulk of the updating
var updater = {
	init: function() {
		els = {
			chattype:				$('chattype'),
			chattospan:				$('chattospan'),
			chatto:					$('chatto'),
			chattext:				$('chattext'),

			headerdiv:				$('headerdiv'),
			hiddenheaders:			$('hiddenheaders'),

			loadingheader:			$('loadingheader'),
			discheader:				$('discheader'),
			notattached:			$('notattached'),
			mainheader:				$('mainheader'),

			connected:				$('connected'),

			classimage: 			$('classimage'),
			name_text: 				$('name_text'),
			level_text: 			$('level_text'),
			class_text: 			$('class_text'),
			health_text: 			$('health_text'),
			health_percent: 		$('health_percent'),
			secondary_text:			$('secondary_text'),
			secondary_percent: 		$('secondary_percent'),
			xp_bar:					$('xp_bar'),
			xp_text: 				$('xp_text'),
			xp_percent: 			$('xp_percent'),
			xp_per_hour_text: 		$('xp_per_hour_text'),
			ttl_text: 				$('ttl_text'),
			mode_text: 				$('mode_text'),
			profilediv:				$('profilediv'),
			profile_text: 			$('profile_text'),
			kills_text:				$('kills_text'),
			loots_text: 			$('loots_text'),
			deaths_text: 			$('deaths_text'),

			screenshot:				$('screenshot')
		};
	},

	url: urls.ajax + "status",

	update: function() {
		new Ajax.Request(updater.url, {
			method: 'get',
			asynchronous: true,
			onSuccess: updater.handle,
			onFailure: updater.fail
		});

		setTimeout("updater.update();", settings.updateInterval);
	},


	fail: function(t) {
		alert('AJAX Error: ' + t.status + "\n" + t.statusText);
	},

	checkStatus: function(xml) {
		var status = SNT(xml, '/status');
		if (status != 'success') {
			var message = SNT(xml, '/message');
			alert(status + ((message != '') ? "\n" + message : ""));
			return false;
		}

		return true;
	},

	handle: function(t) {
		var xml = (new XMLDoc(t.responseText)).docNode;
		if (!updater.checkStatus(xml)) return;

		var mainTitle = '';
		var jgmTitle = 'JGlideMon ' + SNT(xml, '/jgm/version');
		vars.connected = SNT(xml, '/jgm/connected') == 'true';

		settings.updateInterval = SNT(xml, '/jgm/update-interval');

		if (!vars.connected) {
			vars.attached = false;
			mainTitle = 'Disconnected';
			setHeader(els.discheader);
			els.connected.hide();
		} else {
			vars.attached = SNT(xml, '/glider/attached') == 'true';

			if (!vars.attached) {
				mainTitle = 'Connected, Detached';
				setHeader(els.notattached);
				els.connected.hide();
			} else {
				vars.name = SNT(xml, '/glider/name');
				vars.level = SNT(xml, '/glider/level');
				vars.clazz = SNT(xml, '/glider/class');
				vars.lcclazz = SNT(xml, '/glider/lcclass');
				vars.health = SNT(xml, '/glider/health') + '%';
				vars.mana = SNT(xml, '/glider/mana') + '%';
				vars.manaName = SNT(xml, '/glider/mana-name');
				vars.xp = SNT(xml, '/glider/xp');
				vars.nextXp = SNT(xml, '/glider/next-xp');
				vars.xpPercent = SNT(xml, '/glider/xp-percent') + '%';	
				vars.xpPerHour = SNT(xml, '/glider/xp-per-hour');
				vars.ttl = SNT(xml, '/glider/ttl');
				vars.mode = SNT(xml, '/glider/mode');
				vars.profile = SNT(xml, '/glider/profile');
				vars.fullProfile = SNT(xml, '/glider/full-profile');
				vars.kills = SNT(xml, '/glider/kills');
				vars.loots = SNT(xml, '/glider/loots');
				vars.deaths = SNT(xml, '/glider/deaths');

				vars.targetName = SNT(xml, '/glider/target-name');
				vars.targetLevel = SNT(xml, '/glider/target-level');
				vars.targetHealth = SNT(xml, '/glider/target-health' + '%');

				mainTitle = vars.name + ': Level ' + vars.level + ' ' + vars.clazz;
				
				els.classimage.setAttribute('alt', vars.clazz);
				els.classimage.setAttribute('title', vars.clazz);
				els.classimage.src = urls.image + 'classes/' + vars.lcclazz + '.png';
				IH(els.name_text,						vars.name);
				IH(els.level_text, 						vars.level);
				IH(els.class_text, 						vars.clazz);
				IH(els.health_text, 					vars.health);
				els.health_percent.style.width = 		vars.health;
				IH(els.secondary_text, 					vars.mana);
				els.secondary_percent.style.width = 	vars.mana;
				els.secondary_percent.style.className =	vars.manaName;
				IH(els.xp_text, 						vars.xp + ' / ' + vars.nextXp + ' (' + vars.xpPercent + ')');
				els.xp_percent.style.width = 			vars.xpPercent;
				IH(els.xp_per_hour_text, 				vars.xpPerHour);
				IH(els.ttl_text, 						vars.ttl);
				IH(els.mode_text, 						vars.mode);
				els.profilediv.onmouseover = function() {
					Tip(vars.fullProfile);
				};
				IH(els.profile_text, 					vars.profile);
				IH(els.kills_text, 						vars.kills);
				IH(els.loots_text, 						vars.loots);
				IH(els.deaths_text, 					vars.deaths);


				els.screenshot.src =					urls.screenshot + 'rand=' + (new Date()).getTime();

				setHeader(els.mainheader);
				els.connected.show();
			}
		}

		if (mainTitle != '') mainTitle += ' - ';

		document.title = mainTitle + jgmTitle;
	}
};

// shortcuts
function SNT(xml, path) {
	return xml.selectNodeText(path);
}

function IH(element, content) {
//	alert("before: " + element.innerHTML);
	element.innerHTML = content;
//	alert("after: " + element.innerHTML);
}



// functions for start/stopping/sending chat

var commands = {
	url: urls.command + "command=",

	typeChange: function() {
		if (els.chattype.getValue() == 'Whisper') {
			els.chattospan.show();
		} else {
			els.chattospan.hide();
		}
	},

	handle: function(t) {
		var xml = (new XMLDoc(t.responseText)).docNode;

		if (!updater.checkStatus(xml)) return;

		window.status = "Command sent successfully";

		setTimeout("window.status = '';", 3000);
	},

	start: function() {
		if (!(vars.connected && vars.attached)) return;

		new Ajax.Request(commands.url + "start", {
			method: 'get',
			asynchronous: true,
			onSuccess: commands.handle,
			onFailure: updater.fail
		});
	},

	stop: function() {
		if (!(vars.connected && vars.attached)) return;

		new Ajax.Request(commands.url + "stop", {
			method: 'get',
			asynchronous: true,
			onSuccess: commands.handle,
			onFailure: updater.fail
		});
	},

	chat: function() {
		if (!(vars.connected && vars.attached)) return;

		var out = '';
		var type = els.chattype.getValue();
		var to   = trim(els.chatto.getValue());
		var text = trim(els.chattext.getValue());

		if (type == 'Whisper') {
			if (to == '') {
				alert('The To field cannot be empty');
				return;
			}

			out = '/w ' + to + ' ' + text + '|';
		} else if (type == 'Say') {
			out = '/s ' + text + '|';
		} else if (type == 'Raw') {
			out = text;
		}

		if (text == '') {
			alert('You must enter some chat text');
			return;
		}

		if (out != '') {
			new Ajax.Request(commands.url + "chat&keys=" + out, {
				method: 'get',
				asynchronous: true,
				onSuccess: commands.handle,
				onFailure: updater.fail
			});

			els.chatto.clear();
			els.chattext.clear();
		}
	}
};



// helper/util stuff

function setHeader(newHeader) {
	// remove current header and place it in the hidden headers div
	els.hiddenheaders.appendChild(
		els.headerdiv.childElements()[0].remove());
	// remove newHeader from the hidden headers div and place it in headerdiv
	// thus making it the current header
	els.headerdiv.appendChild(
		newHeader.remove());
}

function trim(str) {
	return str.replace(/^\s*/, "").replace(/\s*$/, "");
}
