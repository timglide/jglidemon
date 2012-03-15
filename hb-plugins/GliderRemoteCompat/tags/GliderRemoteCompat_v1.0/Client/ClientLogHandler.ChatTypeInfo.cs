using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals;
using Styx.Helpers;
using System.Drawing;

namespace GliderRemoteCompat {
	partial class ClientLogHandler {
		public class ChatTypeInfo {
			private static readonly Dictionary<string, ChatTypeInfo> cache =
				new Dictionary<string, ChatTypeInfo>();

			public static readonly ChatTypeInfo Instance = new ChatTypeInfo();

			public static ChatTypeInfo Get(string key) {
				if (!cache.ContainsKey(key)) {
					ChatTypeInfo ret = null;

					try {
						List<string> values = Lua.GetReturnValues(string.Format(
							"return ChatTypeInfo[\"{0}\"].r, ChatTypeInfo[\"{0}\"].g, ChatTypeInfo[\"{0}\"].b;", key));
						ret = new ChatTypeInfo();
						ret.r = float.Parse(values[0]);
						ret.g = float.Parse(values[1]);
						ret.b = float.Parse(values[2]);
					} catch (Exception e) {
						Logging.WriteDebug(Color.Yellow, "Error getting ChatTypeInfo");
						Logging.WriteException(Color.OrangeRed, e);
						ret = Instance;
					}

					cache[key] = ret;
				}

				return cache[key];
			}

			public int id = 0;
			public float r = 1f;
			public float g = 1f;
			public float b = 1f;
		}
	}
}
