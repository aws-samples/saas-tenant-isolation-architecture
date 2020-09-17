# Templates

This directory is used to bootstrap the CodeCommit repository.
It contains IAM policy templates, plus a `buildspec.yml`
The buildspec file is used by AWS Code Build. It will create a zip file of the templates.
This zipped file is uploaded to S3, where it can be accessed by the TokenVendingLambda
