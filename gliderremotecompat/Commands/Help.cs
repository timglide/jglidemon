using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat.Commands {
	class Help : Command {
		public override void Execute(Server server, Client client, string args) {
			client.Send(new string[] {
				//string.Format("Connected to {0}'s {1} v{2}", Class1.Instance.Author, Class1.Instance.Name, Class1.Instance.Version),
				"Connected to timglide's GliderRemoteCompat",
				"/exit                         - shut down this connection",
				"/exitglider                   - shut down Glider completely",
				"/status                       - return current status of the game/char",
				"/version                      - return Glider and game version info"
			});
		}
	}
}
