using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Threading;
using System.IO;
using Styx.Helpers;
using System.Text.RegularExpressions;

namespace GliderRemoteCompat {
	class Client : IDisposable {
		private const int ThreadDeathTimeoutMS = 500;

		private static readonly Encoding encoder = Encoding.UTF8;
		private static readonly byte[] emptyBytes = new byte[0];
		private static readonly byte[] dashesBytes = encoder.GetBytes("---\r\n");

		internal ClientSettings
//			lastSettings = new ClientSettings(),
			settings = new ClientSettings();
		private Server server;
		private TcpClient client;
		private NetworkStream stream;

		public NetworkStream Stream {
			get { return stream; }
		}

		public readonly int Number;

		private StreamReader reader;
		private Dictionary<string, Command> commands;

		private ClientLogHandler logHandler;

		private volatile bool running = false;
		private Thread thread;

		public Client(Server server, TcpClient client) {
			this.server = server;
			this.client = client;
			this.stream = client.GetStream();
			this.reader = new StreamReader(client.GetStream(), encoder);
			InitCommands();

			logHandler = new ClientLogHandler(this);

			thread = new Thread(Run);
			thread.IsBackground = true;
			Number = server.ClientCount + 1;
			thread.Name = "GRC ClientHandler-" + Number;
			Debug("New connection from {0}", client.Client.RemoteEndPoint);
			running = true;
			thread.Start();
		}

		private void InitCommands() {
			commands = new Dictionary<string, Command>() {
				{"help",           Commands.Help.Instance},
				{"exit",           Commands.Exit.Instance},
				{"exitglider",     Commands.ExitGlider.Instance},
				{"status",         Commands.Status.Instance},
				{"version",        Commands.NotImplemented.Instance},
				{"log",            Commands.Log.Instance},
				{"nolog",          Commands.NoLog.Instance},
				{"say",            Commands.NotImplemented.Instance},
				{"queuekeys",      Commands.QueueKeys.Instance},
				{"clearsay",       Commands.NotImplemented.Instance},
				{"forcekeys",      Commands.NotImplemented.Instance},
				{"holdkey",        Commands.HoldKey.Instance},
				{"releasekey",     Commands.ReleaseKey.Instance},
				{"grabmouse",      Commands.NotImplemented.Instance},
				{"setmouse",       Commands.SetMouse.Instance},
				{"getmouse",       Commands.NotImplemented.Instance},
				{"clickmouse",     Commands.ClickMouse.Instance},
				{"attach",         Commands.NotImplemented.Instance},
				{"startglide",     Commands.StartGlide.Instance},
				{"stopglide",      Commands.StopGlide.Instance},
				{"loadprofile",    Commands.NotImplemented.Instance},
				{"capture",        new Commands.Capture()},
				{"capturecache",   Commands.NotImplemented.Instance},
				{"capturescale",   Commands.CaptureScale.Instance},
				{"capturequality", Commands.CaptureQuality.Instance},
				{"queryconfig",    Commands.NotImplemented.Instance},
				{"config",         Commands.NotImplemented.Instance},
				{"selectgame",     Commands.SelectGame.Instance},
				{"getgamews",      Commands.NotImplemented.Instance},
				{"setgamews",      Commands.NotImplemented.Instance},
				{"escapehi",       Commands.NotImplemented.Instance},
			};
		}

		internal void Debug(string str) {
			Debug("{0}", str);
		}

		internal void Debug(string str, params object[] args) {
//			Class1.Instance.Log("[{0}] {1}", thread.Name, string.Format(str, args));
//			Console.WriteLine("[{0}] {1}", thread.Name, string.Format(str, args));
//			Logging.WriteDebug("[{0}] {1}", thread.Name, string.Format(str, args));
		}

		private bool disposed = false;
		public void Dispose() {
			if (disposed) {
				return;
			}

			disposed = true;
			running = false;

			if (null != thread && Thread.CurrentThread != thread) {
				thread.Interrupt();

				bool joinResult = false;

				try {
					joinResult = thread.Join(ThreadDeathTimeoutMS);
				} catch { }

				if (!joinResult) {
					try {
						thread.Abort();
					} catch { }
				}
			}

			thread = null;

			logHandler.Dispose();
			reader.Close();

			try {
				client.Close();
			} catch { }
			server.RemoveClient(this);

			foreach (Command c in commands.Values.Where(c => c.ShouldDispose)) {
				c.Dispose();
			}
		}

		public string FormattedLogChannels {
			get {
				List<string> channels = new List<string>();
				foreach (ClientLogType t in settings.LogChannels.Keys.Where(k => settings.LogChannels[k])) {
					channels.Add(t.Name.ToLower());
				}

				if (0 == channels.Count) {
					return "none";
				}

				return string.Join(", ", channels.ToArray());
			}
		}

		public void UpdateLogSettings() {
			logHandler.StatusEnabled    = settings.LogChannels[ClientLogType.Status];
			logHandler.GliderLogEnabled = settings.LogChannels[ClientLogType.GliderLog];
			logHandler.ChatEnabled =
				settings.LogChannels[ClientLogType.Chat] ||
				settings.LogChannels[ClientLogType.ChatRaw];
			logHandler.CombatEnabled    = settings.LogChannels[ClientLogType.Combat];
		}

