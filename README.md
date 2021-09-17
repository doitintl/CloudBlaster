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
* It supports more complexity, since it is in Kotlin rather than bash. 

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
1. The first step, the Lister, does *not* delete assets; rather, it just lists assets in a file, `assets-to-delete.txt`,
that you review.
1. The Lister requires you to explicitly state a project. It does not implicitly use your `gcloud`  default project.
1. The Lister can be filtered (see `list-filter.properties` file) so that specified assets are skipped when 
building the `assets-to-delete.txt` file.
1. After running the Lister, you review and manually edit the list of assets for deletion, before running the Deleter.
 
## Instructions

### Prerequesites
* Install Maven

### Listing step
* Edit `list-filter.properties`. Write  regex for the asset types you don't want to list.
(This is a full-string match on the  local asset name, such as the Disk name  or Topic Name.
See the top of that file for detailed instructions.)
* Run `./lister.sh -p <GCP_PROJECT>` 
   * (In this script, Maven just builds if needed, then executes `java com.doitintl.blaster.Lister` ). 
   * The Lister outputs `assets-to-delete.txt`
   * (If instead you just want to print, to standard output, a list of *all* GCP assets, whether  of a type
   supported by Cloud Blaster or not, add the `-a` or `--print-all-assets` flag.)

### Deletion step
* Review `assets-to-delete.txt` and remove lines for any assets that you do not want to delete.
* Run `./deleter.sh` 
  * (In this script, Maven just builds if needed, then executes `com.doitintl.blaster.Deleter`.). 
  * The Deleter  deletes  assets listed in `assets-to-delete.txt`. 
  * You do not need to specify the project, as this is included in every asset path in  `assets-to-delete.txt`.
  * Note that some assets cannot be deleted, such as attached Disks or the default GAE Service.
   There is no harm in having them in `assets-to-delete.txt` -- you will just get an exception.
  * Deletion is executed concurrently, since it is slow and IO-bound.
## Features
* I focused on the common important asset types that are set up and torn down in typical development and QA.
    * This includes Google Compute Engine Instances and Disks, PubSub Topics and Subscriptions, 
    Google Kubernetes Engine  regional and zonal clusters,
    Google App Engine Services and Versions, and Google Cloud Storage Buckets.
    * For the most up-to-date list of supported asset types, see `list-filter.properties`
* If you want more services or asset types, please either
    * Or submit an issue at GitHub.
    * Add support for the asset type and submit a pull request. 
    * Add support for the asset type and submit a pull request. 
        * To do this, use existing asset types as an example.
        * Uncomment the asset type in `asset-types.properties`. See the documentation at the top of that file.
        * Add the asset type to `list-filter.yamls`.
        * Implement a subclass of `Abstract Deleter`.  

# Future features
* More asset types
* Track asset dependencies, so that if you want to delete asset A, but it is undeletable until 
asset B is gone, you delete B first, then A. 
* Better error messages in `malformed list-filter.properties` (and, though less important because it is 
not edited by users, in `asset-types.properties`)
* More runtime verification by user: "Are you sure. Still, we have to trust the user. A sloppy user will bypass
such checks, and a careful user already has the opportunity to edit the `assets-to-delete.txt`.
# Other projects and approaches
- [Safe Scrub](https://github.come/doitintl/SafeScrub) was an earlier bash-only project that does the same thing. 
- [Travis CI GCloud Cleanup](https://github.com/travis-ci/gcloud-cleanup) and [Bazooka](https://github.com/enxebre/bazooka) also delete GCE assets.
- [Cloud Nuke](https://blog.gruntwork.io/cloud-nuke-how-we-reduced-our-aws-bill-by-85-f3aced4e5876) does this for AWS.
 