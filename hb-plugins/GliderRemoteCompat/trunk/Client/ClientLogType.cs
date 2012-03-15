using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat {
	public class ClientLogType {
		public static readonly ClientLogType
			GliderLog = new ClientLogType("GliderLog"),
			Status = new ClientLogType("Status"),
			ChatRaw = new ClientLogType("ChatRaw"),
			Combat = new ClientLogType("Combat"),
			Chat = new ClientLogType("Chat");

		public static IEnumerable<ClientLogType> Values {
			get {
				yield return GliderLog;
				yield return Status;
				yield return ChatRaw;
				yield return Combat;
				yield return Chat;
			}
		}

		public static ClientLogType ValueOf(string name) {
			name = name.ToLowerInvariant();
			foreach (ClientLogType c in Values) {
				if (c.Name.ToLowerInvariant() == name) {
					return c;
				}
			}

			throw new ArgumentException("invalid name");
		}

		private ClientLogType(string name) {
			this.Name = name;
		}

		public string Name {
			get;
			set;
		}

		public override int GetHashCode() {
			return Name.GetHashCode();
		}
	}
}
