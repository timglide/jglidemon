using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx;
using System.Drawing;

namespace GliderRemoteCompat {
	partial class ClientLogHandler {
		private void Player_OnPlayerDied() {
			if (StyxWoW.IsInGame && StyxWoW.IsInWorld) {
				client.SendLog(ClientLogType.GliderLog, "Died while gliding");
			}
		}
	}
}
