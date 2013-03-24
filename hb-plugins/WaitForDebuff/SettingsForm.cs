using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace WaitForDebuff {
	public partial class SettingsForm : Form {
		private Settings settings;

		public SettingsForm() {
			InitializeComponent();
		}

		private void SettingsForm_Load(object sender, EventArgs e) {
			this.settings = (Settings)Settings.Instance.Clone();

			StringBuilder sb = new StringBuilder();

			foreach (int i in settings.DebuffIDs.OrderBy(id => id)) {
				sb.Append(i).AppendLine();
			}

			foreach (string s in settings.DebuffNames.OrderBy(name => name)) {
				sb.AppendLine(s);
			}

			spellsTextBox.Text = sb.ToString();
		}

		private void ParseSpells() {
			HashSet<string> names = new HashSet<string>(StringComparer.CurrentCultureIgnoreCase);
			HashSet<int> ids = new HashSet<int>();
			string s;
			int i;

			string[] separators = {"|", ";", ":", ",", "\t", "  ", "   ", "    "};
			foreach (string line in spellsTextBox.Lines) {
				foreach (string token in line.Split(separators, StringSplitOptions.RemoveEmptyEntries)) {
					s = token.Trim();

					if (s.Length == 0)
						continue;

					if (int.TryParse(s, out i)) {
						ids.Add(i);
						continue;
					}

					names.Add(s);
				}
			}

			settings.DebuffIDs = ids;
			settings.DebuffNames = names;
			Settings.Instance = settings;
			Settings.Instance.Save();
		}

		private void saveButton_Click(object sender, EventArgs e) {
			ParseSpells();
			Close();
		}

		private void cancelButton_Click(object sender, EventArgs e) {
			Close();
		}
	}
}
