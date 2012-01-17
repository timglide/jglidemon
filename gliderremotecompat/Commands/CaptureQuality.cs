using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat.Commands {
	class CaptureQuality : Command {
		public override void Execute(Server server, Client client, string args) {
			try {
				if ("" != args) {
					client.settings.CaptureQuality = int.Parse(args);
				}

				client.Send("Quality set: " + (int)Math.Round(client.settings.CaptureQuality * 100));
			} catch (FormatException x) {
				client.Send("Error: " + x.Message);
			} catch (ArgumentException x) {
				client.Send("Error: " + x.Message);
			}
		}
	}
}
