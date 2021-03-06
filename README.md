# oboco for android

![oboco-android](art/screenshots.png "screenshots")

oboco for android is a client for [oboco](https://gitlab.com/jeeto/oboco).
[oboco](https://gitlab.com/jeeto/oboco) is a server to help you read the books in your book collection (zip, cbz, cbr, rar, rar5).
[oboco](https://gitlab.com/jeeto/oboco) is short for "open book collection".

you can:
- read your books and book collections.
- search your books and book collections.
- download your books.
- manage your book marks.

oboco for android started as a fork of [bubble](https://github.com/nkanaev/bubble).

## requirements

- [oboco](https://gitlab.com/jeeto/oboco)
- android >= 5.0

## installation

- allow installation source
	- configure android
		- select "settings"
		- select "biometrics and security"
		- select "install unknown apps"
		- select "chrome"
		- select "allow from this source"
- install [the latest release](https://gitlab.com/jeeto/oboco-android/-/jobs/artifacts/91fa695e/raw/app/build/outputs/apk/release/app-release.apk?job=assembleRelease).

## configuration

### application ssl client

- add the ssl certificate authority (server-ca.pem) to the trust store
	- configure android
		- copy server-ca.pem to the device storage
		- select "settings"
		- select "biometrics and security"
		- select "other security settings"
		- select "install from device storage"

## usage

- log in
	- baseUrl: the base url of the oboco server (http://server.address:server.port or https://server.address:server.ssl.port)
	- name: the user name
	- password: the user password
- browse

## test

you can test oboco for android with [the oboco test server](obocos://test:test@oboco-backend-test.herokuapp.com) (the server takes up to 30 seconds to start - retry if needed):
- log in
	- baseUrl: https://oboco-backend-test.herokuapp.com
	- name: test
	- password: test
- browse

## license

mit license