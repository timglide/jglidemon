using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals;

namespace GliderRemoteCompat.Commands {
	class ClickMouse : Command {
		public static readonly Command Instance = new ClickMouse();

		private delegate bool ButtonClickFn();

		private IntPtr WindowHandle {
			get {
				return Styx.StyxWoW.Memory.WindowHandle;
			}
		}

		public override void Execute(Server server, Client client, string args) {
			args = args.ToLowerInvariant();

			//if (Win32Window.GetForegroundWindow() != WindowHandle) {
				Win32Window.SetForegroundWindow(WindowHandle);
			//}

			ButtonClickFn clickFn = null;

			switch (args) {
				case "left":  clickFn = Win32Input.LeftClick; break;
				case "right": clickFn = Win32Input.RightClick; break;
				default:
					client.Send("Error: invalid button");
					return;
			}

			if (!clickFn()) {
				client.Send("Error: {0}() returned false", clickFn.Method.Name);
				return;
			}

			client.Send(); // TODO determine if glider sent anything back
		}
	}
}
