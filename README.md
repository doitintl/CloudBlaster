# Cloud Blaster

Cloud Blaster helps you delete the unwanted resources in your Google Cloud Platform project, 
leaving it clean of confusing clutter and saving you money.

# Compared to Safe Scrub

[Safe Scrub](https://github.come/doitintl/SafeScrub) is  another project that does the same thing. 
See [the blog post](https://blog.doit-intl.com/safe-scrub-clean-up-your-google-cloud-projects-f90f18aca311)
for an explanation of Safe Scrub

The advantages of Safe Scrub over Cloud Blaster:
* Safe Scrub  is in pure Bash. You may have more confidence as you see the code that it running, without a compilation step. 
* Safe Scrub  does not delete anything. It just outputs a Bash script with a simple list of `delete` statements. 
You then review it and run it.

The advantages of Cloud Blaster:
* It supports more complexity, since it is in Kotlin rather than bash. 

Cloud Blaster has its own safety features, listed below.
 
Other than that, the use case is the same.

## Use case
- It is intended for development and QA projects, where you want to start fresh at the end of the day or before a new test run.
- It is less likely to be useful for production projects, where you should determine the potential dependencies between components before deleting
anything.

## Safety First 
To keep it safe, Cloud Blaster has these features.
1. The first step, the Lister, does *not* delete resources; rather, it just lists resources in a file that you can review.
1. The Lister requires you to explicitly state a project, to avoid accidentally listing resources that come from
 a default project.
1. The Lister can be filtered (see `list-filter.yaml` file) so that certain assets are skipped when 
building the `to-be-deleted.txt` file.
1. After running the Lister, you review and manually edit the list of assets for deletion, before running the Deleter.
 
## Instructions

### Listing step
* Edit `list-filter.yaml`. Write  regex for the asset types you don't want to list.
(This is a full-string match on the  local asset name, like the Disk name  or Topic Name.
See the top of that yaml for detailed instructions.)
* Run `./lister.sh -p <GCP_PROJECT>` 
   * (In this script, Maven just builds if needed, then executes `java com.doitintl.blaster.Lister` ). 
   * The Lister outputs `assets-to-delete.txt`
   * (If instead you just want to print, to standard output, a list of *all* GCP assets, whether  of a type
supported for deletion by Cloud Blaster or not, add the `-a` or `--print-all-assets` flag.)

### Deletion step
* Review `assets-to-delete.txt` and remove lines for any resources that you do not want to delete.
* Run `./deleter.sh` 
  * (In this script, Maven just builds if needed, then executes `com.doitintl.blaster.Deleter`.). 
  * The Deleter  deletes  resources listed in `assets-to-delete.txt`. 
  * You do not need to specify the project, as this is included in every asset path in  `assets-to-delete.txt`.
  * Note that some resources cannot be deleted, such as attached Disks or the default GAE Service.
   There is no harm in having them in `assets-to-delete.txt` -- you will just get an exception.

## Features
* I focused on the common important resource types that are set up and torn down in typical development and QA.
    * This includes Google Compute Engine Instances and Disks, PubSub Topics and Subscriptions, 
    Google Kubernetes Engine  regional and zonal clusters,
    Google App Engine Services and Versions, and Google Cloud Storage Buckets.
    * For the most up-to-date list of supported asset types, see `list-filter.yaml`
* If you want more services or resource types, please either
    * Or submit an issue at GitHub.
    * Add support and submit a pull request. 
        * To do this, use existing asset types as an example.
        * Uncomment the asset type in `asset-types.yaml`. See the documentation at the top of that file.
        * Add the asset type to `list-filter.yamls`.
        * Implement a subclass of `Abstract Deleter`.  

# Other projects and approaches
- [Safe Scrub](https://github.come/doitintl/SafeScrub) was an earlier bash-only project that does the same thing. 
- [Travis CI GCloud Cleanup](https://github.com/travis-ci/gcloud-cleanup) and [Bazooka](https://github.com/enxebre/bazooka) also delete GCE resources.
- [Cloud Nuke](https://blog.gruntwork.io/cloud-nuke-how-we-reduced-our-aws-bill-by-85-f3aced4e5876) does this for AWS.
 