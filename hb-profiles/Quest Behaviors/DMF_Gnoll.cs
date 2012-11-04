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
	/// Custom quest behavior for "It's Hammer Time" Darkmoon Faire daily quest.
	/// By timglide
	/// ##Syntax##
	/// HoggerWeight: Relative importance of Hogger vs. regular gnolls
	/// DistanceWeight: Relative importance of distance in determining which gnoll to whack next
	/// MinDistance: If you are within this distance of a target, it will receive the full value of DistanceWeight
	/// MaxDistance: If you are greater than this distance from a target, it will be negatively weighted by DistanceWeight
	/// AgeWeight: Relative importance of how long since the gnoll spawned in determining which gnoll to whack next
	/// MaxAge: (Seconds) The longer a gnoll has been up the lower weight it will receive and if it is older than this value it will not be considered at all
	/// </summary>
	class DMF_Gnoll : CustomForcedBehavior {
		private static readonly int[] QuestIds = { 29463 };
		private const uint GameTokenId = 71083;
		private const int BuffId = 101612; // Whack-a-Gnoll!
		private readonly int[] DebuffIds = new[] { 110966, 109977, 101679 }; // Stay out, Wrong Whack
		private const uint NpcId = 54601; // Mola
		private const int GossipOption = 1; // Ready to whack

		private const float DefaultHoggerWeight = 50;
		private const float DefaultDistanceWeight = 25;
		private const float DefaultMinWeightDistance = 5;
		private const float DefaultMaxWeightDistance = 40; // TODO verify worst case (diaganol)
		private const float DefaultAgeWeight = 25;
		private const double DefaultMaxAge = 5.0; // seconds

		private const uint HoggerUnitId = 54549;
		private const uint HoggerSpellId = 102044;
		private const uint GnollUnitId = 54444;
		private const uint GnollSpellId = 102036;
		private const uint BabyUnitId = 54466;
		private const uint BabySpellId = 102043;
		private const uint ActionButton = 1;
		private const uint WhackRange = 2;
		private const uint WhackRangeSqr = WhackRange * WhackRange;
		private const uint ProximityCheck = 10; // If we're this close to a target keep heading toward that one
		private const uint ProximityCheckSqr = ProximityCheck * ProximityCheck;

		// -3975.677, 6289.321, 13.11582 Bottom left
		// -3994.719, 6292.95, 13.11582 Top right
		private static readonly TimeSpan BlacklistTime = TimeSpan.FromSeconds(60);
		private static readonly WoWPoint Center = new WoWPoint(-3985.259, 6291.168, 13.11582);

		public DMF_Gnoll(Dictionary<string, string> args)
			: base(args) {

			try {
				ConstrainTo.Domain<float> weightDomain = new ConstrainTo.Domain<float>(0, 100);
				ConstrainTo.Domain<float> rangeDomain = new ConstrainTo.Domain<float>(1, 100);
				HoggerWeight = GetAttributeAsNullable<float>("HoggerWeight", false, weightDomain, null) ?? DefaultHoggerWeight;
				DistanceWeight = GetAttributeAsNullable<float>("DistanceWeight", false, weightDomain, null) ?? DefaultDistanceWeight;
				MinDistance = GetAttributeAsNullable<float>("MinDistance", false, rangeDomain, null) ?? DefaultMinWeightDistance;
				MaxDistance = GetAttributeAsNullable<float>("MaxDistance", false, rangeDomain, null) ?? DefaultMinWeightDistance;
				AgeWeight = GetAttributeAsNullable<float>("AgeWeight", false, weightDomain, null) ?? DefaultAgeWeight;
				MaxAge = GetAttributeAsNullable<double>("MaxAge", false, new ConstrainTo.Domain<double>(1, 60), new[] { "TargetAge", "MaxTargetAge" }) ?? DefaultMaxAge;
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

		/// <summary>
		/// Lerp a value from one range to another
		/// </summary>
		/// <example>
		/// Lerp(5, 0, 10, 0, 1) == 0.5
		/// </example>
		/// <param name="value"></param>
		/// <param name="inStart"></param>
		/// <param name="inEnd"></param>
		/// <param name="outStart"></param>
		/// <param name="outEnd"></param>
		/// <returns></returns>
		public static float Lerp(float value, float inStart, float inEnd, float outStart, float outEnd) {
			float normed = (value - inStart) / (inEnd - inStart);
			return outStart + (normed * (outEnd - outStart));
		}

		public static double Lerp(double value, double inStart, double inEnd, double outStart, double outEnd) {
			double normed = (value - inStart) / (inEnd - inStart);
			return outStart + (normed * (outEnd - outStart));
		}


		private class Entry : IComparable<Entry> {
			public readonly DMF_Gnoll parent;
			public readonly WoWUnit Unit;
			public readonly DateTime Time;

			public Entry(DMF_Gnoll parent, WoWUnit Unit) {
				this.parent = parent;
				this.Unit = Unit;
				this.Time = DateTime.Now;
			}

			public override string ToString() {
				return string.Format("{0} (score={1}) at {2}", Name, Score, Location);
			}

			public string Name {
				get { return Unit.Name; }
			}

			public WoWPoint Location {
				get { return Unit.Location; }
			}

			public double Age {
				get { return (DateTime.Now - Time).TotalSeconds; }
			}

			public bool IsExpired {
				get { return !Unit.IsValid || !Unit.IsAlive || Age > parent.MaxAge; }
			}

			public float Score {
				get {
					float f = 0f;
					f += (HoggerUnitId == Unit.Entry) ? parent.HoggerWeight : 0f;

					float distance = Location.Distance2D(Me.Location);
					float clampedDistance = Math.Min(parent.MaxDistance, Math.Max(parent.MinDistance, distance));
					float distanceWeight = Lerp(clampedDistance, parent.MinDistance, parent.MaxDistance, 1f, -1f);
					f += distanceWeight * parent.DistanceWeight;

					double age = Age;
					double clampedAge = Math.Min(parent.MaxAge, age);
					double ageAmount = Lerp(clampedAge, 0, parent.MaxAge, 1, -1);
					f += (float)ageAmount * parent.AgeWeight;

					return f;
				}
			}

			public override bool Equals(object obj) {
				if (!(obj is Entry)) return false;
				if (object.ReferenceEquals(this, obj)) return true;
				Entry e = (Entry)obj;

				return this.Unit.Guid == e.Unit.Guid;
			}

			public override int GetHashCode() {
				return this.Unit.GetHashCode();
			}

			public int CompareTo(Entry that) {
				// prevent System.ArgumentException: IComparer (or the IComparable methods it relies upon) did not return zero when Array.Sort called x.CompareTo(x).
				// since Score changes constantly
				if (object.ReferenceEquals(this, that)) return 0;

				float thisWeight = Score, thatWeight = that.Score;

				if (thisWeight > thatWeight) { // higher weight comes first
					return -1;
				} else if (thisWeight == thatWeight) {
					return 0;
				} else {
					return 1;
				}
			}
		}


		// Attributes provided by caller
		public float HoggerWeight { get; private set; }
		public float DistanceWeight { get; private set; }
		public float MinDistance { get; private set; }
		public float MaxDistance { get; private set; }
		public float AgeWeight { get; private set; }
		public double MaxAge { get; private set; }

		// Private variables for internal state
		private ConfigMemento _configMemento;
		private bool _isDisposed;
		private Composite _root;
		private bool _started = false;

		private readonly object _queueLock = new object();
		private List<Entry> _queue = new List<Entry>(); // using list for RemoveAll()

		// DON'T EDIT THESE--they are auto-populated by Subversion
		public override string SubversionId { get { return ("$Id$"); } }
		public override string SubversionRevision { get { return ("$Revision$"); } }


		~DMF_Gnoll() {
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

		public bool IsBabyNear {
			get {
				WoWUnit baby = ObjectManager.GetObjectsOfType<WoWUnit>()
					.Where(o => o.IsValid && BabyUnitId == o.Entry)
					.OrderBy(o => o.Distance2DSqr)
					.FirstOrDefault();

				if (null == baby) return false;

				return baby.Distance2DSqr <= WhackRangeSqr;
			}
		}

		private bool HasEntry {
			get { return null != FirstEntry; }
		}

		private Entry FirstEntry {
			get {
				lock (_queueLock) {
					foreach (WoWUnit u in ObjectManager.GetObjectsOfType<WoWUnit>()
							.Where(o => !Blacklist.Contains(o) && o.IsValid && HoggerUnitId == o.Entry || GnollUnitId == o.Entry)) {
						Entry e = new Entry(this, u);

						if (!_queue.Contains(e)) {
							LogMessage("debug", "New entry: {0}.", e);
							_queue.Add(e);
						}
					}

					foreach (Entry e in _queue.Where(o => o.IsExpired)) {
						Blacklist.Add(e.Unit, BlacklistTime);
						//_queue.Remove(e); // cause concurrent modification error
					}

					_queue.RemoveAll(o => o.IsExpired);

					if (0 == _queue.Count) return null;

					_queue.Sort();
					return _queue[0];
				}
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
				new Decorator(ret => HasDebuff, new Action(c => {
					// can't move when debuffed even if we're done so just wait
					TreeRoot.StatusText = "Waiting for debuff to go away.";
				})),
				new Decorator(ret => IsDone, new Action(c => {
					TreeRoot.StatusText = "It's Hammer Time complete!";
				})),
				new Decorator(ret => _started && !HasBuff, new Action(c => {
					_isDone = true; // Ran out of time
				})),
				new Decorator(ret => HasBuff, new PrioritySelector(
					new Decorator(ret => !HasEntry, new PrioritySelector(
						new Decorator(ret => Me.Location.Distance2DSqr(Center) <= WhackRangeSqr, new Action(c => {
							TreeRoot.StatusText = "Waiting for gnoll to whack.";
						})),
						new Action(c => {
							TreeRoot.StatusText = "Moving to center to wait for gnoll.";
							WoWMovement.ClickToMove(Center);
						})
					)),
					new Decorator(ret => HasEntry, new PrioritySelector(
						new Decorator(ret => Me.Location.Distance2DSqr(FirstEntry.Location) > WhackRangeSqr, new Action(c => {
							lock (_queueLock) {
								if (!HasEntry) return;
								TreeRoot.StatusText = "Moving to " + FirstEntry;
								WoWMovement.ClickToMove(FirstEntry.Location);
							}
						})),
						new Decorator(ret => IsActionOnCooldown(ActionButton), new ActionAlwaysSucceed()),
						new Action(c => {
							lock (_queueLock) {
								if (!HasEntry) return;
								TreeRoot.StatusText = "Whacking " + FirstEntry;
								Blacklist.Add(_queue[0].Unit, BlacklistTime);
								_queue.RemoveAt(0);
							}

							if (IsBabyNear) { // sanity check
								LogMessage("debug", "We were about to hit a baby?!");
							} else {
								PerformAction(ActionButton);
								StyxWoW.SleepForLagDuration();
							}
						})
					))
				)),
				new Decorator(ret => !_started, new PrioritySelector(
					new Decorator(ret => !HasGameToken, new Action(c => {
						LogMessage("error", "No game tokens, skipping whack-a-gnoll.");
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
				// can't move so wait
				if (HasDebuff) return false;

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
				TreeRoot.GoalText = "It's Hammer Time";
			}
		}

		#endregion
	}
}
