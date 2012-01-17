using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx;
using Styx.Logic.BehaviorTree;

namespace GliderRemoteCompat.Commands {
	class StartGlide : Command {
		public override void Execute(Server server, Client client, string args) {
			if (TreeRoot.IsRunning) {
				client.Send("Already started");
				return;
			}

			TreeRoot.Start();
			client.Send("Glider started");
		}
	}
}
