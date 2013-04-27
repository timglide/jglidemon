using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx;
using System.Reflection;
using Styx.WoWInternals.WoWObjects;
using Styx.Helpers;
using Styx.CommonBot;
using Styx.CommonBot.Profiles;
using System.Globalization;
using Bots.Gatherbuddy;
using Bots.ArchaeologyBuddy;
using Styx.WoWInternals;

namespace GliderRemoteCompat.Commands {
	class Status : Command {
		private const float TwoPI = (float)(Math.PI * 2);

		public static readonly Command Instance = new Status();

		public override void Execute(Server server, Client client, string args) {
			//ObjectManager.Update();

			List<string> l = new List<string>();
			LocalPlayer me = StyxWoW.Me;
			bool attached = null != me /*&& me.IsValid*/;
			//bool attached = StyxWoW.IsInGame;

			// Version
			l.Add("Version: " + System.Windows.Forms.Application.ProductVersion);
			// Attached
			l.Add("Attached: " + attached);
			l.Add("Running: " + TreeRoot.IsRunning);
			// Mode
			l.Add("Mode: " + (null != BotManager.Current ? BotManager.Current.Name : "None"));
			// Profile
			l.Add("Profile: " + 
				(null != ProfileManager.XmlLocation && "" != ProfileManager.XmlLocation
				 ? ProfileManager.XmlLocation
				 : "None"));
			l.Add("Profile-Name: " +
				(null != ProfileManager.CurrentProfile
				 ? ProfileManager.CurrentProfile.Name
				 : ""));
			// Log
			l.Add("Log: " + client.FormattedLogChannels);

			if (null != TreeRoot.GoalText && "" != TreeRoot.GoalText)
				l.Add("Goal-Text: " + TreeRoot.GoalText.Replace('\n', ' ').Replace('\r', ' '));
			if (null != TreeRoot.StatusText && "" != TreeRoot.StatusText)
				l.Add("Status-Text: " + TreeRoot.StatusText.Replace('\n', ' ').Replace('\r', ' '));

			if (attached) {
				l.Add("Account-Name: " + me.AccountName);
				l.Add("Realm: " + me.RealmName);
				l.Add("Faction: " + (me.IsAlliance ? "Alliance" : me.IsHorde ? "Horde" : me.IsNeutralPandaren ? "Pandaren" : "Unknown"));
				
				// Name
				l.Add("Name: " + me.Name);
				// Class
				l.Add("Class: " + me.Class.ToString());
				l.Add("Copper: " + me.Copper);
				// Heading
				float heading = me.Rotation % TwoPI;
				l.Add("Heading: " + (heading >= 0 ? heading : heading + TwoPI));
				// Location
				l.Add(string.Format(CultureInfo.InvariantCulture, "Location: {0}, {1}, {2}", me.X, me.Y, me.Z));

				if (null != me.CurrentMap) {
					l.Add("Map: " + me.CurrentMap.Name);
					l.Add("Map-Id: " + me.CurrentMap.MapId);
					l.Add("Map-Internal-Name: " + me.CurrentMap.InternalName);
				}

				l.Add("Zone: " + me.ZoneText);
				l.Add("Zone-Id: " + me.ZoneId);
				l.Add("Real-Zone: " + me.RealZoneText);
				l.Add("Sub-Zone: " + me.SubZoneText);

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
				l.Add("XP/Hour: " + (int)GameStats.XPPerHour);
				if (GameStats.XPPerHour > 0)
					l.Add("Time-To-Level: " + GameStats.TimeToLevel.TotalSeconds);
				
				// Target-Name
				l.Add("Target-Name: " + (null != me.CurrentTarget ? me.CurrentTarget.Name : ""));

				if (null != me.CurrentTarget) {
					// Target-Level
					l.Add("Target-Level: " + me.CurrentTarget.Level);
					// Target-Health (0-1 as a percent)
					l.Add("Target-Health: " + (me.CurrentTarget.HealthPercent / 100.0));
					l.Add("Target-Is-Player: " + me.CurrentTarget.IsPlayer);
				}

				l.Add("Honor-Gained: " + GameStats.HonorGained);
				l.Add("Honor/Hour: " + GameStats.HonorPerHour);
				l.Add("BGs-Won: " + GameStats.BGsWon);
				l.Add("BGs-Lost: " + GameStats.BGsLost);
				l.Add("BGs-Completed: " + GameStats.BGsCompleted);
				l.Add("BGs-Won/Hour: " + (int)GameStats.BGsWonPerHour);
				l.Add("BGs-Lost/Hour: " + (int)GameStats.BGsLostPerHour);
				l.Add("BGs/Hour: " + (int)GameStats.BGsPerHour);
				// KLD: kills/loots/deaths
				l.Add(string.Format("KLD: {0}/{1}/{2}", GameStats.MobsKilled, GameStats.Loots, GameStats.Deaths));
				l.Add("Kills: " + GameStats.MobsKilled);
				l.Add("Loots: " + GameStats.Loots);
				l.Add("Deaths: " + GameStats.Deaths);
				l.Add("Kills/Hour: " + (int)GameStats.MobsPerHour);
				l.Add("Loots/Hour: " + (int)GameStats.LootsPerHour);
				l.Add("Deaths/Hour: " + (int)GameStats.DeathsPerHour);

				Dictionary<string, int> nodes = GatherbuddyBot.NodeCollectionCount;

				if (null != nodes) {
					int totalNodes = nodes.Aggregate(0, (sum, entry) => sum + entry.Value);
					l.Add("Nodes: " + totalNodes);

					if (TimeSpan.Zero != GatherbuddyBot.runningTime) {
						float nodesPerHour = totalNodes / (float)GatherbuddyBot.runningTime.TotalHours;
						l.Add("Nodes/Hour: " + (int)nodesPerHour);
					}
				}

				l.Add("Solves: " + ArchBuddy.totalCollectedArtifacts);
				if (TimeSpan.Zero != ArchBuddy.runningTime) {
					float solvesPerHour = ArchBuddy.totalCollectedArtifacts / (float)ArchBuddy.runningTime.TotalHours;
					l.Add("Solves/Hour: " + (int)solvesPerHour);
				}

				if (null != BotManager.Current) {
					BotBase bot = BotManager.Current;

					if ("ProfessionBuddy" == bot.Name) {
						try {
							bot = (BotBase)bot
								.GetType().GetProperty("SecondaryBot").GetValue(bot, null);
						} catch { }
					}

					if (null == bot)
						bot = BotManager.Current;

					try {
						switch (bot.Name) {
							case "AutoAngler":
								Dictionary<string, uint> fish =
									(Dictionary<string, uint>)bot
										.GetType().GetProperty("FishCaught").GetValue(null, null);
								if (null != fish) {
									uint totalFish = fish.Aggregate((uint)0, (sum, entry) => sum + entry.Value);
									l.Add("Fish: " + totalFish);

									FieldInfo fi = bot.GetType().GetField("_botStartTime", BindingFlags.Default);

									if (null != fi) {
										DateTime botStart = (DateTime)fi.GetValue(bot);
										TimeSpan runningTime = DateTime.Now - botStart;

										if (TimeSpan.Zero != runningTime) {
											float fishPerHour = totalFish / (float)runningTime.TotalHours;
											l.Add("Fish/Hour: " + (int)fishPerHour);
										}
									}
								}
								break;
						}
					} catch { }
				} // end current bot is not null
			} // end is attached

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
