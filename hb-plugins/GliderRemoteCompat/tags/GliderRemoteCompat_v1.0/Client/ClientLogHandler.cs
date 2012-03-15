using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals;
using Styx.Helpers;
using System.Drawing;
using Styx;

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
			if (Color.White != color) {
				msg = FormatColor(color, msg);
			}

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
					BotEvents.Player.OnPlayerDied += Player_OnPlayerDied;
				} else {
					Logging.OnDebug -= Logging_OnDebug;
					BotEvents.Player.OnPlayerDied -= Player_OnPlayerDied;
				}
			}
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


		private bool combatEnabled = false;

		public bool CombatEnabled {
			get { return combatEnabled; }
			set {
				if (value == combatEnabled) return;

				combatEnabled = value;

				if (value) {
					BotEvents.Player.OnMobKilled += Player_OnMobKilled;
				} else {
					BotEvents.Player.OnMobKilled -= Player_OnMobKilled;
				}
			}
		}

		public static string FormatColor(Color color, string message) {
			return string.Format(
				"|cff{0:x2}{1:x2}{2:x2}{3}|r",
				color.R, color.G, color.B, message);
		}

		public static string FormatColor(float r, float g, float b, string message) {
			return string.Format(
				"|cff{0:x2}{1:x2}{2:x2}{3}|r",
				(int)(r * 255), (int)(g * 255), (int)(b * 255), message);
		}
	}
}
