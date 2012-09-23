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
using Styx.TreeSharp;
using Styx.CommonBot;
using Styx.Common;
using Styx.CommonBot.Profiles;
using Styx.Pathing;
using Styx.CommonBot.Frames;

namespace timglide {
	/// <summary>
	/// Custom quest behavior for "Tonk Commander" Darkmoon Faire daily quest.
	/// By timglide
	/// </summary>
	class DMF_Tonk : CustomForcedBehavior {
		private static readonly int[] QuestIds = { 29434 };
		private const uint GameTokenId = 71083;
		private const int BuffId = 0; // TODO
		private readonly int[] DebuffIds = new[] { 109976 }; // Stay out
		private const uint NpcId = 54605; // Finlay Coolshot
		private const int GossipOption = 1; // TODO
		private const int MarkedSpellId = 102341; // Marked!
		private const int SpeedBoostSpellId = 102297; // Nitrous Boost
		private const uint PlayerTonkId = 54588;
		private const uint EnemyTonkId = 54642;
		private const uint TargetId = 33081; // Tonk Target
		private const float DefaultSafeDistance = 40f;
		private const uint CannonActionButton = 1;
		private const uint SpeedBoostActionButton = 2;
		private const uint DistanceCheck = 2;
		private const uint DistanceCheckSqr = DistanceCheck * DistanceCheck;

		private static readonly TimeSpan BlacklistTime = TimeSpan.FromSeconds(60);
		private static readonly TimeSpan TempBlacklistTime = TimeSpan.FromSeconds(5);
		private static readonly TimeSpan StuckTimeLimit = TimeSpan.FromSeconds(2);
		private static readonly WoWPoint Center = new WoWPoint(-4136.531, 6302.656, 13.1169);

