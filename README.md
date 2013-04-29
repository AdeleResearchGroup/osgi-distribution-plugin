Maven OSGi Distribution plugin
===========

The OSGi Distribution plugin is used to package OSGi applications using Maven. Basically, it just copy all dependencies in
specified folders, copy the provided resources and zip the result. It also supports multiple inheritance between
distributions.
These distributions come as zip files, and can be fine tuned to have dependencies come in separate folders, with different names.

The full documentation is available on :

* http://adeleresearchgroup.github.io/osgi-distribution-plugin/release/ for the latest release
* http://adeleresearchgroup.github.io/osgi-distribution-plugin/snapshot/ for the development version

The plugin is licensed under the Apache Software License 2.0.

Simple usage
========================


An output configuration example :

```xml
 <outputs>
    <output>
        <IncludesArtifactId>org.apache.felix.main</IncludesArtifactId>
        <directory>bin</directory>
        <outputFileName>felix.jar</outputFileName>
    </output>
    <output>
        <IncludesArtifactId>org.apache.felix.fileinstall</IncludesArtifactId>
        <directory>bundles</directory>
    </output>
</outputs>
```

Results in the following :

- The felix dependency will be redirected to the "bin" directory, and will be renamed to "felix.jar".
- The fileinstall dependency will be redirected to the "bundles" directory.
- All other dependencies go to the "load" folder (default behavior).

Projects using this plugin need to use the "osgi-distribution" packaging type, and call the plugin in their plugins section.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>fr.liglab.adele.common</groupId>
            <artifactId>maven-osgi-distribution-plugin</artifactId>
            <version>@project.version@</version>
            <extensions>true</extensions>
            <configuration>
                <defaultOutputDirectory>bundle</defaultOutputDirectory>
                <generateScripts>true</generateScripts>
                <outputs>
                    ...
                </outputs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

To run the plugin, just call the "install" goal on your project.

Plugin configuration options
 * `defaultOutputDirectory` to specifiy the location of the artifacts declared in the dependency section. Except for artifacts defined in the `outputs` section. Default value `load`.
 * `generateScripts` to generate launcher temporal scripts (useful while testing distribution) Default value `false`.
 * `flattenDP` to specify if Deployment-Packages dependencies will be flatten in the distribution or not. Default value `false`.
 * `outputs` used to configure target location for some artifacts.
 * `output` the entry to define the desired artifact.
 * `IncludesArtifactId` the artifactId.
 * `directory` the target directory location of the current artifact.
 * `outputFileName` if defined, the artifact filename will be overrided by this value.
