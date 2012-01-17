using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat {
	partial class ClientLogHandler {
		public class ChatTypeInfo {
			public static readonly ChatTypeInfo Instance = new ChatTypeInfo();

			public int id = 0;
			public float r = 1f;
			public float g = 1f;
			public float b = 1f;
		}
	}
}
