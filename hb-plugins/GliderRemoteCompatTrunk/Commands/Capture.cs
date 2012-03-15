using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;
using System.Drawing;
using System.IO;
using System.Drawing.Imaging;
using Styx.WoWInternals;

using GliderRemoteCompat;
using RECT = GliderRemoteCompat.Win32Window.RECT;

namespace GliderRemoteCompat.Commands {
	class Capture : Command {
		private Bitmap bitmap, clientArea, bitmapSmall;
		private Rectangle clientSrcRect, clientDestRect;
		private EncoderParameter qualityParam;
		private EncoderParameters encoderParams;
		private float lastQuality = 1f;

		public override bool ShouldDispose {
			get { return true; }
		}

		public override void Execute(Server server, Client client, string args) {
			if (IntPtr.Zero == WindowHandle) {
				client.Send("Error: couldn't get window handle");
				return;
			}

			RECT size = new RECT();
			if (!Win32Window.GetWindowRect(new HandleRef(this, WindowHandle), out size)) {
				client.Send("Error: GetWindowRect failed");
				return;
			}

			if (null == bitmap || bitmap.Width != size.Width || bitmap.Height != size.Height) {
				if (null != bitmap) {
					bitmap.Dispose();
				}

				bitmap = new Bitmap(size.Width, size.Height);
			}

			using (Graphics g = Graphics.FromImage(bitmap)) {
				Win32Window.PrintWindow(WindowHandle, g.GetHdc(), 0);
				g.ReleaseHdc();
			}

			// crop to just the client area
			RECT clientSize = new RECT();
			if (!Win32Window.GetClientRect(WindowHandle, out clientSize)) {
				client.Send("Error: GetClientRect failed");
				return;
			}

			Point clientPos = new Point();
			if (!Win32Window.ClientToScreen(WindowHandle, ref clientPos)) {
				client.Send("Error: ClientToScreen failed");
				return;
			}

			// ok for a one time thing but wasteful for continual use
			//Bitmap srcBitmap = bitmap.Clone(new Rectangle(
			//        clientPos.X - size._Left,
			//        clientPos.Y - size._Top,
			//        clientSize.Width,
			//        clientSize.Height),
			//    PixelFormat.Format32bppArgb);

			if (null == clientArea ||
					clientArea.Width != clientSize.Width ||
					clientArea.Height != clientArea.Height) {
				if (null != clientArea) {
					clientArea.Dispose();
				}

				clientArea = new Bitmap(clientSize.Width, clientSize.Height);
				clientSrcRect = new Rectangle(
					clientPos.X - size._Left,
					clientPos.Y - size._Top,
					clientSize.Width,
					clientSize.Height);
				clientDestRect = new Rectangle(Point.Empty, clientArea.Size);
			}

			using (Graphics g = Graphics.FromImage(clientArea)) {
				g.DrawImage(bitmap, clientDestRect, clientSrcRect, GraphicsUnit.Pixel);
			}

			Bitmap srcBitmap = clientArea;

			if (client.settings.CaptureScale < 1f) {
				int nWidth  = (int)(srcBitmap.Width  * client.settings.CaptureScale);
				int nHeight = (int)(srcBitmap.Height * client.settings.CaptureScale);

				if (null == bitmapSmall ||
						bitmapSmall.Width != nWidth ||
						bitmapSmall.Height != nHeight) {
					if (null != bitmapSmall) {
						bitmapSmall.Dispose();
					}

					bitmapSmall = new Bitmap(nWidth, nHeight);
				}

				using (Graphics g = Graphics.FromImage(bitmapSmall)) {
					g.DrawImage(srcBitmap, 0, 0, nWidth, nHeight);
				}

				srcBitmap = bitmapSmall;
			}

			using (MemoryStream s = new MemoryStream()) {
				if (null == encoderParams) {
					encoderParams = new EncoderParameters(1);
				}

				if (null == qualityParam || lastQuality != client.settings.CaptureQuality) {
					lastQuality = client.settings.CaptureQuality;
					qualityParam = new EncoderParameter(
						System.Drawing.Imaging.Encoder.Quality,
						(long)Math.Round(lastQuality * 100)); // must be long
					encoderParams.Param[0] = qualityParam;
				}

				srcBitmap.Save(s, JpegCodec, encoderParams);

				//using (FileStream f = new FileStream("test.jpg", FileMode.Create)) {
				//	s.WriteTo(f);
				//}

				//client.Send(string.Format("DBG: Length is {0} (0x{0:X})", s.Length));
				client.Send(false, "Success! 4-byte length and JPEG stream follow (length={0})", s.Length);
				client.Send(BitConverter.GetBytes((int)s.Length), false);
				s.WriteTo(client.Stream);
				client.Send();
			}
		}

		public override void Dispose() {
			if (null != bitmapSmall) {
				bitmapSmall.Dispose();
				bitmapSmall = null;
			}

			if (null != clientArea) {
				clientArea.Dispose();
				clientArea = null;
			}

			if (null != bitmap) {
				bitmap.Dispose();
				bitmap = null;
			}
		}

		private IntPtr WindowHandle {
			get {
				return ObjectManager.WoWProcess.MainWindowHandle;
			}
		}

		private static ImageCodecInfo jpegCodec;

		public static ImageCodecInfo JpegCodec {
			get {
				if (null == jpegCodec) {
					jpegCodec = GetEncoderInfo("image/jpeg");
				}

				return jpegCodec;
			}
		}

		/// <summary>
		/// Returns the image codec with the given mime type
		/// </summary>
		private static ImageCodecInfo GetEncoderInfo(string mimeType) {
			// Get image codecs for all image formats
			ImageCodecInfo[] codecs = ImageCodecInfo.GetImageEncoders();

			// Find the correct image codec
			for (int i = 0; i < codecs.Length; i++)
				if (codecs[i].MimeType == mimeType)
					return codecs[i];
			return null;
		}
	}
}
