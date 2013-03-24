using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.Plugins;
using Styx.WoWInternals.WoWObjects;
using Styx;
using Styx.Common;
using System.Diagnostics;
using System.IO;
using System.Xml.Serialization;
using Styx.TreeSharp;
using Styx.WoWInternals;
using Styx.CommonBot;

using Action = Styx.TreeSharp.Action;
using CommonBehaviors.Actions;
using Styx.CommonBot.POI;
using System.Threading;
using System.ComponentModel;

namespace WaitForDebuff {
	public class Constants {
		public const string Name = "WaitForDebuff";
	}

	public class Color {
		public static readonly System.Windows.Media.Color Red = System.Windows.Media.Color.FromRgb(255, 0, 0);
	}

	public class WaitForDebuff : HBPlugin {
		private const string _revision = "$Revision$";
		private static readonly int revision;
		private static readonly Version version;

		static WaitForDebuff() {
			string str = string.Empty;

			try {
				str = _revision.Substring(11, _revision.Length - 2 - 11);
				revision = int.Parse(str);
			} catch {
				revision = 0;
			}

			version = new Version(0, 1, 0, revision);
		}

		public static LocalPlayer Me {
			get { return StyxWoW.Me; }
		}

		public override string Author {
			get { return "timglide"; }
		}

		public override string Name {
			get { return Constants.Name; }
		}

		public override Version Version {
			get { return version; }
		}

		public override bool WantButton {
			get { return true; }
		}

		public override string ButtonText {
			get {
				return "Settings";
			}
		}

		public override void OnButtonPress() {
			//if (!initialized) return;
			SettingsForm.ShowDialog();
		}

		private bool initialized = false;

		public override void Initialize() {
			if (initialized) {
				return;
			}

			initialized = true;

			behavior = CreateBehavior();
			combatBehavior = CreateCombatBehavior();
			Logging.Write("{0} v{1} initialized.", Name, Version);

			TreeHooks.Instance.AddHook("Routine_Rest", behavior);
			TreeHooks.Instance.InsertHook("Combat_OOC", 0, behavior);
			TreeHooks.Instance.InsertHook("Combat_PullBuff", 0, behavior);
			TreeHooks.Instance.InsertHook("Combat_Pull", 0, behavior);
			TreeHooks.Instance.InsertHook("Combat_Main", 0, combatBehavior);
		}

		public override void Dispose() {
			if (!initialized)
				return;

			TreeHooks.Instance.RemoveHook("Combat_Main", combatBehavior);
			TreeHooks.Instance.RemoveHook("Combat_Pull", behavior);
			TreeHooks.Instance.RemoveHook("Combat_PullBuff", behavior);
			TreeHooks.Instance.RemoveHook("Combat_OOC", behavior);
			TreeHooks.Instance.RemoveHook("Routine_Rest", behavior);

			initialized = false;
			Logging.Write("{0} v{1} unloaded.", Name, Version);
		}


		private SettingsForm settingsForm;

		private SettingsForm SettingsForm {
			get {
				if (null == settingsForm) {
					settingsForm = new SettingsForm();
				}

				return settingsForm;
			}
		}


		private Composite behavior;
		private string lastStatus = null;
		
		private Composite CreateBehavior() {
			return new PrioritySelector(
				new Decorator(ret => ShouldNotWait(), new Action(ctx => {
					//Logging.Write("Should not wait was true.");
					return RunStatus.Failure;
				})),
				new Action(ctx => {
					WoWAura aura = HasDebuff();

					if (null == aura) {
						if (null != lastStatus) {
							TreeRoot.StatusText = lastStatus;
							lastStatus = null;
						}

						//Logging.Write("[" + Constants.Name + "]: All debuffs gone.");
						return RunStatus.Failure;
					}

					if (null == lastStatus) {
						lastStatus = TreeRoot.StatusText;
					}

					string msg = string.Format(
						"Waiting for [{0}] to go away.", ((WoWAura)aura).Name);
					TreeRoot.StatusText = msg;
					//BotPoi.Clear(msg);

					if (Me.IsMoving) {
						WoWMovement.MoveStop();
						WoWMovement.ClickToMove(Me.Location);
					}

					return RunStatus.Running;
				})
			);
		}


		private Composite combatBehavior;

		private Composite CreateCombatBehavior() {
			return new PrioritySelector(
				new Decorator(ret => Me.IsActuallyInCombat && Me.CurrentTarget.CastingSpellId == 138769, new Action(ctx => {
					Logging.Write("Avoiding Trihorn Charge");
					AvoidEnemyCast(Me.CurrentTarget, 0, 20);
					return RunStatus.Failure;
				})),
				new Decorator(ret => Me.IsActuallyInCombat && Me.CurrentTarget.CastingSpellId == 138772, new Action(ctx => {
					Logging.Write("Avoiding Double Swipe");
					AvoidEnemyCast(Me.CurrentTarget, 90, 20);
					return RunStatus.Failure;
				})),
				new ActionAlwaysFail()
			);
		}

		public WoWAura HasDebuff() {
			return Me.Auras.Values.FirstOrDefault(
				aura => Settings.Instance.DebuffIDs.Contains(aura.SpellId) ||
						Settings.Instance.DebuffNames.Contains(aura.Name, StringComparer.CurrentCultureIgnoreCase)
			);
		}

		public bool ShouldNotWait() {
			return Me.IsActuallyInCombat || Me.IsDead || Me.IsFalling;
		}

		public override void Pulse() {

		}

		/// <summary>
		/// this behavior will move the bot StrafeRight/StrafeLeft only if enemy is casting and we needed to move!
		/// Credits to BarryDurex.
		/// </summary>
		/// <param name="EnemyAttackRadius">EnemyAttackRadius or 0 for move Behind</param>
		public static void AvoidEnemyCast(WoWUnit Unit, float EnemyAttackRadius, float SaveDistance) {
			if (!StyxWoW.Me.IsFacing(Unit)) { Unit.Face(); Thread.Sleep(300); }

			float BehemothRotation = getPositive(Unit.RotationDegrees);
			float invertEnemyRotation = getInvert(BehemothRotation);

			WoWMovement.MovementDirection move = WoWMovement.MovementDirection.None;

			if (getPositive(StyxWoW.Me.RotationDegrees) > invertEnemyRotation) { move = WoWMovement.MovementDirection.StrafeRight; } else { move = WoWMovement.MovementDirection.StrafeLeft; }

			while (Unit.Distance2D <= SaveDistance && Unit.IsCasting && ((EnemyAttackRadius == 0 && !StyxWoW.Me.IsSafelyBehind(Unit)) ||
				(EnemyAttackRadius != 0 && Unit.IsSafelyFacing(StyxWoW.Me, EnemyAttackRadius)) || Unit.Distance2D <= 2)) {
				WoWMovement.Move(move);
				Unit.Face();
			}
			WoWMovement.MoveStop();
		}

		private static float getInvert(float f) {
			if (f < 180)
				return (f + 180);
			//else if (f >= 180)
			return (f - 180);
		}

		private static float getPositive(float f) {
			if (f < 0)
				return (f + 360);
			return f;
		}
	}
}
