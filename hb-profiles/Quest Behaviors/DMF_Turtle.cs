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
using Styx.TreeSharp;
using Styx.CommonBot;
using Styx.Pathing;
using Styx.CommonBot.Frames;
using Styx.Common;

namespace timglide {
	/// <summary>
	/// Custom quest behavior for "Target: Turtle" Darkmoon Faire daily quest.
	/// By timglide
	/// </summary>
	[CustomBehaviorFileName(@"DMF_Turtle")]
	class DMF_Turtle : CustomForcedBehavior {
		private static readonly int[] QuestIds = { 29455 };
		private const uint GameTokenId = 71083;
		private const int BuffId = 102058; // Ring Toss
		private const uint NpcId = 54485; // Jessica Rogers
		private const int GossipOption = 1; // Ready to Play!
		private const uint TargetId = 54490; // Dubenko
		private const float FacingDegrees = 45f;
		private const int ActionSpellId = 101695;
		private const uint ActionButton = 1;
		private const uint DistanceCheck = 1;
		private const uint DistanceCheckSqr = DistanceCheck * DistanceCheck;

		private static readonly WoWPoint StandPoint = new WoWPoint(-4287.872, 6308.999, 13.11773);

		public DMF_Turtle(Dictionary<string, string> args)
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


		~DMF_Turtle() {
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

		public WoWUnit Target {
			get {
				return ObjectManager.GetObjectsOfType<WoWUnit>()
					.Where(o => o.IsValid && o.IsAlive && TargetId == o.Entry)
					.OrderBy(o => o.DistanceSqr)
					.FirstOrDefault();
			}
		}

		public void PerformAction(uint button) {
			Lua.DoString("OverrideActionBarButton{0}:Click()", button);
		}

		public bool IsActionOnCooldown(uint button) {
			return 0 != Lua.GetReturnVal<float>("return GetActionCooldown(" + (120 + button) + ")", 0);
		}

		#region Overrides of CustomForcedBehavior

		protected override Composite CreateBehavior() {
			return _root ?? (_root = new PrioritySelector(
				new Decorator(ret => IsDone, new Action(c => {
					TreeRoot.StatusText = "Target: Turtle complete!";
				})),
				new Decorator(ret => _started && !HasBuff, new Action(c => {
					_isDone = true; // Ran out of rings or time
				})),
				new Decorator(ret => HasBuff, new PrioritySelector(
					new Decorator(ret => StandPoint.DistanceSqr(Me.Location) > DistanceCheckSqr, new Action(c => {
						WoWMovement.ClickToMove(StandPoint);
					})),
					new Decorator(ret => !Me.IsSafelyFacing(Target, FacingDegrees), new Action(c => {
						Target.Face();
					})),
					new Sequence(
						new Action(c => {
							TreeRoot.StatusText = "Throwing ring.";
							PerformAction(ActionButton);
							Thread.Sleep(50);
							SpellManager.ClickRemoteLocation(Target.Location);
						}),
						new Wait(TimeSpan.FromSeconds(1), ret => Me.IsCasting, new ActionAlwaysSucceed()),
						new WaitContinue(TimeSpan.FromSeconds(1), ret => !Me.IsCasting, new ActionAlwaysSucceed())
					)
				)),
				new Decorator(ret => !_started, new PrioritySelector(
					new Decorator(ret => !HasGameToken, new Action(c => {
						LogMessage("error", "No game tokens, skipping ring toss.");
						_isDone = true; // Can't play the game without a token
					})),
					new Decorator(ret => Npc.DistanceSqr > Npc.InteractRangeSqr, new Action(c => {
						Navigator.MoveTo(Npc.Location);
					})),
					new Sequence(
						new Action(c => {
							Npc.Interact();
							Thread.Sleep(2000);
						}),
						new Action(c => {
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
