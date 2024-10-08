// Functions for registration and removal of MediaInfo sparse package

#include "pch.h"
#include "Installer.h"

using namespace winrt::Windows::ApplicationModel;
using namespace winrt::Windows::Foundation;
using namespace winrt::Windows::Foundation::Collections;
using namespace winrt::Windows::Management::Deployment;

using namespace MediaInfo_SparsePackage::Installer;

const std::wstring SparsePackageName = L"MediaInfo";
const std::wstring SparsePackageFileName = L"MediaInfo_SparsePackage.msix";

const std::wstring MediaInfo_SparsePackage::Installer::GetInstallationPath() {
    std::filesystem::path module_path{ wil::GetModuleFileNameW<std::wstring>(wil::GetModuleInstanceHandle()) };
    return module_path.remove_filename().wstring();
}

STDAPI MediaInfo_SparsePackage::Installer::RegisterSparsePackage()
{
    // Cannot handle sparse packages in safe-mode
    if (GetSystemMetrics(SM_CLEANBOOT) > 0)
    {
        return S_FALSE;
    }

    PackageManager packageManager;
    AddPackageOptions options;

    const std::wstring externalLocation = GetInstallationPath();
    const std::wstring sparsePkgPath = externalLocation + L"\\" + SparsePackageFileName;

    Uri externalUri(externalLocation);
    Uri packageUri(sparsePkgPath);

    options.ExternalLocationUri(externalUri);

    auto deploymentOperation = packageManager.AddPackageByUriAsync(packageUri, options);
    auto deployResult = deploymentOperation.get();

    if (!SUCCEEDED(deployResult.ExtendedErrorCode()))
    {
        return deployResult.ExtendedErrorCode();
    }

    SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_IDLIST, NULL, NULL);

    return S_OK;
}

#ifdef INCLUDE_UNINSTALLER
STDAPI MediaInfo_SparsePackage::Installer::UnregisterSparsePackage()
{
    // Cannot handle sparse packages in safe-mode
    if (GetSystemMetrics(SM_CLEANBOOT) > 0)
    {
        return S_FALSE;
    }

    PackageManager packageManager;
    IIterable<Package> packages;

    try
    {
        packages = packageManager.FindPackagesForUser(L"");
    }
    catch (winrt::hresult_error const& ex)
    {
        return ex.code();
    }

    for (const Package& package : packages)
    {
        if (package.Id().Name() != SparsePackageName)
        {
            continue;
        }

        winrt::hstring fullName = package.Id().FullName();
        auto deploymentOperation = packageManager.RemovePackageAsync(fullName, RemovalOptions::None);
        auto deployResult = deploymentOperation.get();

        if (!SUCCEEDED(deployResult.ExtendedErrorCode()))
        {
            return deployResult.ExtendedErrorCode();
        }

        break;
    }

    SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_IDLIST, NULL, NULL);

    return S_OK;
}
#endif