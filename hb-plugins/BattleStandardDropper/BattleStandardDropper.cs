using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Styx.Plugins.PluginClass;
using Styx.WoWInternals.WoWObjects;
using Styx;
using Styx.WoWInternals;
using Styx.Logic.Pathing;
using System.Diagnostics;
using System.Threading;
using Styx.Helpers;

namespace BattleStandardDropper {
	public class BattleStandardDropper : HBPlugin {
		private const string _revision = "$Revision$";
		private static readonly int revision;
		private static readonly Version version;

		private const uint DropLocationRange = 30;
		private const uint DropLocationRangeSqr = DropLocationRange * DropLocationRange;
		private const uint StandardCooldownMS = (uint)(10.1 * 60 * 1000);

		private static readonly uint[] BattleStandards = {
			64402, // H 15%
			64399, // A 15%
			64401, // H 10%
			64398, // A 10%
			64400, // H 5%
			63359  // A 5%
		};

		private static readonly uint[] BattleStandardBuffs = {
			90633, // H 15%
			64399, // A 15%
			90632, // H 10%
			90626, // A 10%
			90631, // H 5%
			89479  // A 5%
		};

		private const uint GuildFaction = 1168;

		private uint dropZone = 0;
		private WoWPoint dropLocation = WoWPoint.Empty;

		static BattleStandardDropper() {
			string str = string.Empty;

			try {
				str = _revision.Substring(11, _revision.Length - 2 - 11);
				revision = int.Parse(str);
			} catch {
				revision = 0;
			}

			version = new Version(0, 1, 0, revision);
		}

		public static LocalPlayer Me {
			get { return StyxWoW.Me; }
		}

		private static uint GetItemCount(uint id) {
			return
				Lua.GetReturnVal<uint>("return GetItemCount(\"" + id + "\")", 0);
		}

		private static bool IsItemOnCooldown(uint id) {
			return
				0 != Lua.GetReturnVal<int>("return GetItemCooldown(" + id + ")", 0);
		}


		public BattleStandardDropper() {

		}

		public override string Author {
			get { return "timglide"; }
		}

		public override string Name {
			get { return "BattleStandardDropper"; }
		}

		public override Version Version {
			get { return version; }
		}

		public override bool WantButton {
			get { return initialized; }
		}

		public override string ButtonText {
			get { return "Set Drop Point"; }
		}

		public override void OnButtonPress() {
			dropZone = Me.ZoneId;
			dropLocation = Me.Location;
			Logging.Write("Will drop battle standard around {0}", dropLocation);
		}

		private void FindBattleStandard() {
			battleStandardId = 0;
			battleStandardBuffId = 0;

			using (new FrameLock()) {
				if ((int)Me.GetReputationLevelWith(GuildFaction) < (int)WoWUnitReaction.Friendly) {
					return;
				}

				for (int i = 0; i < BattleStandards.Length; i++) {
					if (GetItemCount(BattleStandards[i]) >= 1) {
						battleStandardId = BattleStandards[i];
						battleStandardBuffId = BattleStandardBuffs[i];
						return;
					}
				}
			}
		}

		private Stopwatch useStandardSW = new Stopwatch();
		private bool initialized = false;
		private uint battleStandardId = 0;
		private uint battleStandardBuffId = 0;

		public override void Initialize() {
			FindBattleStandard();
			initialized = true;
			useStandardSW.Reset();
			
			if (0 != battleStandardId) {
				Logging.Write("BattleStandardDropper initialized with standard = {0}", battleStandardId);
			} else {
				Logging.Write("BattleStandardDropper initialized but no standard found or you're not friendly with guild!");
			}
		}

		public override void Dispose() {
			initialized = false;
			useStandardSW.Reset();
		}

		public override void Pulse() {
			if (0 == battleStandardId) {
				return;
			}

			if (WoWPoint.Empty == dropLocation) {
				return;
			}

			if (useStandardSW.IsRunning && useStandardSW.ElapsedMilliseconds < StandardCooldownMS) {
				return;
			}

			if (dropZone != Me.ZoneId) {
				return;
			}

			if (IsItemOnCooldown(battleStandardId)) {
				return;
			}

			if (dropLocation.DistanceSqr(Me.Location) > DropLocationRangeSqr) {
				return;
			}

			useStandardSW.Reset();
			Logging.Write("Dropping battle standard!");
			Thread.Sleep(250);
			Lua.DoString("UseItemByName(" + battleStandardId + ")");
			Thread.Sleep(250);

			if (null != Me.GetAuraById((int)battleStandardBuffId)) {
				useStandardSW.Start();
			}
		}
	}
}