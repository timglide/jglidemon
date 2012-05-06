using System;
using System.Linq;
using System.Collections.Generic;

using Styx;
using Styx.Helpers;
using Styx.Logic.BehaviorTree;
using Styx.Logic.Pathing;
using Styx.Logic.Questing;

using TreeSharp;
using Action = TreeSharp.Action;
using Styx.WoWInternals.WoWObjects;
using Styx.WoWInternals;
using System.Drawing;
using Styx.Logic.Combat;
using System.Threading;
using CommonBehaviors.Actions;
using Styx.Combat.CombatRoutine;
using Styx.Logic;
using Styx.Logic.Inventory;

namespace timglide {
	/// <summary>
	/// Custom quest behavior for both the initial and daily versions of wrangle aether rays.
	/// By timglide
	/// ##Syntax##
	/// NakedSet: The name of an equipment manager set to equip when attacking the aether rays,
	///           must capable of auto-attacking them down without 1 shotting them below 30%,
	///           does not necessarily have to be completely naked
	/// DpsSet: The name of an equipment manager set to equip when not attacking aether rays
	/// </summary>
	class Ogrila_Wrangle : CustomForcedBehavior {
		public static readonly int[] QuestIds = { 11065, 11066 };
		public const uint ItemId = 32698; // wrangling rope
		public const uint UnitId = 22181; // aether ray
		public const uint WrangledUnitId = 23343; // wrangled aether ray
		public const int MaxPullZ = 370; // some rays are flying high above ground
		public const double WrangleHealthPercent = 25;
		public const uint DistanceCheck = 5;
		public const uint DistanceCheckSqr = DistanceCheck * DistanceCheck;
		public const uint AttackRange = 30;
		public const uint AttackRangeSqr = AttackRange * AttackRange;
		private static readonly TimeSpan BlacklistTime = TimeSpan.FromSeconds(60);
		private const string DruidFlightForm = "Flight Form";
		private const string DruidSwiftFlightForm = "Swift Flight Form";

		private readonly CircularQueue<WoWPoint> waypoints = new CircularQueue<WoWPoint>() {
			new WoWPoint(1864.471, 7293.342, 364.6465),
			new WoWPoint(1962.786, 7293.749, 364.0372),
			new WoWPoint(2049.986, 7306.999, 363.987),
			new WoWPoint(2111.953, 7261.022, 364.3051),
			new WoWPoint(2154.913, 7196.787, 364.0125),
			new WoWPoint(2237.445, 7191.248, 365.8383),
			new WoWPoint(2279.342, 7120.062, 365.2996),
			new WoWPoint(2363.092, 7132.024, 365.1947),
			new WoWPoint(2399.165, 7194.367, 365.9497),
			new WoWPoint(2504.699, 7200.459, 365.1544),
			new WoWPoint(2528.646, 7112.717, 364.3953),
			new WoWPoint(2427.807, 7099.294, 366.4634),
			new WoWPoint(2322.66, 7121.537, 366.8638),
			new WoWPoint(2216.624, 7084.106, 363.906),
			new WoWPoint(2143.805, 7146.103, 363.7143),
			new WoWPoint(2073.653, 7175.793, 364.9841),
			new WoWPoint(1989.601, 7219.587, 363.971),
			new WoWPoint(1899.92, 7182.863, 363.8503),
			new WoWPoint(1818.6, 7206.105, 364.8537),
			new WoWPoint(1749.531, 7274.329, 364.6611)
		};

		public Ogrila_Wrangle(Dictionary<string, string> args)
			: base(args) {

			try {
				DpsSet = GetAttributeAs<string>("DpsSet", false, ConstrainAs.StringNonEmpty, new[] { "DPSSet" });
				NakedSet = GetAttributeAs<string>("NakedSet", false, ConstrainAs.StringNonEmpty, new[] { "Nakedset" });
				StayNaked = GetAttributeAsNullable<bool>("StayNaked", false, null, null) ?? false;
			} catch (Exception except) {
				// Maintenance problems occur for a number of reasons.  The primary two are...
				// * Changes were made to the behavior, and boundary conditions weren't properly tested.
				// * The Honorbuddy core was changed, and the behavior wasn't adjusted for the new changes.
				// In any case, we pinpoint the source of the problem area here, and hopefully it
				// can be quickly resolved.
				LogMessage("error", "BEHAVIOR MAINTENANCE PROBLEM: " + except.Message
									+ "\nFROM HERE:\n"
									+ except.StackTrace + "\n");
				IsAttributeProblem = true;
			}
		}


		// Attributes provided by caller
		public string DpsSet {
			get; private set;
		}

		public string NakedSet {
			get; private set;
		}

		public bool StayNaked {
			get; private set;
		}

		// Private variables for internal state
		private ConfigMemento _configMemento;
		private bool _isDisposed;
		private Composite _root;

