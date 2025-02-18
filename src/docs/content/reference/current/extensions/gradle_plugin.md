---
title: "Gradle Plugin"
description: "Gradle plugin to run Gatling tests and deploy to Gatling Enterprise"
lead: "The Gradle plugin allows you to run Gatling tests from the command line, without the bundle, as well as to package your simulations for Gatling Enterprise"
date: 2021-04-20T18:30:56+02:00
lastmod: 2021-11-23T09:00:00+02:00
weight: 2080200
---

This Gradle plugin was initially contributed by [Ievgenii Shepeliuk](https://github.com/eshepelyuk) and
[Laszlo Kishalmi](https://github.com/lkishalmi).

This Gradle plugin integrates Gatling with Gradle, allowing to use Gatling as a testing framework.

## Versions

Check out available versions on [Gradle Plugins Portal](https://plugins.gradle.org/plugin/io.gatling.gradle).

## Compatibility

### Gradle version

{{< alert warning >}}
This plugin requires at least Gradle 5.
{{< /alert >}}

The latest version of this plugin is tested against Gradle versions ranging from 5.0.0 to 7.3.
Any version outside this range is not guaranteed to work.

## Setup

Install [Gradle](https://gradle.org/install/) or use the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).
Our official sample projects come pre-configured with the Gradle Wrapper.

### Java

In `build.gradle`, add:

```groovy
 plugins {
   id 'io.gatling.gradle' version "MANUALLY_REPLACE_WITH_LATEST_VERSION"
 }
```

Please check our [official sample project for gradle and Java](https://github.com/gatling/gatling-gradle-plugin-demo-java) on GitHub.

### Kotlin

Please check our [official sample project for gradle and Kotlin](https://github.com/gatling/gatling-gradle-plugin-demo-kotlin) on GitHub.

### Scala

Please check our [official sample project for gradle and Scala](https://github.com/gatling/gatling-gradle-plugin-demo-scala) on GitHub.
This plugin supports the same versions of Scala as the Gatling version you're using, meaning Scala 2.13 since Gatling 3.5.

### Multi-project support

If you have a [multi-project build](https://docs.gradle.org/current/userguide/multi_project_builds.html), make sure to
only configure the subprojects which contain Gatling Simulations with the Gatling plugin as described above. Your
Gatling subproject can, however, depend on other subprojects.

## Source files layout

The plugin creates a dedicated [Gradle sourceSet](https://docs.gradle.org/current/dsl/org.gradle.api.tasks.SourceSet.html)
named `gatling`. This source set is used for storing simulations and Gatling configurations. The following directories
are configured by default.

| Directory               | Purpose                                         |
| ----------------------- | ----------------------------------------------- |
| `src/gatling/java`      | Simulation sources (Java code)                  |
| `src/gatling/kotlin`    | Simulation sources (Kotlin code)                |
| `src/gatling/scala`     | Simulation sources (Scala code)                 |
| `src/gatling/resources` | Resources (feeders, configuration, bodies, etc) |

Using the Gradle API, file locations can be customized.

```groovy
sourceSets {
  gatling {
    scala.srcDir "folder1" <1>
    // or
    scala.srcDirs = ["folder1"] <2>

    resources.srcDir "folder2" <3>
    // or
    resources.srcDirs = ["folder2"] <4>
  }
}
```

1. append `folder1` as an extra simulations' folder.
2. use `folder1` as a single source of simulations.
3. append `folder2` as an extra `Gatling` resources folder.
4. use `folder2` as a single source of `Gatling` resources.

## Plugin configuration

The plugin defines the following extension properties in the `gatling` closure:

| Property name | Type | Default value  | Description |
| --- | --- | --- | --- |
| `gatlingVersion`    | String  | The first 3 digits of this plugin's version | Gatling version |
| `logLevel`          | String  | `'WARN'` | The default Gatling console log level if no `logback.xml` present in the configuration folder |
| `logHttp`           | String  | `'NONE'` | Verbosity of logging HTTP requests performed by Gatling, must be one of: <br/> * `'NONE'` - do not log, <br/> * `'ALL'` - log all requests, <br/> * `'FAILURES'` - only failed requests |
| `includeMainOutput` | Boolean | `true` | `true` |
| `includeTestOutput` | Boolean | `true` | Include test source set output to gatlingImplementation |
| `scalaVersion`      | String  | `'2.13.7'` | Scala version that fits your Gatling version |
| `jvmArgs`           | List    | <pre>[<br> '-server',<br> '-Xmx1G',<br> '-XX:+HeapDumpOnOutOfMemoryError',<br> '-XX:+UseG1GC',<br>  '-XX:+ParallelRefProcEnabled',<br> '-XX:MaxInlineLevel=20',<br> '-XX:MaxTrivialSize=12',<br> '-XX:-UseBiasedLocking'<br>]</pre> | Additional arguments passed to JVM when executing Gatling simulations |
| `systemProperties`  | Map     | `['java.net.preferIPv6Addresses': true]` | Additional systems properties passed to JVM together with caller JVM system properties |
| `simulations`       | Closure | `include("**/*Simulation*.java", "**/*Simulation*.kt", "**/*Simulation*.scala")` | Simulations filter. [See Gradle docs](https://gatling.io/docs/current/extensions/gradle_plugin/?highlight=gradle%20plugin#id2) for details. |

How to override Gatling version, JVM arguments and system properties:

```groovy
gatling {
  gatlingVersion = '3.7.0'
  jvmArgs = ['-server', '-Xms512M', '-Xmx512M']
  systemProperties = ['file.encoding': 'UTF-8']
}
```

How to filter simulations:

```groovy
gatling {
  simulations = {
    include "**/package1/*Simu.scala" // <1>
    include "**/package2/*Simulation.scala" // <2>
  }
}
```

1. all Scala files from plugin simulation dir subfolder `package1` ending with `Simu`.
2. all Scala files from plugin simulation dir subfolder `package2` ending with `Simulation`.

## Gatling configuration

### Override gatling.conf settings

To override Gatling's
[default parameters](https://github.com/gatling/gatling/blob/main/gatling-core/src/main/resources/gatling-defaults.conf),
put your own version of `gatling.conf` into `src/gatling/resources`.

### Logging management

Gatling uses [Logback](http://logback.qos.ch/documentation.html). To change the logging behaviour, put your
custom `logback.xml` configuration file in the resources folder, `src/gatling/resources`.

If no custom `logback.xml` file is provided, by default the plugin will implicitly use the following configuration:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
      <immediateFlush>false</immediateFlush>
    </encoder>
  </appender>
  <root level="${logLevel}"> <!--1-->
    <appender-ref ref="CONSOLE"/>
  </root>
</configuration>
```

1. `logLevel` is configured via plugin extension, `WARN` by default.

In case `logHttp` is configured (except for `'NONE'`), the generated `logback.xml` will look like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
      <immediateFlush>false</immediateFlush>
    </encoder>
  </appender>
  <logger name="io.gatling.http.engine.response" level="${logHttp}"/> <!--1-->
  <root level="${logLevel}"> <!--2-->
    <appender-ref ref="CONSOLE"/>
  </root>
</configuration>
```

1. `logHttp` is configured via plugin extension, `TRACE` for `ALL` value and `DEBUG` for `FAILURES`
2. `logLevel` is configured via plugin extension, `WARN` by default.

## Dependency management

This plugin defines three [Gradle configurations](https://docs.gradle.org/current/dsl/org.gradle.api.artifacts.Configuration.html):
`gatling`, `gatlingImplementation` and `gatlingRuntimeOnly`.

By default, the plugin adds Gatling libraries to `gatling` configuration.
Configurations `gatlingImplementation` and `gatlingRuntimeOnly` extend `gatling`,
i.e. all dependencies declared in `gatling` will be inherited. Dependencies added
to configurations other than these '`gatling`' configurations will not be available
within Gatling simulations.

Also, project classes (`src/main`) and tests classes (`src/test`) are added to
`gatlingImplementation` and `gatlingRuntimeOnly` classpath, so you can reuse
existing production and test code in your simulations.

If you do not need such behaviour, you can use flags. Manage test and main output:

```groovy
gatling {
  // do not include classes and resources from src/main
  includeMainOutput = false
  // do not include classes and resources from src/test
  includeTestOutput = false
}
```

Additional dependencies can be added to any of the configurations mentioned above. Add external libraries for `Gatling`
simulations:

```groovy
dependencies {
  gatling 'com.google.code.gson:gson:2.8.0' // <1>
  gatlingImplementation 'org.apache.commons:commons-lang3:3.4' // <2>
  gatlingRuntimeOnly 'cglib:cglib-nodep:3.2.0' // <3>
}
```

1. adding gson library, available both in compile and runtime classpath.
2. adding commons-lang3 to compile classpath for simulations.
3. adding cglib to runtime classpath for simulations.

## Tasks

### Running your simulations

Use the task `GatlingRunTask` to execute Gatling simulations. You can create your own instances of this task
to run particular simulations, or use the default tasks:

| Task name | Type | Description |
| --- | --- | --- |
| `gatlingClasses`           | ---            | Compiles Gatling simulation and copies resources |
| `gatlingRun`               | GatlingRunTask | Executes all Gatling simulations configured by extension |
| `gatlingRun-SimulationFQN` | GatlingRunTask | Executes single Gatling simulation\n`SimulationFQN` should be replaced by fully qualified simulation class name. |

For example, run all simulations:

```shell
gradle gatlingRun
```

Run a single simulation by its FQN (fully qualified class name):

```console
gradle gatlingRun-com.project.simu.MySimulation
```

{{< alert tip >}}
If you need to run a task several times in a row (with different environment or configuration), you may use the ``--rerun-tasks`` flag.

example:

```console
gradle --rerun-tasks gatlingRun
```

To always rerun the gatling task when called, you may add this line in ``build.gradle``:

```groovy
tasks.withType(io.gatling.gradle.GatlingRunTask) {
  outputs.upToDateWhen { false }
}
```
{{< /alert >}}

The following configuration options are available. Those options are similar to
global `gatling` configurations. Options are used in a fallback manner, i.e. if
an option is not set the value from the `gatling` global config is taken.

| Property name | Type | Default value | Description |
| --- | --- | --- | --- |
| `jvmArgs`          | List<String>        | `null` | Additional arguments passed to JVM when executing Gatling simulations |
| `systemProperties` | Map<String, Object> | `null` | Additional systems properties passed to JVM together with caller JVM system properties |
| `simulations`      | Closure             | `null` | [See Gradle docs](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/util/PatternFilterable.html) for details. |

### Working with Gatling Enterprise Cloud

#### Package

Use the task `GatlingEnterprisePackageTask` to package your simulation for Gatling Enterprise Cloud:

```shell
gradle gatlingEnterprisePackage
```

This will generate the `build/libs/<artifactId>-<version>-tests.jar` package which you can then
[upload to the Cloud](https://gatling.io/docs/enterprise/cloud/reference/user/package_conf/).

#### Package and upload

You can also create and upload the package in a single command, using the `GatlingEnterpriseUploadTask`. You must already have
[configured a package](https://gatling.io/docs/enterprise/cloud/reference/user/package_conf/) (copy the package ID from
the Packages table). You will also need [an API token](https://gatling.io/docs/enterprise/cloud/reference/admin/api_tokens/)
with appropriate permissions to upload a package.

Configure the package ID (and possibly the API token, but see below for other options) on the plugin:

```groovy
gatling {
  enterprise {
    packageId = YOUR_PACKAGE_ID
    // omit apiToken when using environment variable or Java System property instead
    apiToken = YOUR_API_TOKEN
  }
}
```

Since you probably don't want to include you secret token in your source code, you can instead configure it using either:
- the `GATLING_ENTERPRISE_API_TOKEN` environment variable
- the `gatling.enterprise.apiToken` [Java System property](https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html)

Then package and upload your simulation to gatling Enterprise Cloud:

```shell
gradle gatlingEnterpriseUpload
```

### Working with Gatling Enterprise Self-Hosted

#### Build from sources

Once you have configured the Gradle plugin on your project, Gatling Enterprise Self-Hosted can build it from sources
without additional configuration.
[Add your source repository](https://gatling.io/docs/enterprise/self-hosted/reference/current/user/repositories/#sources-repository)
and configure your simulation to
[build from sources](https://gatling.io/docs/enterprise/self-hosted/reference/current/user/simulations/#option-1-build-from-sources)
using Gradle or Gradle Wrapper.

To make sure your setup is correct, you can run the packaging command and check that you get a jar containing all the
classes and extra dependencies of your project in `build/libs/<artifactId>-<version>-tests.jar`:

```shell
gradle gatlingEnterprisePackage
```

#### Publish to a binary repository

Alternatively, you can package your simulations and publish them to a binary repository (JFrog Artifactory, Sonatype
Nexus or AWS S3).

{{< alert tip >}}
We use the official Maven Publish plugin for Gradle; please refer to the [official documentation](https://docs.gradle.org/current/userguide/publishing_maven.html)
for generic configuration options. Please also check the standards within your organization for the best way to configure
the credentials needed to access your binary repository.
{{< /alert >}}

Configure the `maven-publish` plugin to use the task named `gatlingEnterprisePackage`, then define the repository to
publish to:

```groovy
plugins {
  id "maven-publish"
}

publishing {
  publications {
    mavenJava(MavenPublication) {
      artifact gatlingEnterprisePackage
    }
  }
  repositories {
    maven {
      if (project.version.endsWith("-SNAPSHOT")) {
        url "REPLACE_WITH_YOUR_SNAPSHOTS_REPOSITORY_URL"
      } else {
        url "REPLACE_WITH_YOUR_RELEASES_REPOSITORY_URL"
      }
    }
  }
}
```


The packaged artifact will be deployed with the `tests` classifier when you publish it:

```shell
gradle publish
```

## Troubleshooting and known issues

### Spring Boot and Netty version

[Original issue](https://github.com/lkishalmi/gradle-gatling-plugin/issues/53)

Caused by `io.spring.dependency-management` plugin and Spring platform BOM files.
The dependency management plugin ensures that all declared dependencies have
exactly the same versions as declared in BOM. Since Spring Boot declares own
Netty version (e.g. `4.1.22.Final`) - this version is applied globally for all
the configurations of the Gradle project, even if configuration does not use
Spring.

There are 2 ways of solving the problem, depending on the actual usage of Netty in the project.

* When production code does not rely on `Netty`:

`build.gradle`:
 
```groovy
ext['netty.version'] = '4.0.51.Final'
```

This declares Netty version globally for all transitive dependencies in your project, including Spring.

* When production code uses `Netty`:

`build.gradle`:

```groovy
dependencyManagement {
  gatling {
    dependencies {
      dependencySet(group: 'io.netty', version: '4.0.51.Final') {
         entry 'netty-codec-http'
         entry 'netty-codec'
         entry 'netty-handler'
         entry 'netty-buffer'
         entry 'netty-transport'
         entry 'netty-common'
         entry 'netty-transport-native-epoll'
      }
    }
  }
}
```

These options ensure that `4.0.51.Final` will be used only for `gatling` configurations, leaving other dependencies unchanged.

## Sources

If you're interested in contributing, you can find the [io.gatling.gradle plugin sources](https://github.com/gatling/gatling-gradle-plugin) on GitHub.
