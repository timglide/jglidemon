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
			chattype:				$('#chattype'),
			chattospan:				$('#chattospan'),
			chatto:					$('#chatto'),
			chattext:				$('#chattext'),

			headerdiv:				$('#headerdiv'),
			hiddenheaders:			$('#hiddenheaders'),

			loadingheader:			$('#loadingheader'),
			discheader:				$('#discheader'),
			notattached:			$('#notattached'),
			mainheader:				$('#mainheader'),

			connected:				$('#connected'),

			classimage: 			$('#classimage'),
			name_text: 				$('#name_text'),
			level_text: 			$('#level_text'),
			class_text: 			$('#class_text'),
			health_text: 			$('#health_text'),
			health_percent: 		$('#health_percent'),
			secondary_text:			$('#secondary_text'),
			secondary_percent: 		$('#secondary_percent'),
			xp_bar:					$('#xp_bar'),
			xp_text: 				$('#xp_text'),
			xp_percent: 			$('#xp_percent'),
			xp_per_hour_text: 		$('#xp_per_hour_text'),
			ttl_text: 				$('#ttl_text'),
			mode_text: 				$('#mode_text'),
			profilediv:				$('#profilediv'),
			profile_text: 			$('#profile_text'),
			kills_text:				$('#kills_text'),
			loots_text: 			$('#loots_text'),
			deaths_text: 			$('#deaths_text'),

			screenshot:				$('#screenshot')
		};
	},

	url: urls.ajax + "status",

	update: function() {
		$.ajax(updater.url, {
			cache: false,
			dataType: 'json',
			success: updater.handle,
			//error: updater.fail
		});
	},


	fail: function(jqXHR, textStatus, errorThrown) {
		alert('AJAX Error: ' + errorThrown + "\n" + textStatus);
	},

	checkStatus: function(json) {
		var status = json['status'];
		if (status != 'success') {
//			var message = json['message'];
//			alert(status + (message ? "\n" + message : ""));
			return false;
		}

		return true;
	},

	handle: function(json, textStatus, jqXHR) {
		if (!updater.checkStatus(json)) return;

		var app = json['app'];
		var mainTitle = '';
		var appTitle = app['name'] + ' ' + app['version'];
		vars.connected = app['connected'] == 'true';

		settings.updateInterval = app['update-interval'];

		if (!vars.connected) {
			vars.attached = false;
			mainTitle = 'Disconnected';
			setHeader(els.discheader);
			els.connected.hide();
		} else {
			var glider = json['glider'];
			
			vars.attached = glider['attached'] == 'true';

			if (!vars.attached) {
				mainTitle = 'Connected, Detached';
				setHeader(els.notattached);
				els.connected.hide();
			} else {
				vars.name        = glider['name'];
				vars.level       = glider['level'];
				vars.clazz       = glider['class'];
				vars.lcclazz     = glider['lcclass'];
				vars.health      = glider['health'] + '%';
				vars.mana        = glider['mana'] + '%';
				vars.manaName    = glider['mana-name'];
				vars.xp          = glider['xp'];
				vars.nextXp      = glider['next-xp'];
				vars.xpPercent   = glider['xp-percent'] + '%';	
				vars.xpPerHour   = glider['xp-per-hour'];
				vars.ttl         = glider['ttl'];
				vars.mode        = glider['mode'];
				vars.profile     = glider['profile'];
				vars.fullProfile = glider['full-profile'];
				vars.kills       = glider['kills'];
				vars.loots       = glider['loots'];
				vars.deaths      = glider['deaths'];

				vars.targetName   = glider['target-name'];
				vars.targetLevel  = glider['target-level'];
				vars.targetHealth = glider['target-health'] + '%';

				mainTitle = vars.name + ': Level ' + vars.level + ' ' + vars.clazz;
				
				els.classimage.attr('alt',				vars.clazz);
				els.classimage.attr('title',			vars.clazz);
				els.classimage.attr('src',				urls.image + 'classes/' + vars.lcclazz + '.png');
				IH(els.name_text,						vars.name);
				IH(els.level_text, 						vars.level);
				IH(els.class_text, 						vars.clazz);
				IH(els.health_text, 					vars.health);
				els.health_percent.css('width', 		vars.health);
				IH(els.secondary_text, 					vars.mana);
				els.secondary_percent.css('width',		vars.mana);
				els.secondary_percent.attr('class',		'info_percent ' + vars.manaName);
				IH(els.xp_text, 						vars.xp + ' / ' + vars.nextXp + ' (' + vars.xpPercent + ')');
				els.xp_percent.css('width', 			vars.xpPercent);
				IH(els.xp_per_hour_text, 				vars.xpPerHour);
				IH(els.ttl_text, 						vars.ttl);
				IH(els.mode_text, 						vars.mode);
				els.profilediv.attr('title', vars.fullProfile);
				IH(els.profile_text, 					vars.profile);
				IH(els.kills_text, 						vars.kills);
				IH(els.loots_text, 						vars.loots);
				IH(els.deaths_text, 					vars.deaths);


				els.screenshot.attr('src',				urls.screenshot + 'rand=' + (new Date()).getTime());

				setHeader(els.mainheader);
				els.connected.show();
			}
		}

		if (mainTitle != '') mainTitle += ' - ';

		document.title = mainTitle + appTitle;

		setTimeout(updater.update, settings.updateInterval);
	}
};

function IH(element, content) {
	// element.innterHTML = content;
	element.html(content);
}



// functions for start/stopping/sending chat

var commands = {
	url: urls.command + "command=",

	typeChange: function() {
		if (els.chattype.val() == 'w') {
			els.chattospan.show();
		} else {
			els.chattospan.hide();
		}
	},

	handle: function(json, textStatus, jqXHR) {
		if (!updater.checkStatus(json)) return;

		window.status = "Command sent successfully";

		setTimeout("window.status = '';", 3000);
	},

	start: function() {
		if (!(vars.connected && vars.attached)) return;

		$.ajax(commands.url + "start", {
			dataType: 'json',
			success: commands.handle,
			//error: updater.fail
		});
	},

	stop: function() {
		if (!(vars.connected && vars.attached)) return;

		$.ajax(commands.url + "stop", {
			dataType: 'json',
			success: commands.handle,
			//error: updater.fail
		});
	},

	chat: function() {
		if (!(vars.connected && vars.attached)) return;

		var out = '';
		var type = els.chattype.val();
		var to   = trim(els.chatto.val());
		var text = trim(els.chattext.val());

		if (type != 'Raw') {
			out += '#13#/' + type + ' ';
		}

		if (type == 'w') {
			if (to == '') {
				alert('The To field cannot be empty');
				return;
			}

			out += to + ' ';
		}

		out += text;

		if (type != 'Raw') {
			out += '#13#';
		}

		if (text == '') {
			alert('You must enter some chat text');
			return;
		}

		if (out != '') {
			$.ajax(commands.url + "chat&keys=" + escape(out), {
				dataType: 'json',
				success: commands.handle,
				error: updater.fail
			});

			els.chattext.val('');
		}
	}
};



// helper/util stuff

function setHeader(newHeader) {
	var oldHeader = els.headerdiv.children(":first").remove();
	els.hiddenheaders.append(oldHeader);
	els.headerdiv.append(newHeader);
}

function trim(str) {
	return str.replace(/^\s*/, "").replace(/\s*$/, "");
}
