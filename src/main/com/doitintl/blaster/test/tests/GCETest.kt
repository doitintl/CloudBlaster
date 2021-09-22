package com.doitintl.blaster.test.tests


import com.doitintl.blaster.deleter.GCEBaseDeleter.Companion.getComputeService
import com.doitintl.blaster.deleter.GCEBaseDeleter.Companion.waitOnGlobalOperation
import com.doitintl.blaster.deleter.GCEBaseDeleter.Companion.waitOnZoneOperation
import com.doitintl.blaster.shared.randomString
import com.doitintl.blaster.test.TestBase
import com.google.api.services.compute.model.*
import com.google.api.services.compute.model.Firewall.Allowed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class GCETest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf(
        "compute.googleapis.com/Firewall",
        "compute.googleapis.com/Disk",
        "compute.googleapis.com/Instance",
        "compute.googleapis.com/Address",
    )

    override fun createAssets(sfx: String, project: String): List<String> {

        val diskName = assetName("disk")
        val instanceName = assetName("instance")
        val firewallName = assetName("firewall")
        val addressName = assetName("address")
        val creations = listOf(
            { createInstance(project, instanceName) },
            { createDisk(project, diskName) },
            { createFirewall(project, firewallName) },
            { createAddress(project, addressName) })

        runBlocking {
            creations.forEach { creation ->
                launch(Dispatchers.IO) {
                    creation()
                }
            }
        }

        return listOf(firewallName, diskName, instanceName, addressName)
    }

    private fun createAddress(project: String, addressName: String) {

        val address = Address().setName(addressName)

        val operation = getComputeService().addresses().insert(project, addressName, address).execute()

        waitOnGlobalOperation(project, operation)

    }

    private fun createFirewall(project: String, fwName: String) {

        val fw = Firewall().setName(fwName)
        fw.allowed = listOf(Allowed().setIPProtocol("icmp"))
        val operation = getComputeService().firewalls().insert(project, fw).execute()

        waitOnGlobalOperation(project, operation)

    }


    private fun createInstance(project: String, instanceName: String) {
        val location = "us-central1-c"

        val config = AccessConfig().setType("ONE_TO_ONE_NAT").setName("External NAT")

        val ifc = NetworkInterface().setNetwork(
            "https://www.googleapis.com/compute/v1/projects/$project/global/networks/default"
        ).setAccessConfigs(listOf(config))

        // We use a nondefault name. Otherwise the deleter will try to delete this boot disk and fail (though with no functional harm)
        val diskName = "boot" + randomString()
        val params = AttachedDiskInitializeParams().setDiskName(diskName).setSourceImage(
            "https://www.googleapis.com/compute/v1/projects/" +
                    "ubuntu-os-cloud/global/images/ubuntu-2004-focal-v20200529"
        ).setDiskType(
            "https://www.googleapis.com/compute/v1/projects/$project/zones/$location/diskTypes/pd-standard"
        )

        val disk = AttachedDisk().setBoot(true).setAutoDelete(true).setType("PERSISTENT").setInitializeParams(params)

        val instance = Instance().setName(instanceName).setMachineType(
            "https://www.googleapis.com/compute/v1/projects/$project/zones/$location/machineTypes/e2-standard-2",
        ).setNetworkInterfaces(listOf(ifc)).setDisks(listOf(disk))


        val operation = getComputeService().instances().insert(project, location, instance).execute()

        waitOnZoneOperation(project, location, operation)
    }

    private fun createDisk(project: String, diskName: String) {
        val location = "us-central1-c"

        val disk = Disk().setName(diskName).setZone(String.format("projects/$project/zones/$location"))
        val operation = getComputeService().disks().insert(project, location, disk).execute()
        waitOnZoneOperation(project, location, operation)
    }
}




