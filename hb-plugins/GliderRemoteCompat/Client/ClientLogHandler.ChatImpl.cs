using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Styx.WoWInternals;
using System.Diagnostics;

using AT.MIN;
using Styx.Helpers;
using System.Text.RegularExpressions;

namespace GliderRemoteCompat {
	partial class ClientLogHandler {
		private static readonly Dictionary<string, string[]> CHAT_CATEGORY_LIST = new Dictionary<string,string[]>(){
			{"PARTY", new string[] { "PARTY_LEADER", "PARTY_GUIDE", "MONSTER_PARTY" }},
			{"RAID", new string[] { "RAID_LEADER", "RAID_WARNING" }},
			{"GUILD", new string[] { "GUILD_ACHIEVEMENT" }},
			{"WHISPER", new string[] { "WHISPER_INFORM", "AFK", "DND" }},
			{"CHANNEL", new string[] { "CHANNEL_JOIN", "CHANNEL_LEAVE", "CHANNEL_NOTICE", "CHANNEL_USER" }},
			{"BATTLEGROUND", new string[] { "BATTLEGROUND_LEADER" }},
			{"BN_WHISPER", new string[] { "BN_WHISPER_INFORM" }},
			{"BN_CONVERSATION", new string[] { "BN_CONVERSATION_NOTICE", "BN_CONVERSATION_LIST" }},
		};

		private static readonly Dictionary<string, string> CHAT_INVERTED_CATEGORY_LIST = new Dictionary<string, string>();

		static ClientLogHandler() {
			foreach (string category in CHAT_CATEGORY_LIST.Keys) {
				var sublist = CHAT_CATEGORY_LIST[category];

				foreach (string item in sublist) {
					CHAT_INVERTED_CATEGORY_LIST[item] = category;
				}
			}
		}

		private static string Chat_GetChatCategory(string chatType) {
			return CHAT_INVERTED_CATEGORY_LIST.ContainsKey(chatType)
				? CHAT_INVERTED_CATEGORY_LIST[chatType]
				: chatType;
		}

		private static readonly Regex ExtraSpacesRegex = new Regex("     +");
		public static string RemoveExtraSpaces(string str) {
			return ExtraSpacesRegex.Replace(str, "    ");
		}

		private static string GetColoredName(string eventName, string arg1, string arg2, string arg3, string arg4, string arg5, string arg6, string arg7, string arg8, string arg9, string arg10, string arg11, string arg12, params string[] rest) {
			return arg2;
		}

		private void AddMessage(string s, float r, float g, float b, int id, params object[] rest) {
			if (!(1f == r && 1f == g && 1f == b)) {
				s = string.Format(
					"|cff{0:x2}{1:x2}{2:x2}{3}|r",
					(int)(r * 255), (int)(g * 255), (int)(b * 255), s);
			}

			client.SendLog(ClientLogType.ChatRaw, s);
		}

		private void AddCombatMessage(string s) {
			client.SendLog(ClientLogType.Combat, Client.RemoveChatFormatting(s));
		}

		private void AddCombatMessage(string s, float r, float g, float b, int id, params object[] rest) {
			client.SendLog(ClientLogType.Combat, Client.RemoveChatFormatting(s));
		}

		private void Lua_ChatMsg(object sender, LuaEventArgs args) {
			try {
				Lua_ChatMsg_Impl(sender, args);
			} catch (Exception e) {
				client.Send(e.ToString());
			}
		}

