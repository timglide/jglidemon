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
using Styx.CommonBot.Frames;
using Styx.Common;

namespace timglide {
	/// <summary>
	/// Custom quest behavior for basic fishing for DMF profession quest. Parts inspired
	/// from AutoAngler.
	/// By timglide
	/// </summary>
	[CustomBehaviorFileName(@"DMF_Fishing")]
	class DMF_Fishing : CustomForcedBehavior {
		private static readonly int[] QuestIds = { 29513 };
		private const float FacingDirectionDegrees = 180f; // due south
		private const float FacingLeewayDegrees = 5f;
		private static readonly int[] FishingSpellIds = { 131474, 7620, 7731, 7732, 18248, 33095, 51294, 88868 };

		public DMF_Fishing(Dictionary<string, string> args)
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

		private int fishingSpellId;
		private WoWItem mainHand;
		private WoWItem offHand;

		// DON'T EDIT THESE--they are auto-populated by Subversion
		public override string SubversionId { get { return ("$Id$"); } }
		public override string SubversionRevision { get { return ("$Revision$"); } }


		~DMF_Fishing() {
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

		public int GetFishingSpellId() {
			foreach (int i in FishingSpellIds) {
				if (SpellManager.HasSpell(i)) return i;
			}

			return 0;
		}

		public WoWGameObject Bobber {
			get {
				return ObjectManager.GetObjectsOfType<WoWGameObject>()
					.FirstOrDefault(
						o => o.IsValid && o.CreatedByGuid == Me.Guid &&
						o.SubType == WoWGameObjectType.FishingNode);
			}
		}

		public bool IsBobbing {
			get {
				WoWGameObject bobber = Bobber;
				return null != bobber ? 1 == bobber.AnimationState : false;
			}
		}

		public void UseBobber() {
			WoWGameObject bobber = Bobber;

			if (null != bobber) {
				bobber.SubObj.Use();
			}
		}

		public bool IsPoleEquipped {
			get {
				return true;
				WoWItem mainHand = Me.Inventory.Equipped.MainHand;
				return null != mainHand && WoWItemWeaponClass.FishingPole == mainHand.ItemInfo.WeaponClass;
			}
		}

		public bool EquipPole() {
			var pole = Me.BagItems.FirstOrDefault(i => i.ItemInfo.WeaponClass == WoWItemWeaponClass.FishingPole);

			if (null != pole) {
				LogMessage("info", "Equipping {0}.", pole.Name);
				Lua.DoString("EquipItemByName({0})", pole.Entry);
				Thread.Sleep(500);
				return true;
			}

			return false;
		}

		public void CastLine() {
			TreeRoot.StatusText = "Casting line.";
			SpellManager.Cast(fishingSpellId);
		}

		public void EquipOriginalWeapons() {
			//using (new FrameLock()) { // FIXME
				SpellManager.StopCasting();

				if (null != mainHand && mainHand != Me.Inventory.Equipped.MainHand) {
					LogMessage("info", "Equipping original main hand: {0}.", mainHand.Name);
					Lua.DoString("EquipItemByName({0})", mainHand.Entry);
				}

				if (null != offHand && offHand != Me.Inventory.Equipped.OffHand) {
					LogMessage("info", "Equipping original off hand: {0}.", offHand.Name);
					Lua.DoString("EquipItemByName({0})", offHand.Entry);
				}

				if (null != mainHand || null != offHand) {
					Thread.Sleep(500);
				}
			//}
		}

		#region Overrides of CustomForcedBehavior

		protected override Composite CreateBehavior() {
			return _root ?? (_root = new PrioritySelector(
				new Decorator(ret => IsDone, new Action(c => {
					TreeRoot.StatusText = "DMF Fishing complete!";
				})),
				new Decorator(ret => null != LootFrame.Instance && LootFrame.Instance.IsVisible, new Action(c => {
					LootFrame.Instance.LootAll();
				})),
				/*new Decorator(ret => !IsPoleEquipped true, new Action(c => {
					try {
						Logging.Write("pole: {0} [{1}]", Me.Inventory.Equipped.MainHand, Me.Inventory.Equipped.MainHand.ItemInfo.WeaponClass);
					} catch {}

					bool result = EquipPole();

					if (!result) {
						LogMessage("error", "No fishing pole found, skipping.");
						_isDone = true;
					}
				})),*/
				new Decorator(ret => Math.Abs(FacingDirectionDegrees - Math.Abs(Me.RotationDegrees)) > FacingLeewayDegrees, new Action(c => {
					LogMessage("debug", "Facing water");
					Me.SetFacing(WoWMathHelper.DegreesToRadians(FacingDirectionDegrees));
				})),
				new Decorator(ret => Me.IsCasting, new PrioritySelector(
					new Decorator(ret => null == Bobber, new Action(c => {
						LogMessage("debug", "Casting line because bobber was null");
						CastLine();
						Thread.Sleep(250);
					})),
					new Decorator(ret => IsBobbing, new Action(c => {
						TreeRoot.StatusText = "Looting bobber";
						LogMessage("debug", "Using bobber because it bobbed");
						UseBobber();
						Thread.Sleep(250);
					})),
					new Action(c => {
						TreeRoot.StatusText = "Waiting for bobber to bob.";
					})
				)),
				new Action(c => {
					LogMessage("debug", "Casting line because we weren't casting already");
					CastLine();
					Thread.Sleep(250);
				}),
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
				if (Me.IsCasting) return false;

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
				TreeRoot.GoalText = "DMF Fishing";
				mainHand = Me.Inventory.Equipped.MainHand;
				offHand  = Me.Inventory.Equipped.OffHand;
				fishingSpellId = GetFishingSpellId();

				if (0 == fishingSpellId) {
					LogMessage("error", "You don't have the fishing skill, skipping.");
					_isDone = true;
				}
			}
		}

		#endregion
	}
}
