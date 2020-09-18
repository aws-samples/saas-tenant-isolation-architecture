# Govern access to tenants’ data in your SaaS application using AWS IAM

This repository contains a small application to demonstrate the principles discussed in the blogpost [Govern access to tenants’ data in your SaaS application using AWS IAM]().

What is the problem this work aims to solve? Ideally IAM roles should be scoped to the minimum set of resource and actions permissions required. In a multi-tenant application, the combination of multiple tenants and services can result in a high number of roles to manage.

## Overview

This project contains source code and supporting files to deploy the application. It implements two lambda functions and a DynamoDB table.

The `Description Service` demonstrates a small microservice used by multiple tenants of an application. The ExportTenantData function accesses some tenant data from a multi-tenant DynamoDB table and writes it to an multi-tenant S3 bucket.

TODO Add section explaining the Token Vending Machine, assuming role with an inline policy etc

### Template Pipeline

Templated IAM Policies are stored and versioned using AWS CodeCommit.
An AWS CodePipeline executes on change to the repository.
The IAM policy templates contained in `templates/` are zipped and uploaded to an S3 bucket.

## Deploy the sample application

This project uses AWS SAM.

- Install prerequisites
- Clone repository
- Fill env vars
- Deploy

The application uses several AWS resources, including Lambda functions and an API Gateway API. These resources are defined in the `template.yaml` file in this project.

The Serverless Application Model Command Line Interface (SAM CLI) is an extension of the AWS CLI that adds functionality for building and testing Lambda applications. It uses Docker to run your functions in an Amazon Linux environment that matches Lambda.
To use the SAM CLI, you need the following tools.

- AWS CLI - [Install the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html) and [configure it with your AWS credentials].
- Java8 - [Install the Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

To build and deploy your application for the first time, run the following in your shell:

```bash
export STACK_NAME=<Define the stack name>

# Packaged templates are uploaded to an S3 bucket.
export DEPLOYMENT_S3_BUCKET="$STACK_NAME"-packaging-bucket

# The S3 Bucket containing the zipped policy templates to be used
export TEMPLATE_BUCKET_NAME=<tempalte s3 bucket>
export TEMPLATE_OBJECT_KEY=<template zip file>

# Create the bucket if it doesn't exist
# aws s3 mb s3://$DEPLOYMENT_S3_BUCKET

# Build the source
make build

# Deploy
make deploy
```

The first command will build the source of your application.
The second command will package the application and deploy your application to AWS.

You can find the Description Service API Gateway Endpoint URL in the output values displayed after deployment.

### Testing the Application

TODO

## Use the SAM CLI to build and test locally

Build your application with the `sam build --use-container` command.

```bash
test$ sam build --use-container
```

## Cleanup

To delete the sample application that you created, use the AWS CLI. Assuming you used your project name for the stack name, you can run the following:

```bash
aws cloudformation delete-stack --stack-name <STACK NAME>
```

## License
