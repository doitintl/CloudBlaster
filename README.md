# Cloud Blaster

Cloud Blaster helps you delete the unwanted resources in your Google Cloud Platform project, 
leaving it clean of confusing clutter and saving you money.

# Compared to Safe Scrub

[Safe Scrub](https://github.come/doitintl/SafeScrub) was an earlier project that does the same thing. 
See [the blog post](https://blog.doit-intl.com/safe-scrub-clean-up-your-google-cloud-projects-f90f18aca311)
for an explanation of Safe Scrub

The advantages of Safe Scrub over Cloud Blaster:
* Safe Scrub  is in pure bash. You may have more confidence as you see just the code that it running.
* It does not delete anything. It just outputs a bash script with a simple list of `delete` statements 
that does the deletion. You then review it and run it as code that you generated.

The advantages of Cloud Blaster:
* It supports more complexity, since it is in Kotlin rather than bash. 

Though Cloud Blaster is not as transparent as Safe Scrub's bash, it has safety features, listed below.
 
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
1. You can filter to choose the resources that you want to *not* list for potential deletion, using 
the  `list-filter.yaml` file, 
1. And after that, you review and manually edit the list of assets for deletion, just to be sure.
 
## Instructions

### Listing step
* Edit `list-filter.yaml`. Write  regex for the asset types you don't want to list for deletion
(Full-string match on the  local asset name, like Disk name  or Topic Name.)
See the top of that yaml for detailed instructions. 
* Run `./lister.sh -p  <GCP_PROJECT>` 
   * (With this script, Maven just builds if needed, then executes `java com.doitintl.blaster.Lister -p <GCP_PROJECT>` ). 
   * The Lister outputs `assets-to-delete.txt`
   * (If you just want to print, to standard output, a list of *all* GCP assets, whether  of a type
supported for deletion by Cloud Blaster or not, add 
the `--print-all-assets` flag.)

### Deletion step
* Review `assets-to-delete.txt` and remove lines for any resources that you do not want to delete.
* (With this script, Maven just builds if needed, then executes `com.doitintl.blaster.Lister -p <GCP_PROJECT>` ). 
* Run `./deleter.sh -p  <GCP_PROJECT>` 
  * (With this script, Maven just builds if needed, then executes `com.doitintl.blaster.Deleter` ). 
  * The Deleter  deletes  resources listed in `assets-to-delete.txt`. 
  * You do not need to specify the project, as this is included in every asset path in  `assets-to-delete.txt`.
  * Note that some resources cannot be deleted, such as attached disks or the default GAE service.
   There is no harm in having them in `assets-to-delete.txt` -- you will just get an exception.

## Features
* I focused on the common important resource types that are set up and torn down in typical development and QA.
    * This includes Google Compute Engine Instances and Disks, PubSub Topics and Subscriptions, 
    Google Kubernetes Engine  regional and zonal clusters,
    Google App Engine Services and Versions, and Google Cloud Storage Buckets.
    * For the most up-to-date list of supported asset types, see `lister-filter.yaml`
* If you want more services or resource types, please either
    * Submit a pull request. Use the `com.doitintl.blaster.deleter.XDeleter` classes as exampes. 
    Copy one, and implement `doDelete` to call the relevant Google API. 
    * Or submit an issue at GitHub.

# Other projects and approaches
- [Safe Scrub](https://github.come/doitintl/SafeScrub) was an earlier bash-only project that does the same thing. 
- [Travis CI GCloud Cleanup](https://github.com/travis-ci/gcloud-cleanup) and [Bazooka](https://github.com/enxebre/bazooka) also delete GCE resources.
- [Cloud Nuke](https://blog.gruntwork.io/cloud-nuke-how-we-reduced-our-aws-bill-by-85-f3aced4e5876) does this for AWS.
 