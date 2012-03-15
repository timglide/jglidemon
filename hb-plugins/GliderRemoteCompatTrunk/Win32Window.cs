using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;
using System.Drawing;

namespace GliderRemoteCompat {
	class Win32Window {
		/// <summary>The GetForegroundWindow function returns a handle to the foreground window.</summary>
		[DllImport("user32.dll")]
		public static extern IntPtr GetForegroundWindow();

		[DllImport("user32.dll")]
		[return: MarshalAs(UnmanagedType.Bool)]
		public static extern bool SetForegroundWindow(IntPtr hWnd);

		[DllImport("user32.dll")]
		public static extern bool ClientToScreen(IntPtr hWnd, ref Point lpPoint);

		public static IntPtr FindWindowByCaption(string lpWindowName) {
			return FindWindowByCaption(IntPtr.Zero, lpWindowName);
		}

		[DllImport("user32.dll", EntryPoint = "FindWindow", SetLastError = true)]
		public static extern IntPtr FindWindowByCaption(IntPtr ZeroOnly, string lpWindowName);

		[StructLayout(LayoutKind.Sequential)]
		public struct RECT {
			public int _Left;
			public int _Top;
			public int _Right;
			public int _Bottom;

			public int Width {
				get { return _Right - _Left; }
			}

			public int Height {
				get { return _Bottom - _Top; }
			}

			public void Shift(int dx, int dy) {
				_Left += dx;
				_Right += dx;
				_Top += dy;
				_Bottom += dy;
			}
		}

		[DllImport("user32.dll")]
		public static extern bool GetClientRect(IntPtr hWnd, out RECT lpRect);

		[DllImport("user32.dll")]
		[return: MarshalAs(UnmanagedType.Bool)]
		public static extern bool GetWindowRect(HandleRef hwnd, out RECT lpRect);

		[DllImport("User32.dll", SetLastError = true)]
		[return: MarshalAs(UnmanagedType.Bool)]
		public static extern bool PrintWindow(IntPtr hwnd, IntPtr hDC, uint nFlags);
	}
}
