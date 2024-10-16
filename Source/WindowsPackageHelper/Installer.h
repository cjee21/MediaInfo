#pragma once
#include "pch.h"

namespace MediaInfo_SparsePackage::Installer
{
    extern "C" __declspec(dllexport) HRESULT __stdcall Install();
    extern "C" __declspec(dllexport) HRESULT __stdcall Uninstall();
}