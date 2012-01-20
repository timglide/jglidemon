using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace GliderRemoteCompat {
	public partial class SettingsForm : Form {
		private ServerSettings serverSettings;

		public SettingsForm() {
			InitializeComponent();
		}

		private void SettingsForm_Load(object sender, EventArgs e) {
			serverSettings = (ServerSettings)ServerSettings.Instance.Clone();
			propertyGrid.SelectedObject = serverSettings;
		}

		private void closeButton_Click(object sender, EventArgs e) {
			this.Close();
		}

		private void SettingsForm_FormClosing(object sender, FormClosingEventArgs e) {
			ServerSettings.Instance = serverSettings;
			ServerSettings.Instance.Save();
			Class1.Instance.RefreshSettings();
		}
	}
}
