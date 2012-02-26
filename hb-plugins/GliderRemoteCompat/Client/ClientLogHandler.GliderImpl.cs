using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx;
using System.Drawing;

namespace GliderRemoteCompat {
	partial class ClientLogHandler {
		private void Logging_OnDebug(string msg, Color color) {
			client.SendLog(ClientLogType.GliderLog, msg);
		}

		private void Player_OnPlayerDied() {
			client.SendLog(ClientLogType.GliderLog, "Died while gliding");
		}
	}
}
