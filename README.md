# capturl

[![Continuous Integration](https://github.com/RustedBones/capturl/workflows/Continuous%20Integration/badge.svg?branch=master)](https://github.com/RustedBones/capturl/actions?query=branch%3Amaster+workflow%3A"Continuous+Integration")
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.davit/capturl_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/fr.davit/capturl_2.12)
[![Software License](https://img.shields.io/badge/license-Apache%202-brightgreen.svg?style=flat)](LICENSE)

Capturl is a Scala/Java library that provide parsers and models to work with Internationalized Resource Identifier aka 
[IRIs](https://en.wikipedia.org/wiki/Internationalized_Resource_Identifier).

The implementation is strongly inspired by the great [`akka-http`](https://github.com/akka/akka-http) 
[Uri model](https://doc.akka.io/docs/akka-http/current/common/uri-model.html) with modularity and simplicity in mind.

## Setup

Add to your `build.sbt`:

```scala
libraryDependencies += "fr.davit" %% "capturl" % "<version>"
```

## Parsers

All the `apply`/`create` methods accepting String input will be validated against 
[RFC 3987](https://tools.ietf.org/rfc/rfc3987.txt) compliant parsers to create the model classes.

**Warning**: Exception is made for spaces in path, query and fragment which are allowed at the moment (see TODOs)

If you are sure about your input, and want to skip validation for efficiency reason, you can construct the models
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
    - IPv4 and [IPv6](https://tools.ietf.org/html/rfc5952#section-4) are normalized
- Path
    - collapse current folder
    - collapse parent folder
- Query
    - spaces are replaced by '+'
- Iri
    - omit custom port if matches the scheme default port
    - root path is added for absolute / host relative iris
    - default port is hidden
- General
    - all encoded characters are decoded

### IRI string interpolation

Add to your `build.sbt`:

```scala
libraryDependencies += "fr.davit" %% "capturl-contextual" % <version>
```

This sub module provides compile time string interpolation to create IRIs:

```scala
val iri = iri"http://localhost:8080/path?key#identifier"
```

If iri string is not valid, the scala compiler will notify you:

```scala
val iri = iri"http://user{info@example.com/"
```

```
[error] Invalid IRI http://user{info@example.com/
[error]     iri"http://user{info@example.com/"
[error]         ^
[error] one error found
```


### TODO

- provide strict / relax parser mode
- provide more normalization options
- support holes in interpolation 

## capturl-akka-http

Add to your `build.sbt`:

```scala
libraryDependencies += "fr.davit" %% "capturl-akka-http" % <version>
```

This sub module provides all the necessary implicits in [`UriConverters`](/src/main/scala/fr/davit/capturl/akka/http/UriConverters.scala) 
to go from an `Iri` to the akka `Uri` model.

### TODO

- provide converters for akka-http javadsl model

