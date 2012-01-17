using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Drawing.Imaging;

namespace GliderRemoteCompat {
	class Runner {
		public static void Main(string[] args) {
			foreach (var v in ImageCodecInfo.GetImageEncoders()) {
				Console.WriteLine("{0}, {1}: {2}", v.CodecName, v.MimeType, v.FormatDescription);
			}
			Console.ReadLine();
			if (true)
				return;

			Server server = new Server();
			Console.ReadLine();
			server.Dispose();
		}
	}
}
