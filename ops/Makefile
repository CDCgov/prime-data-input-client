# Makefile for az cli shortcuts for SimpleReport operations

SHELL:=/bin/bash # I gave up on being compatible with ye olde "sh"
SOURCE_SLOT?=staging
TARGET_SLOT?=production
DEPLOYED_COMMIT?=$(shell git show --abbrev=7 -s --pretty=%h)
RELEASE_TAG?=$(subst refs/tags/,,$(GITHUB_REF))

# Internal DRYing
API_NAME=simple-report-api-$*
INFO_ENDPOINT=https://$(API_NAME).azurewebsites.net/actuator/info
SLOT_INFO_ENDPOINT=https://$(API_NAME)-$(SOURCE_SLOT).azurewebsites.net/actuator/info
CURL:=curl --silent --fail --max-time 1
APP_READY=$(CURL) https://$(API_NAME).azurewebsites.net/actuator/health/readiness > /dev/null
SLOT_READY=$(CURL) https://$(API_NAME)-$(SOURCE_SLOT).azurewebsites.net/actuator/health/readiness > /dev/null

default:
	@echo "Hello, SimpleReport operator!"
	@echo "You can use 'make promote-ENV' to promote the $(SOURCE_SLOT) slot of any environment, if you are logged in to azure"
	@echo "You can also run 'make init-ENV' or 'make deploy-ENV-api' but those may not be as useful locally."
	@echo "In addition, there are a variety of targets that start with 'check' or 'wait-for', which you should just look at."

# Internal target: check if we are currently logged in, so we get a friendly error if not
.be-logged-in:
	@if ! az account show >& /dev/null ; then echo "You must be logged in to the az command line"; exit 1; fi

# Internal target: check if the passed-in wildcard is a known environment name. Hard-coding them because let's be real here.
.valid-env-%:
	@case $* in test|dev|demo|training|pentest|stg|prod) ;; *) echo "$* is not a valid environment"; exit 1;; esac

api.tfvars: /dev/null
	echo "acr_image_tag=\"$(DEPLOYED_COMMIT)\"" > $@; \
	echo "deploy_workflow=\"${GITHUB_WORKFLOW}\"" >> $@; \
	if [[ "release" == "$(GITHUB_EVENT_NAME)" ]]; \
		then echo "deploy_tag=\"$(RELEASE_TAG)\"" >> $@;\
	fi; \
	echo "deploy_runnumber=${GITHUB_RUN_NUMBER}" >> $@; \
	echo "deploy_timestamp=\"$(shell date +%Y-%m-%dT%H:%M:%S%z) \"" >> $@; \
	echo "deploy_actor=\"$(GITHUB_ACTOR)\"" >> $@;

init-%: .valid-env-%
	terraform -chdir=$* init

deploy-%-api: .valid-env-% api.tfvars
	terraform -chdir=$* apply -auto-approve -var-file=../api.tfvars -target="module.simple_report_api"

promote-%: .be-logged-in .valid-env-%
	az webapp deployment slot swap -g prime-simple-report-$* -n $(API_NAME) --slot $(SOURCE_SLOT) --target-slot $(TARGET_SLOT)

wait-for-%-slot-commit: .valid-env-%
	@echo "Starting wait for commit $(DEPLOYED_COMMIT) in $(SOURCE_SLOT) slot of $* at `date`"
	until  [[ "$(DEPLOYED_COMMIT)"  == "`$(CURL) $(SLOT_INFO_ENDPOINT) | jq -r .git.commit.id`" ]] ; do sleep 5; done;
	@echo "Finished waiting at `date`"

wait-for-%-commit: .valid-env-%
	@echo "Starting wait for commit $(DEPLOYED_COMMIT) in $* at `date`"
	until  [[ "$(DEPLOYED_COMMIT)"  == "`$(CURL) $(INFO_ENDPOINT) | jq -r .git.commit.id`" ]] ; do sleep 5; done;
	@echo "Finished waiting at `date`"

wait-for-%-slot-readiness: .valid-env-% # better use an external timeout for this!
	@echo "Waiting for a successful readiness probe in $*/$(SOURCE_SLOT)"
	until $(SLOT_READY); do sleep 5; done;

wait-for-%-readiness: .valid-env-% # better use an external timeout for this!
	@echo "Waiting for a successful readiness probe in $*"
	until $(APP_READY); do sleep 5; done;

check-%-slot-commit: .valid-env-%
	@echo Checking that $(DEPLOYED_COMMIT) is the current deployed commit for $*/$(SOURCE_SLOT)
	[[ "$(DEPLOYED_COMMIT)"  == "`$(CURL) $(SLOT_INFO_ENDPOINT) | jq -r .git.commit.id`" ]]

check-%-commit: .valid-env-%
	@echo Checking that $(DEPLOYED_COMMIT) is the current deployed commit for $*
	[[ "$(DEPLOYED_COMMIT)"  == "`$(CURL) $(INFO_ENDPOINT) | jq -r .git.commit.id`" ]]

check-%-slot-release: .valid-env-%
	@echo Checking that $(RELEASE_TAG) is the current release on $*/$(SOURCE_SLOT)
	[[ "$(RELEASE_TAG)"  == "`$(CURL) $(SLOT_INFO_ENDPOINT) | jq -r .deploy.tag`" ]]

check-%-release: .valid-env-%
	@echo Checking that $(RELEASE_TAG) is the current release on $*
	[[ "$(RELEASE_TAG)"  == "`$(CURL) $(INFO_ENDPOINT) | jq -r .deploy.tag`" ]]

check-%-slot-readiness: .valid-env-%
	@echo Checking the readiness probe for $*/$(SOURCE_SLOT)
	$(SLOT_READY)

check-%-readiness: .valid-env-%
	@echo Checking the readiness probe for $*
	$(APP_READY)

# DEPRECATED: This is too greedy a match, replacing it
wait-for-%: .valid-env-%
	@echo "Starting wait for commit $(DEPLOYED_COMMIT) in $(SOURCE_SLOT) slot of $* at `date`"
	until  [[ "$(DEPLOYED_COMMIT)"  == "`$(CURL) $(SLOT_INFO_ENDPOINT) | jq -r .git.commit.id`" ]] ; do sleep 5; done;
	@echo "Finished waiting at `date`"
