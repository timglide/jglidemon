using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals;

namespace GliderRemoteCompat.Commands {
	class RunLua : Command {
		public static readonly Command Instance = new RunLua();

		public override void Execute(Server server, Client client, string args) {
			if ("" == args) {
				client.Send();
				return;
			}

			List<string> retValues = Lua.GetReturnValues(args);
			client.Send(retValues);
		}
	}
}
