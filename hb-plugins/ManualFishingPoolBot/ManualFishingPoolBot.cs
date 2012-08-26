using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx;
using Styx.Helpers;
using TreeSharp;
using Action = TreeSharp.Action;
using Styx.WoWInternals.WoWObjects;
using CommonBehaviors.Actions;
using Styx.WoWInternals;
using Styx.Logic.BehaviorTree;
using System.Threading;
using Styx.Logic.Combat;
using Styx.Logic;

namespace timglide {
	/// <summary>
	/// A bot that takes over fishing from a pool after your first, manual cast.
	/// by: timglide
	/// $Revision$
	/// </summary>
	class ManualFishingPoolBot : BotBase {
		private static readonly int[] FishingSpellIds = { 7620, 7731, 7732, 18248, 33095, 51294, 88868 };

		private const float
			PoolDistanceCheck = 30f,
			PoolDistanceCheckSqr = PoolDistanceCheck * PoolDistanceCheck,
			BobberDistanceCheck = 3.5f, // this is a little less than the value autoangler uses
			BobberDistanceCheckSqr = BobberDistanceCheck * BobberDistanceCheck;


		public override string Name {
			get { return "ManualFishingPoolBot"; }
		}

		public override PulseFlags PulseFlags {
			get { return PulseFlags.All; }
		}

		private enum State {
			WaitingForFirstCast, Running
		}
		
		private State state;
		private int fishingSpellId;
		WoWGameObject bobber;
		WoWGameObject pool;

		public override void Start() {
			state = State.WaitingForFirstCast;
			bobber = null;
			fishingSpellId = GetFishingSpellId();
			base.Start();
		}

		public int GetFishingSpellId() {
			foreach (int i in FishingSpellIds) {
				if (SpellManager.HasSpell(i)) return i;
			}

			return 0;
		}

		public WoWGameObject Bobber {
			get {
				return bobber = ObjectManager.GetObjectsOfType<WoWGameObject>()
					.FirstOrDefault(o =>
						o.IsValid && o.SubType == WoWGameObjectType.FishingBobber &&
						o.CreatedByGuid == Me.Guid);
			}
		}

		public bool IsBobbing {
			get {
				bobber = Bobber;
				return null != bobber ? ((WoWFishingBobber) bobber.SubObj).IsBobbing : false;
			}
		}

		public void UseBobber() {
			bobber = Bobber;

			if (null != bobber) {
				((WoWFishingBobber) bobber.SubObj).Use();
			}
		}

		public WoWGameObject NearestPool {
			get {
				return pool = ObjectManager.GetObjectsOfType<WoWGameObject>()
					.Where(o =>
						o.IsValid && o.SubType == WoWGameObjectType.FishingHole &&
						o.Distance2DSqr <= PoolDistanceCheckSqr)
					.OrderBy(o => o.Distance2DSqr)
					.FirstOrDefault();
			}
		}

		public void CastLine() {
			TreeRoot.StatusText = "Casting line.";

			// I use cast with an id because I'm not sure that passing the name works
			// with clients other than English but I try to cast by name first so that
			// it should continue working (for English clients at least) in the event
			// new fishing spell IDs are added (i.e. in MoP)
			if (SpellManager.HasSpell("Fishing")) {
				SpellManager.Cast("Fishing");
			} else {
				SpellManager.Cast(fishingSpellId);
			}
		}

		private Composite root = null;

		public override Composite Root {
			get {
				return root ?? (root = new PrioritySelector(
					new Decorator(ret => Me.IsMoving || Me.IsSwimming || Me.Mounted, new Action(c => {
						state = State.WaitingForFirstCast;
						TreeRoot.StatusText = "Waiting for player to get in position.";
					})),
					new Decorator(ret => !IsPoleEquipped, new Action(c => {
						state = State.WaitingForFirstCast;
						TreeRoot.StatusText = "Waiting for player to equip fishing pole.";
					})),
					new Decorator(ret => State.Running == state && null != pool && null == NearestPool, new Action(c => {
						//System.Media.SystemSounds.Exclamation.Play();
						using (new FrameLock()) {
							Lua.DoString(@"PlaySoundFile('Sound\\Creature\\HoodWolf\\HoodWolfTransformPlayer01.wav', 'Master')");
							Lua.DoString("DEFAULT_CHAT_FRAME:AddMessage('**** Pool Empty! Move to next! ****')");
						}
					})),
					new Decorator(ret => null == NearestPool, new Action(c => {
						state = State.WaitingForFirstCast;
						TreeRoot.StatusText = "Waiting for pool.";
					})),
					new Switch<State>(ret => state,
						new ActionAlwaysFail(),
						new SwitchArgument<State>(State.Running, new PrioritySelector(
							new Decorator(ret => Me.IsCasting, new PrioritySelector(
								new Decorator(ret => null == Bobber, new Action(c => {
									Logging.WriteDebug("Casting line because bobber was null");
									CastLine();
									Thread.Sleep(250); // sue me
								})),
								new Decorator(ret => IsBobbing, new Action(c => {
									TreeRoot.StatusText = "Looting bobber";
									Logging.WriteDebug("Using bobber because it bobbed");
									UseBobber();
									StyxWoW.SleepForLagDuration(); // sue me
								})),
								new Decorator(ret => bobber.Location.Distance2DSqr(pool.Location) > BobberDistanceCheckSqr, new Action(c => {
									Logging.WriteDebug("Casting line because it wasn't close enough to the pool");
									CastLine();
									Thread.Sleep(250); // sue me
								})),
								new Action(c => {
									TreeRoot.StatusText = "Waiting for bobber to bob.";
								})
							)),
							new Action(c => {
								Logging.WriteDebug("Casting line because we weren't casting already");
								CastLine();
								Thread.Sleep(250); // sue me
							})
						)),
						new SwitchArgument<State>(State.WaitingForFirstCast, new PrioritySelector(
							new Decorator(ret => null == Bobber, new Action(c => {
								TreeRoot.StatusText = "Waiting for player's initial cast.";
							})),
							new Action(c => {
								state = State.Running;
								TreeRoot.StatusText = "Taking over fishing.";
							})
						))
					),
					new Action(c => {
						TreeRoot.StatusText = "ASSERT: Shouldn't get here.";
					})
				));
			}
		}

		public static LocalPlayer Me {
			get {
				return StyxWoW.Me;
			}
		}

		public static bool IsPoleEquipped {
			get {
				WoWItem mh = Me.Inventory.Equipped.MainHand;
				return null != mh && mh.ItemInfo.WeaponClass == WoWItemWeaponClass.FishingPole;
			}
		}
	}
}
