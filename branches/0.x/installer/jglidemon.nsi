; Taken from http://nsis.sourceforge.net/Simple_installer_with_JRE_check by weebib
; Use it as you desire.
 
; Credit given to so many people of the NSIS forum.
 
!define AppName "JGlideMon"

; so it can be passed via /D
!ifndef AppVersion
  !define AppVersion "0.0.0"
!endif
!define ShortName "JGlideMon"
!define MIN_JRE_VERSION "1.5"
!define PREF_JRE_VERSION "1.6"
!define JRE_URL "http://javadl.sun.com/webapps/download/AutoDL?BundleId=11292"
 
!include "MUI.nsh"
; !include "Sections.nsh"
!include "WordFunc.nsh"

!insertmacro VersionCompare

; to allow one nsi file to make different installers
!ifdef InstallerType
  !if ${InstallerType} == "full"
  !else if ${InstallerType} == "core"
  !else
    !undef InstallerType
  !endif
!endif

!ifndef InstallerType
  !define InstallerType "full"
!endif

;--------------------------------
; Configuration
 
Name "${AppName}"
OutFile "${ShortName}_v${AppVersion}_${InstallerType}_setup.exe"
InstallDir "$PROGRAMFILES\${ShortName}"
InstallDirRegKey HKLM "SOFTWARE\${ShortName}" "InstallPath"

ShowInstDetails show
ShowUninstDetails show
 
VIAddVersionKey /LANG=${LANG_ENGLISH} "ProductName" "${AppName}"
VIAddVersionKey /LANG=${LANG_ENGLISH} "FileVersion" "${AppVersion}"
VIProductVersion "${AppVersion}.0"

; Installation types
InstType "Full"
InstType "Minimal"

Var StartMenuFolder

; MUI Settings
!define MUI_COMPONENTSPAGE_SMALLDESC 
!define MUI_HEADERIMAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Header\nsis.bmp"

 
;--------------------------------
;Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "..\License.txt"
!insertmacro MUI_PAGE_COMPONENTS
!define MUI_PAGE_CUSTOMFUNCTION_PRE "SkipPageIfInstalled"
!insertmacro MUI_PAGE_DIRECTORY

; Start Menu Folder Page Configuration
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKLM" 
!define MUI_STARTMENUPAGE_REGISTRY_KEY "SOFTWARE\${ShortName}" 
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "StartMenuFolder"
!define MUI_PAGE_CUSTOMFUNCTION_PRE "SkipPageIfInstalled"
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuFolder

!insertmacro MUI_PAGE_INSTFILES

!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_NOTCHECKED
!define MUI_FINISHPAGE_RUN_TEXT "Run JGlideMon"
!define MUI_FINISHPAGE_RUN_FUNCTION "LaunchLink"
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH
 
;--------------------------------
;Modern UI Configuration
 
  !define MUI_ABORTWARNING
 
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"
 
;--------------------------------
;Language Strings
LangString DESC_SecAppFiles ${LANG_ENGLISH} "Install core application files"
!if ${InstallerType} == "full"
LangString DESC_SecTTS ${LANG_ENGLISH} "Instal text-to-speach components"
!endif
LangString DESC_SecHelp ${LANG_ENGLISH} "Install help file"

Function LaunchLink
;  MessageBox MB_OK "Reached LaunchLink $\r$\n \
;                   SMPROGRAMS: $SMPROGRAMS  $\r$\n \
;                   Start Menu Folder: $STARTMENU_FOLDER $\r$\n \
;                   InstallDirectory: $INSTDIR "
  ExecShell "" "$INSTDIR\${AppName}.jar"
FunctionEnd


;--------------------------------
;Installer Sections
 
Section "${AppName} Core" SecAppFiles
  SectionIn 1 2 RO ; Full install, cannot be unselected

  Call DetectJGM
  Call DetectJRE

  SetOutPath $INSTDIR

  CreateDirectory "$INSTDIR\sounds"
  
  ; Put file there
  File "..\bin\JGlideMon.jar"
  File "..\bin\Profiler.jar"
  File "..\bin\ChangeLog.txt"
  File "..\bin\License.txt"

  ; Store install folder, version
  WriteRegStr HKLM "SOFTWARE\${ShortName}" "InstallDir" $INSTDIR
  WriteRegStr HKLM "SOFTWARE\${ShortName}" "CurrentVersion" "${AppVersion}"
 
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${ShortName}" "DisplayName" "${AppName}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${ShortName}" "DisplayVersion" "${AppVersion}"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${ShortName}" "UninstallString" '"$INSTDIR\Uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${ShortName}" "NoModify" "1"
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${ShortName}" "NoRepair" "1"
  
  ; Create uninstaller
  WriteUninstaller "Uninstall.exe"

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
  CreateShortCut "$SMPROGRAMS\$StartMenuFolder\${AppName}.lnk" "$INSTDIR\${AppName}.jar"
  CreateShortCut "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk" "$INSTDIR\Uninstall.exe" "" "$INSTDIR\Uninstall.exe" 0
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

