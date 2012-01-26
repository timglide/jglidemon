using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace GliderRemoteCompat {
	abstract class Command : IDisposable {
		public abstract void Execute(Server server, Client client, string args);

		/// <summary>
		/// If false, the subclass should provide a singleton instance of the class
		/// that is thread-safe, if true then new instances should be created and
		/// <see cref="Dispose()" /> called appropriately.
		/// </summary>
		public virtual bool ShouldDispose {
			get { return false; }
		}

		public virtual void Dispose() {
			
		}
	}
}