		private void Run() {
			string line = null;

			// check the password before entering main loop
			line = reader.ReadLine();

			if (server.settings.Password != line) {
				Debug("Incorrect password, disposing");
				Dispose();
				return;
			}

			Send("Authenticated OK", false);

			bool needToDispose = false;

			while (running) {
				try {
					line = reader.ReadLine();

					if (null == line) {
						Debug("line was null, connection closed?, disposing");
						needToDispose = true;
						running = false;
						break;
					}

					line = line.Trim();

					if ("" == line) {
						continue;
					}

					Debug("Handling command: {0}", line);
					HandleCommand(line);
				} catch (IOException x) {
					Debug("IOException during read, disposing (" + x.Message + ")");
					needToDispose = true;
					running = false;
					break;
				} catch (ThreadInterruptedException) { }
			}

			if (needToDispose) {
				Dispose();
			}
		}

		private static readonly char[] commandSplitChars = new char[] { ' ' };

		private void HandleCommand(string cmd) {
			Command command = commands["help"];
			string args = "";

			if (cmd.StartsWith("/")) {
				string[] parts = cmd.Substring(1).Split(commandSplitChars, 2);

				if (commands.ContainsKey(parts[0])) {
					command = commands[parts[0]];

					if (parts.Length > 1) {
						args = parts[1].Trim();
					}
				}
			}

			try {
				command.Execute(server, this, args);
			} catch (Exception x) {
				Send(x.ToString());
			}
		}

		private static readonly Regex
			ChatColorRegex = new Regex("\\|c[A-Za-z0-9]{6,8}"),
			ChatLinkRegex = new Regex("\\|H.*?\\|h");

		/// <summary>
		/// Removes all color and link escape sequences from a line of WoW chat.
		/// </summary>
		/// <param name="str"></param>
		/// <returns></returns>
		public static string RemoveChatFormatting(string str) {
			str = ChatColorRegex.Replace(str, "");
			str = ChatLinkRegex.Replace(str, "");
			str = str.Replace("|h", "");
			str = str.Replace("|r", "");
			return str;
		}

		/// <summary>
		/// Sends the supplied line of text to the specified log channel. If type
		/// is ChatRaw, its corresponding plain Chat line will be sent automatically.
		/// </summary>
		/// <param name="type"></param>
		/// <param name="line"></param>
		public void SendLog(ClientLogType type, string line) {
			if (settings.LogChannels[type]) {
				Send(false, "[{0}] {1}", type.Name, line);
			}

			// ClientLogHandler only sends ChatRaw lines so we need to resend
			// them as plain Chat lines
			if (ClientLogType.ChatRaw == type && settings.LogChannels[ClientLogType.Chat]) {
				Send(false, "[{0}] {1}", ClientLogType.Chat.Name, RemoveChatFormatting(line));
			}
		}

		/// <summary>
		/// Sends "---\r\n".
		/// </summary>
		public void Send() {
			Send("");
		}

		/// <summary>
		/// Sends the supplied lines joined with "\r\n" followed by "---\r\n".
		/// </summary>
		/// <param name="lines"></param>
		public void Send(string[] lines) {
			Send(string.Join("\r\n", lines));
		}

		/// <summary>
		/// Sends the supplied lines joined with "\r\n" followed by "---\r\n".
		/// </summary>
		/// <param name="lines"></param>
		public void Send(IEnumerable<string> lines) {
			Send(lines.ToArray());
		}

		/// <summary>
		/// Sends the formatted string followed by "---\r\n".
		/// </summary>
		/// <param name="format"></param>
		/// <param name="args"></param>
		public void Send(string format, params object[] args) {
			Send(true, format, args);
		}

		/// <summary>
		/// Sends the formatted string and appends "---\r\n" if sendDashes is true.
		/// </summary>
		/// <param name="sendDashes"></param>
		/// <param name="format"></param>
		/// <param name="args"></param>
		public void Send(bool sendDashes, string format, params object[] args) {
			Send(string.Format(format, args), sendDashes);
		}

		/// <summary>
		/// Sends the string followed by "---\r\n".
		/// </summary>
		/// <param name="str"></param>
		public void Send(string str) {
			Send(str, true);
		}

		/// <summary>
		/// Sends the string and appends "---\r\n" if sendDashes is true.
		/// </summary>
		/// <param name="str"></param>
		/// <param name="sendDashes"></param>
		public void Send(string str, bool sendDashes) {
			Send(0 == str.Length ? emptyBytes : encoder.GetBytes(str + "\r\n"), sendDashes);
		}

		/// <summary>
		/// Sends the raw bytes in buffer followed by "---\r\n".
		/// </summary>
		/// <param name="buffer"></param>
		public void Send(byte[] buffer) {
			Send(buffer, true);
		}

		/// <summary>
		/// Sends the raw bytes in buffer and appends "---\r\n" if sendDashes is true.
		/// </summary>
		/// <param name="buffer"></param>
		/// <param name="sendDashes"></param>
		public void Send(byte[] buffer, bool sendDashes) {
			Send(buffer, 0, buffer.Length, sendDashes);
		}

		/// <summary>
		/// Sends the specified raw bytes from buffer and appends "---\r\n" if sendDashes is true.
		/// </summary>
		/// <param name="buffer"></param>
		/// <param name="offset"></param>
		/// <param name="size"></param>
		/// <param name="sendDashes"></param>
		public void Send(byte[] buffer, int offset, int size, bool sendDashes) {
			stream.Write(buffer, offset, size);

			if (sendDashes) {
				stream.Write(dashesBytes, 0, dashesBytes.Length);
			}

			stream.Flush();
		}
	}
}
