using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat.Commands {
	class Exit : Command {
		public static readonly Command Instance = new Exit();

		public override void Execute(Server server, Client client, string args) {
			client.Send("Bye!", false);
			client.Debug("Got exit command, disposing");
			client.Dispose();
		}
	}
}
