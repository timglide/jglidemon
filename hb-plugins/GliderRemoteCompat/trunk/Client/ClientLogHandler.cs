using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals;
using Styx.Helpers;
using System.Drawing;
using Styx;
using Styx.Common;
using System.Collections.ObjectModel;
using Styx.CommonBot;
using AT.MIN;
using System.Text.RegularExpressions;

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

		private bool isLogEventAttached = false;

		private void CheckLogEventAttached() {
			if (statusEnabled || gliderLogEnabled) {
				if (!isLogEventAttached) {
					Logging.OnLogMessage += Logging_OnLogMessage;
					isLogEventAttached = true;
				}
			} else {
				if (isLogEventAttached) {
					Logging.OnLogMessage -= Logging_OnLogMessage;
					isLogEventAttached = false;
				}
			}
		}

		private bool statusEnabled = false;

		public bool StatusEnabled {
			get { return statusEnabled; }
			set {
				if (value == statusEnabled) return;

				statusEnabled = value;
				CheckLogEventAttached();
			}
		}

		private void Logging_OnLogMessage(ReadOnlyCollection<Logging.LogMessage> messages) {
			foreach (Logging.LogMessage m in messages) {
				switch (m.Level) {
					case LogLevel.Normal:
					case LogLevel.Quiet:
						if (statusEnabled) {
							SendLog(ClientLogType.Status, m);
						}
						break;

					case LogLevel.Diagnostic:
					case LogLevel.Verbose:
						if (gliderLogEnabled) {
							SendLog(ClientLogType.GliderLog, m);
						}
						break;
				}
			}
		}

		private void SendLog(ClientLogType type, Logging.LogMessage message) {
			string msg = message.Message;

			if (Color.White != message.Color) {
				msg = FormatColor(message.Color, msg);
			}

			client.SendLog(type, msg);
		}

		private bool gliderLogEnabled = false;

		public bool GliderLogEnabled {
			get { return gliderLogEnabled; }
			set {
				if (value == gliderLogEnabled) return;

				gliderLogEnabled = value;
				CheckLogEventAttached();

				if (value) {
					BotEvents.Player.OnPlayerDied += Player_OnPlayerDied;
				} else {
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

		public static string FormatColor(System.Drawing.Color color, string message) {
			return string.Format(
				"|cff{0:x2}{1:x2}{2:x2}{3}|r",
				color.R, color.G, color.B, message);
		}

		public static string FormatColor(System.Windows.Media.Color color, string message) {
			return string.Format(
				"|cff{0:x2}{1:x2}{2:x2}{3}|r",
				color.R, color.G, color.B, message);
		}

		public static string FormatColor(float r, float g, float b, string message) {
			return string.Format(
				"|cff{0:x2}{1:x2}{2:x2}{3}|r",
				(int)(r * 255), (int)(g * 255), (int)(b * 255), message);
		}


		private static readonly Regex COUNT_CHECK_PATTERN = new Regex("\\|4([^:]*):([^;]*);");

		/// <summary>
		/// Performs additional WoW-specific processing before passing off to sprintf,
		/// namely checking for strings like "|4request:requests;"
		/// </summary>
		/// <param name="fmt"></param>
		/// <param name="args"></param>
		/// <returns></returns>
		public static string sprintf(string fmt, params object[] args) {
			// assume this only occurs once per format string and uses the first argument
			if (args.Length > 0) {
				Match m = COUNT_CHECK_PATTERN.Match(fmt);

				if (null != m && m.Success) {
					int i;

					if (int.TryParse(args[0].ToString(), out i)) {
						fmt = fmt.Substring(0, m.Index) +
							(1 == i ? m.Groups[1].Value : m.Groups[2].Value) +
							fmt.Substring(m.Index + m.Length);
					}
				}
			}


			return Tools.sprintf(fmt, args);
		}
	}
}
