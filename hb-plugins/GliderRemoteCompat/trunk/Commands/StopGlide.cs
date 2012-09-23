﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx;
using Styx.CommonBot;

namespace GliderRemoteCompat.Commands {
	class StopGlide : Command {
		public static readonly Command Instance = new StopGlide();

		public override void Execute(Server server, Client client, string args) {
			if (!TreeRoot.IsRunning) {
				client.Send("Already stopped");
				return;
			}

			TreeRoot.Stop();
			client.Send("Glider stopped");
		}
	}
}
