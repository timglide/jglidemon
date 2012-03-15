using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat.Commands {
	class Log : Command {
		public static readonly Command Instance = new Log();

		public override void Execute(Server server, Client client, string args) {
			try {
				if ("" != args) {
					client.settings.SetLogState(args, true);
					client.UpdateLogSettings();
				}

				client.Send("Log: " + client.FormattedLogChannels);
			} catch (ArgumentException x) {
				client.Send("Error: " + x.Message);
			}
		}
	}
}
