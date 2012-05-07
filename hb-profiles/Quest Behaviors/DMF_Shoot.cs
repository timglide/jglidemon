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
using Styx.Logic.Inventory.Frames.Gossip;

namespace timglide {
	/// <summary>
	/// Custom quest behavior for "He Shoots, He Scores!" Darkmoon Faire daily quest.
	/// By timglide
	/// </summary>
	class DMF_Shoot : CustomForcedBehavior {
		private static readonly int[] QuestIds = { 29438 };
		private const uint GameTokenId = 71083;
		private const int BuffId = 101871; // Crack Shot!
		private const uint NpcId = 14841; // Rinling
		private const int GossipOption = 1; // Let's shoot!
		private const uint TargetId = 24171; // Darkmoon Faire Target Bunny
		private const int TargetBuffId = 101010; // Target Indicator - Quick Shot
		private const float FacingDegrees = 15;
		private const uint ActionButton = 1;
		private const float DistanceCheck = 0.5f;
		private const float DistanceCheckSqr = DistanceCheck * DistanceCheck;

		private static readonly WoWPoint StandPoint = new WoWPoint(-4074.602, 6350.226, 13.60911);

		public DMF_Shoot(Dictionary<string, string> args)
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
		private bool _started = false;

		// DON'T EDIT THESE--they are auto-populated by Subversion
		public override string SubversionId { get { return ("$Id$"); } }
		public override string SubversionRevision { get { return ("$Revision$"); } }


		~DMF_Shoot() {
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

		public static LocalPlayer Me {
			get { return StyxWoW.Me; }
		}

		public bool HasGameToken {
			get { return null != Me.CarriedItems.FirstOrDefault(i => GameTokenId == i.Entry); }
		}

		public bool HasBuff {
			get { return Me.HasAura(BuffId); }
		}

		public WoWUnit Npc {
			get {
				return ObjectManager.GetObjectsOfType<WoWUnit>()
					.Where(o => o.IsValid && o.IsAlive && NpcId == o.Entry)
					.OrderBy(o => o.DistanceSqr)
					.FirstOrDefault();
			}
		}

		private WoWUnit _target;

		public WoWUnit Target {
			get {
				return _target = ObjectManager.GetObjectsOfType<WoWUnit>()
					.Where(o => o.IsValid && o.IsAlive && TargetId == o.Entry && o.HasAura(TargetBuffId))
					.OrderBy(o => o.DistanceSqr)
					.FirstOrDefault();
			}
		}

		public void PerformAction(uint button) {
			Lua.DoString("BonusActionButton{0}:Click()", button);
		}

		#region Overrides of CustomForcedBehavior

		protected override TreeSharp.Composite CreateBehavior() {
			return _root ?? (_root = new PrioritySelector(
				new Decorator(ret => IsDone, new Action(c => {
					TreeRoot.StatusText = "He Shoots, He Scores! complete!";
				})),
				new Decorator(ret => _started && !HasBuff, new Action(c => {
					_isDone = true; // Ran out of time
				})),
				new Decorator(ret => HasBuff, new PrioritySelector(
					new Decorator(ret => StandPoint.Distance2DSqr(Me.Location) > DistanceCheckSqr, new Action(c => {
						WoWMovement.ClickToMove(StandPoint);
					})),
					new Decorator(ret => null == Target, new ActionAlwaysSucceed()), // wait for target
					new Decorator(ret => !Me.IsSafelyFacing(_target, FacingDegrees), new Action(c => {
						_target.Face();
					})),
					new Sequence(
						new Action(c => {
							TreeRoot.StatusText = "Shooting.";
							PerformAction(ActionButton);
							Thread.Sleep(1333);
							StyxWoW.SleepForLagDuration();
						}),
						new Action(c => {
							TreeRoot.StatusText = "Shooting.";
							PerformAction(ActionButton);
							Thread.Sleep(1333);
							StyxWoW.SleepForLagDuration();
						})
					)
				)),
				new Decorator(ret => !_started, new PrioritySelector(
					new Decorator(ret => !HasGameToken, new Action(c => {
						LogMessage("error", "No game tokens, skipping shooting booth.");
						_isDone = true; // Can't play the game without a token
					})),
					new Decorator(ret => Npc.DistanceSqr > Npc.InteractRangeSqr, new Action(c => {
						Navigator.MoveTo(Npc.Location);
					})),
					new Sequence(
						new Action(c => {
							Npc.Interact();
							Thread.Sleep(2000);
							GossipFrame.Instance.SelectGossipOption(GossipOption);
							Thread.Sleep(1000);
							_started = true;
						})
					)
				)),
				new Action(c => { TreeRoot.StatusText = "ASSERT: Shouldn't get here"; })
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

				// if none of the quests were in the log then do the behavior anyway for testing
				return false;
			}
		}

		public override void OnStart() {
			// This reports problems, and stops BT processing if there was a problem with attributes...
			// We had to defer this action, as the 'profile line number' is not available during the element's
			// constructor call.
			OnStart_HandleAttributeProblem();

			// If the quest is complete, this behavior is already done...
			// So we don't want to falsely inform the user of things that will be skipped.
			if (!IsDone) {
				BotEvents.OnBotStop += BotEvents_OnBotStop;
				TreeRoot.GoalText = "Target: Turtle";
			}
		}

		#endregion
	}
}
