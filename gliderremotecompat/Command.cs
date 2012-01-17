using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat {
	abstract class Command : IDisposable {
		public abstract void Execute(Server server, Client client, string args);

		public virtual void Dispose() {

		}
	}
}
