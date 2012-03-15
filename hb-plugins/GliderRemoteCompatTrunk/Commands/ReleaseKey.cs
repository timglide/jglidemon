using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.Helpers;

namespace GliderRemoteCompat.Commands {
	class ReleaseKey : Command {
		public static readonly Command Instance = new ReleaseKey();

		public override void Execute(Server server, Client client, string args) {
			int keyCode = int.Parse(args);
			KeyboardManager.ReleaseKey((char)keyCode);
			client.Send(); // TODO determine if Glider sent any text back
		}
	}
}