		/// <summary>
		/// This method mimics the default Blizzard chat frame from
		/// FrameXML\ChatFrame.lua:ChatFrame_MessageEventHandler().
		/// </summary>
		/// <param name="sender"></param>
		/// <param name="args"></param>
		private void Lua_ChatMsg_Impl(object sender, LuaEventArgs args) {
			//Logging.Write("Lua_ChatMsg() called: {0}, Args.Length={1}", args.EventName, args.Args.Length);
			Debug.Assert(args.EventName.StartsWith("CHAT_MSG"));
			object[] Args = args.Args;
			var arg1  = Args.Length < 1 ? "" : Args[0].ToString();
			var arg2  = Args.Length < 2 ? "" : Args[1].ToString();
			var arg3  = Args.Length < 3 ? "" : Args[2].ToString();
			var arg4  = Args.Length < 4 ? "" : Args[3].ToString();
			var arg5  = Args.Length < 5 ? "" : Args[4].ToString();
			var arg6  = Args.Length < 6 ? "" : Args[5].ToString();
			var arg7  = Args.Length < 7 ? "" : Args[6].ToString();
			var arg8  = Args.Length < 8 ? "" : Args[7].ToString();
			var arg9  = Args.Length < 9 ? "" : Args[8].ToString();
			var arg10 = Args.Length < 10 ? "" : Args[9].ToString();
			var arg11 = Args.Length < 11 ? "" : Args[10].ToString();
			var arg12 = Args.Length < 12 ? "" : Args[11].ToString();
			var arg13 = Args.Length < 13 ? "" : Args[12].ToString();
			var arg14 = Args.Length < 14 ? "" : Args[13].ToString();
			var arg15 = Args.Length < 15 ? "" : Args[14].ToString();
			
			string type = args.EventName.Substring(9);
			ChatTypeInfo info = ChatTypeInfo.Get(type);

			string coloredName = GetColoredName(args.EventName, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9, arg10, arg11, arg12, arg13, arg14, arg15);

			int channelLength = arg4.Length;
			string infoType = type;

			string chatGroup = Chat_GetChatCategory(type);
			string chatTarget = "";

			//Logging.Write("{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12}, {13}, {14}, {15}",
			//    args.EventName, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9,
			//    arg10, arg11, arg12, arg13, arg14, arg15);

			if ("CHANNEL" == chatGroup || "BN_CONVERSATION" == chatGroup) {
				chatTarget = arg8;
			} else if ("WHISPER" == chatGroup || "BN_WHISPER" == chatGroup) {
				if (!(arg2.Substring(1, 2) == "|K")) {
					chatTarget = arg2.ToUpper();
				} else {
					chatTarget = arg2;
				}
			}

			if (type == "SYSTEM" || type == "SKILL" || type == "LOOT" || type == "MONEY" ||
				type == "OPENING" || type == "TRADESKILLS" || type == "PET_INFO" || type == "TARGETICONS") {
				AddMessage(arg1, info.r, info.g, info.b, info.id);
			} else if (type.StartsWith("COMBAT_")) {
				switch (type) {
					case "COMBAT_XP_GAIN":
					case "COMBAT_HONOR_GAIN":
						AddCombatMessage(arg1, info.r, info.g, info.b, info.id);
						break;

					default:
						AddMessage(arg1, info.r, info.g, info.b, info.id);
						break;
				}
			} else if (type.StartsWith("SPELL_")) {
				AddMessage(arg1, info.r, info.g, info.b, info.id);
			} else if (type.StartsWith("BG_SYSTEM_")) {
				AddMessage(arg1, info.r, info.g, info.b, info.id);
			} else if (type.StartsWith("ACHIEVEMENT")) {
				AddMessage(Tools.sprintf(arg1, "|Hplayer:" + arg2 + "|h" + "[" + coloredName + "]" + "|h"), info.r, info.g, info.b, info.id);
			} else if (type.StartsWith("GUILD_ACHIEVEMENT")) {
				AddMessage(Tools.sprintf(arg1, "|Hplayer:" + arg2 + "|h" + "[" + coloredName + "]" + "|h"), info.r, info.g, info.b, info.id);
			} else if (type == "IGNORED") {
				AddMessage(Tools.sprintf(CHAT_IGNORED, arg2), info.r, info.g, info.b, info.id);
			} else if (type == "FILTERED") {
				AddMessage(Tools.sprintf(CHAT_FILTERED, arg2), info.r, info.g, info.b, info.id);
			} else if (type == "RESTRICTED") {
				AddMessage(CHAT_RESTRICTED, info.r, info.g, info.b, info.id);
			} else if (type == "CHANNEL_LIST") {
				if (channelLength > 0) {
					AddMessage(Tools.sprintf(_G["CHAT_" + type + "_GET"] + arg1, int.Parse(arg8), arg4), info.r, info.g, info.b, info.id);
				} else {
					AddMessage(arg1, info.r, info.g, info.b, info.id);
				}
			} else if (type == "CHANNEL_NOTICE_USER") {
				string globalstring = _G["CHAT_" + arg1 + "_NOTICE_BN"];

				if ("" == globalstring) {
					globalstring = _G["CHAT_" + arg1 + "_NOTICE"];
				}

				if (arg5.Length > 0) {
					// TWO users in this notice (E.G. x kicked y)
					AddMessage(Tools.sprintf(globalstring, arg8, arg4, arg2, arg5), info.r, info.g, info.b, info.id);
				} else if (arg1 == "INVITE" ) {
					AddMessage(Tools.sprintf(globalstring, arg4, arg2), info.r, info.g, info.b, info.id);
				} else {
					AddMessage(Tools.sprintf(globalstring, arg8, arg4, arg2), info.r, info.g, info.b, info.id);
				}
			} else if (type == "CHANNEL_NOTICE") {
				string globalstring = _G["CHAT_" + arg1 + "_NOTICE_BN"];

				if ("" == globalstring) {
					globalstring = _G["CHAT_" + arg1 + "_NOTICE"];
				}

				if (int.Parse(arg10) > 0 ) {
					arg4 = arg4 + " " + arg10;
				}

				int accessID = 0; //ChatHistory_GetAccessID(Chat_GetChatCategory(type), arg8);
				int typeID = 0; //ChatHistory_GetAccessID(infoType, arg8);
				AddMessage(Tools.sprintf(globalstring, arg8, arg4), info.r, info.g, info.b, info.id, false, accessID, typeID);
			} else if (type == "BN_CONVERSATION_NOTICE") {
				string channelLink = Tools.sprintf(CHAT_BN_CONVERSATION_GET_LINK, arg8, MAX_WOW_CHAT_CHANNELS + arg8);
				string playerLink = Tools.sprintf("|HBNplayer:%s:%s:%s:%s:%s|h[%s]|h", arg2, arg13, arg11, Chat_GetChatCategory(type), arg8, arg2);
				string message = Tools.sprintf(_G["CHAT_CONVERSATION_" + arg1 + "_NOTICE"], channelLink, playerLink);
				int accessID = 0; //ChatHistory_GetAccessID(Chat_GetChatCategory(type), arg8);
				int typeID = 0; //ChatHistory_GetAccessID(infoType, arg8);
				AddMessage(message, info.r, info.g, info.b, info.id, false, accessID, typeID);
			} else if (type == "BN_CONVERSATION_LIST") {
				string channelLink = Tools.sprintf(CHAT_BN_CONVERSATION_GET_LINK, arg8, MAX_WOW_CHAT_CHANNELS + arg8);
				string message = Tools.sprintf(CHAT_BN_CONVERSATION_LIST, channelLink, arg1);
				AddMessage(message, info.r, info.g, info.b, info.id, false/*, accessID, typeID*/);
			} else if (type == "BN_INLINE_TOAST_ALERT") {
				string globalstring = _G["BN_INLINE_TOAST_" + arg1];
				string message;

				if (arg1 == "FRIEND_REQUEST") {
					message = globalstring;
				} else if (arg1 == "FRIEND_PENDING") {
					message = Tools.sprintf(BN_INLINE_TOAST_FRIEND_PENDING, 0 /*BNGetNumFriendInvites()*/);
				} else if (arg1 == "FRIEND_REMOVED") {
					message = Tools.sprintf(globalstring, arg2);
				} else {
					string playerLink = Tools.sprintf("|HBNplayer:%s:%s:%s:%s:%s|h[%s]|h", arg2, arg13, arg11, Chat_GetChatCategory(type), 0, arg2);
					message = Tools.sprintf(globalstring, playerLink);
				}

				AddMessage(message, info.r, info.g, info.b, info.id);
			} else if (type == "BN_INLINE_TOAST_BROADCAST") {
				if (arg1 != "") {
					arg1 = RemoveExtraSpaces(arg1);
					string playerLink = Tools.sprintf("|HBNplayer:%s:%s:%s:%s:%s|h[%s]|h", arg2, arg13, arg11, Chat_GetChatCategory(type), 0, arg2);
					AddMessage(Tools.sprintf(BN_INLINE_TOAST_BROADCAST, playerLink, arg1), info.r, info.g, info.b, info.id);
				}
			} else if (type == "BN_INLINE_TOAST_BROADCAST_INFORM") {
				if (arg1 != "") {
					arg1 = RemoveExtraSpaces(arg1);
					AddMessage(BN_INLINE_TOAST_BROADCAST_INFORM, info.r, info.g, info.b, info.id);
				}
			} else if (type == "BN_INLINE_TOAST_CONVERSATION") {
				AddMessage(Tools.sprintf(BN_INLINE_TOAST_CONVERSATION, arg1), info.r, info.g, info.b, info.id);
			} else {
				string body = "";

				// Add AFK/DND flags
				string pflag;

				if (arg6.Length > 0) {
					if (arg6 == "GM") {
						// If it was a whisper, dispatch it to the GMChat addon.
						//if (type == "WHISPER" ) {
						//    return;
						//}

						// Add Blizzard Icon, this was sent by a GM
						pflag = "<GM>"; // "|TInterface\\ChatFrame\\UI-ChatIcon-Blizz.blp:0:2:0:-3|t ";
					} else if (arg6 == "DEV") {
						// Add Blizzard Icon, this was sent by a Dev
						pflag = "<DEV>"; // "|TInterface\\ChatFrame\\UI-ChatIcon-Blizz.blp:0:2:0:-3|t ";
					} else {
						pflag = _G["CHAT_FLAG_" + arg6];
					}
				} else {
					pflag = "";
				}

				//if (type == "WHISPER_INFORM" && GMChatFrame_IsGM && GMChatFrame_IsGM(arg2)) {
				//    return;
				//}

				bool showLink = true;
				if (type.StartsWith("MONSTER") || type.StartsWith("RAID_BOSS")) {
					showLink = false;
				} else {
					arg1 = arg1.Replace("%", "%%");
				}

				//if  ((type == "PARTY_LEADER") && (HasLFGRestrictions())) {
				//    type = "PARTY_GUIDE";
				//}

				//// Search for icon links and replace them with texture links.
				//string term;
				//for tag in string.gmatch(arg1, "%b{}") do
				//    term = strlower(string.gsub(tag, "[{}]", ""));
				//    if ( ICON_TAG_LIST[term] and ICON_LIST[ICON_TAG_LIST[term]] ) then
				//        arg1 = string.gsub(arg1, tag, ICON_LIST[ICON_TAG_LIST[term]] .. "0|t");
				//    end
				//end

				// Remove groups of many spaces
				arg1 = RemoveExtraSpaces(arg1);

				string playerLink;

				if (type != "BN_WHISPER" && type != "BN_WHISPER_INFORM" && type != "BN_CONVERSATION") {
					playerLink = "|Hplayer:" + arg2 + ":" + arg11 + ":" + chatGroup + ("" != chatTarget ? ":" + chatTarget : "") + "|h";
				} else {
					playerLink = "|HBNplayer:" + arg2 + ":" + arg13 + ":" + arg11 + ":" + chatGroup + ("" != chatTarget ? ":" + chatTarget : "") + "|h";
				}

				string message = arg1;
				//if (arg15) {	// isMobile
				//    message = ChatFrame_GetMobileEmbeddedTexture(info.r, info.g, info.b) + message;
				//}

				//if ((arg3.Length > 0) && (arg3 != "Universal") && (arg3 != self.defaultLanguage)) {
				if (false) {
					string languageHeader = "[" + arg3 + "] ";
					if (showLink && (arg2.Length > 0)) {
						body = Tools.sprintf(_G["CHAT_" + type + "_GET"] + languageHeader + message, pflag + playerLink + "[" + coloredName + "]" + "|h");
					} else {
						body = Tools.sprintf(_G["CHAT_" + type + "_GET"] + languageHeader + message, pflag + arg2);
					}
				} else {
					if (!showLink || arg2.Length == 0) {
						if (type == "TEXT_EMOTE") {
							body = message;
						} else {
							body = Tools.sprintf(_G["CHAT_" + type + "_GET"] + message, pflag + arg2, arg2);
						}
					} else {
						if (type == "EMOTE") {
							body = Tools.sprintf(_G["CHAT_" + type + "_GET"] + message, pflag + playerLink + coloredName + "|h");
						} else if (type == "TEXT_EMOTE") {
							//body = string.gsub(message, arg2, pflag + playerLink + coloredName + "|h", 1);
							body = message.Replace(arg2, pflag + playerLink + coloredName + "|h");
						} else {
							body = Tools.sprintf(_G["CHAT_" + type + "_GET"] + message, pflag + playerLink + "[" + coloredName + "]" + "|h");
						}
					}
				}

				// Add Channel
				//arg4 = gsub(arg4, "%s%-%s.*", "");
				if (chatGroup  == "BN_CONVERSATION") {
					body = Tools.sprintf(CHAT_BN_CONVERSATION_GET_LINK, arg8, MAX_WOW_CHAT_CHANNELS + arg8) + body;
				} else if (channelLength > 0) {
					body = "|Hchannel:channel:" + arg8 + "|h[" + arg4 + "]|h " + body;
				}
			
				// Add Timestamps
				//if (CHAT_TIMESTAMP_FORMAT) {
				//    body = BetterDate(CHAT_TIMESTAMP_FORMAT, time()) + body;
				//}

				int accessID = 0; // ChatHistory_GetAccessID(chatGroup, chatTarget);
				int typeID = 0; // ChatHistory_GetAccessID(infoType, chatTarget);
				AddMessage(body, info.r, info.g, info.b, info.id, false, accessID, typeID);
			}
		}
	}
}