		// DON'T EDIT THESE--they are auto-populated by Subversion
		public override string SubversionId { get { return ("$Id$"); } }
		public override string SubversionRevision { get { return ("$Revision$"); } }


		~Ogrila_Wrangle() {
			Dispose(false);
		}


		public void Dispose(bool isExplicitlyInitiatedDispose) {
			if (!_isDisposed) {
				// NOTE: we should call any Dispose() method for any managed or unmanaged
				// resource, if that resource provides a Dispose() method.

				// Clean up managed resources, if explicit disposal...
				if (isExplicitlyInitiatedDispose) {
					// empty, for now
				}

				// Clean up unmanaged resources (if any) here...
				if (_configMemento != null) {
					_configMemento.Dispose();
					_configMemento = null;
				}

				BotEvents.OnBotStop -= BotEvents_OnBotStop;
				TreeRoot.GoalText = string.Empty;
				TreeRoot.StatusText = string.Empty;

				ReequipStuff();

				// Call parent Dispose() (if it exists) here ...
				base.Dispose();
			}

			_isDisposed = true;
		}

		public void BotEvents_OnBotStop(EventArgs args) {
			Dispose();
		}

		private void UnequipStuff() {
			if (null != NakedSet) {
				Lua.DoString("UseEquipmentSet(\"{0}\")", NakedSet, null);
			}
		}

		private void ReequipStuff() {
			if (null != DpsSet && null != NakedSet) { // if naked was null we wouldn't have unequipped
				Lua.DoString("UseEquipmentSet(\"{0}\")", DpsSet, null);
			}
		}

		public static LocalPlayer Me {
			get { return StyxWoW.Me; }
		}

		public WoWItem Item {
			get {
				return StyxWoW.Me.CarriedItems.FirstOrDefault(i => i.Entry == ItemId);
			}
		}

		private WoWUnit _nearestUnit;

		public WoWUnit NearestUnit {
			get {
				return _nearestUnit = ObjectManager.GetObjectsOfType<WoWUnit>()
					.Where(o => o.IsValid && o.IsAlive &&
						!Blacklist.Contains(o) &&
						(o.IsTargetingMeOrPet ||
						 (UnitId == o.Entry && o.Z < MaxPullZ)))
					.OrderBy(o => o.IsTargetingMeOrPet ? 0 : 1)
					.ThenBy(o => o.DistanceSqr)
					.FirstOrDefault();
			}
		}

		#region Overrides of CustomForcedBehavior

		private WoWPoint _nextWaypoint = WoWPoint.Empty;

		protected override TreeSharp.Composite CreateBehavior() {
			return _root ?? (_root = new PrioritySelector(
				new Decorator(ret => IsDone, new Action(c => {
					TreeRoot.StatusText = "Aether ray wrangling complete!";
					ReequipStuff();
				})),
				new Decorator(ret => null == NearestUnit, new PrioritySelector(
					new Decorator(ret => waypoints.Peek() != _nextWaypoint, new Action(c => {
						_nextWaypoint = waypoints.Peek();
						ReequipStuff();
						TreeRoot.StatusText = "No mobs nearby, moving to next waypoint: " + _nextWaypoint;
					})),
					new Decorator(ret => _nextWaypoint.DistanceSqr(Me.Location) > DistanceCheckSqr, new Action(c => {
						Flightor.MoveTo(_nextWaypoint);
					})),
					new Action(c => { waypoints.Dequeue(); })
				)),
				new Decorator(ret => null != _nearestUnit, new PrioritySelector(
					new Decorator(ret => !_nearestUnit.IsAlive, new Action(c => {
						TreeRoot.StatusText = "Current target is dead.";
						_nearestUnit = NearestUnit;
					})),
					new Decorator(ret => _nearestUnit.DistanceSqr > DistanceCheckSqr, new Action(c => {
						TreeRoot.StatusText = "Approaching target.";
						Flightor.MoveTo(_nearestUnit.Location);
					})),
					new Decorator(ret => Item.Cooldown > 0f, new Action(c => {
						TreeRoot.StatusText = "Waiting on wrangling rope cooldown.";
					})),
					new Decorator(ret => Me.Auras.ContainsKey(DruidSwiftFlightForm) || Me.Auras.ContainsKey(DruidFlightForm), new Action(c => {
						TreeRoot.StatusText = "Cancelling flight form.";
						Lua.DoString("CancelShapeshiftForm()");
					})),
					new Decorator(ret => Me.Mounted, new Action(c => {
						TreeRoot.StatusText = "Dismounting.";
						Lua.DoString("Dismount()");
					})),
					new Action(c => {
						// not supposed to have to use loops and sleep but I don't know how to prevent CC combat otherwise

						if (null != Me.Pet) {
							Lua.DoString("PetPassiveMode()");
						}

						TreeRoot.StatusText = "Pulling target.";
						UnequipStuff();

						bool doBackUp = false;
						while (null != _nearestUnit && _nearestUnit.IsValid && _nearestUnit.IsAlive) {
							if (!_nearestUnit.IsWithinMeleeRange) {
								Navigator.MoveTo(_nearestUnit.Location);
							} else {
								while (!Me.IsSafelyFacing(_nearestUnit)) {
									_nearestUnit.Face();
									doBackUp = true;
								}

								if (doBackUp) {
									WoWMovement.Move(WoWMovement.MovementDirection.Backwards);
									Thread.Sleep(500);
									WoWMovement.MoveStop(WoWMovement.MovementDirection.Backwards);
									doBackUp = false;
								}
							}

							if (Me.CurrentTargetGuid != _nearestUnit.Guid) {
								_nearestUnit.Target();
							}

							if (_nearestUnit.HealthPercent < WrangleHealthPercent) {
								if (Me.IsAutoAttacking) {
									Lua.DoString("AttackTarget()");
								}

								TreeRoot.StatusText = "Trying to wrangle.";
								Thread.Sleep(2000);
								Item.UseContainerItem();
								Thread.Sleep(50);

								while (Me.IsCasting) {
									Thread.Sleep(50);
								}

								Thread.Sleep(10000);
								TreeRoot.StatusText = "Wrangle logic complete.";
								Blacklist.Add(_nearestUnit, BlacklistTime);
								_nearestUnit = null;
								break;
							} else {
								if (!Me.IsAutoAttacking) {
									Lua.DoString("AttackTarget()");
								}
							}

							Thread.Sleep(50);
						}

						if (!StayNaked) {
							ReequipStuff();
						}
					})
				))//,
				//new Action(c => { TreeRoot.StatusText = "ASSERT: Shouldn't get here"; })
			));
		}


