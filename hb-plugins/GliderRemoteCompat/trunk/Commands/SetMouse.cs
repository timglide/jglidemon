using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals;

using GliderRemoteCompat;
using RECT = GliderRemoteCompat.Win32Window.RECT;
using System.Runtime.InteropServices;
using System.Drawing;

namespace GliderRemoteCompat.Commands {
	class SetMouse : Command {
		public static readonly Command Instance = new SetMouse();

		private IntPtr WindowHandle {
			get {
				return Styx.StyxWoW.Memory.WindowHandle;
			}
		}

		public override void Execute(Server server, Client client, string args) {
			string[] parts = args.Split('/');

			if (2 != parts.Length) {
				client.Send("Error: invalid mouse position");
				return;
			}

			double xPercent, yPercent;

			try {
				xPercent = double.Parse(parts[0]);
				yPercent = double.Parse(parts[1]);

				if (!(0d <= xPercent && xPercent < 1d)) {
					throw new ArgumentOutOfRangeException("xPercent");
				}

				if (!(0d <= yPercent && yPercent < 1d)) {
					throw new ArgumentOutOfRangeException("yPercent");
				}
			} catch {
				client.Send("Error: invalid mouse position");
				return;
			}

			IntPtr hWnd = WindowHandle;

			if (IntPtr.Zero == hWnd) {
				client.Send("Error: couldn't get window handle");
				return;
			}

			RECT clientSize = new RECT();
			if (!Win32Window.GetClientRect(hWnd, out clientSize)) {
				client.Send("Error: GetClientRect failed");
				return;
			}

			Point clientPos = new Point();
			if (!Win32Window.ClientToScreen(hWnd, ref clientPos)) {
				client.Send("Error: ClientToScreen failed");
				return;
			}

			int x = clientPos.X + (int)(xPercent * clientSize.Width);
			int y = clientPos.Y + (int)(yPercent * clientSize.Height);

			if (!Win32Input.Move(x, y)) {
				client.Send("Error: Move() returned false");
				return;
			}

			client.Send("Set mouse position: {0},{1}", x, y);
		}
	}
}