!if ${InstallerType} == "full"
Section "Text-To-Speach" SecTTS
  SectionIn 1
  SetOutPath "$INSTDIR\lib\speech"
  File "..\bin\lib\speech\*.*"
SectionEnd
!endif

Section "Documentation" SecHelp
  SectionIn 1
  SetOutPath $INSTDIR
  File "..\bin\${ShortName}.chm"

  !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
  CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
  CreateShortCut "$SMPROGRAMS\$StartMenuFolder\${AppName} Help.lnk" "$INSTDIR\${AppName}.chm"
  !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

;--------------------------------
;Installer Functions
 
Function .onInit
 
FunctionEnd
 
 
;--------------------------------
;Uninstaller Section
 
Section "Uninstall"
 
  ; remove registry keys
  !insertmacro MUI_STARTMENU_GETFOLDER Application $StartMenuFolder
  StrCmp $StartMenuFolder "" +2
  RMDir /r "$SMPROGRAMS\$StartMenuFolder"

  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${ShortName}"
  DeleteRegKey HKLM  "SOFTWARE\${ShortName}"
  RMDir /r "$INSTDIR"
 
SectionEnd

Function SkipPageIfInstalled
  ReadRegStr $2 HKLM "SOFTWARE\${ShortName}" "CurrentVersion"
  StrCmp $2 "" done
  ${VersionCompare} $2 ${AppVersion} $R0
  StrCmp $R0 "0" abrt
  StrCmp $R0 "1" abrt
  StrCmp $R0 "2" abrt

  Goto done

  abrt:
    Abort

  done:
FunctionEnd

Function DetectJGM
  ReadRegStr $2 HKLM "SOFTWARE\${ShortName}" "CurrentVersion"
  StrCmp $2 "" done
  ${VersionCompare} $2 ${AppVersion} $R0
  StrCmp $R0 "0" sameversion
  StrCmp $R0 "1" overrite
  StrCmp $R0 "2" upgrade

  Goto done ; not found so fresh install

  sameversion:
    MessageBox MB_YESNO|MB_ICONQUESTION "${AppName} v$2 is already installed. Would you like to reinstall?" \
      IDYES done
    Quit

  overrite:
    MessageBox MB_YESNO|MB_ICONQUESTION "${AppName} v$2 is already installed. Do you want to install version ${AppVersion} anyway?" \
      IDYES done
    Quit

  upgrade:
    MessageBox MB_YESNO|MB_ICONQUESTION "${AppName} v$2 is already installed. Do you want to upgrade to version ${AppVersion}?" \
      IDYES done
    Quit

  done:
FunctionEnd



Function GetJRE
        MessageBox MB_OKCANCEL|MB_ICONQUESTION "${AppName} requires Java ${MIN_JRE_VERSION} or greater. \
                         Press Ok to download and install the latest version or cancel to exit." \
                         IDOK getjava
  Quit
;  Abort "${AppName} cannot be isntalled without Java."

  getjava: 
        StrCpy $2 "$TEMP\Java Runtime Environment.exe"
        nsisdl::download /TIMEOUT=30000 ${JRE_URL} $2
        Pop $R0 ;Get the return value
                StrCmp $R0 "success" installjava
;                MessageBox MB_OK "Download failed: $R0"
                Abort "There was an error downloading the JRE ($R0). Please manually install a JRE and run setup again."
  installjava:
        ExecWait $2
        Delete $2
FunctionEnd
 
 
Function DetectJRE
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ${VersionCompare} $2 ${MIN_JRE_VERSION} $R0
  StrCmp $R0 "0" detectdone
  StrCmp $R0 "1" detectdone
  
  Call GetJRE
  Call JustDetectJRE
  detectdone:
FunctionEnd

Function JustDetectJRE
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ${VersionCompare} $2 ${MIN_JRE_VERSION} $R0
  StrCmp $R0 "0" justdone
  StrCmp $R0 "1" justdone

  Abort "JRE installation was unsuccessful. Please manually install a JRE and run setup again."

  justdone:
FunctionEnd



;--------------------------------
; Descriptions
 
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
!insertmacro MUI_DESCRIPTION_TEXT ${SecAppFiles} $(DESC_SecAppFiles)
!if ${InstallerType} == "full"
!insertmacro MUI_DESCRIPTION_TEXT ${SecTTS} $(DESC_SecTTS)
!endif
!insertmacro MUI_DESCRIPTION_TEXT ${SecHelp} $(DESC_SecHelp)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

