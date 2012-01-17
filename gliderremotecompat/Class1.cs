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

		private static readonly Version version = new Version(0, 1);

		public override Version Version {
			get { return version; }
		}


		private bool initialized = false;

		public override void Initialize() {
			if (initialized) return;
			base.Initialize();

			Logging.Write("{0} v{1} loaded, starting server 8", Name, Version);
			logQueue.Clear();
			server = new Server();

			initialized = true;
		}

		public override void Dispose() {
			if (!initialized) return;

			if (null != server) {
				server.Dispose();
			}
			
			base.Dispose();
			initialized = false;
			Logging.Write("{0} unloaded", Name);
		}

		public void Log(string format, params string[] args) {
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
