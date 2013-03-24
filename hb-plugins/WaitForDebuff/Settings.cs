using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Xml.Serialization;
using System.IO;
using System.ComponentModel;
using Styx;
using System.Diagnostics;
using Styx.Common;

namespace WaitForDebuff {
	[Serializable]
	public class Settings : ICloneable {
		private static Settings instance;

		public static Settings Instance {
			get {
				if (null == instance) {
					instance = Load();
				}

				return instance;
			}
			set {
				instance = value;
			}
		}

		#region Settings Serialization

		public const string ConfigFileFormat = "{0}.xml";

		public static string ConfigFile {
			get { return string.Format(ConfigFileFormat, StyxWoW.Me.Name); }
		}

		public static string SavePath {
			get {
				string path = Process.GetCurrentProcess().MainModule.FileName;
				path = Path.GetDirectoryName(path);
				path = Path.Combine(path, @"Settings\" + Constants.Name);
				return path;
			}
		}

		private static XmlSerializer serializer;

		private static XmlSerializer Serializer {
			get {
				if (null == serializer) {
					serializer = new XmlSerializer(typeof(Settings));
				}

				return serializer;
			}
		}

		public static Settings Load() {
			string path = SavePath;
			string file = Path.Combine(path, ConfigFile);

			try {
				using (FileStream fStream = new FileStream(file, FileMode.Open, FileAccess.Read)) {
					return (Settings)Serializer.Deserialize(fStream);
				}
			} catch {
				return new Settings();
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
				Logging.Write(Color.Red, "Error saving " + Constants.Name + " settings");
				Logging.WriteException(Color.Red, e);
			}
		}

		#endregion

		private HashSet<int> debuffIDs = new HashSet<int>() {
			138768 // Triple Puncture
		};

		[CategoryAttribute("Settings")]
		[DescriptionAttribute("Debuffs by ID")]
		public HashSet<int> DebuffIDs {
			get { return debuffIDs; }
			set {
				debuffIDs = value;
			}
		}

		private HashSet<string> debuffNames = new HashSet<string>(StringComparer.CurrentCultureIgnoreCase) {
			"Triple Puncture"
		};

		[CategoryAttribute("Settings")]
		[DescriptionAttribute("Debuffs by Name")]
		public HashSet<string> DebuffNames {
			get { return debuffNames; }
			set {
				debuffNames = value;
			}
		}

		#region ICloneable Members

		public object Clone() {
			Settings clone = (Settings)this.MemberwiseClone();
			clone.debuffIDs = new HashSet<int>(this.debuffIDs);
			clone.debuffNames = new HashSet<string>(this.debuffNames, StringComparer.CurrentCultureIgnoreCase);
			return clone;
		}

		#endregion
	}
}
