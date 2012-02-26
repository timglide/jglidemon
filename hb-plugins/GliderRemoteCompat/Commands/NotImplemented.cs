using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat.Commands {
	class NotImplemented : Command {
		public static readonly Command Instance = new NotImplemented();

		public override void Execute(Server server, Client client, string args) {
			client.Send("Error: not yet implemented");
		}
	}
}
