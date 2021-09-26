# Cloud Blaster

Cloud Blaster helps you delete the unwanted assets in your Google Cloud Platform project, 
leaving it clean of confusing clutter and saving you money.

The advantages of Cloud Blaster:
* It supports more complexity, since it is in Kotlin rather than bash. This makes it easier to add new asset types
 and make other changes. Safe Scrub is at the limit of the complexity that can be accomodted in Bash.
 
Cloud Blaster has its own safety features, listed below. It also supports some of the most common asset types (see below),
with the possibility of easily adding more (see below).
 

## Use case
* It is intended for development and QA projects, where you want to start fresh at the end of the day or 
before a new test run.
* It is less likely to be useful for production projects, 
where you should use Terraform or other Infrastructure as Code. This will track all
assets so it later can delete just what was created.
* The use case for Cloud Blaster is the same as for Safe Scrub. (See below in this README.)

## Safety First 
To keep it safe, Cloud Blaster has these features.
1. The first step, the Lister, does *not* delete assets; rather, it just lists assets into
 a file, `asset-list.txt`, hat you review.
1. The Lister requires you to explicitly state a project. It does not implicitly use your `gcloud` default project.
1. The Lister can be filtered (see `list-filter.yaml` file) so that specified assets are skipped when 
building the `asset-list.txt` file.
1. After running the Lister, you review the list of assets for deletion
      * Manually edit it.
       * Add a comment line `# Ready to delete` to the top 
       * If you like danger, write a script to add this comment between steps.
       * Then run the Deleter. 

## Instructions

### Prerequisites
* Install Maven. We use it here to run the project, building it if needed, with a single command. You could also build the jars
and run them separately.
* If you wants to run the tests, install and initialize `gcloud`

### Listing the assets
* Edit `list-filter.yaml` (or another file whose name you will specify on the command line of Lister and Deleter.)
You can add filters for each asset type to specify assets that you don't or do wants to include in listing.s 
(See the top of that file for detailed instructions.)
* Run `./lister.sh -p <GCP_PROJECT>` 
   * (In `lister.sh`, Maven builds if needed, then executes `java com.doitintl.blaster.lister.Lister` .) 
   * The Lister outputs `asset-list.txt` (configurable with the `-o` flag).
   * Note:
       * If instead you want to print a list of *all* GCP assets, whether or not of a type
       supported by Cloud Blaster, add the `-n` flag. The default output file  
       for this is `all-types-assets-list.txt`,
       though you can set this value with the `-o` flag.
* Command line flags: Run `./lister.sh -h` 

### Deleting listed assets
* Review `asset-list.txt` (or the other file you plan to use) and remove lines for any assets 
that you do not want to delete.
* Add a comment `# Ready to delete` (or just add those words, case insensitive, to any comment line).
* Run `./deleter.sh` 
  * (In `deleter.sh`, Maven just builds if needed, then executes `com.doitintl.blaster.deleter.Deleter`.). 
  * The Deleter tries to delete the assets listed in `asset-list.txt` (configurable). 
* Notes:
  * You do not need to specify the project, as this is included in every asset path in `asset-list.txt`.
  * Note that some assets cannot be deleted, such as attached Disks or the default GAE Service; or where
  permission is not available.  There is no harm in having them in `asset-list.txt` -- you 
  will just get an exception.
  * For speed, deletion is executed concurrently.
* Command line flags: Run `./deleter.sh -h`

## Features
### Supported asset types
Cloud Blaster now supports common important asset types that are set up and torn down in typical development and QA. 
This includes: 
    * Google Compute Engine instances, disks, firewalls, and addresses
    * Google Cloud PubSub topics and Subscriptions 
    * Google Kubernetes Engine regional and zonal clusters
    * Google Cloud Operations log metrics
    * Google Cloud Functions
    * Cloud Run services
    * Cloud SQL instances
    * Google App Engine services and versions
    * Google Cloud Storage buckets
    
     For the most up-to-date list of supported asset types, see `list-filter.yaml`
    

### Future features
* More asset types.
* Track asset dependencies, so that if you want to delete asset A, but it is undeletable until 
asset B is gone, you delete B first, then A. 

### Adding features   
* If you want more asset types or new features, please either
    * Submit an issue at GitHub.
    * Or add support for the asset type and submit a pull request. 
         * For instructions see the comment in `asset-types.properties`. 
         * Uncomment the asset type in `asset-types.properties` and specify the deleter class here if needed. 
         Instructions at the top of that file.
         * Add the asset type to `list-filter.properties`. Optionally add a default filter as in the `Firewall` example there.
         * Implement a subclass of `BaseDeleter` alongside
          [the others](https://github.com/doitintl/CloudBlaster/tree/master/src/main/com/doitintl/blaster/deleters),
          which you can use as examples.
                                                     c, 
### Testing
* Run `tester.sh <PROJECT_ID>`
* This is an integration test rather than a unit test, which is why it is not in a
JUnit/TestNG suite that would be run on every CI build. 
* Integration tests exist for asset types in GKE, GCE Instances, Disks, Addresses, and Firewalls, 
GAE Services, GCS regional and multiregional Buckets, CloudRun Services, and PubSub Topics and Subscriptions.
* The test creates an asset of each type, confirms that it 
exists, then runs the deleter, then confirms that the asset does not exist.
* Since cloud operations can take several minutes each, it tests ach asset type in parallel
(grouping together some asset types like all GCE asset types)  
* Since it works with real cloud assets and blocks on each creation/deletion operation,
it is not robust. Permissions errors, slow response from the cloud, etc., can cause test failure. 
* The Lister/Deleter are normally run is more robust than the Test because
the GCP Asset Service only sees assets some time after they are created. 
Generally this is within seconds, but for some asset types, much longer. 
In normal usage, this is not a problem, since you don't clean up immediately, but the test
only waits a short time and if it does not see the asset, it will fail. 
     

## Other projects and approaches
- [Safe Scrub](https://github.com/doitintl/SafeScrub) was an earlier bash-only project that does the same thing. 
- [Travis CI GCloud Cleanup](https://github.com/travis-ci/gcloud-cleanup) and [Bazooka](https://github.com/enxebre/bazooka) also delete GCE assets.
- [Cloud Nuke](https://blog.gruntwork.io/cloud-nuke-how-we-reduced-our-aws-bill-by-85-f3aced4e5876) does this for AWS.
 
 ### Compared to Safe Scrub
 
 [Safe Scrub](https://github.com/doitintl/SafeScrub) is another project that does the same thing. 
 See [the blog post](https://blog.doit-intl.com/safe-scrub-clean-up-your-google-cloud-projects-f90f18aca311?source=friends_link&sk=bce56e27b568c8209f3da94eac17099f)
 for an explanation of Safe Scrub.
 
 The advantages of Safe Scrub over Cloud Blaster:
 * Safe Scrub does not delete anything. It just outputs a Bash script with a simple list of `delete` statements. 
 You then review it and run it.
 * Safe Scrub is in pure Bash. You may have more confidence as you see the code that it is running, without a compilation step. 
 * Safe Scrub supports more asset types (for now). It supports these asset types that are not supported by Cloud Blaster:
      * GCE routes, instance templates, networks, routers, and load balancers, along with a variety of subassets within LBs
      * GAE firewall rules
      * PubSub snapshots