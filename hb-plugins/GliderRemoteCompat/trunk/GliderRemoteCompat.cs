using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Reflection;

using Styx;
using Styx.Helpers;
using Styx.Plugins;
using Styx.WoWInternals;
using Styx.WoWInternals.WoWObjects;
using System.Net.Sockets;
using System.Windows.Forms;
using System.Drawing;
using System.Text.RegularExpressions;
using System.IO;
using System.Diagnostics;
using System.Threading;
using Styx.Common;

namespace GliderRemoteCompat {
	public class GliderRemoteCompat : HBPlugin {
		private const string _revision = "$Revision$";
		private static readonly int revision;
		private static readonly Version version;
		private static string pluginPath = null;

		static GliderRemoteCompat() {
			string str = string.Empty;

			try {
				str = _revision.Substring(11, _revision.Length - 2 - 11);
				revision = int.Parse(str);
			} catch {
				revision = Updater.GetInstalledRevision();
			}

			version = new Version(1, 1, 0, revision);
		}

		public static string PluginPath {
			get {
				if (null == pluginPath)
					pluginPath = GetPluginPath();
				return pluginPath;
			}
		}

		private static string GetPluginPath() {   // taken from Singular.
			// bit of a hack, but location of source code for assembly is only.
			var asmName = Assembly.GetExecutingAssembly().GetName().Name;
			var len = asmName.LastIndexOf("_", StringComparison.Ordinal);
			var folderName = asmName.Substring(0, len);

			var botsPath = GlobalSettings.Instance.PluginsPath;
			if (!Path.IsPathRooted(botsPath)) {
				botsPath = Path.Combine(Utilities.AssemblyDirectory, botsPath);
			}
			return Path.Combine(botsPath, folderName);
		}

		private Server server;

		private Queue<string> logQueue = new Queue<string>();
		private string logFile = null;
 
		public string LogFile {
			get {
				if (null == logFile) {
					string path = PluginPath + "\\Logs";

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

		public GliderRemoteCompat() {
			//logStream = new BufferedStream(new FileStream(LogFile, FileMode.Create));
			//logWriter = new StreamWriter(logStream);

			//AppDomain.CurrentDomain.UnhandledException += UnhandledException;

			//logThread = new Thread(LogThreadRunner);
			//logThread.Name = "GRC LogThread";
			//logThread.IsBackground = true;
			//logThread.Priority = ThreadPriority.BelowNormal;
			//logThread.Start();
		}

		~GliderRemoteCompat() {
			//logThread.Abort();
			//AppDomain.CurrentDomain.UnhandledException -= UnhandledException;

			//logWriter.Close();
			//logStream.Close();
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
				return true;
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
			initialized = true;

			Logging.Write("{0} v{1} loaded", Name, Version);
			RefreshSettings();

			new Thread(Updater.CheckForUpdate) { Name = "GRC-Updater" }.Start();
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
			if (!initialized)
				return;

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
