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
	mobsTimeout: null,
	lootTimeout: null,
	
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
			ajaxerror:				$('#ajaxerror'),
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
			},
			
			mobs: {
				tab: $('#view-mobs'),
				mobs: $('#mobs-table'),
				reps: $('#reps-table'),
				skills: $('#skills-table')
			},
			
			
			lootTab: $('#view-loot'),
			
			goldPanels: {
				goldLooted:  $('#gold-looted-container'),
				lootWorth:   $('#loot-worth-container'),
				goldPerHour: $('#gold-per-hour-container')
			},
			
			loot: {
				poor: $('#loot-table-poor'),
				common: $('#loot-table-common'),
				uncommon: $('#loot-table-uncommon'),
				rare: $('#loot-table-rare'),
				epic: $('#loot-table-epic')
			}
		};
		
		for (var key in els.goldPanels) {
			var replace = {
				container: els.goldPanels[key]
			};
			replace.g = $('span.gold',   replace.container[0]);
			replace.s = $('span.silver', replace.container[0]);
			replace.c = $('span.copper', replace.container[0]);
			els.goldPanels[key] = replace;
		}
		
		for (var key in els.chat) {
			updater.bindChatClick(key);
		}
		
		updater.bindMobsClick();
		updater.bindLootClick();
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

	bindMobsClick: function() {
		$('a[href="#view-mobs"]').click(function() {
			if (updater.mobsTimeout) {
				clearTimeout(updater.mobsTimeout);
			}
			
			updater.updateMobs(true);
		});
	},
	
	bindLootClick: function() {
		$('a[href="#view-loot"]').click(function() {
			if (updater.lootTimeout) {
				clearTimeout(updater.lootTimeout);
			}
			
			updater.updateLoot(true);
		});
	},
	
	url: urls.ajax + "status",
	
	update: function() {
		updater.updateStatus();
		updater.updateAllChat();
		updater.updateScreenshot();
		updater.updateMobs();
		updater.updateLoot();
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
		
		var url = urls.chat + 'type=' + type + '&count=50';
		
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
	
	updateMobs: function(force) {
		var $container = els.mobs.tab;
		clearTimeout(updater.mobsTimeout);
		
		if (!force && !($container && $container.is(':visible'))) {
			updater.mobsTimeout = setTimeout(function() {
				updater.updateLoot();
			}, settings.updateInterval);
			return;
		}
		
		var url = urls.mobs;
		
		if ($container.data('lastUpdate')) {
			url += 'since=' + $container.data('lastUpdate').getTime();
		}
		
		$.ajax(url, {
			dataType: 'json',
			context: $container,
			success: updater.handleMobs,
			error: function(jqXHR, textStatus, errorThrown) {
				updater.failMobs(jqXHR, textStatus, errorThrown);
			}
		});
	},
	
	updateLoot: function(force) {
		var $container = els.lootTab;
		clearTimeout(updater.lootTimeout);
		
		if (!force && !($container && $container.is(':visible'))) {
			updater.lootTimeout = setTimeout(function() {
				updater.updateLoot();
			}, settings.updateInterval);
			return;
		}
		
		var url = urls.loot;
		
		if ($container.data('lastUpdate')) {
			url += 'since=' + $container.data('lastUpdate').getTime();
		}
		
		$.ajax(url, {
			dataType: 'json',
			context: $container,
			success: updater.handleLoot,
			error: function(jqXHR, textStatus, errorThrown) {
				updater.failLoot(jqXHR, textStatus, errorThrown);
			}
		});
	},
	
	checkStatus: function(json) {
		var status = json['status'];
		if (status != 'success') {
			return false;
		}

		return true;
	},

	formatChatTimestamp: function(date) {
		if ('object' != typeof date) {
			date = new Date(date);
		}
		
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
		
		setHeader(els.ajaxerror);
	},
	
	handleChat: function(json, textStatus, jqXHR) {
		var
			newest = null,
			lastTimestamp = this.data('lastUpdate');
		
		if (json.entries) {
			for (var i = 0; i < json.entries.length; i++) {
				var
					entry = json.entries[i],
					timestamp = new Date();
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
		
		
		if (null != newest) {
			// if there were no new entries we keep the old date
			// which can help if there is some lag that would cause
			// us to miss an entry
			this.data('lastUpdate', new Date());
			
			if (newest.data('timestamp') > this.data('lastUpdate')) {
				this.data('lastUpdate', newest.data('timestamp'));
			}

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
	
	failMobs: function(jqXHR, textStatus, errorThrown) {
		updater.mobsTimeout = setTimeout(function() {
			updater.updateMobs();
		}, settings.updateInterval);
		
		setHeader(els.ajaxerror);
	},
	
	mobsTableSort: function(a, b) {
		var countA = parseInt(a.e.children().eq(0).text()),
			countB = parseInt(b.e.children().eq(0).text());
	
		// higher count should come first (descending order)
		if (countA < countB) return 1;
		if (countA > countB) return -1;
		
		var nameA = a.e.children().eq(2).text(),
		nameB = a.e.children().eq(2).text();
	
		if (nameA == nameB) return 0;
		if (nameA < nameB) return -1;
		return 1;
	},
	
	repsTableSort: function(a, b) {
		var levelA = parseInt(a.e.children().eq(1).text()),
			levelB = parseInt(b.e.children().eq(1).text());
	
		// higher level should come first (descending order)
		if (levelA < levelB) return 1;
		if (levelA > levelB) return -1;
		
		var timeA = a.e.data('time'),
			timeB = a.e.data('time');
		
		if (timeA == timeB) return 0;
		if (timeA < timeB) return -1;
		return 1;
	},
	
	skillsTableSort: function(a, b) {
		var levelA = parseInt(a.e.children().eq(1).text()),
			levelB = parseInt(b.e.children().eq(1).text());
	
		// higher level should come first (descending order)
		if (levelA < levelB) return 1;
		if (levelA > levelB) return -1;
		
		var timeA = a.e.data('time'),
			timeB = a.e.data('time');
		
		if (timeA == timeB) return 0;
		if (timeA < timeB) return -1;
		return 1;
	},
	
	handleMobs: function(json, textStatus, jqXHR) {
		var newest = null,
			lastTimestamp = this.data('lastUpdate');
		
		if (json.mobs) {
			for (var i = 0; i < json.mobs.length; i++) {
				var entry  = json.mobs[i],
					$table = els.mobs.mobs,
					$existing = $('tr[data-mob="' + entry.name + '"]', $table[0]);
				
				if ($existing.length) {
					$existing.children().eq(0).text(entry.qty);
					$existing.children().eq(1).text(entry.avgXp);
					newest = $existing;
					continue;
				}
				
				var $element = $('<tr/>')
					// attr() not data(), need to see if data always sets attribute as well
					.attr('data-mob', entry.name)
					.append($('<td class="count">' + entry.qty + '</td>'))
					.append($('<td class="xp">' + entry.avgXp + '</td>'))
					.append($('<td/>').text(entry.name));
				
				$table.append($element);
				newest = $element;
			}

			$('tr', els.mobs.mobs).tsort('', {
				sortFunction: updater.mobsTableSort
			});
		}
		
		if (json.rep) {
			for (var i = 0; i < json.rep.length; i++) {
				var entry  = json.rep[i],
					$table = els.mobs.reps,
					$existing = $('tr[data-faction="' + entry.faction + '"]', $table[0]);
				
				if ($existing.length) {
					$existing.data('time', entry.time);
					$existing.children().eq(0).text(updater.formatChatTimestamp(entry.time));
					$existing.children().eq(1).text(entry.gained);
					newest = $existing;
					continue;
				}
				
				var $element = $('<tr/>')
					// attr() not data(), need to see if data always sets attribute as well
					.attr('data-faction', entry.faction)
					.data('time', entry.time)
					.append($('<td class="time">' + updater.formatChatTimestamp(entry.time) + '</td>'))
					.append($('<td class="gain">' + entry.gained + '</td>'))
					.append($('<td/>').text(entry.faction));
				
				$table.append($element);
				newest = $element;
			}
			
			$('tr', els.mobs.reps).tsort('', {
				sortFunction: updater.repsTableSort
			});
		}
		
		if (json.skills) {
			for (var i = 0; i < json.skills.length; i++) {
				var entry  = json.skills[i],
					$table = els.mobs.skills,
					$existing = $('tr[data-skill="' + entry.skill + '"]', $table[0]);
				
				if ($existing.length) {
					$existing.data('time', entry.time);
					$existing.children().eq(0).text(updater.formatChatTimestamp(entry.time));
					$existing.children().eq(1).text(entry.level);
					newest = $existing;
					continue;
				}
				
				var $element = $('<tr/>')
					// attr() not data(), need to see if data always sets attribute as well
					.attr('data-skill', entry.skill)
					.data('time', entry.time)
					.append($('<td class="time">' + updater.formatChatTimestamp(entry.time) + '</td>'))
					.append($('<td class="level">' + entry.level + '</td>'))
					.append($('<td/>').text(entry.skill));
				
				$table.append($element);
				newest = $element;
			}
			
			$('tr', els.mobs.skills).tsort('', {
				sortFunction: updater.skillsTableSort
			});
		}
		
		if (null != newest) {
			// if there were no new entries we keep the old date
			// which can help if there is some lag that would cause
			// us to miss an entry
			this.data('lastUpdate', new Date());
			
			if (newest.data('timestamp') > this.data('lastUpdate')) {
				this.data('lastUpdate', newest.data('timestamp'));
			}
		}
		
		updater.mobsTimeout = setTimeout(function() {
			updater.updateMobs();
		}, settings.updateInterval);
	},
	
	failLoot: function(jqXHR, textStatus, errorThrown) {
		updater.lootTimeout = setTimeout(function() {
			updater.updateLoot();
		}, settings.updateInterval);
		
		setHeader(els.ajaxerror);
	},
	
	copperToGSC: function(copper) {
		// i = 1234567 (123.45.67)
		
		var ret = {};
		
		var i = Math.floor(copper / 100); // 12345 (123.45)
		ret.g = Math.floor(i / 100);      // 123
		ret.s = i - ret.g * 100;          // 12345 - 12300 = 45
		ret.c = copper - i * 100;         // 1234567 - 1234500 = 67
		
		return ret;
	},
	
	lootTableSort: function(a, b) {
		var countA = parseInt(a.e.children().eq(2).text()),
			countB = parseInt(b.e.children().eq(2).text());
	
		// higher count should come first (descending order)
		if (countA < countB) return 1;
		if (countA > countB) return -1;
		
		var nameA = a.e.children().eq(1).text(),
			nameB = a.e.children().eq(1).text();
		
		if (nameA == nameB) return 0;
		if (nameA < nameB) return -1;
		return 1;
	},
	
	handleLoot: function(json, textStatus, jqXHR) {
		var newest = null,
			lastTimestamp = this.data('lastUpdate');
		
		if (json.goldLooted) {
			var gsc = updater.copperToGSC(json.goldLooted),
				cur = els.goldPanels.goldLooted;
			cur.g.text(gsc.g).parent().toggle(0 != gsc.g);
			cur.s.text(gsc.s).parent().toggle(0 != gsc.s);
			cur.c.text(gsc.c);
		}
		
		if (json.lootWorth) {
			var gsc = updater.copperToGSC(json.lootWorth),
				cur = els.goldPanels.lootWorth;
			cur.g.text(gsc.g).parent().toggle(0 != gsc.g);
			cur.s.text(gsc.s).parent().toggle(0 != gsc.s);
			cur.c.text(gsc.c);
		}
		
		if (json.runtime) {
			var copper = Math.floor((json.goldLooted + json.lootWorth) / (json.runtime / 1000 / 60 / 60)),
				gsc = updater.copperToGSC(copper),
				cur = els.goldPanels.goldPerHour;
			cur.g.text(gsc.g).parent().toggle(0 != gsc.g);
			cur.s.text(gsc.s).parent().toggle(0 != gsc.s);
			cur.c.text(gsc.c);
		}
		
		if (json.loot) {
			for (var i = 0; i < json.loot.length; i++) {
				var entry  = json.loot[i],
					$table = els.loot[entry.qualityName],
					$existing = $('#loot-row-' + entry.id);
				
				if ($existing.length) {
					$existing.children().eq(2).text(entry.qty);
					newest = $existing;
					continue;
				}
				
				var $element = $('<tr/>')
						.attr('id', 'loot-row-' + entry.id)
						.append($('<td class="icon"><img src="' + settings.iconBase + entry.icon + settings.iconExt + '"/></td>')),
					$td = $('<td/>'),
					$a  = $('<a class="' + entry.qualityName + '" target="_blank" href="http://www.wowhead.com/item=' + entry.id + '"/>')
						.text('[' + entry.name + ']');
				
				$td.append($a);
				$element
					.append($td)
					.append('<td class="qty">' + entry.qty + '</td>');
				$table.append($element);
				newest = $element;
			}
			
			for (var key in els.loot) {
				$('tr', els.loot[key][0]).tsort('', {
					sortFunction: updater.lootTableSort
				});
			}
		}		
		
		if (null != newest) {
			// if there were no new entries we keep the old date
			// which can help if there is some lag that would cause
			// us to miss an entry
			this.data('lastUpdate', new Date());
			
			if (newest.data('timestamp') > this.data('lastUpdate')) {
				this.data('lastUpdate', newest.data('timestamp'));
			}
		}
		
		updater.lootTimeout = setTimeout(function() {
			updater.updateLoot();
		}, settings.updateInterval);
	},
	

	failStatus: function(jqXHR, textStatus, errorThrown) {
//		alert('AJAX Error: ' + errorThrown + "\n" + textStatus);
		setTimeout(updater.updateStatus, settings.updateInterval);
		
		setHeader(els.ajaxerror);
	},
	
	handleStatus: function(json, textStatus, jqXHR) {
		if (!updater.checkStatus(json)) {
			setTimeout(updater.updateStatus, settings.updateInterval);
			return;
		}

		var app = json['app'],
			mainTitle = '',
			appTitle = app['name'] + ' ' + app['version'];
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
