package com.doitintl.blaster.test.tests


import com.doitintl.blaster.deleters.GCEAbstractDeleter.Companion.createComputeService
import com.doitintl.blaster.shared.randomString
import com.doitintl.blaster.test.TestBase
import com.google.api.services.compute.Compute
import com.google.api.services.compute.model.*
import com.google.api.services.compute.model.Firewall.Allowed
import java.lang.Thread.sleep


/**
 * This test generates exception messages on an attempt to delete  the attached disk. But that does not affect functionality.
 */
class GCETest(project: String) : TestBase(project) {

    override fun assetTypeIds(): List<String> = listOf("compute.googleapis.com/Firewall",
            "compute.googleapis.com/Disk", "compute.googleapis.com/Instance")

    override fun createAssets(sfx: String, project: String): List<String> {

        val diskName = assetName("disk")
        val instanceName = assetName("instance")
        val firewallName = assetName("firewall")
        createInstance(project, instanceName)
        createDisk(project, diskName)
        createFirewall(project, firewallName)
        return listOf(firewallName, diskName, instanceName)
    }

    private fun createFirewall(project: String, fwName: String) {

        val fw = Firewall().setName(fwName)
        fw.allowed = listOf(Allowed().setIPProtocol("icmp"))
        val compute = createComputeService()
        val operation = compute.firewalls().insert(project, fw).execute()
        while (true) {
            val currentOperation: Operation = compute
                    .globalOperations()
                    .get(project, operation.name)
                    .execute()
            if (currentOperation.status == "DONE") {
                return
            }
            sleep(500)
        }
    }

    private fun createInstance(project: String, instanceName: String) {
        val location = "us-central1-c"

        val config = AccessConfig().setType("ONE_TO_ONE_NAT").setName("External NAT")

        val ifc = NetworkInterface().setNetwork(
                "https://www.googleapis.com/compute/v1/projects/$project/global/networks/default").setAccessConfigs(listOf(config))

        // We use a nondefault name. Otherwise the deleter will try to delete this boot disk and fail (though with no functional harm)
        val diskName = "boot" + randomString()
        val params = AttachedDiskInitializeParams().setDiskName(diskName).setSourceImage(
                "https://www.googleapis.com/compute/v1/projects/" +
                        "ubuntu-os-cloud/global/images/ubuntu-2004-focal-v20200529").setDiskType(
                "https://www.googleapis.com/compute/v1/projects/$project/zones/$location/diskTypes/pd-standard")

        val disk = AttachedDisk().setBoot(true).setAutoDelete(true).setType("PERSISTENT").setInitializeParams(params)

        val instance = Instance().setName(instanceName).setMachineType(
                "https://www.googleapis.com/compute/v1/projects/$project/zones/$location/machineTypes/e2-standard-2",
        ).setNetworkInterfaces(listOf(ifc)).setDisks(listOf(disk))


        val compute = createComputeService()
        val insert = compute.instances().insert(project, location, instance)
        val operation = insert.execute()

        waitForCreation(compute, project, location, operation)

    }

    private fun createDisk(project: String, diskName: String) {
        val location = "us-central1-c"

        val disk = Disk().setName(diskName).setZone(String.format("projects/$project/zones/$location"))
        val compute = createComputeService()
        val operation = compute.disks().insert(project, location, disk).execute()
        waitForCreation(compute, project, location, operation)
        return
    }

    private fun waitForCreation(compute: Compute, project: String, location: String, operation: Operation) {
        while (true) {
            val currentOperation: Operation = compute
                    .zoneOperations()
                    .get(project, location, operation.name)
                    .execute()
            if (currentOperation.status == "DONE") {
                return
            }
            sleep(500)
        }
    }


}


