# Cloud Blaster

Cloud Blaster helps you delete the unwanted assets in your Google Cloud Platform project, 
leaving it clean of confusing clutter and saving you money.

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
 a file, `asset-list.txt`, that you review.
1. The Lister requires you to explicitly state a project. It does not implicitly use your `gcloud` default project.
1. The Lister can be filtered (see `config/list-filter.yaml`) so that specified assets are skipped when 
building the `asset-list.txt` file.
1. After running the Lister, you review the list of assets for deletion
    * Manually edit it.
    * Add a comment line `# Ready to delete` to the top. 
        * (If you like danger, write a script to add this comment between steps.)
    * Then run the Deleter. 
1. The two-step process also means that if an asset is created between steps, it will not be deleted.

## Instructions

### Prerequisites

* Install and initialize `gcloud`. This tool provides authentication, though  it is used to give commands only in the tests. 

### Building and running it with Maven
* The following instructions explain how to do it using Maven.
* Install Maven. We use it here to run the project, building it if needed, with a single command.  
* You can also use a Docker image if you want to avoid Maven.  See below.

#### Listing the assets
* Edit `config/list-filter.yaml` (or another file whose name you will specify on the command line of Lister and Deleter.)
You can add filters for each asset type to specify assets that you don't or do wants to include in listing.s 
(See the top of that file for detailed instructions.)
* To build if needed, followed by running the Lister, run  `./mvn_build_and_run_lister.sh -p <GCP_PROJECT>` 
   * The Lister outputs `asset-list/asset-list.txt` (configurable with the `-o` flag).
   * To list  *all* GCP assets.
       * Add the `-n` flag to list all assets, whether or not of a type supported by Cloud Blaster,   
       * The default output file for this is `all-types-assets-list.txt`. 
           * You can set different file for output with the `-o` flag.
* To see command line flags: Run `./build-and-run-lister -h` 

#### Deleting listed assets
* Review `asset-list/asset-list.txt` (or the other file you plan to use) and remove lines for any assets 
that you do not want to delete.
* Add a comment `# Ready to delete` (or just add those words, case insensitive, to any comment line).
* * To build  if needed, followed by running the Lister, run `./mvn_build_and_run_deleter.sh` 
  * (In this script, Maven  builds if needed, then executes `com.doitintl.blaster.deleter.Deleter`.). 
  * The Deleter tries to delete the assets listed in `asset-list.txt` (configurable). 
* Notes:
  * You do not need to specify the project, as this is included in every asset path in `asset-list.txt`.
  * Note that some assets cannot be deleted, such as attached Disks or the default GAE Service; or where
  permission is not available.  There is no harm in having them in `asset-list.txt` -- you 
  will just get an exception.
  * For speed, deletion is executed concurrently.
* To see, command line flags: Run`./build-and-run-deleter.sh -h`

### Building and running it with Docker
* Install Docker as a prerequisite.
* Run the following  scripts from the  `scripts` folder.
* To build, run `./build_docker.sh`
* Lister: In the `scripts` folder, run `./in_docker_lister.sh`. See [above](#listing-the-assets) for tips 
on the command-line options and on the output.
* Deleter: In the `scripts` folder, run `./in_docker_deleter.sh`. See [above](#deleting-listed-assets) for tips on the command-line options,
and editing the asset-list file to indicate readiness.



## Features
### Supported asset types
Cloud Blaster now supports common asset types that are set up and torn down in typical development and QA.
* This includes:
     * Google Compute Engine instances, disks, firewalls, and addresses
     * Google Cloud PubSub topics and Subscriptions 
     * Google Kubernetes Engine regional and zonal clusters
     * Google Cloud Operations log metrics
     * Google Cloud Functions
     * Cloud Run services
     * Cloud SQL instances
     * Google App Engine services and versions
     * Google Cloud Storage buckets
    
* For the most up-to-date list of supported asset types, see `config/list-filter.yaml`.
    
### Future features
* More asset types.
* Track asset dependencies, so that if you want to delete asset A, but it is undeletable until 
asset B is gone, the Deleter deletes B first, then A. 

### Adding features   
* If you want more asset types or new features, please either
    * Submit an issue at GitHub.
    * Or add support for the asset type and submit a pull request. 
         * For instructions see the comment in `asset-types.properties`. 
         * Uncomment the asset type in `asset-types.properties` and specify the deleter class here if needed. 
         Instructions at the top of that file.
         * Add the asset type to `config/list-filter.yaml`. Optionally add a default filter as in the `Firewall` example there.
         * Implement a subclass of `BaseDeleter` alongside
          [the others](https://github.com/doitintl/CloudBlaster/tree/master/src/main/com/doitintl/blaster/deleters),
          which you can use as examples.
                                                      
### Testing
* Run `tester.sh <PROJECT_ID>`
* This is an integration test rather than a unit test, which is why it is not in a
suite (JUnit/TestNG) that would be run on every CI build. 
* Code coverage is 90% as of 2021-09-29
* Integration tests exist for asset types in 
   * GKE
   * GCE Instances, Disks, Addresses, and Firewalls,
   * GAE Services
   * GCS regional and multiregional Buckets
   * CloudRun Services
   * PubSub Topics and Subscriptions.
   * Cloud Functions
* Not covered by the automated test (but tested manually)
   * LoggingMetrics,  because they take up to 7 hours to appear in the Asset Service,
    [as documented](https://cloud.google.com/asset-inventory/docs/supported-asset-types#:~:text=Cloud%20Logging).
   * GAE Versions, because they are subsumed by GAE Services (See `GAEServiceTest` for comment)
   * SQL Instances, because these take 10 minutes to be created.
* The test creates an asset of each type, confirms that it 
exists, then runs the deleter, then confirms that the asset does not exist.
* Since cloud operations can take several minutes each, it tests each asset type in parallel
(grouping together some, like the GCE asset types)  
* Since it works with real cloud assets and blocks on each creation/deletion operation,
it is not robust. Permissions errors, extremely slow response from the cloud, etc., can cause test failure. 
* The functional code, the Lister and Deleter, are more robust than the Test because
the GCP Asset Service only sees assets some time after they are created. 
Generally this is within seconds, but for some asset types, much longer. 
In normal usage, this is not a problem, since you don't usually clean up immediately, but the test
only waits a short time and if it does not see the asset, it will fail. 
     

## Other projects and approaches
- [Safe Scrub](https://github.com/doitintl/SafeScrub) was an earlier bash-only project that does the same thing. 
- [Travis CI GCloud Cleanup](https://github.com/travis-ci/gcloud-cleanup) and [Bazooka](https://github.com/enxebre/bazooka) also delete GCE assets.
- [Cloud Nuke](https://blog.gruntwork.io/cloud-nuke-how-we-reduced-our-aws-bill-by-85-f3aced4e5876) does this for AWS.
- [Sandbox Projects](https://help.doit-intl.com/cloud-sandbox-management/create-gcp-sandbox-accounts) are available at no charge to customers of DoiT International. 
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
      * App Engine firewall rules (but does support the more common GCE firewall rules)
      * PubSub snapshots
      
      
 The advantages of Cloud Blaster:
  * It supports more complexity, since it is in Kotlin rather than bash. This makes it easier to add new asset types
       and make other changes. Safe Scrub is at the limit of the complexity that can be accomodted in Bash.
       