using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx;
using System.Reflection;
using Styx.WoWInternals.WoWObjects;
using Styx.Combat.CombatRoutine;
using Styx.Helpers;
using Styx.Logic.Profiles;

namespace GliderRemoteCompat.Commands {
	class Status : Command {
		public static readonly Command Instance = new Status();

		public override void Execute(Server server, Client client, string args) {
			List<string> l = new List<string>();
			bool attached = StyxWoW.IsInGame;
			LocalPlayer me = StyxWoW.Me;

			// Version
			l.Add("Version: " + System.Windows.Forms.Application.ProductVersion);
			// Attached
			l.Add("Attached: " + (attached ? "True" : "False"));
			// Mode
			l.Add("Mode: " + (null != BotManager.Current ? BotManager.Current.Name : "None"));
			// Profile
			l.Add("Profile: " + 
				(null != ProfileManager.CurrentProfile
				 ? ProfileManager.CurrentProfile.Name
				 : "None"));
			// Log
			l.Add("Log: " + client.FormattedLogChannels);

			if (attached) {
				// Name
				l.Add("Name: " + me.Name);
				// Class
				l.Add("Class: " + me.Class.ToString());
				// Location
				l.Add(string.Format("Location: {0}, {1}, {2}", me.X, me.Y, me.Z));
				// Target-Name
				l.Add("Target-Name: " + (null != me.CurrentTarget ? me.CurrentTarget.Name : ""));
				// Health (0-1 as a percent)
				l.Add("Health: " + (me.HealthPercent / 100.0));
				// Mana
				l.Add("Mana: " + GetMana(me));
				// Level
				l.Add("Level: " + me.Level);
				// Experience
				l.Add("Experience: " + me.Experience);
				// Next-Experience
				l.Add("Next-Experience: " + me.NextLevelExperience);
				// XP/Hour
				l.Add("XP/Hour: " + (int)InfoPanel.XPPerHour);
				// Heading
				l.Add("Heading: " + me.Rotation);
				// KLD: kills/loots/deaths
				l.Add(string.Format("KLD: {0}/{1}/{2}", InfoPanel.MobsKilled, InfoPanel.Loots, InfoPanel.Deaths));

				if (null != me.CurrentTarget) {
					// Target-Level
					l.Add("Target-Level: " + me.CurrentTarget.Level);
					// Target-Health (0-1 as a percent)
					l.Add("Target-Health: " + (me.CurrentTarget.HealthPercent / 100.0));
				}
			}

			client.Send(l);
		}

		private static string GetMana(LocalPlayer me) {
			//   Caster: 123 (42%)
			//   Rogue:  100 (CP = 0)
			//   Warr/DK: just rage or rp
			//   Druid: R = ##, E=##, (##%)
			switch (me.Class) {
				case WoWClass.Druid:
					return string.Format("R = {0}, E={1}, ({2}%)", me.CurrentRage, me.CurrentEnergy, (int)(me.ManaPercent));
				case WoWClass.Rogue:
					return string.Format("{0} (CP = {1})", me.CurrentEnergy, me.ComboPoints);
				case WoWClass.Warrior:
					return me.CurrentRage.ToString();
				case WoWClass.DeathKnight:
					return me.CurrentRunicPower.ToString();
				case WoWClass.Hunter:
					return me.CurrentFocus.ToString();
				default:
					return string.Format("{0} ({1}%)", me.CurrentMana, (int)(me.ManaPercent));
			}
		}
	}
}
