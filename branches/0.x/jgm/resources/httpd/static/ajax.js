function refreshNifty() {
	Nifty("#main,#connected");
	Nifty("div#content,div#nav","same-height");
	Nifty("#loadingheader,#discheader,#mainheader");
}

function hideAll(except) {
	/*if (except != 'loadingheader')*/ els.loadingheader.hide();
	if (except != els.notattached) 	els.notattached.hide();
	if (except != els.discheader)	els.discheader.hide(); 
	if (except != els.connected) 	els.connected.hide();

	// need this in case it's the first time making this one visible
	if (except != null)
		except.show();
}


// these property lists are so that we only allocate memory once
// as well as to only use $(...) once per element in order to reduce 
// the browser's memory usage

var els = null;

var vars = {
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
	kills: null,
	loots: null,
	deaths: null,

	targetName: null,
	targetLevel: null,
	targetHealth: null
};

var updater = {
	init: function() {
		els = {
			loadingheader:			$('loadingheader'),
			notattached:			$('notattached'),
			discheader:				$('discheader'),
			connected:				$('connected'),


			classimage: 			$('classimage'),
			name_text: 				$('name_text'),
			level_text: 			$('level_text'),
			class_text: 			$('class_text'),
			health_text: 			$('health_text'),
			health_percent: 		$('health_percent'),
			secondary_text:			$('secondary_text'),
			secondary_percent: 		$('secondary_percent'),
			xp_text: 				$('xp_text'),
			xp_percent: 			$('xp_percent'),
			xp_per_hour_text: 		$('xp_per_hour_text'),
			ttl_text: 				$('ttl_text'),
			mode_text: 				$('mode_text'),
			profile_text: 			$('profile_text'),
			kills_text:				$('kills_text'),
			loots_text: 			$('loots_text'),
			deaths_text: 			$('deaths_text'),

			screenshot:				$('screenshot')
		};

	},

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

		var status = SNT(xml, '/status');
		if (status != 'success') {
			var message = SNT(xml, '/message');
			alert(status + ((message != '') ? "\n" + message : ""));
			return;
		}

		var mainTitle = '';
		var jgmTitle = 'JGlideMon ' + SNT(xml, '/jgm/version');
		var connected = SNT(xml, '/jgm/connected') == 'true';
		var attached = false;

		var showMe = null;

		if (!connected) {
			mainTitle = 'Disconnected';
			showMe = els.discheader;
		} else {
			attached = SNT(xml, '/glider/attached') == 'true';

			if (!attached) {
				mainTitle = 'Connected, Detached';
				showMe = els.notattached;
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
				vars.kills = SNT(xml, '/glider/kills');
				vars.loots = SNT(xml, '/glider/loots');
				vars.deaths = SNT(xml, '/glider/deaths');

				vars.targetName = SNT(xml, '/glider/target-name');
				vars.targetLevel = SNT(xml, '/glider/target-level');
				vars.targetHealth = SNT(xml, '/glider/target-health' + '%');

				mainTitle = vars.name + ': Level ' + vars.level + ' ' + vars.clazz;
				
				
				els.classimage.src = '/static/classes/' + vars.lcclazz + '.png';
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
				IH(els.profile_text, 					vars.profile);
				IH(els.kills_text, 						vars.kills);
				IH(els.loots_text, 						vars.loots);
				IH(els.deaths_text, 					vars.deaths);


				els.screenshot.src =						'/screenshot?rand=' + (new Date()).getTime();

				showMe = els.connected;
			}
		}

		if (mainTitle != '') mainTitle += ' - ';

		document.title = mainTitle + jgmTitle;
//		refreshNifty();	
		hideAll(showMe);
	}
};

// shortcuts
function SNT(xml, path) {
	return xml.selectNodeText(path);
}

function IH(element, content) {
	element.innerHTML = content;
}
