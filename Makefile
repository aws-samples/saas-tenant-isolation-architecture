STACK_NAME="hmb29"

TEMPLATE_BUCKET_NAME="svozza-local-cfn"
TEMPLATE_OBJECT_KEY="example-templates.zip"

BUCKET="svozza-local-cfn"

LAYER_JAR="TokenVendingLayer.jar"

ARTEFACT_BUCKET=""

packaged.yaml: template.yaml
	aws cloudformation package \
		--s3-bucket $(BUCKET) \
		--s3-prefix $(STACK_NAME) \
		--template-file template.yaml \
		--output-template-file packaged.yaml

deploy: packaged.yaml templates.zip
	aws cloudformation deploy \
		--stack-name $(STACK_NAME) \
		--template-file packaged.yaml \
		--capabilities CAPABILITY_IAM \
		--parameter-overrides \
			TemplateBucketName=$(TEMPLATE_BUCKET_NAME) \
			TemplateObjectKey=$(TEMPLATE_OBJECT_KEY) \
			DeploymentS3Bucket=$(BUCKET)


templates.zip: templates/*
	zip -r templates.zip templates/*.json
	zip -j templates.zip templates/buildspec.yaml
	zip -j templates.zip templates/readme.md
	aws s3 cp templates.zip s3://$(TEMPLATE_BUCKET_NAME)/$(TEMPLATE_OBJECT_KEY)

build-java:
	mvn clean install

build-layer:
	cd TokenVendingLayer && mvn clean install

upload-lambda-layer: build-layer
	aws s3 cp TokenVendingLayer/target/$(LAYER_JAR)  s3://$(BUCKET)/$(LAYER_JAR)

clean: 
	# Delete bootstrapped templates.zip
	aws s3 rm s3://$(TEMPLATE_BUCKET_NAME)/$(TEMPLATE_OBJECT_KEY)

	# Delete objects in Artefact bucket
	aws s3 rm s3://`aws cloudformation describe-stacks --stack-name $(STACK_NAME) --query "Stacks[0].Outputs[?OutputKey=='ArtefactBucketName'].OutputValue" --output text` --recursive
