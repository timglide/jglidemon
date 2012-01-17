using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net.Sockets;
using System.Threading;
using System.Net;

namespace GliderRemoteCompat {
	class Server : IDisposable {
		private TcpListener tcpListener;
		private Thread listenThread;
		private volatile bool running = false;

		public bool Running {
			get { return running; }
			set { running = value; }
		}

		private List<Client> clients = new List<Client>();

		public Server() {
			tcpListener = new TcpListener(IPAddress.Any, 3200);
			listenThread = new Thread(Listen);
			listenThread.Name = "GRC Server";
			running = true;
			listenThread.Start();
		}

		~Server() {
			Dispose(false);
		}

		public void Dispose() {
			Dispose(true);
			GC.SuppressFinalize(this);
		}

		private bool disposed = false;
		public void Dispose(bool explicitlyInitiated) {
			if (disposed) return;

			if (explicitlyInitiated) {
				// clean up managed resources
			}

			running = false;
			listenThread.Interrupt();
			tcpListener.Stop();

			for (int i = clients.Count - 1; i >= 0; i--) {
				clients[i].Dispose();
			}

			disposed = true;
		}

		public int ClientCount { get { return clients.Count; } }

		internal void RemoveClient(Client client) {
			clients.Remove(client);
		}

		private void Listen() {
			tcpListener.Start();

			while (running) {
				try {
					//blocks until a client has connected
					clients.Add(new Client(this, tcpListener.AcceptTcpClient()));
				} catch { }
			}
		}
	}
}
