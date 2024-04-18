# prettify-exceptions

## Usage

### Agent

Add as JVM option :
`-javaagent:prettify-exceptions-agent-0.1.0-SNAPSHOT-jar-with-dependencies.jar=my.classpath.to.load.settings:my_yml_containing_settings.yml`

### Runtime

Add anywhere (preferably at the very beginning of your main) :
`PrettifyExceptionInstaller.install(MyApplication.class, "my_yml_containing_settings.yml");`

## Settings

```yml
prettify-exceptions:
    skipOtherPackages: [true/false] #Should packages that aren't highlighted be printer ?
    transformPrintStackTrace: [true/false] #Should exception.printStackTrace() be affected ?
    silentFailures: ["FULL"/"MESSAGE"/"SILENT"] #Should internal errors inside the prettifier be logged ?
    targets: #Target a method within a class that returns a standard Exception
        ["path.to.logger.exception.formatter": "methodToTarget"]
    theme: #Set ANSI 8 bit color codes
        message: ["0-255"]
        log: ["0-255"]
        caller: ["0-255"]
        first: ["0-255"]
        other: ["0-255"]
        skipped: ["0-255"]
        common: ["0-255"]
        atPackage: ["0-255"]
        suffix: ["0-255"]
    highlights: #Set a color for all packages to highlight in the stacktrace
        ["path.to.package.to.highlight": "0-255"]
```