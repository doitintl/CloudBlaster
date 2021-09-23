package com.doitintl.blaster.deleters

import com.doitintl.blaster.deleter.BaseDeleter
import com.doitintl.blaster.shared.Constants.CLOUD_BLASTER
import com.doitintl.blaster.shared.Constants.ID
import com.doitintl.blaster.shared.Constants.PROJECT
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory

import com.google.api.services.sqladmin.SQLAdmin

class SQLInstanceDeleter : BaseDeleter() {

    override val pathPatterns: Array<String>
        get() = arrayOf("//cloudsql.googleapis.com/projects/{PROJECT}/instances/{ID}")

    override fun doDelete(p: Map<String, String>) {
        val response = createSqlAdminService().instances().delete(p[PROJECT], p[ID]).execute()
    }

    private fun createSqlAdminService(): SQLAdmin {
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        var cred = GoogleCredential.getApplicationDefault()
        if (cred.createScopedRequired()) {
            cred = cred.createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
        }

        return SQLAdmin.Builder(httpTransport, JacksonFactory.getDefaultInstance(), cred).
        setApplicationName(CLOUD_BLASTER).build()
    }
}