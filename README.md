A Data/Code repository for ML in Scala book
===========================================

# Download Scala, SBT and Java
- Get Scala from [www.scala-lang.org](http://www.scala-lang.org/download)
- Get SBT from [www.scala-sbt.org](http://www.scala-sbt.org/download.html)
- Get Java from [download.oracle.com](http://download.oracle.com)

For example:
```bash
$ wget --no-cookies --no-check-certificate --header 'Cookie: oraclelicense=accept-securebackup-cookie' http://download.oracle.com/otn-pub/java/jdk/8u77-b03/jdk-8u77-linux-x64.rpm
```

# Compiling
To compile all code, do

```bash
$ sbt package
```

Alternativelym, you can go to individual directories and type `sbt run`, which in most cases should run the application (chapter 10 needs to start a jetty service, in which case you need `sbt jetty:start`).

# Interactive mode
Sbt has an interactive command line capabilites.  For example, to continuously compile code on each source file change, do

```bash
$ sbt
...
> ~run
...
```

# Using with Eclipse
Currently, you need to install the sbteclipse plugin with your sbt environemnt (add the following line to your build.sbt)

```bash
echo 'addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")' >> project/plugins.sbt
```

After which the `sbt eclipse` target will be added to create Eclipse `.project` and `.classpath` files.  Import the project from [Eclipse for Scala](http://scala-ide.org).
