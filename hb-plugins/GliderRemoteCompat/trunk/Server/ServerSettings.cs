using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.IO;
using System.Xml.Serialization;
using Styx.Helpers;
using System.Drawing;
using System.ComponentModel;
using Styx.Common;
using Styx;

namespace GliderRemoteCompat {
	[Serializable]
	public class ServerSettings : ICloneable {
		private static ServerSettings instance;

		public static ServerSettings Instance {
			set { instance = value; }
			get {
				if (null == instance) {
					instance = Load();
				}

				return instance;
			}
		}

		public static string ConfigFile {
			get {
				return string.Format(configFile, StyxWoW.Me.Name);
			}
		}

		private const string configFile = "{0}.xml";

		public static string SavePath {
			get {
				string path = Process.GetCurrentProcess().MainModule.FileName;
				path = Path.GetDirectoryName(path);
				path = Path.Combine(path, "Settings\\GliderRemoteCompat");
				return path;
			}
		}

		private const string oldConfigFile = "GliderRemoteCompat.config";

		private static string OldSavePath {
			get {
				string path = Process.GetCurrentProcess().MainModule.FileName;
				path = Path.GetDirectoryName(path);
				path = Path.Combine(path, "Plugins\\GliderRemoteCompat");
				return path;
			}
		}

		private static XmlSerializer serializer;

		private static XmlSerializer Serializer {
			get {
				if (null == serializer) {
					serializer = new XmlSerializer(typeof(ServerSettings));
				}

				return serializer;
			}
		}

		public static ServerSettings Load() {
			string oldPath = OldSavePath;
			string oldFile = Path.Combine(oldPath, oldConfigFile);

			string path = SavePath;
			string file = Path.Combine(path, ConfigFile);

			try {
				if (File.Exists(oldFile) && !File.Exists(file)) {
					if (!Directory.Exists(path))
						Directory.CreateDirectory(path);

					Logging.Write("[GRC] Moving old config file to new location: {0}", file);
					File.Move(oldFile, file);
				}

				using (FileStream fStream = new FileStream(file, FileMode.Open, FileAccess.Read)) {
					return (ServerSettings)Serializer.Deserialize(fStream);
				}
			} catch {
				return new ServerSettings();
			}
		}

		public void Save() {
			string path = SavePath;
			string file = Path.Combine(path, ConfigFile);

			if (!Directory.Exists(path)) {
				Directory.CreateDirectory(path);
			}

			try {
				using (FileStream fStream = new FileStream(file, FileMode.Create, FileAccess.Write)) {
					Serializer.Serialize(fStream, this);
				}
			} catch (Exception e) {
				Logging.Write(Color.Red, "Error saving GliderRemoteCompat settings");
				Logging.WriteException(Color.Red, e);
			}
		}

		private int port = 3200;

		[CategoryAttribute("Server Settings")]
		[DescriptionAttribute("The port to run the server on (between 1 and 65535)")]
		public int Port {
			get { return port; }
			set {
				if (!(1 <= value && value <= 65535)) {
					throw new ArgumentException("port must be between 1 and 65535");
				}

				port = value;
			}
		}

		private string password = "";

		[CategoryAttribute("Server Settings")]
		[DescriptionAttribute("The password a remote client needs to access this server (can be blank)")]
		public string Password {
			get { return password; }
			set { password = value; }
		}

		public override bool Equals(object obj) {
			if (object.ReferenceEquals(this, obj))
				return true;
			if (!(obj is ServerSettings))
				return false;
			ServerSettings s = (ServerSettings)obj;

			return this.password == s.password && this.port == s.port;
		}

		#region ICloneable Members

		public object Clone() {
			return this.MemberwiseClone();
		}

		#endregion
	}
}
