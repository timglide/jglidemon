using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat.Commands {
	class CaptureScale : Command {
		public override void Execute(Server server, Client client, string args) {
			try {
				if ("" != args) {
					client.settings.CaptureScale = int.Parse(args);
				}

				client.Send("Scale set: " + (int)Math.Round(client.settings.CaptureScale * 100));
			} catch (FormatException x) {
				client.Send("Error: " + x.Message);
			} catch (ArgumentException x) {
				client.Send("Error: " + x.Message);
			}
		}
	}
}
