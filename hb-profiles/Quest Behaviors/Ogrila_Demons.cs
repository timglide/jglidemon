using System;
using System.Linq;
using System.Collections.Generic;

using Styx;
using Styx.Helpers;

using Action = Styx.TreeSharp.Action;
using Styx.WoWInternals.WoWObjects;
using Styx.WoWInternals;
using System.Drawing;
using System.Threading;
using CommonBehaviors.Actions;
using Styx.CommonBot.Profiles;
using Styx.Common;
using Styx.TreeSharp;
using Styx.CommonBot;
using Styx.Pathing;
using Styx.Common.Helpers;

namespace timglide {
	/// <summary>
	/// Custom quest behavior for initial and daily version of banish demons quests.
	/// By timglide
	/// </summary>
	[CustomBehaviorFileName(@"Ogrila_Demons")]
	class Ogrila_Demons : CustomForcedBehavior {
		public static readonly int[] QuestIds = { 11026, 11051 };
		public const uint ItemId = 32696; // banishing crystal
		public const int SpellId = 40817; // summon banishing portal
		public static readonly uint[] UnitIds = { 19973, 22204 }; // abyssal flamebringer, fear fiend
		public const uint PortalId = 23322; // banishing crystal bunny
		public const int BanishmentId = 40825; // banishment buff when demon in range
		public const uint DistanceCheck = 5;
		public const uint DistanceCheckSqr = DistanceCheck * DistanceCheck;
		public const uint AttackRange = 30;
		public const uint AttackRangeSqr = AttackRange * AttackRange;
		public const uint PullRange = 60;
		public const uint PullRangeSqr = PullRange * PullRange;
		private static readonly TimeSpan BlacklistTime = TimeSpan.FromSeconds(60);
		private const string DruidFlightForm = "Flight Form";
		private const string DruidSwiftFlightForm = "Swift Flight Form";

		private readonly CircularQueue<WoWPoint> waypoints = new CircularQueue<WoWPoint>() {
			new WoWPoint(1618.769, 7319.127, 364.1201),
			new WoWPoint(1305.906, 7234.548, 371.9041)
		};

		public Ogrila_Demons(Dictionary<string, string> args)
			: base(args) {

			try {
				// no attributes
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

		// Private variables for internal state
		private ConfigMemento _configMemento;
		private bool _isDisposed;
		private Composite _root;

		// DON'T EDIT THESE--they are auto-populated by Subversion
		public override string SubversionId { get { return ("$Id$"); } }
		public override string SubversionRevision { get { return ("$Revision$"); } }


		~Ogrila_Demons() {
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

				// Call parent Dispose() (if it exists) here ...
				base.Dispose();
			}

			_isDisposed = true;
		}

		public void BotEvents_OnBotStop(EventArgs args) {
			Dispose();
		}

		public LocalPlayer Me {
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
				var portal = DroppedPortal;

				return _nearestUnit = ObjectManager.GetObjectsOfType<WoWUnit>()
					.Where(o => o.IsAlive &&
						!Blacklist.Contains(o) &&
						(o.IsTargetingMeOrPet || UnitIds.Contains(o.Entry)) &&
						((null != portal &&
						  portal.Location.DistanceSqr(o.Location) < PullRangeSqr) ||
						 (null == portal && o.DistanceSqr < PullRangeSqr)))
					.OrderBy(o => o.IsTargetingMeOrPet ? 0 : 1)
					.ThenBy(o => o.DistanceSqr)
					.FirstOrDefault();
			}
		}

		public WoWUnit DroppedPortal {
			get {
				var p = ObjectManager.GetObjectsOfType<WoWUnit>()
					.Where(o => PortalId == o.Entry)
					.OrderBy(o => o.DistanceSqr)
					.FirstOrDefault();
				//Logging.WriteDebug("Portal: {0}", null == p ? "null" : p.ToString());
				return p;
			}
		}

		#region Overrides of CustomForcedBehavior

		private WoWPoint _nextWaypoint = WoWPoint.Empty;

