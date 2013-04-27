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
		private GliderRemoteCompat owner;
		private ServerSettings serverSettings;

		public SettingsForm(GliderRemoteCompat owner) {
			this.owner = owner;
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
		}

		private void saveButton_Click(object sender, EventArgs e) {
			if (!ServerSettings.Instance.Equals(serverSettings)) {
				ServerSettings.Instance = serverSettings;
				ServerSettings.Instance.Save();
				owner.RefreshSettings();
			}

			this.Close();
		}
	}
}
