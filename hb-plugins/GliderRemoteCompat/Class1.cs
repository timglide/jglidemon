using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Reflection;

using Styx;
using Styx.Helpers;
using Styx.Logic;
using Styx.Plugins;
using Styx.Combat.CombatRoutine;
using Styx.Logic.Combat;
using Styx.Logic.Questing;
using Styx.Plugins.PluginClass;
using Styx.WoWInternals;
using Styx.WoWInternals.WoWObjects;
using System.Net.Sockets;
using System.Windows.Forms;
using System.Drawing;
using System.Text.RegularExpressions;

namespace GliderRemoteCompat {
	public class Class1 : HBPlugin {
		private static Class1 instance;

		public static Class1 Instance {
			get {
				if (null == instance) {
					instance = new Class1();
				}

				return instance;
			}
		}

		private const string _revision = "$Revision$";
		private static readonly int revision;
		private static readonly Version version;

		static Class1() {
			string str = string.Empty;

			try {
				str = _revision.Substring(11, _revision.Length - 2 - 11);
				revision = int.Parse(str);
			} catch {
				revision = 0;
			}

			version = new Version(0, 1, 0, revision);
		}

		private Server server;
		private List<string> logQueue = new List<string>();

		public Class1() {
			instance = this;
		}

		public override string Author {
			get { return "timglide"; }
		}

		public override string Name {
			get { return "GliderRemoteCompat"; }
		}

		public override Version Version {
			get { return version; }
		}

		public override bool WantButton {
			get {
				return initialized;
			}
		}

		public override string ButtonText {
			get {
				return "Settings";
			}
		}

		private bool initialized = false;

		public override void Initialize() {
			if (initialized) return;
			base.Initialize();

			Logging.Write("{0} v{1} loaded", Name, Version);
			logQueue.Clear();
			RefreshSettings();

			initialized = true;
		}

		public override void Dispose() {
			lock (this) {
				if (!initialized) return;

				if (null != server) {
					server.Dispose();
					server = null;
				}

				if (null != settingsForm) {
					settingsForm.Dispose();
					settingsForm = null;
				}

				base.Dispose();
				initialized = false;
				Logging.Write("{0} unloaded", Name);
			}
		}

		public void RefreshSettings() {
			lock (this) {
				if (null != server) {
					server.Dispose();
					server = null;
				}

				try {
					server = new Server();
				} catch (SocketException e) {
					Logging.Write(Color.Red, "Error starting GliderRemoteCompat server");
					Logging.WriteException(Color.Red, e);
					MessageBox.Show(
						"The port is already in use. You must change it in the settings.",
						"GliderRemoteCompat", MessageBoxButtons.OK, MessageBoxIcon.Error);
				} catch (Exception e) {
					Logging.Write(Color.Red, "Error starting GliderRemoteCompat server");
					Logging.WriteException(Color.Red, e);
					MessageBox.Show(
						"Error starting server:\n\n" + e,
						"GliderRemoteCompat", MessageBoxButtons.OK, MessageBoxIcon.Error);
				}
			}
		}


		private SettingsForm settingsForm;

		private SettingsForm SettingsForm {
			get {
				if (null == settingsForm) {
					settingsForm = new SettingsForm();
				}

				return settingsForm;
			}
		}

		public override void OnButtonPress() {
			if (!initialized) return;
			SettingsForm.ShowDialog();
		}

		public void Log(string format, params object[] args) {
			Log(string.Format(format, args));
		}

		public void Log(string str) {
			lock (logQueue) {
				logQueue.Add(str);
			}
		}

		public override void Pulse() {
			//lock (logQueue) {
			//    while (logQueue.Count > 0) {
			//        Logging.Write(logQueue[0]);
			//        logQueue.RemoveAt(0);
			//    }
			//}
		}
	}
}
