using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat {
	public class Color {
		public static System.Windows.Media.Color FromRgb(byte r, byte g, byte b) {
			return System.Windows.Media.Color.FromRgb(r, g, b);
		}

		public static System.Windows.Media.Color FromColor(System.Drawing.Color color) {
			return System.Windows.Media.Color.FromArgb(color.A, color.R, color.G, color.B);
		}

		public static readonly System.Windows.Media.Color
			White = FromColor(System.Drawing.Color.White),
			Red = FromColor(System.Drawing.Color.Red),
			Yellow = FromColor(System.Drawing.Color.Yellow),
			OrangeRed = FromColor(System.Drawing.Color.OrangeRed);
	}
}