		protected override Composite CreateBehavior() {
			return _root ?? (_root = new PrioritySelector(
				new Decorator(ret => IsDone, new Action(c => {
					TreeRoot.StatusText = "Demon banishing complete!";
				})),
				new Decorator(ret => null == NearestUnit, new PrioritySelector(
					new Decorator(ret => waypoints.Peek() != _nextWaypoint, new Action(c => {
						_nextWaypoint = waypoints.Peek();
						TreeRoot.StatusText = "No mobs nearby, moving to next waypoint: " + _nextWaypoint;
					})),
					new Decorator(ret => _nextWaypoint.DistanceSqr(Me.Location) > DistanceCheckSqr, new Action(c => {
						Navigator.MoveTo(_nextWaypoint);
					})),
					new Action(c => { waypoints.Dequeue(); })
				)),
				new Decorator(ret => null == DroppedPortal, new PrioritySelector(
					new Decorator(ret => Me.Auras.ContainsKey(DruidSwiftFlightForm) || Me.Auras.ContainsKey(DruidFlightForm), new Action(c => {
						TreeRoot.StatusText = "Cancelling flight form.";
						Lua.DoString("CancelShapeshiftForm()");
					})),
					new Decorator(ret => Me.Mounted, new Action(c => {
						TreeRoot.StatusText = "Dismounting.";
						Lua.DoString("Dismount()");
					})),
					new Decorator(ret => Item.Cooldown > 0f, new Action(c => {
						TreeRoot.StatusText = "Waiting on banishing crystal cooldown.";
					})),
					new Action(c => {
						TreeRoot.StatusText = "Dropping banishing portal.";
						Item.UseContainerItem();
					})
				)),
				new Decorator(ret => null != _nearestUnit && (null != DroppedPortal || _nearestUnit.IsTargetingMeOrPet), new PrioritySelector(
					new Decorator(ret => !_nearestUnit.IsAlive, new Action(c => {
						TreeRoot.StatusText = "Current target is dead.";
						_nearestUnit = NearestUnit;
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

						TreeRoot.StatusText = "Agroing target.";
						while (_nearestUnit.IsAlive && !_nearestUnit.IsTargetingMeOrPet) {
							Navigator.MoveTo(_nearestUnit.Location);
							Thread.Sleep(100);
						}
						
						if (_nearestUnit.IsDemon) {
							TreeRoot.StatusText = "Moving back to portal.";
							while (null != DroppedPortal &&
									_nearestUnit.IsAlive &&
									_nearestUnit.IsTargetingMeOrPet &&
									DroppedPortal.Location.DistanceSqr(_nearestUnit.Location) > AttackRangeSqr) {
								Navigator.MoveTo(DroppedPortal.Location);
								Thread.Sleep(100);
							}

							WaitTimer banishTimer = new WaitTimer(TimeSpan.FromSeconds(20));
							TreeRoot.StatusText = "Waiting for target to get banishment.";
							while (null != DroppedPortal &&
									_nearestUnit.IsAlive &&
									_nearestUnit.IsTargetingMeOrPet &&
									null == _nearestUnit.GetAuraById(BanishmentId)) {
								Thread.Sleep(100);

								if (banishTimer.IsFinished) break;
							}
						}

						bool doBackUp = false;
						while (_nearestUnit.IsAlive && _nearestUnit.IsTargetingMeOrPet) {
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

							if (Me.CurrentTargetGuid != _nearestUnit.Guid) {
								_nearestUnit.Target();
							}

							if (!Me.IsAutoAttacking) {
								Lua.DoString("AttackTarget()");
							}

							Thread.Sleep(100);
						}

						Blacklist.Add(_nearestUnit, BlacklistTime);
						_nearestUnit = null;
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
				Logging.Write("Not on demon banishing quest.");
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
				// FIXME color
				Logging.Write(/*Color.Red,*/ "Missing item Banishing Crystal, skipping.");
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
				GlobalSettings.Instance.KillBetweenHotspots = false;
				CharacterSettings.Instance.HarvestHerbs = false;
				CharacterSettings.Instance.HarvestMinerals = false;
				CharacterSettings.Instance.LootChests = false;
				CharacterSettings.Instance.LootMobs = false;
				CharacterSettings.Instance.NinjaSkin = false;
				CharacterSettings.Instance.SkinMobs = false;
				CharacterSettings.Instance.PullDistance = 1;

				TreeRoot.GoalText = "Ogri'la demon banishing";
			}
		}

		#endregion
	}
}