		public override void Dispose() {
			Dispose(true);
			GC.SuppressFinalize(this);
		}


		private bool _isDone = false;

		public override bool IsDone {
			get {
				// manually set to done
				if (_isDone) return true;

				foreach (int qid in QuestIds) {
					// if any of the quests are in the log and complete then this behavior is done
					if (UtilIsProgressRequirementsMet(qid, QuestInLogRequirement.InLog, QuestCompleteRequirement.Complete)) {
						Logging.Write("Completed {0}.", Me.QuestLog.GetQuestById((uint)qid).Name);
						return true;
					}

					// if any of the quests are in the log and not complete then then this behavior is not done
					if (UtilIsProgressRequirementsMet(qid, QuestInLogRequirement.InLog, QuestCompleteRequirement.NotComplete)) {
						return false;
					}
				}

				// if none of the quests were in the log then this behavior is done
				Logging.Write("Not on wrangle quest.");
				return true;
			}
		}

		public override void OnStart() {
			// This reports problems, and stops BT processing if there was a problem with attributes...
			// We had to defer this action, as the 'profile line number' is not available during the element's
			// constructor call.
			OnStart_HandleAttributeProblem();

			if (null == Item) {
				_isDone = true;
				Logging.Write(Color.Red, "Missing item Wrangling Rope, skipping.");
			}

			// If the quest is complete, this behavior is already done...
			// So we don't want to falsely inform the user of things that will be skipped.
			if (!IsDone) {
				// The ConfigMemento() class captures the user's existing configuration.
				// After its captured, we can change the configuration however needed.
				// When the memento is dispose'd, the user's original configuration is restored.
				// More info about how the ConfigMemento applies to saving and restoring user configuration
				// can be found here...
				//     http://www.thebuddyforum.com/mediawiki/index.php?title=Honorbuddy_Programming_Cookbook:_Saving_and_Restoring_User_Configuration
				_configMemento = new ConfigMemento();

				BotEvents.OnBotStop += BotEvents_OnBotStop;
				
				// Disable any settings that may cause us to dismount --
				// When we mount for travel via FlyTo, we don't want to be distracted by other things.
				// We also set PullDistance to its minimum value.  If we don't do this, HB will try
				// to dismount and engage a mob if it is within its normal PullDistance.
				// NOTE: these settings are restored to their normal values when the behavior completes
				// or the bot is stopped.
				StyxSettings.Instance.KillBetweenHotspots = false;
				CharacterSettings.Instance.HarvestHerbs = false;
				CharacterSettings.Instance.HarvestMinerals = false;
				CharacterSettings.Instance.LootChests = false;
				CharacterSettings.Instance.LootMobs = false;
				CharacterSettings.Instance.NinjaSkin = false;
				CharacterSettings.Instance.SkinMobs = false;
				CharacterSettings.Instance.PullDistance = 1;

				TreeRoot.GoalText = "Ogri'la wrangling";

				UnequipStuff();
			}
		}

		#endregion
	}
}
