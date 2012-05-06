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
	/// Custom quest behavior for "The Humanoid Cannonball" Darkmoon Faire daily quest.
	/// By timglide
	/// </summary>
	class DMF_Cannon : CustomForcedBehavior {
		private static readonly int[] QuestIds = { 29436 };
		private const byte DefaultTickRate = 30;
		private const uint GameTokenId = 71083;
		private const int BuffId = 102116; // Magic Wings
		private const float BuffMaxDuration = 8.5f;
		private const float BuffCancelDuration = 0.975f;
		private static readonly int[] BadBuffIds = { 130, 1706, 3714 }; // slow fall, levitate, path of frost
		private const int CancelWingsSpellId = 102120; // Cancel Magic Wings
		private const uint NpcId = 15303; // Maxima Blastenheimer
		private const int GossipOption = 1; // Launch me!
		private const uint TeleportNpcId = 57850; // Teleportologist Fozlebub
		private const int TeleportGossipOption = 0;
		private const ulong TeleportCostSilver = 30;
		private const uint ActionButton = 1;
		private const uint DistanceCheck = 2;
		private const uint DistanceCheckSqr = DistanceCheck * DistanceCheck;

		private static readonly WoWPoint BullseyePoint = new WoWPoint(-4477.992, 6221.702, 0.0008631438);

		public DMF_Cannon(Dictionary<string, string> args)
			: base(args) {

			try {
				CancelTimeLeft = GetAttributeAsNullable<float>("CancelTimeLeft", false, new ConstrainTo.Domain<float>(0f, BuffMaxDuration), new[] { "Cancel", "TimeLeft" }) ?? BuffCancelDuration;
				UseTeleport = GetAttributeAsNullable<bool>("UseTeleport", false, null, new[] { "Teleport", "PortBack" }) ?? true;
				TickRate = GetAttributeAsNullable<byte>("TickRate", false, new ConstrainTo.Domain<byte>(15, byte.MaxValue), new[] { "TicksPerSecond" }) ?? DefaultTickRate;
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
		public float CancelTimeLeft { get; private set; }
		public bool UseTeleport { get; private set; }
		public byte TickRate { get; private set; }

		// Private variables for internal state
		private byte _origTickRate;
		private ConfigMemento _configMemento;
		private bool _isDisposed;
		private Composite _root;
		private float _latency;
		private bool _started = false;
		private bool _hadWings = false;
		private WoWPoint _landPoint = WoWPoint.Empty;

		// DON'T EDIT THESE--they are auto-populated by Subversion
		public override string SubversionId { get { return ("$Id$"); } }
		public override string SubversionRevision { get { return ("$Revision$"); } }


		~DMF_Cannon() {
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
				TreeRoot.TicksPerSecond = _origTickRate;
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

		public static void CancelBuff(int BuffId) {
			Lua.DoString("local id={0} for i=0,40 do if select(11, UnitBuff(\"player\", i))==id then CancelUnitBuff(\"player\", i) return end end", BuffId);
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

		public double BuffDuration {
			get {
				WoWAura a = Me.GetAuraById(BuffId);

				if (null == a) return 0;

				return a.TimeLeft.TotalSeconds;
			}
		}

		public WoWUnit Npc {
			get {
				return ObjectManager.GetObjectsOfType<WoWUnit>()
					.Where(o => o.IsValid && o.IsAlive && NpcId == o.Entry)
					.OrderBy(o => o.DistanceSqr)
					.FirstOrDefault();
			}
		}

		public WoWUnit TeleportNpc {
			get {
				return ObjectManager.GetObjectsOfType<WoWUnit>()
					.Where(o => o.IsValid && o.IsAlive && TeleportNpcId == o.Entry)
					.OrderBy(o => o.DistanceSqr)
					.FirstOrDefault();
			}
		}

		public void PerformAction(uint button) {
			KeyboardManager.KeyUpDown((char)('1' + (button - 1)));
		}

		#region Overrides of CustomForcedBehavior

		protected override TreeSharp.Composite CreateBehavior() {
			return _root ?? (_root = new PrioritySelector(
				new Decorator(ret => IsDone, new Action(c => {
					TreeRoot.StatusText = "The Humanoid Cannonball complete!";
				})),
				new Decorator(ret => HasBuff, new PrioritySelector(
					new Decorator(ret => !_hadWings, new Action(c => {
						_hadWings = true;
					})),
					new Decorator(ret => BuffDuration <= (CancelTimeLeft + _latency), new Action(c => {
						TreeRoot.StatusText = "Cancelling wings.";
						PerformAction(ActionButton);
					})),
					new Action(c => {
						TreeRoot.StatusText = "Waiting to cancel wings.";
					})
				)),
				new Decorator(ret => _started && _hadWings && !HasBuff, new PrioritySelector(
					new Decorator(ret => Me.IsFalling, new Action(c => {
						TreeRoot.StatusText = "Waiting to land.";
					})),
					new Decorator(ret => WoWPoint.Empty == _landPoint && !Me.IsMoving, new Action(c => {
						_landPoint = Me.Location;
						LogMessage("info", "Landed {0} yards from bullseye.", _landPoint.Distance2D(BullseyePoint));
					})),
					new Decorator(ret => WoWPoint.Empty == _landPoint, new ActionAlwaysSucceed()),
					new Decorator(ret => null == TeleportNpc, new Action(c => {
						TreeRoot.StatusText = "Waiting for teleport npc to load.";
					})),
					new Decorator(ret => TeleportNpc.DistanceSqr < DistanceCheckSqr, new Sequence(
						new Action(c => {
							TreeRoot.StatusText = "Teleporting back to cannon.";
							TeleportNpc.Interact();
							Thread.Sleep(2000);
						}),
						new Action(c => {
							if (UseTeleport && Me.Silver >= TeleportCostSilver) {
								GossipFrame.Instance.SelectGossipOption(TeleportGossipOption);
								Thread.Sleep(1000);
								Lua.DoString("RunMacroText(\"/click StaticPopup1Button1\")"); // accept cost
							}
						}),
						new Action(c => { _isDone = true; })
					)),
					new Action(c => {
						TreeRoot.StatusText = "Moving to teleport back.";
						//WoWMovement.ClickToMove(TeleportNpc.Location);
						Navigator.MoveTo(TeleportNpc.Location);
					})
				)),
				new Decorator(ret => !_started, new PrioritySelector(
					new Decorator(ret => !HasGameToken, new Action(c => {
						LogMessage("error", "No game tokens, skipping cannon.");
						_isDone = true; // Can't play the game without a token
					})),
					new Decorator(ret => Npc.DistanceSqr > Npc.InteractRangeSqr, new Action(c => {
						Navigator.MoveTo(Npc.Location);
					})),
					new Sequence(
						new Action(c => {
							using (new FrameLock()) {
								foreach (int buffId in BadBuffIds) {
									if (Me.HasAura(buffId)) {
										CancelBuff(buffId);
									}
								}
							}
						}),
						new Action(c => {
							Npc.Interact();
							Thread.Sleep(2000);
							GossipFrame.Instance.SelectGossipOption(GossipOption);
							Thread.Sleep(1000);
							_started = true;
						})
					)
				)),
				new Action(c => { TreeRoot.StatusText = "Waiting to get wings."; })
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

				// Can't say we're done by the quest being completed since we still want to
				// teleport back even after the quest requirement is done.
				//foreach (int qid in QuestIds) {
				//    // if any of the quests are in the log and complete then this behavior is done
				//    if (UtilIsProgressRequirementsMet(qid, QuestInLogRequirement.InLog, QuestCompleteRequirement.Complete)) {
				//        Logging.Write("Completed {0}.", Me.QuestLog.GetQuestById((uint)qid).Name);
				//        return true;
				//    }

				//    // if any of the quests are in the log and not complete then then this behavior is not done
				//    if (UtilIsProgressRequirementsMet(qid, QuestInLogRequirement.InLog, QuestCompleteRequirement.NotComplete)) {
				//        return false;
				//    }
				//}

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
				TreeRoot.TicksPerSecond = TickRate;
				_latency = (float)((double)StyxWoW.WoWClient.Latency / 1000.0);
				BotEvents.OnBotStop += BotEvents_OnBotStop;
				TreeRoot.GoalText = "The Humanoid Cannonball";
			}

			// outside IsDone check since we will set it back in Dispose() regardless
			_origTickRate = TreeRoot.TicksPerSecond;
		}

		#endregion
	}
}
