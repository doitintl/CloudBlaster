package com.doitintl.blaster.shared

import com.google.cloud.asset.v1.AssetServiceClient
import com.google.cloud.asset.v1.ContentType
import com.google.cloud.asset.v1.ListAssetsRequest
import com.google.cloud.asset.v1.ProjectName

class AssetIterator {

    fun listAssets(projectId: String?, callback: Callback<String>, justPrintAllAssets: Boolean) {
        var justPrintAll = justPrintAllAssets
        val client = AssetServiceClient.create()
        val contentType = ContentType.CONTENT_TYPE_UNSPECIFIED
        var apiIdentifiers: List<String?> = AssetTypeMap.instance.identifiers()
        if (justPrintAll) {
            apiIdentifiers = emptyList<String>()
        }
        var request = ListAssetsRequest.newBuilder()
            .setParent(ProjectName.of(projectId).toString())
            .addAllAssetTypes(apiIdentifiers)
            .setContentType(contentType)
            .build()
        if (apiIdentifiers.isEmpty()) {//Will also be empty if   asset-types.yaml is empty
            justPrintAll = true
        }
        if (justPrintAll) {
            println("Just printing all assets of all types to stout")
        }
        // Repeatedly call ListAssets until page token is empty.
        var response = client.listAssets(request)
        iterateListingResponse(response, callback, justPrintAll)
        while (response.nextPageToken.isNotEmpty()) {
            request = request.toBuilder().setPageToken(response.nextPageToken).build()
            response = client.listAssets(request)
            iterateListingResponse(response, callback, justPrintAll)
        }
    }

    private fun iterateListingResponse(
        response: AssetServiceClient.ListAssetsPagedResponse,
        callback: Callback<String>,
        allAssetTypes: Boolean
    ) {
        for (asset in response.iterateAll()) {
            if (allAssetTypes) {//Just printing ALL assets
                println(asset.name)
                continue
            }
            val parts = asset.name.split("/").toTypedArray()
            val id = parts[parts.size - 1]
            val assetTypeIdentifier = asset.assetType
            val assetType: AssetType? = AssetTypeMap.instance[assetTypeIdentifier]
            val filterRegex = assetType!!.filterRegex
            if (!filterRegex!!.matcher(id).matches()) {
                println("Found ${asset.name}")
                if (assetType.supportedForDeletion()) {
                    //TODO Avoid listing assets that we can't possibly delete, like Disks attached to Instances or default GAE services
                    callback.call(asset.name)
                } else {
                    System.err.println("$assetTypeIdentifier not supported for deletion")
                }
            }
        }
    }
}