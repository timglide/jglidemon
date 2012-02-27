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
	chatTimeouts: {},
	
	init: function() {
		els = {
			theform:				$('#theform'),
			
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

			screenshot:				$('#screenshotImage'),
			
			chat: {
				all:				$('#chat-all-container'),
				public:				$('#chat-public-container'),
				whisper:			$('#chat-whisper-container'),
				guild:				$('#chat-guild-container'),
				urgent:				$('#chat-urgent-container'),
				combat:				$('#chat-combat-container'),
				glider:				$('#chat-glider-container'),
				status:				$('#chat-status-container'),
			}
		};
		
		for (var key in els.chat) {
			updater.bindChatClick(key);
		}
	},

	bindChatClick: function(type) {
		$('a[href="#view-chat-' + type + '"]').click(function() {
			if (updater.chatTimeouts[type]) {
				clearTimeout(updater.chatTimeouts[type]);
			}
			
			var element = els.chat[type];
			
			if ('auto' != element.css('overflow')) {
				element = element.parent();
			}
			
			element.animate({
				scrollTop: element[0].scrollHeight});
			updater.updateChat(els.chat[type], true);
		});
	},
	
	url: urls.ajax + "status",
	
	update: function() {
		updater.updateStatus();
		updater.updateAllChat();
		updater.updateScreenshot();
	},
	
	updateStatus: function() {
		$.ajax(updater.url, {
			cache: false,
			dataType: 'json',
			success: updater.handleStatus,
			error:   updater.failStatus
		});
	},

	updateAllChat: function() {
		for (var key in els.chat) {
			var $container = els.chat[key];
			updater.updateChat($container);
		}
	},
	
	updateChat: function($container, force) {
		var type = $container.data('type');
		clearTimeout(updater.chatTimeouts[type]);
		
		if (!force && !($container && $container.is(':visible'))) {
			updater.chatTimeouts[type] = setTimeout(function() {
				updater.updateChat($container);
			}, settings.updateInterval);
			return;
		}
		
		var url = urls.chat + 'type=' + type + '&count=30';
		
		if ($container.data('lastUpdate')) {
			url += '&since=' + $container.data('lastUpdate').getTime();
		}
		
		$.ajax(url, {
			dataType: 'json',
			context: $container,
			success: updater.handleChat,
			error: function(jqXHR, textStatus, errorThrown) {
				updater.failChat($container, jqXHR, textStatus, errorThrown);
			}
		});
	},
	
	updateScreenshot: function() {
		if (els.screenshot.is(':visible')) {
			els.screenshot.attr('src', urls.screenshot + 'rand=' + (new Date()).getTime());
		}
		
		setTimeout(updater.updateScreenshot, settings.updateInterval);
	},

	checkStatus: function(json) {
		var status = json['status'];
		if (status != 'success') {
			return false;
		}

		return true;
	},

	formatChatTimestamp: function(date) {
		var h = date.getHours();
		var m = date.getMinutes();
		var s = date.getSeconds();
		
		if (h < 10) h = '0' + h;
		if (m < 10) m = '0' + m;
		if (s < 10) s = '0' + s;
		
		return '[' + h + ':' + m + ':' + s + '] ';
	},
	
	failChat: function($container, jqXHR, textStatus, errorThrown) {
		updater.chatTimeouts[$container.data('type')] = setTimeout(function() {
			updater.updateChat($container);
		}, settings.updateInterval);
	},
	
	handleChat: function(json, textStatus, jqXHR) {
		var newest = null;
		
		var lastTimestamp = this.data('lastUpdate');
		
		if (json.entries) {
			for (var i = 0; i < json.entries.length; i++) {
				var entry = json.entries[i];
				var timestamp = new Date();
				timestamp.setTime(entry.timestamp);
				
				if (lastTimestamp &&
						timestamp <= lastTimestamp) {
					// this entry is too old
					continue;
				}
				
				var $element = $('<p/>')
					.data('timestamp', timestamp)
					.html(updater.formatChatTimestamp(timestamp) + entry.text);
				
				this.append($element);
				newest = $element;
			}
		}
		
		while (this.children().length > settings.maxChatEntries) {
			this.children().eq(0).remove();
		}
		
		this.data('lastUpdate', new Date());
		
		if (null != newest) {
			if (newest.data('timestamp') > this.data('lastUpdate')) {
				this.data('lastUpdate', newest.data('timestamp'));
			}
			
			// this accomodates both normal and mobile versions
			var element = this;
			if ('auto' != element.css('overflow')) {
				element = element.parent();
			}
			
			element.animate({
				scrollTop: element[0].scrollHeight});
		}
		
		var self = this;
		updater.chatTimeouts[this.data('type')] = setTimeout(function() {
			updater.updateChat(self);
		}, settings.updateInterval);
	},
	

	failStatus: function(jqXHR, textStatus, errorThrown) {
//		alert('AJAX Error: ' + errorThrown + "\n" + textStatus);
		setTimeout(updater.updateStatus, settings.updateInterval);
	},
	
	handleStatus: function(json, textStatus, jqXHR) {
		if (!updater.checkStatus(json)) {
			setTimeout(updater.updateStatus, settings.updateInterval);
			return;
		}

		var app = json['app'];
		var mainTitle = '';
		var appTitle = app['name'] + ' ' + app['version'];
		vars.connected = app['connected'] == 'true';

		settings.updateInterval = app['update-interval'];

		if (!vars.connected) {
			vars.attached = false;
			mainTitle = 'Disconnected';
			setHeader(els.discheader);
		} else {
			var glider = json['glider'];
			
			vars.attached = glider['attached'] == 'true';

			if (!vars.attached) {
				mainTitle = 'Connected, Detached';
				setHeader(els.notattached);
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

				setHeader(els.mainheader);
			}
		}

		if (mainTitle != '') mainTitle += ' - ';

		document.title = mainTitle + appTitle;

		setTimeout(updater.updateStatus, settings.updateInterval);
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
			els.theform.removeClass('hideTo');
			els.chattospan.show();
		} else {
			els.theform.addClass('hideTo');
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

		els.chatto.removeClass('error');
		els.chattext.removeClass('error');
		
		if (type != 'Raw') {
			out += '#13#/' + type + ' ';
		}

		if (type == 'w') {
			if (to == '') {
				els.chatto.addClass('error');
				return;
			}

			out += to + ' ';
		}

		out += text;

		if (type != 'Raw') {
			out += '#13#';
		}

		if (text == '') {
			els.chattext.addClass('error');
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
