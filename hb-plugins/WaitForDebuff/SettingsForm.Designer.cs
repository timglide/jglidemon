namespace WaitForDebuff {
	partial class SettingsForm {
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.IContainer components = null;

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		/// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
		protected override void Dispose(bool disposing) {
			if (disposing && (components != null)) {
				components.Dispose();
			}
			base.Dispose(disposing);
		}

		#region Windows Form Designer generated code

		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent() {
			this.spellsTextBox = new System.Windows.Forms.TextBox();
			this.spellIdsGroupBox = new System.Windows.Forms.GroupBox();
			this.saveButton = new System.Windows.Forms.Button();
			this.cancelButton = new System.Windows.Forms.Button();
			this.spellIdsGroupBox.SuspendLayout();
			this.SuspendLayout();
			// 
			// spellsTextBox
			// 
			this.spellsTextBox.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
						| System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.spellsTextBox.Location = new System.Drawing.Point(6, 26);
			this.spellsTextBox.Multiline = true;
			this.spellsTextBox.Name = "spellsTextBox";
			this.spellsTextBox.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
			this.spellsTextBox.Size = new System.Drawing.Size(624, 281);
			this.spellsTextBox.TabIndex = 2;
			this.spellsTextBox.WordWrap = false;
			// 
			// spellIdsGroupBox
			// 
			this.spellIdsGroupBox.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom)
						| System.Windows.Forms.AnchorStyles.Left)
						| System.Windows.Forms.AnchorStyles.Right)));
			this.spellIdsGroupBox.Controls.Add(this.spellsTextBox);
			this.spellIdsGroupBox.Location = new System.Drawing.Point(12, 12);
			this.spellIdsGroupBox.Name = "spellIdsGroupBox";
			this.spellIdsGroupBox.Size = new System.Drawing.Size(636, 313);
			this.spellIdsGroupBox.TabIndex = 5;
			this.spellIdsGroupBox.TabStop = false;
			this.spellIdsGroupBox.Text = "Spell Names or IDs, one per line";
			// 
			// saveButton
			// 
			this.saveButton.Anchor = System.Windows.Forms.AnchorStyles.Bottom;
			this.saveButton.Location = new System.Drawing.Point(114, 335);
			this.saveButton.Name = "saveButton";
			this.saveButton.Size = new System.Drawing.Size(213, 56);
			this.saveButton.TabIndex = 6;
			this.saveButton.Text = "&Save";
			this.saveButton.UseVisualStyleBackColor = true;
			this.saveButton.Click += new System.EventHandler(this.saveButton_Click);
			// 
			// cancelButton
			// 
			this.cancelButton.Anchor = System.Windows.Forms.AnchorStyles.Bottom;
			this.cancelButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
			this.cancelButton.Location = new System.Drawing.Point(333, 335);
			this.cancelButton.Name = "cancelButton";
			this.cancelButton.Size = new System.Drawing.Size(213, 56);
			this.cancelButton.TabIndex = 8;
			this.cancelButton.Text = "&Cancel";
			this.cancelButton.UseVisualStyleBackColor = true;
			this.cancelButton.Click += new System.EventHandler(this.cancelButton_Click);
			// 
			// SettingsForm
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(10F, 20F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.CancelButton = this.cancelButton;
			this.ClientSize = new System.Drawing.Size(660, 403);
			this.Controls.Add(this.cancelButton);
			this.Controls.Add(this.saveButton);
			this.Controls.Add(this.spellIdsGroupBox);
			this.Name = "SettingsForm";
			this.SizeGripStyle = System.Windows.Forms.SizeGripStyle.Hide;
			this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
			this.Text = "Wait For Debuff Settings";
			this.Load += new System.EventHandler(this.SettingsForm_Load);
			this.spellIdsGroupBox.ResumeLayout(false);
			this.spellIdsGroupBox.PerformLayout();
			this.ResumeLayout(false);

		}

		#endregion

		private System.Windows.Forms.TextBox spellsTextBox;
		private System.Windows.Forms.GroupBox spellIdsGroupBox;
		private System.Windows.Forms.Button saveButton;
		private System.Windows.Forms.Button cancelButton;
	}
}