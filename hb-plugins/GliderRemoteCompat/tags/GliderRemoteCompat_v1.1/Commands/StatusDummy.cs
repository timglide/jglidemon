using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx;
using System.Reflection;
using Styx.WoWInternals.WoWObjects;
using Styx.Helpers;

namespace GliderRemoteCompat.Commands {
	class StatusDummy : Command {
		private Version hbVersion;

		public StatusDummy() {
			hbVersion = new Version(2, 0, 0, 5588);
		}

		public override void Execute(Server server, Client client, string args) {
			List<string> l = new List<string>();
			bool attached = true;

			// Version
			l.Add("Version: " + hbVersion /*+ ", Game=4.3.0.13333"*/);
			// Attached
			l.Add("Attached: " + (attached ? "True" : "False"));
			// Mode
			// Profile
			l.Add("Profile: ");
			// Log
			l.Add("Log: " + client.FormattedLogChannels);

			if (attached) {
				// Name
				l.Add("Name: Tester");
				// Class
				l.Add("Class: DeathKnight");
				// Location
				l.Add(string.Format("Location: {0}, {1}, {2}", 1.2, 3.4, 5.6));
				// Target-Name
				l.Add("Target-Name: Test Target");
				// Health (0-1 as a percent)
				l.Add("Health: 0.84");
				// Mana
				l.Add("Mana: 42");
				// Level
				l.Add("Level: 85");
				// Experience
				l.Add("Experience: 1234567");
				// Next-Experience
				l.Add("Next-Experience: 2234567");
				// XP/Hour
				l.Add("XP/Hour: 25000");
				// Heading
				l.Add("Heading: 1");
				// KLD: kills/loots/deaths
				l.Add(string.Format("KLD: {0}/{1}/{2}", 12, 34, 56));

				// Target-Level
				l.Add("Target-Level: 86");
				// Target-Health (0-1 as a percent)
				l.Add("Target-Health: 0.69");
			}

			client.Send(l);
		}

		private string GetMana(LocalPlayer me) {
			//   Caster: 123 (42%)
			//   Rogue:  100 (CP = 0)
			//   Warr/DK: just rage or rp
			//   Druid: R = ##, E=##, (##%)
			switch (me.Class) {
				case WoWClass.Druid:
					return string.Format("R = {0}, E={1}, ({2}%)", me.CurrentRage, me.CurrentEnergy, (int)(me.ManaPercent * 100));
				case WoWClass.Rogue:
					return string.Format("{0} (CP = {1})", me.CurrentEnergy, me.ComboPoints);
				case WoWClass.Warrior:
					return me.CurrentRage.ToString();
				case WoWClass.DeathKnight:
					return me.CurrentRunicPower.ToString();
				case WoWClass.Hunter:
					return me.CurrentFocus.ToString();
				default:
					return string.Format("{0} ({1}%)", me.CurrentMana, (int)(me.ManaPercent * 100));
			}
		}
	}
}
