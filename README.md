# CloudBlaster

CloudBlaster helps you delete the unwanted resources in your Google Cloud Platform project, 
leaving it clean of confusing clutter and saving you money.

# TODO
1. Bundle app in container, wqith scripts and instructions for running.
1. Drdering of dependent resources, e.g. GAE Service, Version, Instace

# Safe Scrub

[Safe Scrub](https://github.come/doitintl/SafeScrub) was an earlier project that does the same thing. See [the blog post](https://blog.doit-intl.com/safe-scrub-clean-up-your-google-cloud-projects-f90f18aca311)
                                                                                                     for an explanation.

The advantages of Safe Scrub:
* It is in pure bash. You see exactly what it is doing. 
* It does not delete anything. It just outputs a bash script (with `delete` statements) that does the deletion. 
You then review it and run it as code that you wrote (actually, generated), so you see
exactly what it does

The advantages of CloudBlaster:
* It supports more complexity, as it is in Java/Kotlin rather than bash. For example,
if you want to add lots of asset types, some with special deletion commands (e.g., regional
assets), you need a language that can support that.
* Though CloudBlaster is not as transparent as Safe Scrub's bash, it has safety features, listed below.
 
Other than that, the use case is the same.

## Use case
- It is intended for development and QA projects, where you want to start fresh at the end of the day or before a new test run.
- It is unlikely to be useful for production projects, where you should determine the potential dependencies between components before deleting
anything.

## Safety First 
To keep it safe, CloudBlaster has these features.
1. The first step, the Lister, does *not* delete resources; rather, it just lists resources in a file that you can review.
1. It requires you to explicitly state a project, to avoid accidentally listing resources that come from
 a default project.
1.  The Lister and the Deleter require that you  specify the project in the deletion script, so that deletion is not run against  your current default project.
1. CloudBlaster supports the filtering by regex in the `list-filter.yaml` file, so you only 
list files relevant for deletion. And after that, you review and manually edit the list of assets for deletion, just to be sure.
 
## Instructions

### Listing step
* Edit `list-filter.yaml`. Include the asset types you want to list, and a regex that indicates which
 assets to list. (Full-string match on their  local asset name, like Disk name  or Topic Name.)
See the top of that yaml for detailed instructions. 
* Run `java com.doitintl.blaster.Lister -p <GCP_PROJECT>`. This outputs `assets-to-delete.txt`.

* (If you just want to print, to standard output, a list of *all* GCP assets, whether 
supported for deletion by Cloud Blaster or not, use
the `--print-all-assets` flag.)

### Deletion step
* Review `assets-to-delete.txt` and remove lines for any resources that you want to keep.
* Run `java com.doitintl.blaster.Deleter` to delete all resources listed in `assets-to-delete.txt`.
## Features
- I focused on the common important resource types that are set up and torn down
 in typical development and QA.
- If you want more services or resource types, please submit a pull request or issue at GitHub.

## Usage
 For usage text,  
- run `java com.doitintl.cloudblaster.Lister`.
- or  `java com.doitintl.cloudblaster.Deleter`.

# Other projects and approaches
- [Safe Scrub](https://github.come/doitintl/SafeScrub) was an earlier bash-only project that does the same thing. 
- [Travis CI GCloud Cleanup](https://github.com/travis-ci/gcloud-cleanup) and [Bazooka](https://github.com/enxebre/bazooka) also delete GCE resources.
- [Cloud Nuke](https://blog.gruntwork.io/cloud-nuke-how-we-reduced-our-aws-bill-by-85-f3aced4e5876) does this for AWS.
 