using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals;
using Styx.Helpers;
using System.Drawing;

namespace GliderRemoteCompat {
	partial class ClientLogHandler : IDisposable {
		private Client client;

		public ClientLogHandler(Client client) {
			this.client = client;
		}

		public void Dispose() {
			StatusEnabled = false;
			GliderLogEnabled = false;
			ChatEnabled = false;
		}

		private bool statusEnabled = false;

		public bool StatusEnabled {
			get { return statusEnabled; }
			set {
				if (value == statusEnabled) return;

				statusEnabled = value;

				if (value) {
					Logging.OnWrite += Logging_OnWrite;
				} else {
					Logging.OnWrite -= Logging_OnWrite;
				}
			}
		}

		private void Logging_OnWrite(string msg, Color color) {
			client.SendLog(ClientLogType.Status, msg);
		}


		private bool gliderLogEnabled = false;

		public bool GliderLogEnabled {
			get { return gliderLogEnabled; }
			set {
				if (value == gliderLogEnabled) return;

				gliderLogEnabled = value;

				if (value) {
					Logging.OnDebug += Logging_OnDebug;
				} else {
					Logging.OnDebug -= Logging_OnDebug;
				}
			}
		}

		private void Logging_OnDebug(string msg, Color color) {
			client.SendLog(ClientLogType.GliderLog, msg);
		}


		private bool chatEnabled = false;

		public bool ChatEnabled {
			get { return chatEnabled; }
			set {
				if (value == chatEnabled) return;

				chatEnabled = value;

				if (value) {
					//client.Debug("Attaching LUA chat events");

					foreach (string e in ChatEvents) {
						Lua.Events.AttachEvent(e, Lua_ChatMsg);
					}
				} else {
					//client.Debug("Detaching LUA chat events");

					foreach (string e in ChatEvents) {
						Lua.Events.DetachEvent(e, Lua_ChatMsg);
					}
				}
			}
		}
	}
}
