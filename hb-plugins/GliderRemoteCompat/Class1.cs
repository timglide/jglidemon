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
using System.IO;
using System.Diagnostics;
using System.Threading;

namespace GliderRemoteCompat {
	public class Class1 : HBPlugin {
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

		private Queue<string> logQueue = new Queue<string>();
		private string logFile = null;
 
		public string LogFile {
			get {
				if (null == logFile) {
					string path = Process.GetCurrentProcess().MainModule.FileName;
					path = Path.GetDirectoryName(path);
					path = Path.Combine(path, @"Plugins\GliderRemoteCompat\Logs");

					if (!Directory.Exists(path)) {
						Directory.CreateDirectory(path);
					}

					string file = string.Format("{0:yyyy-MM-dd HH-mm-ss}.txt", DateTime.Now);
					logFile = Path.Combine(path, file);
				}

				return logFile;
			}
		}

		private Stream logStream;
		private StreamWriter logWriter;
		private Thread logThread;

		public Class1() {
			logStream = new BufferedStream(new FileStream(LogFile, FileMode.Create));
			logWriter = new StreamWriter(logStream);

			AppDomain.CurrentDomain.UnhandledException += UnhandledException;

			logThread = new Thread(LogThreadRunner);
			logThread.Name = "GRC LogThread";
			logThread.IsBackground = true;
			logThread.Start();
		}

		~Class1() {
			logThread.Abort();
			AppDomain.CurrentDomain.UnhandledException -= UnhandledException;

			logWriter.Close();
			logStream.Close();
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
					server = new Server(this);
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
					settingsForm = new SettingsForm(this);
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

		public void Log(object obj) {
			lock (logQueue) {
				logQueue.Enqueue(obj.ToString());
			}
		}

		private void UnhandledException(object sender, UnhandledExceptionEventArgs e) {
			Log(e.ExceptionObject);
		}

		private void LogThreadRunner() {
			try {
				while (true) {
					lock (logQueue) {
						while (0 != logQueue.Count) {
							logWriter.WriteLine(logQueue.Dequeue());
						}
					}

					try {
						Thread.Sleep(100);
					} catch (ThreadInterruptedException) { }
				}
			} catch (ThreadAbortException) { }
		}

		public override void Pulse() {

		}
	}
}
