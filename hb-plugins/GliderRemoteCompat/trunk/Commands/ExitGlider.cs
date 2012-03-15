using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.Threading;

namespace GliderRemoteCompat.Commands {
	class ExitGlider : Command {
		public static readonly Command Instance = new ExitGlider();

		private const int ProcessExitTimeoutMS = 10000;

		public override void Execute(Server server, Client client, string args) {
			client.Send();

			Thread t = new Thread(ExitHB);
			t.Name = "ExitHB";
			t.IsBackground = true;
			t.Start();
		}

		private void ExitHB() {
			try {
				Process p = Process.GetCurrentProcess();
				p.CloseMainWindow();
				p.WaitForExit(ProcessExitTimeoutMS);
				p.Kill();
			} catch { }
		}
	}
}
