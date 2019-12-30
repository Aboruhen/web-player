# Media Web Player

The reason is to avoid additional player programs.
Run-on any device with Web Browser locally stored media(video, audio, etc.)

No needs for internet access

* Run server
  `mvn clean install`
* Web url
  `http://localhost:8080/`

### Requirements

* Latest java (13.0.1-open) - [Sdk Manager](https://sdkman.io/) `sdk list java`
* Maven
* Speing WebFlux
* Thymeleaf
* [x] Docker

## Configs

Spring boot used for easy/quick start

Web application should be extremly easy to use and run.

env variable `STORE_LOCATION_PATH` can be used as default folder resource location "./home/"

### Web available Video types

* [Vedio Media Type](https://www.iana.org/assignments/media-types/media-types.xhtml#video)