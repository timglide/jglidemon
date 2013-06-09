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

namespace timglide {
	[CustomBehaviorFileName(@"Ogrila_Bomb")]
	class Ogrila_Bomb : CustomForcedBehavior {
		/// <summary>
		/// Custom quest behavior for initial and daily versions of bombing run quests.
		/// By timglide
		/// </summary>
		public static readonly int[] QuestIds = { 11010, 11102, 11023 };
		public const uint ItemId = 32456; // skyguard bombs
		public const int SpellId = 40160; // throw bomb
		public const uint UnitId = 23118; // cannonball stack
		public const int DebuffId = 40075; // fel flak fire
		public const uint DistanceCheck = 5;
		public const uint DistanceCheckSqr = DistanceCheck * DistanceCheck;
		public const uint SafeHeight = 140;
		public const uint AttackRange = 30;
		public const uint AttackRangeSqr = AttackRange * AttackRange;
		private static readonly TimeSpan BlacklistTime = TimeSpan.FromSeconds(60);

		private readonly CircularQueue<WoWPoint> waypoints = new CircularQueue<WoWPoint>() {
			new WoWPoint(1648.385, 7196.906, 483.3324),
			new WoWPoint(1475.662, 7250.958, 481.1898),
			new WoWPoint(1335.321, 7205.595, 510.372),
			new WoWPoint(1696.257, 7370.372, 475.0768)
		};

		public Ogrila_Bomb(Dictionary<string, string> args)
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


		~Ogrila_Bomb() {
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

		public bool IsDebuffed {
			get {
				WoWAura debuff = Me.GetAuraById(DebuffId);
				return null != debuff;
			}
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
					.Where(o => o.Entry == UnitId && o.IsAlive && !Blacklist.Contains(o))
					.OrderBy(o => o.DistanceSqr)
					.FirstOrDefault();
			}
		}

		#region Overrides of CustomForcedBehavior

		private WoWPoint _nextWaypoint = WoWPoint.Empty;
		private WoWPoint _nextPoint = WoWPoint.Empty;

		protected override Composite CreateBehavior() {
			return _root ?? (_root = new PrioritySelector(
				new Decorator(ret => IsDone, new Action(c => {
					TreeRoot.StatusText = "Bombing run complete!";
				})),
				new Decorator(ret => null == NearestUnit, new PrioritySelector(
					new Decorator(ret => waypoints.Peek() != _nextWaypoint, new Action(c => {
						_nextWaypoint = waypoints.Peek();
						TreeRoot.StatusText = "No cannonballs nearby, moving to next waypoint: " + _nextWaypoint;
					})),
					new Decorator(ret => _nextWaypoint.DistanceSqr(Me.Location) > DistanceCheckSqr, new Action(c => {
						Flightor.MoveTo(_nextWaypoint);
					})),
					new Action(c => { waypoints.Dequeue(); }),
					// make sure objects load
					new WaitContinue(5, ret => false, new ActionAlwaysSucceed())
				)),
				new Decorator(ret => null != _nearestUnit &&
						_nearestUnit.Distance2DSqr > DistanceCheckSqr, new Action(c => {
					TreeRoot.StatusText = "Moving safely above next cannonball.";
					WoWPoint point = _nearestUnit.Location;
					point.Z += SafeHeight;
					WoWMovement.ClickToMove(point);
					//Flightor.MoveTo(point);
				})),
				new Decorator(ret => IsDebuffed, new Action(c => {
					TreeRoot.StatusText = "Waiting for Fel Flak Fire debuff to go away.";
				})),
				new Decorator(ret => Item.Cooldown > 0f, new Action(c => {
					TreeRoot.StatusText = "Waiting on bomb cooldown.";
				})),
				new Decorator(ret => null != _nearestUnit, new Sequence(
					new Action(c => {
						TreeRoot.StatusText = "Moving to cannonball.";
						_nextPoint = _nearestUnit.Location;
						_nextPoint.Z += AttackRange;
					}),
					new PrioritySelector(
						new Decorator(ret => _nextPoint.DistanceSqr(Me.Location) > DistanceCheckSqr, new Action(c => {
							WoWMovement.ClickToMove(_nextPoint);
							//Flightor.MoveTo(_nextPoint);
						})),
						new Sequence(
							new Action(c => {
								TreeRoot.StatusText = "Bombing cannonball.";
								Item.UseContainerItem();
								SpellManager.ClickRemoteLocation(_nearestUnit.Location);
							}),
							new Wait(TimeSpan.FromSeconds(0.5), ret => StyxWoW.Me.IsCasting, new ActionAlwaysSucceed()),
							new WaitContinue(TimeSpan.FromSeconds(0.5), ret => !StyxWoW.Me.IsCasting, new ActionAlwaysSucceed()),
							new Action(c => {
								TreeRoot.StatusText = "Moving safely above cannonball.";
								_nextPoint = _nearestUnit.Location;
								_nextPoint.Z += SafeHeight;
								Blacklist.Add(_nearestUnit, BlacklistTime);
								_nearestUnit = null;
							}),
							new Decorator(ret => _nextPoint.DistanceSqr(Me.Location) > DistanceCheckSqr, new Action(c => {
							    WoWMovement.ClickToMove(_nextPoint);
							})),
							new Action(c => {
								_nextPoint = WoWPoint.Empty;
							})
						)
					)
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
				Logging.Write("Not on bombing quest.");
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
				Logging.Write(/*Color.Red,*/ "Missing item Skyguard Bombs, skipping.");
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
				CharacterSettings.Instance.HarvestHerbs = false;
				CharacterSettings.Instance.HarvestMinerals = false;
				CharacterSettings.Instance.LootChests = false;
				CharacterSettings.Instance.LootMobs = false;
				CharacterSettings.Instance.NinjaSkin = false;
				CharacterSettings.Instance.SkinMobs = false;
				CharacterSettings.Instance.PullDistance = 1;

				TreeRoot.GoalText = "Ogri'la bombing run";
			}
		}

		#endregion
	}
}
