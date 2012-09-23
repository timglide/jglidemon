using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using Styx.WoWInternals;
using Styx.Helpers;
using System.Drawing;

namespace GliderRemoteCompat.Commands {
	class QueueKeys : Command {
		public static readonly Command Instance = new QueueKeys();

		private static readonly Regex VKRegex = new Regex("#\\d+#");

		public override void Execute(Server server, Client client, string args) {
			if ("" == args) {
				client.Send();
				return;
			}

			// see if this is a JGlideMon style chat command, JGM added manually
			// pressing enter before and after

			if (args.StartsWith("#13#/")) {
				args = args.Substring(4); // remove leading #13#

				if (args.EndsWith("#13#")) {
					args = args.Substring(0, args.Length - 4); // remove trailing #13#
				}
			}
			
			if (VKRegex.IsMatch(args)) {
				// ignore anything that requires sending VK codes
			} else if (args.StartsWith("/")) {
				// escape so it's a valid Lua string (order of replacement is important)
				args = args.Replace("\\", "\\\\").Replace("\"", "\\\"");
				args = string.Format("RunMacroText(\"{0}\")", args);
				Lua.DoString(args, 0);
			} else {
				// ignore anything that isn't a slash command
			}

			client.Send();
		}
	}
}
