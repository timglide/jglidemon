using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat.Commands {
	class NoLog : Command {
		public override void Execute(Server server, Client client, string args) {
			try {
				if ("" != args) {
					client.settings.SetLogState(args, false);
					client.UpdateLogSettings();
				}

				client.Send("Log: " + client.FormattedLogChannels);
			} catch (ArgumentException x) {
				client.Send("Error: " + x.Message);
			}
		}
	}
}
