package com.doitintl.blaster.lister

import com.google.cloud.asset.v1.AssetServiceClient
import com.google.cloud.asset.v1.ContentType
import com.google.cloud.asset.v1.ListAssetsRequest
import com.google.cloud.asset.v1.ProjectName

class AssetIterator {

    fun listAssets(projectId: String, callback: Callback<String>, justPrintAllAssets: Boolean, filterFile: String) {
        var justPrintAll = justPrintAllAssets
        val client = AssetServiceClient.create()
        val contentType = ContentType.CONTENT_TYPE_UNSPECIFIED
        val assetTypeMap = AssetTypeMap(filterFile)
        var apiIdentifiers: List<String> = assetTypeMap.identifiers()
        if (justPrintAll || apiIdentifiers.isEmpty()) { //if "-a" arg was used or if asset-types is empty
            apiIdentifiers = emptyList()
            justPrintAll = true
            if (justPrintAll) {
                println("Printing all assets, even if deletion is not supported, to stdout")
            }
        }

        var request = ListAssetsRequest.newBuilder()
            .setParent(ProjectName.of(projectId).toString())
            .addAllAssetTypes(apiIdentifiers)
            .setContentType(contentType)
            .build()

        var response = client.listAssets(request)
        iterateListingResponse(response, callback, justPrintAll, assetTypeMap)
        while (response.nextPageToken.isNotEmpty()) {
            request = request.toBuilder().setPageToken(response.nextPageToken).build()
            response = client.listAssets(request)
            iterateListingResponse(response, callback, justPrintAll, assetTypeMap)
        }

    }

    private fun iterateListingResponse(
        response: AssetServiceClient.ListAssetsPagedResponse,
        callback: Callback<String>,
        allAssetTypes: Boolean,
        assetTypeMap: AssetTypeMap
    ) {
        for (asset in response.iterateAll()) {
            if (allAssetTypes) {//Just printing ALL assets
                println(asset.name)
                continue
            }
            val parts = asset.name.split("/").toTypedArray()
            val id = parts[parts.size - 1]
            val assetTypeIdentifier = asset.assetType
            val filterRegex = assetTypeMap.getFilterRegex(assetTypeIdentifier)
            if (!filterRegex.matches(id)) {
                println(asset.name)

                //TODO Avoid listing assets that we can't possibly delete, like Disks attached to Instances or default GAE services
                callback.call(asset.name)

            }
        }
    }
}