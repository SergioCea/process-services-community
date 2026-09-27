# Process Services Community

An Alfresco Content Services (ACS) extension built with Alfresco SDK 4.9. It exposes authenticated web scripts for
managing BPMN 2.0 workflow definitions, starting process instances, and working with user tasks. This is useful when
another application or integration needs to start and track approval, review, or request workflows in ACS over HTTP,
without calling workflow services directly. ACS, Share, Search Services, PostgreSQL, and ActiveMQ run as Docker Compose
services.

## What the project is for

The project adds an integration API to the Alfresco repository. A client can deploy a BPMN 2.0 process definition, start
an instance with business data, and let the workflow move through its modeled steps. When a step requires a person,
authenticated users can find their active tasks, claim a task, and complete it with values that the process can use in
later steps. For example, an application could start a request for review and provide its request number and requester
as process variables; a reviewer could then complete the corresponding task and provide a decision.

The typical workflow is:

1. Create a BPMN 2.0 definition in a modeling tool and send its XML to the deployment endpoint. This project provides
   the deployment API, not a visual BPMN editor.
2. Start an instance of the deployed definition, optionally passing variables such as a request ID or other business
   data.
3. Let the process reach a user task, then use the task endpoints to list, claim, and complete that task. Completion can
   include variables for subsequent process steps.
4. List, inspect, or undeploy definitions as needed.

This is an ACS extension and development environment, not a standalone workflow server: the repository and its workflow
runtime must be running. The web scripts provide the HTTP integration surface; the BPMN definition determines the
process steps and transitions.

## Project modules

| Module                                         | Purpose                                             |
|------------------------------------------------|-----------------------------------------------------|
| `process-services-community-platform`          | Repository extension and workflow web scripts.      |
| `process-services-community-platform-docker`   | ACS image and extension assembly.                   |
| `process-services-community-share`             | Share extension.                                    |
| `process-services-community-share-docker`      | Share image and extension assembly.                 |
| `process-services-community-integration-tests` | Integration tests against the running ACS instance. |

## Prerequisites

- Java and Maven compatible with Alfresco SDK 4.9 (Maven 3.3 or later).
- Docker Engine and the Docker Compose plugin.

## Run the environment

Build the project, start the Docker environment, and follow its logs:

```sh
./run.sh build_start
```

On Windows, use `run.bat build_start`. The default local endpoints are:

| Service                   | URL / port                       |
|---------------------------|----------------------------------|
| Alfresco Content Services | <http://localhost:8080/alfresco> |
| Alfresco Share            | <http://localhost:8180/share>    |
| Search Services           | <http://localhost:8983/solr>     |
| PostgreSQL                | `localhost:5555`                 |
| ActiveMQ web console      | <http://localhost:8161>          |

The run script follows container logs after starting the environment; press `Ctrl+C` to stop following logs. This does
not stop the containers. Run `./run.sh stop` to stop them. ACS data, database data, and search indexes are kept in
Docker volumes across restarts. `./run.sh purge` removes those volumes and permanently deletes their data.

## Run script commands

| Command                    | What it does                                                                                                                       |
|----------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| `build_start`              | Stops the existing Compose environment, builds the project, starts the services, and follows logs.                                 |
| `build_start_it_supported` | Builds the project and integration-test artifacts, starts the services, and follows logs; it does not run the tests.               |
| `start`                    | Starts the existing environment without building and follows logs.                                                                 |
| `stop`                     | Stops the Compose environment without removing persistent volumes.                                                                 |
| `purge`                    | Stops the environment and removes the ACS, database, and search-service volumes.                                                   |
| `tail`                     | Follows logs from all services.                                                                                                    |
| `reload_share`             | Rebuilds the Share modules and restarts the Share container.                                                                       |
| `reload_acs`               | Rebuilds the platform, integration-test, and ACS Docker modules and restarts ACS.                                                  |
| `build_test`               | Builds the project and test artifacts, starts the environment, runs integration tests, prints the logs, and stops the environment. |
| `test`                     | Runs the integration tests; the environment must already be running.                                                               |

For example, run integration tests against an already-started environment with:

```sh
./run.sh test
```

The integration tests use `admin` / `admin` by default and target `http://localhost:8080/alfresco`. Configure
`acs.username`, `acs.password`, or `test.acs.endpoint.path` in the Maven invocation when your environment uses different
values.

## Workflow web scripts

The endpoints are available below `http://localhost:8080/alfresco/service/process-services-community`. Requests and
responses use JSON except that deployment also accepts raw BPMN XML. Endpoints require Alfresco authentication:
deployment is admin-only; the other endpoints require an authenticated user.

| Method   | Path                  | Purpose / request                                                                                                                                                                          |
|----------|-----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST`   | `/deploy`             | Deploy a BPMN 2.0 definition. Send raw XML, or JSON with `xml` and optional `name`, `title`, and `forms` fields. The `forms` object maps BPMN element IDs to form schemas. Admin required. |
| `GET`    | `/list`               | List deployed workflow definitions.                                                                                                                                                        |
| `GET`    | `/definition?id={id}` | Get a definition, including its BPMN XML and associated forms.                                                                                                                             |
| `DELETE` | `/definition?id={id}` | Undeploy a definition. Add `&all=true` to undeploy all versions.                                                                                                                           |
| `POST`   | `/start`              | Start a process with JSON `{ "id": "<definition-id>", "variables": { } }`. `variables` is optional.                                                                                        |
| `GET`    | `/tasks`              | List active workflow tasks visible to the current user, including associated form data.                                                                                                    |
| `POST`   | `/task/claim`         | Claim a task with JSON `{ "id": "<task-id>" }`.                                                                                                                                            |
| `POST`   | `/task/complete`      | Complete a task with JSON `{ "id": "<task-id>", "variables": { } }`. `variables` is optional.                                                                                              |

Example: list definitions and start a process (replace the placeholder with an ID returned by deployment):

```sh
curl -u admin:admin \
  http://localhost:8080/alfresco/service/process-services-community/list

curl -u admin:admin -H 'Content-Type: application/json' \
  -d '{"id":"<definition-id>","variables":{"customer":"Ada"}}' \
  http://localhost:8080/alfresco/service/process-services-community/start
```

## Build

The root Maven project builds all modules:

```sh
mvn clean package
```
