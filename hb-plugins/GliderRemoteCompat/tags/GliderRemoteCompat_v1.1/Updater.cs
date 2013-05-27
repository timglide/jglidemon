using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text.RegularExpressions;
using System.Windows.Media;
using Styx.Common;

namespace GliderRemoteCompat {
    class Updater
    {
		private const string SvnUrl = "http://jglidemon.googlecode.com/svn/hb-plugins/GliderRemoteCompat/trunk/";
		private const string SvnHistUrl = "http://jglidemon.googlecode.com/svn-history/r{0}/hb-plugins/GliderRemoteCompat/trunk/";

        private static readonly Regex _linkPattern = new Regex(@"<li><a href="".+"">(?<ln>.+(?:..))</a></li>",
                                                               RegexOptions.CultureInvariant);

        public static void CheckForUpdate()
        {
            try
            {
				Logging.Write("[GRC] Checking for new version");
				int installedRev = GetInstalledRevision();
                int remoteRev = GetLatestRevision();
				if (installedRev != remoteRev)
                {
					if (SkipUpdate) {
						Logging.Write("[GRC] A new version was found but automatic updates are disabled (current=r{0}, available=r{1})", installedRev, remoteRev);
						return;
					}

					Logging.Write("[GRC] A new version was found, downloading Update");
                    DownloadFilesFromSvn(new WebClient(), string.Format(SvnHistUrl, remoteRev));
					Logging.Write("[GRC] Download complete.");

					Logging.Write(Colors.Red, "[GRC] A new version of GliderRemoteCompat was installed (r{0}), please restart Honorbuddy", remoteRev);
                }
                else
                {
					Logging.Write("[GRC] No updates found (current=r{0})", installedRev);
                }
            }
            catch (Exception ex)
            {
                Logging.Write(Colors.Red, "[GRC] Error checking for updates: {0}", ex.Message);
				Logging.WriteException(ex);
            }
        }

		public static bool SkipUpdate {
			get {
				return
					Directory.Exists(Path.Combine(GliderRemoteCompat.PluginPath, ".svn")) ||
					Directory.Exists(Path.Combine(GliderRemoteCompat.PluginPath, "_svn"));
			}
		}

		public static int GetInstalledRevision() {
			try {
				string revStr = File.ReadAllText(GliderRemoteCompat.PluginPath + "\\revision.txt");
				return int.Parse(revStr);
			} catch {
				return 0;
			}
		}

        private static int GetLatestRevision()
        {
			try {
				using (WebClient client = new WebClient()) {
					string html = client.DownloadString(SvnUrl + "revision.txt");
					return int.Parse(html);
				}
			} catch (Exception e) {
				throw new Exception("Unable to retreive latest revision", e);
			}
        }

		private static void DownloadFilesFromSvn(WebClient client, string baseUrl) {
			DownloadFilesFromSvn(client, baseUrl, baseUrl);
		}

        private static void DownloadFilesFromSvn(WebClient client, string baseUrl, string url)
        {
            string html = client.DownloadString(url);
            MatchCollection results = _linkPattern.Matches(html);

            IEnumerable<Match> matches = from match in results.OfType<Match>()
                                         where match.Success && match.Groups["ln"].Success
                                         select match;
            foreach (Match match in matches)
            {
                string file = RemoveXmlEscapes(match.Groups["ln"].Value);
                string newUrl = url + file;
                if (newUrl[newUrl.Length - 1] == '/') // it's a directory...
                {
                    DownloadFilesFromSvn(client, baseUrl, newUrl);
                }
                else // its a file.
                {
					string filePath, dirPath;
					string relativePath = url.Substring(baseUrl.Length);
                    if (url.Length > baseUrl.Length)
                    {
                        dirPath = Path.Combine(GliderRemoteCompat.PluginPath, relativePath);
                        filePath = Path.Combine(dirPath, file);
                    }
                    else
                    {
                        dirPath = Environment.CurrentDirectory;
						filePath = Path.Combine(GliderRemoteCompat.PluginPath, file);
                    }
					//Logging.Write("[GRC] Downloading: {0}{1}", relativePath, file);
					//Logging.Write("[GRC]          To: {0}", filePath);
                    if (!Directory.Exists(dirPath))
                        Directory.CreateDirectory(dirPath);
                    client.DownloadFile(newUrl, filePath);
                }
            }
        }

        private static string RemoveXmlEscapes(string xml)
        {
            return
                xml.Replace("&amp;", "&").Replace("&lt;", "<").Replace("&gt;", ">").Replace("&quot;", "\"").Replace(
                    "&apos;", "'");
        }
    }
}

