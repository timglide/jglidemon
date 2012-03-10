using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals;

namespace GliderRemoteCompat.Commands {
	class ClickMouse : Command {
		public static readonly Command Instance = new ClickMouse();

		private IntPtr WindowHandle {
			get {
				return ObjectManager.WoWProcess.MainWindowHandle;
			}
		}

		public override void Execute(Server server, Client client, string args) {
			args = args.ToLowerInvariant();
			bool result;

			if (Win32Window.GetForegroundWindow() != WindowHandle) {
				Win32Window.SetForegroundWindow(WindowHandle);
			}

			switch (args) {
				case "left":  result = Win32Input.LeftClick();  break;
				case "right": result = Win32Input.RightClick(); break;
				default:
					client.Send("Error: invalid button");
					return;
			}

			if (!result) {
				client.Send("Error: Click() returned false");
				return;
			}

			client.Send(); // TODO determine if glider sent anything
		}
	}
}
