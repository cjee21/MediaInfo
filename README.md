# MediaInfo README

MediaInfo is a convenient unified display of the most relevant technical and tag data for video and audio files.

## About this branch

This branch is for testing changes to MediaInfo's Windows GUI (VCL)

This branch has been successfully tested using the latest C++Builder, MSVC and NSIS. MediaInfo installs and uninstalls properly including the Windows 11 Explorer context menu and WebView2 loader on the latest Windows 11 public build. More testing and changes may be needed for the Windows 11 Explorer context menu to ensure robustness.

This branch contains the following additions on top of MediaInfo's master branch:
- Binaries from MSVC2022 and C++Builder 12 are used by installer script
- Initial implementation of Windows 11 Explorer context menu


## How to build MediaInfo with Windows 11 Explorer context menu

- Update version numbers in the following files as necessary
  - `Source\GUI\VCL\Manifest.manifest`
  - `Source\WindowsSparsePackage\AppxManifest.xml`
  - `Source\WindowsShellExtension\Resource.rc`
  - `Source\WindowsPackageHelper\Resource.rc`
- Build the MSIX sparse package, shell extension DLL and helper DLL:
  ```cmd
  call "C:\Program Files\Microsoft Visual Studio\2022\Community\Common7\Tools\VsDevCmd.bat"
  makeappx pack /d "Source\WindowsSparsePackage\MSIX" /p "Project\MSVC2022\x64\Release\MediaInfo_SparsePackage.msix" /nv
  MSBuild /t:MediaInfo_WindowsShellExtension /restore /p:RestorePackagesConfig=true;Configuration=Release;Platform=x64 Project\MSVC2022\MediaInfo.sln
  MSBuild /t:MediaInfo_PackageHelper /restore /p:RestorePackagesConfig=true;Configuration=Release;Platform=Win32 Project\MSVC2022\MediaInfo.sln
  ```
- Sign `MediaInfo_SparsePackage.msix`, `MediaInfo_WindowsShellExtension.dll` and `MediaInfo_PackageHelper.dll` in `MediaInfo\Project\MSVC2022\x64\Release`
- Build the other parts as usual.

Note: NSIS may have to be updated to latest version to handle Windows 11. Make changes as necessary if not using MSVC2022 and C++Builder 12.

## Limitations of new context menu implementation

- It is only used on Windows 11 at the moment. It should be possible to use it for older Windows versions too to enable opening multiple files in the same instance of MediaInfo and phase out the old registry-based implementation (ref: https://learn.microsoft.com/en-us/windows/win32/shell/reg-shell-exts).
- This branch is only tested on Windows 11 23H2 and 24H2 on a single user account. More testing on various Windows versions and configurations is needed including tests on multi-account/user setups. The new context menu should appear on all user accounts and should be cleanly removed from all accounts on uninstall. Updating MediaInfo should also be tested to ensure sparse package and DLLs are updated properly. Compatibility with older Windows versions should not be affected as the new changes are only active on Windows 11. It is expected that if there are any issues, it is more likely to occur in the sparse package (un)installation stage. The context menu itself is already considered to be stable.

## Explanation of approach chosen for this new context menu implementation

- The sparse package is provisioned for all users by the installer during install and removed for all users during uninstall using a helper dll. This should ensure all users will have the context menu on login after install and a clean removal after uninstall.

- The 'Assets' folder and `resources.pri` file are needed because when the app runs with app identity, the assets referenced by the sparse package manifest is used for the app icon. Since it is a sparse package, it itself does not contain assets and instead references the assets that are in the declared external location. The `resources.pri` is needed for the 'unplated' icons which are needed to prevent the icon from having accent-coloured plating as the background.

- In order to minimize the size of the DLL while ensuring it runs on clean Windows installations without Microsoft Visual C++ Redistributable installed, `vcruntime` is statically linked while `ucrt` is dynamically linked. This results in something between using `/MT` and `/MD`. It is known as [Hybrid CRT](https://github.com/microsoft/WindowsAppSDK/blob/main/docs/Coding-Guidelines/HybridCRT.md) and is supported according to the CRT maintainer.

- `/PDBALTPATH:%_PDB%` is added to linker in release mode so that the PDB file path is not contained in the DLL. This ensures no path information leakage and reduces the size of DLL slightly while still enabling analysis that requires PDB files to be done, for example SizeBench.

- The shell extension is re-written from WRL to C++/WinRT. See 'Note' at https://learn.microsoft.com/en-us/cpp/cppcx/wrl/windows-runtime-cpp-template-library-wrl?view=msvc-170 for details.

- The sparse package and shell extension is moved to subdirectory instead of doing it 100% the proper way in order to prevent MediaInfo.exe from having app/package identity.
  - Reason: With package identity, for some reason, when MediaInfo is launched from start menu and running, right-clicking a file in File Explorer fails to load the context menu entry and causes MediaInfo to force-close. This does not happen if a MediaInfo instance that is started from the context menu is running.
  - Effect of workaround: MediaInfo no longer has app identity. Since we are not using any other features that require identity at the moment, there are no disadvantages other than there is no package name shown in task manager for MediaInfo's process.

## Enabled security mitigations for new context menu

The following are enabled in the project file for the Explorer context menu shell extension DLL.

GS (Buffer Security Check), sdl (Additional Security Checks), NXCOMPAT (Data Execution Prevention), DYNAMICBASE (Address space layout randomization), HIGHENTROPYVA (64-Bit ASLR), guard:cf (Control Flow Guard), guard:ehcont (EH Continuation Metadata), Qspectre (Spectre variant 1 mitigation), CETCOMPAT (CET Shadow Stack)

## Video of this branch in action

https://github.com/user-attachments/assets/f8da9135-3e76-4695-be19-42c2f7d1e2d5

## References

The following resources helped in the making of the Windows 11 File Explorer context menu
- https://blogs.windows.com/windowsdeveloper/2021/07/19/extending-the-context-menu-and-share-dialog-in-windows-11/
- https://blogs.windows.com/windowsdeveloper/2019/10/29/identity-registration-and-activation-of-non-packaged-win32-apps/
- https://learn.microsoft.com/en-us/windows/apps/desktop/modernize/grant-identity-to-nonpackaged-apps
- https://learn.microsoft.com/en-us/windows/win32/api/shobjidl_core/nn-shobjidl_core-iexplorercommand
- https://github.com/microsoft/AppModelSamples/tree/master/Samples/SparsePackages
- https://github.com/microsoft/vscode-explorer-command
- https://github.com/xandfis/W11ContextMenuDemo
- https://github.com/M2Team/NanaZip/blob/main/NanaZip.UI.Modern/NanaZip.ShellExtension.cpp
- https://github.com/notepad-plus-plus/notepad-plus-plus/blob/ce4d374a4782185f2e0dbaa2e6527ba7f8b9ad39/PowerEditor/src/tools/NppModernShell/Installer.cpp
- https://github.com/notepad-plus-plus/nppShell/blob/5e410dd6450a2572dd2eb209250561b6eec97535/Installer.cpp
- https://learn.microsoft.com/en-us/windows/uwp/cpp-and-winrt-apis/intro-to-using-cpp-with-winrt
- https://learn.microsoft.com/en-us/cpp/cppcx/wrl/windows-runtime-cpp-template-library-wrl?view=msvc-170
- https://github.com/microsoft/wil
