# Cloud Blaster

Cloud Blaster helps you delete the unwanted assets in your Google Cloud Platform project, 
leaving it clean of confusing clutter and saving you money.

# Compared to Safe Scrub

[Safe Scrub](https://github.come/doitintl/SafeScrub) is  another project that does the same thing. 
See [the blog post](https://blog.doit-intl.com/safe-scrub-clean-up-your-google-cloud-projects-f90f18aca311)
for an explanation of Safe Scrub.

The advantages of Safe Scrub over Cloud Blaster:
* Safe Scrub  does not delete anything. It just outputs a Bash script with a simple list of `delete` statements. 
You then review it and run it.
* Safe Scrub  is in pure Bash. You may have more confidence as you see the code that it is running, without a compilation step. 
* Safe Scrub supports more asset types (for now).

The advantages of Cloud Blaster:
* It supports more complexity, since it is in Kotlin rather than bash. This makes it easier to  add new asset types
 and make other changes. Safe Scrub is at the limit of the complexity that can be accomodted in Bash.
 
Cloud Blaster has its own safety features, listed below. It also supports some of the most common asset types (see below),
with the possibility of easily adding more (see below).
 

## Use case
* The use case for Cloud Blaster is the same as for Safe Scrub.
* It is intended for development and QA projects, where you want to start fresh at the end of the day or 
before a new test run.
* It is less likely to be useful for production projects, where you should determine the potential dependencies 
between components before deleting anything.

## Safety First 
To keep it safe, Cloud Blaster has these features.
1. The first step, the Lister, does *not* delete assets; rather, it just lists assets in a file, `asset-list.txt`,
that you review.
1. The Lister requires you to explicitly state a project. It does not implicitly use your `gcloud`  default project.
1. The Lister can be filtered (see `list-filter.yaml` file) so that specified assets are skipped when 
building the `asset-list.txt` file.
1. After running the Lister, you review and manually edit the list of assets for deletion, before running the Deleter.
 
## Instructions

### Prerequisites
* Install Maven

### Listing the assets
* Edit `list-filter.yaml` (configurable). You can add filters to specify assets that you don't 
(See the top of that file for detailed instructions.)
* Run `./lister.sh -p <GCP_PROJECT>` 
   * (In `lister.sh`, Maven builds if needed, then executes `java com.doitintl.blaster.lister.Lister` .) 
   * The Lister outputs `asset-list.txt` (configurable)
   * Note:
       * If instead you   want to print  a list of *all* GCP assets, whether or not of a type
       supported by Cloud Blaster, add the `-n` flag.
* Command line flags: Run `./lister.sh -h`  
### Deleting listed assets
* Review `asset-list.txt` (or the other file you plan to use) and remove lines for any assets 
that you do not want to delete.
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
* I focused on the common important asset types that are set up and torn down in typical development and QA.  This includes 
    * Google Compute Engine instances, disks, and firewalls
    * Google Cloud PubSub topics and Subscriptions 
    * Google Kubernetes Engine regional and zonal clusters
    * Google Cloud Operations log metrics
    * Google Cloud Functions
    * Cloud Run services
    * Google App Engine services and versions
    * Google Cloud Storage buckets
    * Vertex AI Training pipelines
    
     For the most up-to-date list of supported asset types, see `list-filter.yaml`
    
* If you want more asset types or new features, please either
    * Submit an issue at GitHub.
    * Or add support for the asset type and submit a pull request. 
        * To do this, see the  comment in `asset-types.properties`.

# Future features
* More asset types.
* Track asset dependencies, so that if you want to delete asset A, but it is undeletable until 
asset B is gone, you delete B first, then A. 
* Runtime verification by user along the lines of "Are you sure". Still, we have to trust the user. 
A sloppy user will bypass such checks, and a careful user already has the opportunity 
to edit  `asset-list.txt`.

# Other projects and approaches
- [Safe Scrub](https://github.come/doitintl/SafeScrub) was an earlier bash-only project that does the same thing. 
- [Travis CI GCloud Cleanup](https://github.com/travis-ci/gcloud-cleanup) and [Bazooka](https://github.com/enxebre/bazooka) also delete GCE assets.
- [Cloud Nuke](https://blog.gruntwork.io/cloud-nuke-how-we-reduced-our-aws-bill-by-85-f3aced4e5876) does this for AWS.
 