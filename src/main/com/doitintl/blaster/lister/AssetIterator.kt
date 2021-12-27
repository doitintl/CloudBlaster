package com.doitintl.blaster.lister

import com.doitintl.blaster.shared.IllegalConfigException
import com.google.cloud.asset.v1.AssetServiceClient
import com.google.cloud.asset.v1.ContentType
import com.google.cloud.asset.v1.ListAssetsRequest
import com.google.cloud.asset.v1.ProjectName

class AssetIterator {

    fun listAssets(projectId: String, callback: Callback<String>, unfiltered: Boolean, filterFile: String) {

        AssetServiceClient.create().use { client ->
            val contentType = ContentType.CONTENT_TYPE_UNSPECIFIED
            val assetTypeMap = AssetTypeMap(filterFile)
            var supportedAssetTypeIds: List<String> = assetTypeMap.assetTypeIds()
            if (supportedAssetTypeIds.isEmpty()) {
                throw IllegalConfigException("No asset types in config file")
            }
            if (unfiltered) {
                supportedAssetTypeIds = emptyList()

            }
            var request = ListAssetsRequest.newBuilder()
                .setParent(ProjectName.of(projectId).toString())
                .addAllAssetTypes(supportedAssetTypeIds)
                .setContentType(contentType)
                .build()

            var response = client.listAssets(request)
            iterateListingResponse(response, callback, unfiltered, assetTypeMap)
            while (response.nextPageToken.isNotEmpty()) {
                // Each page is approx 1600 assets
                request = request.toBuilder().setPageToken(response.nextPageToken).build()
                response = client.listAssets(request)
                iterateListingResponse(response, callback, unfiltered, assetTypeMap)
            }
        }
    }

    private fun iterateListingResponse(
        response: AssetServiceClient.ListAssetsPagedResponse,
        callback: Callback<String>,
        unfiltered: Boolean,
        assetTypeMap: AssetTypeMap,

        ) {
        for (asset in response.iterateAll()) {
            val matched: Boolean =
                if (unfiltered) {//Just printing ALL assets
                    true
                } else {
                    val parts = asset.name.split("/").toTypedArray()
                    val id = parts[parts.size - 1]
                    val assetTypeIdentifier = asset.assetType
                    val (filterRegex, isWhitelist) = assetTypeMap.getFilterRegex(assetTypeIdentifier)

                    val match = filterRegex.matches(id)
                    // Filter can be either whitelist (listThese true) or blacklist,
                    match == isWhitelist
                }
            if (matched) {
                callback.call(asset.name)
            }
        }

    }
}