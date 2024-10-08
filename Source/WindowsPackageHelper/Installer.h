#pragma once
#include "pch.h"

namespace MediaInfo_SparsePackage::Installer
{
    const std::wstring GetInstallationPath();
    extern "C" __declspec(dllexport) HRESULT __stdcall RegisterSparsePackage();
#ifdef INCLUDE_UNINSTALLER
    extern "C" __declspec(dllexport) HRESULT __stdcall UnregisterSparsePackage();
#endif
}