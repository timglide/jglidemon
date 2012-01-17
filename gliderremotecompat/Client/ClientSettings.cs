using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat {
	/// <summary>
	/// This class contains settings that are set by the remote client after they
	/// connect via various slash commands.
	/// </summary>
	class ClientSettings {
		public bool EscapeHi;

		private float captureScale;

		public float CaptureScale {
			get { return captureScale; }
			set {
				if (value > 1f) {
					if (value < 10f || value > 100f) {
						throw new ArgumentException("value must be between 10 and 100");
					}

					captureScale = value / 100f;
				} else {
					if (value < 0.1f || value > 1f) {
						throw new ArgumentException("value must be between 0.1 and 1");
					}

					captureScale = value;
				}
			}
		}

		private float captureQuality;

		public float CaptureQuality {
			get { return captureQuality; }
			set {
				if (value > 1f) {
					if (value < 10f || value > 100f) {
						throw new ArgumentException("value must be between 10 and 100");
					}

					captureQuality = value / 100f;
				} else {
					if (value < 0.1f || value > 1f) {
						throw new ArgumentException("value must be between 0.1 and 1");
					}

					captureQuality = value;
				}
			}
		}

		public Dictionary<ClientLogType, bool> LogChannels;
		public List<string> QueuedKeys;

		public ClientSettings() {
			EscapeHi = true;
			CaptureScale = 1f;
			CaptureQuality = 0.75f;
			LogChannels = new Dictionary<ClientLogType, bool>();
			QueuedKeys = new List<string>();

			LogChannels[ClientLogType.Status] = false;
			LogChannels[ClientLogType.ChatRaw] = false;
			LogChannels[ClientLogType.GliderLog] = false;
			LogChannels[ClientLogType.Chat] = false;
			LogChannels[ClientLogType.Combat] = false;
		}

		public ClientSettings(ClientSettings copyFrom) : this() {
			Set(copyFrom);
		}

		public void Set(ClientSettings copyFrom) {
			EscapeHi = copyFrom.EscapeHi;
			CaptureScale = copyFrom.CaptureScale;
			CaptureQuality = copyFrom.CaptureQuality;

			LogChannels.Clear();
			foreach (KeyValuePair<ClientLogType, bool> kvp in copyFrom.LogChannels) {
				LogChannels.Add(kvp.Key, kvp.Value);
			}

			QueuedKeys.Clear();
			QueuedKeys.AddRange(copyFrom.QueuedKeys);
		}

		public void SetLogState(string channel, bool state) {
			if (("none" == channel && state) || ("all" == channel && !state)) {
				LogChannels[ClientLogType.Status] = false;
				LogChannels[ClientLogType.ChatRaw] = false;
				LogChannels[ClientLogType.GliderLog] = false;
				LogChannels[ClientLogType.Chat] = false;
				LogChannels[ClientLogType.Combat] = false;
				return;
			}

			if ("all" == channel && state) {
				LogChannels[ClientLogType.Status] = true;
				LogChannels[ClientLogType.ChatRaw] = true;
				LogChannels[ClientLogType.GliderLog] = true;
				LogChannels[ClientLogType.Chat] = true;
				LogChannels[ClientLogType.Combat] = true;
				return;
			}

			if ("none" == channel && !state) {
				throw new ArgumentException("invalid channel and state");
			}

			
			try {
				LogChannels[ClientLogType.ValueOf(channel)] = state;
			} catch (ArgumentException x) {
				throw new ArgumentException("invalid channel", x);
			}
		}
	}
}
