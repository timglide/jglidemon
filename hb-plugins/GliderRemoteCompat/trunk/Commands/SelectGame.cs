using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals;
using Styx.MemoryManagement;
using Styx;

namespace GliderRemoteCompat.Commands {
	class SelectGame : Command {
		public static readonly Command Instance = new SelectGame();

		private IntPtr WindowHandle {
			get {
				return StyxWoW.Memory.WindowHandle;
			}
		}

		public override void Execute(Server server, Client client, string args) {
			//if (Win32Window.GetForegroundWindow() != WindowHandle) {
				Win32Window.SetForegroundWindow(WindowHandle);
			//}

			client.Send();
		}
	}
}
