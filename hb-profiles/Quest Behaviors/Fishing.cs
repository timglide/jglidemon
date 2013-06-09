using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Threading;
using System.Windows.Media;
using Styx;
using Styx.Common;
using Styx.Common.Helpers;
using Styx.CommonBot;
using Styx.CommonBot.Profiles;
using Styx.Helpers;
using Styx.Pathing;
using Styx.TreeSharp;
using Styx.WoWInternals;
using Styx.WoWInternals.WoWObjects;
using Action = Styx.TreeSharp.Action;
using CommonBehaviors.Actions;

namespace QuestBehavoirs {
	/// <summary>
	/// Custom quest behavior for basic fishing for DMF profession quest. Parts inspired
	/// from AutoAngler.
	/// By timglide
	/// Modified by Nuok :o, Lbniese
	/// 
	/// Time limit, facing cardinal direction, fishing until item is looted added by timglide
	///	as well as cleaned up code and fixed isDone bug.
	///	
	/// Credit to Nesox for dismount code
	///
	/// You must set either a facing direction or a PoolId
	/// ItemId (Optional): Will use the item if it in your inventory, useful for one of the quests where you fish an item and have to open it
	/// FishUntilItemCount (Optional): If true, fishing will continue until this many of ItemId are looted
	/// QuestId (Optional): Will stop if you have completed said quest
	/// Facing Direction: This must be postive and can be found by calling Me.RotationDegrees, if its negative then the bot has bugged; 0-360 is fine
	/// PoolFish: If you want to pool fish enable
	/// PoolId: The Id of the pool you want to fish
	/// FishingSkill: The skill level required to fish, only needed for open water, will apply a lure if needed
	/// FlyToFish (Optional): Set to true to and the behavior will fly to the pool
	/// Location: Location where you want it to bot
	///
	/// Example:
	/// Normal Fishing
	/// <CustomBehavior File="Fishing" QuestId="26420" ItemId="100" FacingDegree="260" X="-8190.623" Y="732.9164" Z="68.12978" />
	/// Higher Level Fishing, will use a rod and lure to boost your skill if it's below the set level.
	/// <CustomBehavior File="Fishing" QuestId="26420" FishingSkill="300" ItemId="100" FacingDegree="260" X="-8190.623" Y="732.9164" Z="68.12978" />
	/// Don't Fly to the fish
	/// <CustomBehavior File="Fishing" QuestId="26420" FlyToFish="False" FacingDegree="260" X="-8190.623" Y="732.9164" Z="68.12978" />
	/// Pool Fish
	/// <CustomBehavior File="Fishing" QuestId="26420" PoolFish="true" PoolId="100" X="-8190.623" Y="732.9164" Z="68.12978" />
	/// </summary>
	[CustomBehaviorFileName(@"Fishing")]
	internal class Fishing : CustomForcedBehavior {
		#region Base
		public Fishing(Dictionary<string, string> args)
			: base(args) {
			try {
				ItemId = GetAttributeAsNullable<int>("ItemId", false, ConstrainAs.ItemId, null) ?? 0;
				FishUntilItemCount = GetAttributeAsNullable<int>("FishUntilItemCount", false, ConstrainAs.ItemId, null) ?? 0;

				FacingDirection = GetAttributeAsNullable<float>("FacingDegree", false, null, new string[] { "Degrees", "FacingDegrees" }) ?? 0;


				// N is 0, W is 90
				// Will convert cardinal direction to degrees if a string is provided
				IConstraintChecker<string> facingConstraints = new ConstrainTo.SpecificValues<string>(new string[]{
					"N", "NNW", "NW", "WNW", "W", "WSW", "SW", "SSW", "S", "SSE", "SE", "ESE", "E", "ENE", "NE", "NNE"
				});
				string facingStr = GetAttributeAs<string>("Facing", false, facingConstraints, new string[] { "Face" });

				if (null != facingStr) {
					switch (facingStr) {
						case "N": FacingDirection = 0; break;
						case "NNW": FacingDirection = 22.5f; break;
						case "NW": FacingDirection = 45; break;
						case "WNW": FacingDirection = 67.5f; break;
						case "W": FacingDirection = 90; break;
						case "WSW": FacingDirection = 112.5f; break;
						case "SW": FacingDirection = 135; break;
						case "SSW": FacingDirection = 157.5f; break;
						case "S": FacingDirection = 180; break;
						case "SSE": FacingDirection = 202.5f; break;
						case "SE": FacingDirection = 225; break;
						case "ESE": FacingDirection = 247.5f; break;
						case "E": FacingDirection = 270; break;
						case "ENE": FacingDirection = 292.5f; break;
						case "NE": FacingDirection = 315; break;
						case "NNE": FacingDirection = 337.5f; break;
					}
				}

				Location = GetAttributeAsNullable("", true, ConstrainAs.WoWPointNonEmpty, null) ?? Me.Location;
				PoolFish = GetAttributeAsNullable<bool>("PoolFish", false, null, null) ?? false;
				PoolId = GetAttributeAsNullable("PoolId", false, ConstrainAs.ObjectId, null) ?? 0;
				FlyToFish = GetAttributeAsNullable<bool>("FlyToPool", false, null, null) ?? true;
				FishingSkillLevel = GetAttributeAsNullable<int>("FishingSkill", false, null, null) ?? 0;

				double? fishingTimeInSeconds = GetAttributeAsNullable<double>("MaxTime", false, null, new string[] { "Time", "TimeInSeconds" });

				if (null != fishingTimeInSeconds && fishingTimeInSeconds > 0.0) {
					MaxFishingTime = TimeSpan.FromSeconds(fishingTimeInSeconds.Value);
				} else {
					MaxFishingTime = TimeSpan.Zero;
				}

				QuestId = GetAttributeAsNullable("QuestId", false, ConstrainAs.QuestId(this), null) ?? 0;
				QuestRequirementComplete =
					GetAttributeAsNullable<QuestCompleteRequirement>("QuestCompleteRequirement", false, null, null) ??
					QuestCompleteRequirement.NotComplete;
				QuestRequirementInLog =
					GetAttributeAsNullable<QuestInLogRequirement>("QuestInLogRequirement", false, null, null) ??
					QuestInLogRequirement.InLog;
			} catch (Exception except) {
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

		public int QuestId { get; private set; }
		public QuestCompleteRequirement QuestRequirementComplete { get; private set; }
		public QuestInLogRequirement QuestRequirementInLog { get; private set; }
		public WoWPoint Location { get; private set; }
		public float FacingDirection { get; private set; }
		public int ItemId { get; private set; }
		public int FishUntilItemCount { get; private set; }
		public bool PoolFish { get; private set; }
		public int PoolId { get; private set; }
		public bool FlyToFish { get; private set; }
		public static int FishingSkillLevel { get; set; }
		private WoWItem _mainHand;
		private WoWItem _offHand;
		private const float FacingLeewayDegrees = 4f;
		private const float MaxPoolDistance = 18f;
		public TimeSpan MaxFishingTime { get; private set; }
		public DateTime StartTime { get; private set; }


		~Fishing() {
			Dispose(false);
		}

		public override string SubversionId {
			get {
				return "$Id$";
			}
		}

		public override string SubversionRevision {
			get {
				return "$Revision$";
			}
		}

		public static void Log(string format, params object[] args) {
			Logging.Write(Colors.LightBlue, string.Format("Fishing: ") + format, args);
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

				EquipOriginalWeapons();

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

		#endregion

		/// <summary>
		/// Just ensures the angle isnt messed up
		/// </summary>
		public static float AngleNormalized {
			get {
				if (Me.RotationDegrees > 0)
					return Me.RotationDegrees;

				return 360 - Math.Abs(Me.RotationDegrees);
			}
		}

		#region Fishing

		public WoWItem Item {
			get { return StyxWoW.Me.CarriedItems.FirstOrDefault(ret => ret.Entry == ItemId); }
		}

		public uint ItemCount {
			get {
				uint count = StyxWoW.Me.CarriedItems
					.Where(ret => ret.Entry == ItemId)
					.Aggregate(0U, (sum, value) => sum += value.StackCount);
				return count;
			}
		}

		public WoWGameObject FishingPool {
			get {
				return
					ObjectManager.GetObjectsOfType<WoWGameObject>().Where(o =>
							o.IsValid && o.SubType == WoWGameObjectType.FishingHole && o.Entry == PoolId &&
							o.Distance2DSqr < MaxPoolDistance * MaxPoolDistance)
						.OrderBy(o => o.DistanceSqr).FirstOrDefault();
			}
		}

		public static WoWGameObject Bobber {
			get {
				return ObjectManager.GetObjectsOfType<WoWGameObject>().FirstOrDefault(o =>
					o.IsValid && o.SubType == WoWGameObjectType.FishingNode &&
					o.CreatedByGuid == Me.Guid);
			}
		}


		public static bool IsBobbing {
			get { return Bobber != null ? Bobber.AnimationState == 1 : false; }
		}

		public static bool IsPoleEquipped {
			get {
				WoWItem mainHand = Me.Inventory.Equipped.MainHand;
				return null != mainHand && WoWItemWeaponClass.FishingPole == mainHand.ItemInfo.WeaponClass;
			}
		}

		public static WoWItem FishingPole {
			get {
				return
					Me.BagItems.Where(i =>
						i.ItemInfo.WeaponClass == WoWItemWeaponClass.FishingPole)
					.OrderBy(i => i.ItemInfo.Level)
					.FirstOrDefault();
			}
		}

		public static bool EquipPole() {
			if (null != FishingPole) {
				Log("Equipping {0}.", FishingPole.Name);
				Lua.DoString("EquipItemByName({0})", FishingPole.Entry);
				return true;
			}

			return false;
		}

		public static int MyFishingSkillLevel {
			get { return Me.GetSkill(SkillLine.Fishing).CurrentValue; }
		}

		public static bool NeedsLureToFish {
			get { return MyFishingSkillLevel < FishingSkillLevel; }
		}

		#region Composites

		public static Composite CreateUseBobber() {
			return
				new Decorator(ret => Bobber != null && IsBobbing,
				new Action(ret => {
					Log("Fishing Bobber Bobed");
					Bobber.SubObj.Use();
					CastDelay.Reset();
				}));
		}

		public Composite EquipFishingGear() {
			return
				new Decorator(ret => !IsPoleEquipped && NeedsLureToFish,
					new Action(ret => {
						if (!EquipPole()) {
							LogMessage("error", "Couldn't equip fishing pole, skipping behavior");
							_isDone = true;
						}
					})
				);
		}

		private static readonly WaitTimer CastDelay = new WaitTimer(TimeSpan.FromSeconds(2));

		public static Composite CastLine() {
			return new PrioritySelector(
				new Decorator(ret => CastDelay.IsFinished, new Sequence(
					new Action(ret => {
						Log("Casting Fishing Line");
						TreeRoot.StatusText = "Casting line.";
						SpellManager.Cast("Fishing");
						CastDelay.Reset();
					})
				))
			);
		}

		#endregion

		#region Loot Override

		public static int NumLootItems {
			get {
				return Lua.GetReturnVal<int>("return GetNumLootItems()", 0);
			}
		}

		public static void Loot() {
			if (NumLootItems != 0) {
				for (int i = 1; i <= NumLootItems; i++) {
					List<string> lootInfo = Lua.GetReturnValues(String.Format("return GetLootSlotInfo({0})", i));

					if (lootInfo != null && !string.IsNullOrEmpty(lootInfo[1])) {
						Log("Looting {0}", lootInfo[1]);
						Lua.DoString("LootSlot(" + i + ")");
						Lua.DoString("ConfirmLootSlot(" + i + ")");
					}
				}
			}
		}

		#endregion

		public void EquipOriginalWeapons() {
			SpellManager.StopCasting();
			StyxWoW.SleepForLagDuration();

			if (null != _mainHand && _mainHand != Me.Inventory.Equipped.MainHand) {
				Log("Equipping original main hand: {0}.", _mainHand.Name);
				Lua.DoString("EquipItemByName({0})", _mainHand.Entry);
			}

			if (null != _offHand && _offHand != Me.Inventory.Equipped.OffHand) {
				Log("Equipping original off hand: {0}.", _offHand.Name);
				Lua.DoString("EquipItemByName({0})", _offHand.Entry);
			}

			if (null != _mainHand || null != _offHand) {
				Thread.Sleep(500);
			}
		}

		#region Lures

		private static readonly HashSet<uint> LureIds = new HashSet<uint> {
			68049,62673,34861,46006,6533,7307,6532,6530,6811,6529,67404
		};

		public static bool HasLureApplied {
			get {
				var fishingPole = StyxWoW.Me.Inventory.Equipped.GetEquippedItem(WoWInventorySlot.MainHand);
				return fishingPole.TemporaryEnchantment.IsValid;
			}
		}

		public static WoWItem FishingLure {
			get {
				return
					ObjectManager.GetObjectsOfType<WoWItem>().Where(i =>
							i.ItemInfo.RequiredSkillLevel <= StyxWoW.Me.GetSkill(SkillLine.Fishing).CurrentValue &&
							LureIds.Contains(i.Entry))
						.OrderBy(i => i.ItemInfo.RequiredSkillLevel)
						.FirstOrDefault();
			}
		}

		private static readonly WaitTimer LureTimer = new WaitTimer(TimeSpan.FromSeconds(10));

		public static Composite ApplyLure() {
			return new PrioritySelector(
				new Decorator(ret =>
						IsPoleEquipped && !HasLureApplied && NeedsLureToFish &&
						FishingLure != null && LureTimer.IsFinished,
					new Sequence(
						new Action(ctx => Log("Applying Lure {0}", FishingLure.Name)),
						new Action(ctx => TreeRoot.StatusText = "Applying Lure " + FishingLure.Name),
						new Action(ctx => FishingLure.Use()),
						new Action(ctx => LureTimer.Reset()),
						new ActionAlwaysSucceed()
					)
				)
			);
		}

		#endregion

		#endregion

		#region Overrides of CustomForcedBehavior

		protected override Composite CreateBehavior() {
			return _root ?? (_root = new PrioritySelector(
				new Decorator(ret => IsDone, new Action(c => {
					TreeRoot.StatusText = "Fishing complete!";
				})),
				new Decorator(ret => NumLootItems != 0, new Action(c => Loot())),
				new Decorator(ret => Item != null, new PrioritySelector(
					// This is for quests where you're required to use the fish to complete the quest
					new Decorator(ret => 0 == FishUntilItemCount && Item.Usable, new Action(ret => {
						Log("Using {0}", Item.Name);
						Item.Use();
					})),
					new Decorator(ret => 0 != FishUntilItemCount && ItemCount >= FishUntilItemCount, new Action(ctx => {
						Log("Finished after looting [{0}]x{1}", Item.Name, FishUntilItemCount);
						_isDone = true;
					})),
					new ActionAlwaysSucceed()
				)),
				new Decorator(ret => TimeSpan.Zero != MaxFishingTime && DateTime.Now - StartTime > MaxFishingTime, new Action(ret => {
					Log("Finished after reaching max fishing time");
					_isDone = true;
				})),

				// Make sure we're at the location
				new Decorator(ret => Location.Distance(Me.Location) <= 2, new PrioritySelector(
					EquipFishingGear(),
					// If we're pool fishing and the pool isn't there then we're done
					new Decorator(ret => PoolFish && FishingPool == null, new Action(ctx => {
						LogMessage("info", "No pool was found with Id: {0} at {1}", PoolId, Location);
						_isDone = true;
					})),
					new Decorator(ret => Flightor.MountHelper.Mounted, new Action(ctx => Flightor.MountHelper.Dismount())),
					// Make sure we're not moving before trying to cast
					new Decorator(ret => Me.IsMoving, new Action(ctx => WoWMovement.MoveStop())),
					// Need to face water
					new Decorator(ret => !PoolFish && Math.Abs((FacingDirection - AngleNormalized)) > FacingLeewayDegrees, new Action(ctx => {
						Me.SetFacing(
							WoWMathHelper.DegreesToRadians(FacingDirection));
					})),
					// Face the pool
					new Decorator(ret => PoolFish && !Me.IsFacing(FishingPool.Location), new Sequence(
						new Action(ctx => Log("Facing {0}", FishingPool.Name)),
						new Action(ctx => Me.SetFacing(FishingPool.Location)))),
						ApplyLure(),
						new Decorator(ret => Me.IsCasting, new PrioritySelector(
							CreateUseBobber(),
							new Decorator(ret => null == Bobber, CastLine()),
							// Rescast the line if it's too far from the pool
							new Decorator(ret => PoolFish && (Bobber.Location.Distance2D(FishingPool.Location) > 3.6f), CastLine()),
							new ActionAlwaysSucceed()
						)),
						CastLine()
					)),
				// Lets fly to the pool if we can
				new Decorator(ret => Location.Distance(Me.Location) > 20 && FlyToFish, new Action(ctx => {
					Flightor.MoveTo(Location, 5f);
				})),
				new Decorator(ret => Location.Distance(Me.Location) > 2, new Sequence(
					new Action(ctx => {
						TreeRoot.StatusText = "Moving towards fishing spot";
					}),
					new Action(ctx => {
						Navigator.MoveTo(Location);
					})
				))
			));
		}

		public override void Dispose() {
			Dispose(true);
			GC.SuppressFinalize(this);
		}


		private bool _isDone = false;

		public override bool IsDone {
			get {
				if (_isDone) {
					//LogMessage("debug", "Fishing done via _isDone");
					return true;
				}

				if (!UtilIsProgressRequirementsMet(QuestId, QuestRequirementInLog, QuestRequirementComplete)) {
					//LogMessage("debug", "Fishing done via progress requirements not met");
					return true;
				}

				return false;
			}
		}

		public override void OnStart() {
			_isDone = false;

			// This reports problems, and stops BT processing if there was a problem with attributes...
			// We had to defer this action, as the 'profile line number' is not available during the element's
			// constructor call.
			OnStart_HandleAttributeProblem();

			// If the quest is complete, this behavior is already done...
			// So we don't want to falsely inform the user of things that will be skipped.
			
			if (!SpellManager.HasSpell("Fishing")) {
				LogMessage("error", "You don't have the fishing skill, skipping.");
				_isDone = true;
			}

			if (!IsDone) {
				BotEvents.OnBotStop += BotEvents_OnBotStop;
				TreeRoot.GoalText = "Fishing";
				_mainHand = Me.Inventory.Equipped.MainHand;
				_offHand = Me.Inventory.Equipped.OffHand;

				CastDelay.Reset();
				LureTimer.Reset();

				StartTime = DateTime.Now;

				if (TimeSpan.Zero != MaxFishingTime) {
					Log("Will fish until about {0}", StartTime + MaxFishingTime);
				}

				if (0 != ItemId && 0 != FishUntilItemCount) {
					Log("Will fish until we loot item with ID {0} x{1}", ItemId, FishUntilItemCount);
				}
			}
		}

		#endregion
	}
}