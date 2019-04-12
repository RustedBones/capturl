# capturl

[![Build Status](https://travis-ci.org/RustedBones/capturl.svg?branch=master)](https://travis-ci.org/RustedBones/capturl)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.davit/capturl_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.davit/capturl_2.12)
[![Software License](https://img.shields.io/badge/license-Apache%202-brightgreen.svg?style=flat)](LICENSE)

Capturl is a Scala/Java library that provide parsers and models to work with Internationalized Resource Identifier aka 
[IRIs](https://en.wikipedia.org/wiki/Internationalized_Resource_Identifier).

The implementation is strongly inspired by the great [`akka-http`](https://github.com/akka/akka-http) 
[Uri model](https://doc.akka.io/docs/akka-http/current/common/uri-model.html) with modularity and simplicity in mind.


## Setup

Add to your `build.sbt`:

```scala
libraryDependencies += "fr.davit" %% "capturl" % <version>
```

## Parsers

All the `apply`/`create` methods accepting String input will be validated against 
[RFC 3987](https://tools.ietf.org/rfc/rfc3987.txt) compliant parsers to create the model classes.

If you are sure about your input, and don't want to skip validation for efficiency reason, you can construct the models
using the implementation classes. eg:

```scala
Scheme("b@d_scheme") // throws a parsing exception
Scheme.Protocol("b@d_scheme") // this is very wrong but will create your scheme
```

## Normalization

Those are the IRI normalization steps:

- Scheme
    - lower case normalization
- Hosts
    - NamedHost are lower cased and decoded (punycode)
    - IPs leading zeros in bytes are dropped
- Path
    - collapse double slashes
    - collapse current folder
    - collapse parent folder
- Query
    - spaces are replaced by '+'
- Iri
    - omit custom port if matches the scheme default port
    - empty path for absolute, and scheme relative IRI is replaced by root
- General
    - all encoded characters are decoded

### TODO

- provide relax parsers
- support scala 2.11


## capturl-akka-http

This sub module provides all the necessary implicits in [`UriConverters`](/src/main/scala/fr/davit/capturl/akka/http/UriConverters.scala) 
to go from an `Iri` to the akka `Uri` model.

### TODO

- provide converters for akka-http javadsl model

