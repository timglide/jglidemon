using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx;
using Styx.CommonBot;

namespace GliderRemoteCompat {
	partial class ClientLogHandler {
		private void Player_OnMobKilled(BotEvents.Player.MobKilledEventArgs args) {
			AddCombatMessage(string.Format("You have slain {0}!", args.KilledMob.Name));
		}
	}
}
