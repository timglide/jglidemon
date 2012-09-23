using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx;
using System.Threading;
using Styx.WoWInternals.WoWObjects;
using Styx.WoWInternals;
using Styx.Helpers;
using Action = Styx.TreeSharp.Action;
using Styx.WoWInternals.Misc;
using Styx.CommonBot;
using Styx.Common;
using Styx.TreeSharp;
using Styx.CommonBot.Frames;
using Styx.Pathing;

namespace LoveTokenBuyBot {
	/// <summary>
	/// Turns Lovely Charms into Lovely Charm Bracelets and optionally
	/// buys Love Tokens with the bracelets.
	/// 
	/// by: timglide
	/// </summary>
	class LoveTokenBuyBot : BotBase {
		/// <summary>
		/// If true, will buy Love Tokens from a nearby Lovely Merchant.
		/// You must start the bot near a Lovely Merchant (it can be in any city).
		/// If false, will only convert charms into bracelets.
		/// </summary>
		public static bool BuyLoveTokens     = true;

		/// <summary>
		/// The maximum number of Lovely Bracelets to create at one time. 0 for no limit.
		/// Will stop if your bags are full regardless. Useful if not buying Love Tokens
		/// or if you want it to create x bracelets then buy x tokens for some reason
		/// instead of making all possible bracelets first.
		/// </summary>
		public static uint MaxBraceletCount  = 0;

		public const uint LovelyCharmId      = 49655;
		public const uint LovelyCharmsNeeded = 10;
		public const uint LovelyBraceletId   = 49916;
		public const uint LoveTokenId        = 49927;
		public const uint LoveTokenBuyIndex  = 0;
		public const uint LovelyMerchantId   = 37674;
		public const double MerchantInteractDistance = 10.0;

		public override string Name {
			get { return "LoveTokenBuyBot"; }
		}

		public override PulseFlags PulseFlags {
			get { return PulseFlags.All; }
		}

		private static Action CreateBraceletsAction = new Action(c => {
			uint charmCount, createCount, createdCount, braceletCount, latency;
			charmCount = CharmCount;

			createCount = charmCount / LovelyCharmsNeeded;
			createCount = Math.Min(createCount, Me.FreeNormalBagSlots);

			if (0 != MaxBraceletCount) {
				createCount = Math.Min(createCount, MaxBraceletCount);
			}

			latency = StyxWoW.WoWClient.Latency;
			createdCount = 0;
			braceletCount = BraceletCount;

			Logging.Write("Creating {0} bracelets.", createCount);

			while (!Me.NormalBagsFull && createdCount < createCount) {
				Lua.DoString("UseItemByName(" + LovelyCharmId + ")");

				while (Me.IsCasting) {
					try { Thread.Sleep(50); } catch (ThreadInterruptedException) { }
				}

				try { Thread.Sleep((int)latency); } catch (ThreadInterruptedException) { }

				createdCount = BraceletCount - braceletCount;
			}

			Logging.WriteDiagnostic("Wanted {0} bracelets, actually made {1}.", createCount, createdCount);

			return RunStatus.Success;
		});

		private static Decorator CreateBraceletsDecorator =
			new Decorator(c => 0 == MaxBraceletCount || BraceletCount < MaxBraceletCount, CreateBraceletsAction);

		private static Action BuyLoveTokensAction = new Action(c => {
			if (!MerchantFrame.Instance.IsVisible) {
				merchant.Interact();
			}

			uint braceletCount = BraceletCount;
			uint tokenCount = TokenCount;

			Logging.Write("Buying {0} tokens.", braceletCount);
			
			MerchantFrame.Instance.BuyItem((int)LoveTokenBuyIndex, (int)braceletCount);

			while (tokenCount == TokenCount) {
				Thread.Sleep(50);
			}

			return RunStatus.Success;
		});

		private static Decorator BuyLoveTokensDecorator =
			new Decorator(c => BuyLoveTokens && BraceletCount > 0, BuyLoveTokensAction);

		private Composite root = null;

		public override Composite Root {
			get {
				return root ?? (root = new PrioritySelector(
					// first find the lovely merchant if we want to buy tokens
					new Decorator(c => null == merchant && BuyLoveTokens,
						new Action(c => {
							if (!FindMerchant()) {
								Logging.Write("No Lovely Merchant found, stopping.");
								TreeRoot.Stop();
								return RunStatus.Failure;
							}

							return RunStatus.Success;
						})
					),
					// if bags are full
					new Decorator(c => Me.NormalBagsFull,
						new PrioritySelector(
							// if we're buying tokens and have bracelets, buy the tokens
							BuyLoveTokensDecorator,
							// else nothing to do
							new Action(c => {
								Logging.Write("Bags are full, stopping.");
								TreeRoot.Stop(); // stop the bot
								return RunStatus.Success;
							})
						)
					),
					// if we're out of charms
					new Decorator(c => CharmCount < LovelyCharmsNeeded,
						new PrioritySelector(
							// if we're buying tokens and have bracelets, buy the tokens
							BuyLoveTokensDecorator,
							// else nothing to do
							new Action(c => {
								Logging.Write("Can't make more bracelets or buy tokens, stopping.");
								TreeRoot.Stop();
								return RunStatus.Success;
							})
						)
					),
					// else if we have charms and haven't reached the max bracelets count, create bracelets
					CreateBraceletsDecorator,
					// else we've reached the max bracelets count
					new PrioritySelector(
						// if we're buying tokens and have bracelets, buy the tokens
						BuyLoveTokensDecorator,
						// else nothing to do
						new Action(c => {
							Logging.Write("Max bracelets count reached, stopping.");
							TreeRoot.Stop();
							return RunStatus.Success;
						})
					)
				));
			}
		}

		private static LocalPlayer me = null;

		private static LocalPlayer Me {
			get {
				return me ?? (me = StyxWoW.Me);
			}
		}

		private static uint GetItemCount(uint id) {
			return
				Lua.GetReturnVal<uint>("return GetItemCount(\"" + id + "\")", 0);
		}

		private static uint CharmCount {
			get {
				return GetItemCount(LovelyCharmId);
			}
		}

		private static uint BraceletCount {
			get {
				return GetItemCount(LovelyBraceletId);
			}
		}

		private static uint TokenCount {
			get {
				return GetItemCount(LoveTokenId);
			}
		}

		private static WoWUnit merchant = null;

		private bool FindMerchant() {
			ObjectManager.Update();
			WoWUnit merchant = ObjectManager.GetObjectsOfType<WoWUnit>()
				.FirstOrDefault(u => u.Entry == LovelyMerchantId);

			if (null == merchant) {
				BuyLoveTokens = false;
				return true;
//				return false;
			}

			LoveTokenBuyBot.merchant = merchant;

			if (merchant.Distance > MerchantInteractDistance) {
				Navigator.MoveTo(merchant.Location);
			}

			merchant.Interact();

			BuyLoveTokens = true;
			return true;
		}
	}
}
