STACK_NAME?="hmb29"

DEPLOYMENT_S3_BUCKET?=""
TEMPLATE_BUCKET_NAME?=""
TEMPLATE_OBJECT_KEY="example-templates.zip"

LAYER_JAR="TokenVendingLayer.jar"

packaged.yaml: template.yaml
	aws cloudformation package \
		--s3-bucket $(DEPLOYMENT_S3_BUCKET) \
		--s3-prefix $(STACK_NAME) \
		--template-file template.yaml \
		--output-template-file packaged.yaml

deploy: packaged.yaml templates.zip upload-lambda-layer
	aws cloudformation deploy \
		--stack-name $(STACK_NAME) \
		--template-file packaged.yaml \
		--capabilities CAPABILITY_IAM \
		--parameter-overrides \
			TemplateBucketName=$(TEMPLATE_BUCKET_NAME) \
			TemplateObjectKey=$(TEMPLATE_OBJECT_KEY) \
			DeploymentS3Bucket=$(DEPLOYMENT_S3_BUCKET)


templates.zip: templates/*
	zip -r templates.zip templates/*.json
	zip -j templates.zip templates/buildspec.yaml
	zip -j templates.zip templates/readme.md
	aws s3 cp templates.zip s3://$(TEMPLATE_BUCKET_NAME)/$(TEMPLATE_OBJECT_KEY)

build:
	mvn clean install

build-layer:
	cd TokenVendingLayer && mvn clean install

upload-lambda-layer:
	aws s3 cp TokenVendingLayer/target/$(LAYER_JAR)  s3://$(DEPLOYMENT_S3_BUCKET)/$(LAYER_JAR)

clean: 
	# Delete bootstrapped templates.zip
	aws s3 rm s3://$(TEMPLATE_BUCKET_NAME)/$(TEMPLATE_OBJECT_KEY)

	# Delete objects in Artefact bucket
	aws s3 rm s3://`aws cloudformation describe-stacks --stack-name $(STACK_NAME) --query "Stacks[0].Outputs[?OutputKey=='ArtefactBucketName'].OutputValue" --output text` --recursive
