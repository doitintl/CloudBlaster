package com.doitintl.blaster.lister

import com.google.cloud.asset.v1.AssetServiceClient
import com.google.cloud.asset.v1.ContentType
import com.google.cloud.asset.v1.ListAssetsRequest
import com.google.cloud.asset.v1.ProjectName

class AssetIterator {

    fun listAssets(projectId: String, callback: Callback<String>, noFilter: Boolean, filterFile: String) {

        AssetServiceClient.create().use { client ->

            val contentType = ContentType.CONTENT_TYPE_UNSPECIFIED
            val assetTypeMap = AssetTypeMap(filterFile)
            var apiIdentifiers: List<String> = assetTypeMap.identifiers()
            if (apiIdentifiers.isEmpty()) {
                throw IllegalStateException("No asset types in config file3")
            }
            if (noFilter) {
                apiIdentifiers = emptyList()

            }

            var request = ListAssetsRequest.newBuilder()
                .setParent(ProjectName.of(projectId).toString())
                .addAllAssetTypes(apiIdentifiers)
                .setContentType(contentType)
                .build()

            var response = client.listAssets(request)
            iterateListingResponse(response, callback, noFilter, assetTypeMap)
            while (response.nextPageToken.isNotEmpty()) {
                request = request.toBuilder().setPageToken(response.nextPageToken).build()
                response = client.listAssets(request)
                iterateListingResponse(response, callback, noFilter, assetTypeMap)
            }
        }
    }

    private fun iterateListingResponse(
        response: AssetServiceClient.ListAssetsPagedResponse,
        callback: Callback<String>,
        noFilter: Boolean,
        assetTypeMap: AssetTypeMap
    ) {
        for (asset in response.iterateAll()) {
            val matched: Boolean =
                if (noFilter) {//Just printing ALL assets
                    callback.call(asset.name)
                    true
                } else {
                    val parts = asset.name.split("/").toTypedArray()
                    val id = parts[parts.size - 1]
                    val assetTypeIdentifier = asset.assetType
                    val (filterRegex, isWhitelist) = assetTypeMap.getFilterRegex(assetTypeIdentifier)

                    val match = filterRegex.matches(id)
                    match == isWhitelist
                }
            if (matched) {
                callback.call(asset.name)
            }
        }
    }
}