		public DMF_Tonk(Dictionary<string, string> args)
			: base(args) {
			
			try {
				SafeDistance = GetAttributeAsNullable<float>("SafeDistance", false, null, new[] { "MarkDistance", "RunDistance" }) ?? DefaultSafeDistance;
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
		public float SafeDistance { get; private set; }

		// Private variables for internal state
		private ConfigMemento _configMemento;
		private bool _isDisposed;
		private Composite _root;
		private bool _started = false;

		private Queue<WoWPoint> _pathPoints = new Queue<WoWPoint>();
		private Queue<WoWPoint> _escapePathPoints = new Queue<WoWPoint>();

		private DateTime _lastMoveTime = DateTime.Now;
		private WoWPoint _lastPlayerTonkLocation = WoWPoint.Empty;

		// DON'T EDIT THESE--they are auto-populated by Subversion
		public override string SubversionId { get { return ("$Id$"); } }
		public override string SubversionRevision { get { return ("$Revision$"); } }


		~DMF_Tonk() {
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

				Lua.Events.DetachEvent("COMBAT_LOG_EVENT_UNFILTERED", COMBAT_LOG_EVENT_UNFILTERED);
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

		public bool HasMark {
			get { return Me.HasAura(MarkedSpellId) || (HasTonk && PlayerTonk.HasAura(MarkedSpellId)); }
		}

		public bool HasSpeedBoost {
			get { return Me.HasAura(SpeedBoostSpellId) || (HasTonk && PlayerTonk.HasAura(SpeedBoostSpellId)); }
		}

		private bool CanFireCannon {
			get { return !IsActionOnCooldown(CannonActionButton); }
		}

		private bool CanSpeedBoost {
			get { return !IsActionOnCooldown(SpeedBoostActionButton); }
		}

		public bool HasDebuff {
			get {
				foreach (int id in DebuffIds) {
					if (Me.HasAura(id)) return true;
				}

				return false;
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

		private WoWUnit _target = null;

		public WoWUnit Target {
			get {
				return _target = ObjectManager.GetObjectsOfType<WoWUnit>()
					.Where(o => !Blacklist.Contains(o) && o.IsValid && o.IsAlive && TargetId == o.Entry)
					.OrderBy(o => o.DistanceSqr)
					.FirstOrDefault();
			}
		}

		public bool HasTonk {
			get { return null != PlayerTonk; }
		}

		public WoWUnit PlayerTonk {
			get {
				if (null != Me.Pet && PlayerTonkId == Me.Pet.Entry) {
					return Me.Pet;
				}

				return null;

				//string guidStr = Lua.GetReturnVal<string>("return UnitGUID(\"vehicle\")", 0);
				
				//if (string.IsNullOrWhiteSpace(guidStr)) return null;

				//ulong guid = Convert.ToUInt64(guidStr.Substring(2));
				//WoWUnit vehicle = ObjectManager.GetObjectByGuid<WoWUnit>(guid);

				//return (null != vehicle && PlayerTonkId == vehicle.Entry) ? vehicle : null;

				//return ObjectManager.GetObjectsOfType<WoWUnit>()
				//    .Where(o => PlayerTonkId == o.Entry && o.OwnedByUnit == Me)
				//    .FirstOrDefault();
			}
		}

		public bool HasEnemyTonk {
			get { return null != EnemyTonk && EnemyTonk.IsValid && EnemyTonk.IsAlive; }
		}

		/// <summary>
		/// The last enemy tonk to mark us
		/// </summary>
		public WoWUnit EnemyTonk {
			get; private set;
		}

		private WoWPoint SafeLocation {
			get {
				return WoWMathHelper.CalculatePointFrom(PlayerTonk.Location, Center, -SafeDistance);
			}
		}

		public void PerformAction(uint button) {
			Lua.DoString("CastPetAction({0})", button);
		}

		public bool IsActionOnCooldown(uint button) {
			return 0 != Lua.GetReturnVal<float>("return GetPetActionCooldown(" + button + ")", 0);
		}

		private void COMBAT_LOG_EVENT_UNFILTERED(object sender, LuaEventArgs args) {
			string @event = args.Args[1].ToString();

			if ("SPELL_CAST_SUCCESS" != @event) return;

			try {
				string spellIdStr = args.Args[11].ToString();
				string spellName = args.Args[12].ToString();
				uint spellId = uint.Parse(spellIdStr);

				if (MarkedSpellId != spellId) return;

				string destGuidStr = args.Args[7].ToString();
				ulong destGuid = Convert.ToUInt64(destGuidStr.Substring(2), 16); // remove "0x"
				WoWUnit dest = ObjectManager.GetObjectByGuid<WoWUnit>(destGuid);

				if (null == dest) {
					LogMessage("debug", "Invalid dest GUID ({0}).", destGuidStr);
					return;
				}

				if (PlayerTonk != dest) return;

				string sourceGuidStr = args.Args[3].ToString();
				ulong sourceGuid = Convert.ToUInt64(sourceGuidStr.Substring(2), 16); // remove "0x"
				WoWUnit source = ObjectManager.GetObjectByGuid<WoWUnit>(sourceGuid);

				if (null == source) {
					LogMessage("debug", "Invalid source GUID ({0}).", sourceGuidStr);
					return;
				}

				LogMessage("debug", "{0} ({1}) was cast by {2} at {3}.", spellName, spellId, source.Name, source.Location);

				EnemyTonk = source;
			} catch (Exception e) {
				LogMessage("debug", "Exception during COMBAT_LOG_EVENT_UNFILTERED.");
				Logging.WriteException(e);
			}
		}

		#region Overrides of CustomForcedBehavior

		protected override Composite CreateBehavior() {
			return _root ?? (_root = new PrioritySelector(
				new Decorator(ret => HasDebuff, new Action(c => {
					// can't move when debuffed even if we're done so just wait
					TreeRoot.StatusText = "Waiting for debuff to go away.";
				})),
				new Decorator(ret => IsDone, new Action(c => {
					TreeRoot.StatusText = "Tonk Commander complete!";
				})),
				new Decorator(ret => _started && !HasTonk, new Action(c => {
					_isDone = true; // Ran out of time
				})),
				new Decorator(ret => HasTonk, new PrioritySelector(
					new Decorator(ret => !(PlayerTonk.IsValid && PlayerTonk.IsAlive), new ActionAlwaysSucceed()), // when it dies you can't do anything for a moment
					new Decorator(ret => IsQuestComplete, new Action(c => {
						TreeRoot.StatusText = "Exiting vehicle.";
						Lua.DoString("VehicleExit()");
						Thread.Sleep(1000); // sue me
					})),
					new Decorator(ret => HasMark && HasEnemyTonk, new PrioritySelector(
						new Decorator(ret => 0 == _escapePathPoints.Count, new Action(c => {
							WoWPoint safeLocation = SafeLocation;
							TreeRoot.StatusText = "Running away from enemy tonk to " + safeLocation + ".";
							WoWPoint[] points = Navigator.GeneratePath(PlayerTonk.Location, safeLocation);

							foreach (WoWPoint p in points) {
								_escapePathPoints.Enqueue(p);
							}
						})),
						new Decorator(ret => _escapePathPoints.Peek().DistanceSqr(PlayerTonk.Location) <= DistanceCheckSqr, new Action(c => {
							_escapePathPoints.Dequeue();
						})),
						// not working well, but unnecessary anyway
						//new Decorator(ret => !HasSpeedBoost && CanSpeedBoost, new Action(c => {
						//    PerformAction(SpeedBoostActionButton);
						//})),
						new Action(c => {
							WoWMovement.ClickToMove(_escapePathPoints.Peek());
							// no stuck detection when fleeing
							//_stuckPoint = Me.Location;
						})
					)),
					new Decorator(ret => 0 != _escapePathPoints.Count, new Action(c => {
						_escapePathPoints.Clear();
					})),
					new Decorator(ret => _target != Target, new Action(c => {
						WoWPoint loc = Center;

						if (null != _target) {
							TreeRoot.StatusText = "Moving to next target at " + _target.Location + ".";
							loc = _target.Location;
						} else {
							TreeRoot.StatusText = "Moving to center to wait for new target.";
						}

						_pathPoints.Clear();
						WoWPoint[] points = Navigator.GeneratePath(PlayerTonk.Location, loc);

						foreach (WoWPoint p in points) {
							_pathPoints.Enqueue(p);
						}
					})),
					new Decorator(ret => null != _target && _target.Location.DistanceSqr(PlayerTonk.Location) <= DistanceCheckSqr && CanFireCannon, new Action(c => {
						TreeRoot.StatusText = "Firing on target at " + _target.Location + ".";
						PerformAction(CannonActionButton);
						Blacklist.Add(_target, BlacklistTime);
					})),
					new Decorator(ret => 0 == _pathPoints.Count, new Action(c => {
						TreeRoot.StatusText = "Waiting for new target.";
					})),
					new Decorator(ret => _pathPoints.Peek().DistanceSqr(PlayerTonk.Location) <= DistanceCheckSqr, new Action(c => {
						_pathPoints.Dequeue();
					})),
					new Action(c => {
						WoWMovement.ClickToMove(_pathPoints.Peek());

						if (null != _target && WoWPoint.Empty != _lastPlayerTonkLocation) {
							if ((DateTime.Now - _lastMoveTime) >= StuckTimeLimit) {
								LogMessage("debug", "Stuck, blacklisting current target.");
								Blacklist.Add(_target, TempBlacklistTime);
							}

							if (PlayerTonk.Location.Distance2DSqr(_lastPlayerTonkLocation) <= 0.5 * 0.5) {
								// we might be stuck, keep the stuck time/point where they are
							} else {
								_lastMoveTime = DateTime.Now;
								_lastPlayerTonkLocation = PlayerTonk.Location;
							}
						} else {
							_lastMoveTime = DateTime.Now;
							_lastPlayerTonkLocation = PlayerTonk.Location;
						}
					})
				)),
				new Decorator(ret => !_started, new PrioritySelector(
					new Decorator(ret => !HasGameToken, new Action(c => {
						LogMessage("error", "No game tokens, skipping tonk commander.");
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
						}),
						new Action(c => {
							_started = true;
							Lua.Events.AttachEvent("COMBAT_LOG_EVENT_UNFILTERED", COMBAT_LOG_EVENT_UNFILTERED);
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

		private bool IsQuestComplete {
			get {
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

		public override bool IsDone {
			get {
				// can't move so wait
				if (HasDebuff) return false;

				// manually set to done
				if (_isDone) return true;

				if (HasTonk) return false;

				return IsQuestComplete;
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
				TreeRoot.GoalText = "Tonk Commander";


				// TODO see if we need to manually add blackspots for the buildings
				// Styx.Logic.Pathing.BlackspotManager.AddBlackspot(Styx.Logic.Pathing.WoWPoint, float, float);
			}
		}

		#endregion
	}
}
