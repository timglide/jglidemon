using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals.WoWObjects;
using Styx;
using Styx.WoWInternals;
using System.Globalization;
using Styx.MemoryManagement;

namespace GliderRemoteCompat.Commands {
	class Inventory : Command {
		public static readonly Command Instance = new Inventory();

		public override void Execute(Server server, Client client, string args) {
			ObjectManager.Update();
			LocalPlayer me = StyxWoW.Me;

			if (null == me || !me.IsValid)
			{
				client.Send("Error: me is null or invalid");
				return;
			}

			WoWBagSlot? slotArg = null;

			try {
				slotArg = (WoWBagSlot) Enum.Parse(typeof(WoWBagSlot), args, true);
			} catch { }

			List<string> l = new List<string>();

			using (StyxWoW.Memory.AcquireFrame()) {
				WoWPlayerInventory inv = me.Inventory;

				if (null != slotArg) {
					AddContainer(l, me.GetBag(slotArg.Value));
				} else if ("backpack".Equals(args, StringComparison.InvariantCultureIgnoreCase)) {
					AddItems(l, 0, inv.Backpack);
				} else if ("equipped".Equals(args, StringComparison.InvariantCultureIgnoreCase)) {
					AddEquipped(l, inv.Equipped);
				} else if ("bags".Equals(args, StringComparison.InvariantCultureIgnoreCase)) {
					AddItems(l, 0, inv.Backpack);
					AddContainer(l, me.GetBag(WoWBagSlot.Bag1));
					AddContainer(l, me.GetBag(WoWBagSlot.Bag2));
					AddContainer(l, me.GetBag(WoWBagSlot.Bag3));
					AddContainer(l, me.GetBag(WoWBagSlot.Bag4));
				} else if ("".Equals(args)) {
					AddEquipped(l, inv.Equipped);
					AddItems(l, 0, inv.Backpack);
					AddContainer(l, me.GetBag(WoWBagSlot.Bag1));
					AddContainer(l, me.GetBag(WoWBagSlot.Bag2));
					AddContainer(l, me.GetBag(WoWBagSlot.Bag3));
					AddContainer(l, me.GetBag(WoWBagSlot.Bag4));
				} else {
					l.Add("Error: invalid slot");
				}
			}

			client.Send(l);
		}

		private void AddEquipped(List<string> l, WoWPaperDoll e) {
			AddItem(l, "Head", e.Head);
			AddItem(l, "Neck", e.Neck);
			AddItem(l, "Shoulder", e.Shoulder);
			AddItem(l, "Back", e.Back);
			AddItem(l, "Chest", e.Chest);
			AddItem(l, "Shirt", e.Shirt);
			AddItem(l, "Tabard", e.Tabard);
			AddItem(l, "Wrist", e.Wrist);
			
			AddItem(l, "Hands", e.Hands);
			AddItem(l, "Waist", e.Waist);
			AddItem(l, "Legs", e.Legs);
			AddItem(l, "Feet", e.Feet);
			AddItem(l, "Finger1", e.Finger1);
			AddItem(l, "Finger2", e.Finger2);
			AddItem(l, "Trinket1", e.Trinket1);
			AddItem(l, "Trinket2", e.Trinket2);

			AddItem(l, "MainHand", e.MainHand);
			AddItem(l, "OffHand", e.OffHand);
		}

		private void AddContainer(List<string> l, WoWContainer c) {
			if (null == c || !c.IsValid) {
				l.Add("! container was null or invalid");
				return;
			}
			int bagIndex = c.BagIndex + 1;
			AddItem(l, bagIndex + ": " + c.Slots, c);
			AddItems(l, bagIndex, c);
		}

		private void AddItems(List<string> l, int bagIndex, WoWBag b) {
			if (null == b) {
				l.Add("! bag " + bagIndex + " was null");
				return;
			}

			WoWItem[] items = b.Items;

			if (null == items) {
				l.Add("! items was null for bag " + bagIndex);
				return;
			}

			for (int i = 0; i < items.Length; i++) {
				AddItem(l, bagIndex + "|" + i, items[i]);
			}
		}

		private void AddItem(List<string> l, string slot, WoWItem i) {
			if (null == i || !i.IsValid)
				return;
			l.Add(slot + ": " + i.ItemLink + (i.StackCount > 1 ? "x" + i.StackCount : ""));
		}
	}
}
