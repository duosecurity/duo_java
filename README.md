# Deprecation Notice

Duo Security will deprecate and archive this repository on July 18, 2022. The repository will remain public and visible after that date, and integrations built using this repository’s code will continue to work. You can also continue to fork, clone, or pull from this repository after it is deprecated.

However, Duo will not provide any further releases or enhancements after the deprecation date.

Duo recommends migrating your application to the Duo Universal Prompt. Refer to [our documentation](https://duo.com/docs/universal-prompt-update-guide) for more information on how to update.

For frequently asked questions about the impact of this deprecation, please see the [Repository Deprecation FAQ](https://duosecurity.github.io/faq.html)

----

# Overview

[![Build Status](https://github.com/duosecurity/duo_java/workflows/Java%20CI/badge.svg)](https://github.com/duosecurity/duo_universal_java/actions)
[![Issues](https://img.shields.io/github/issues/duosecurity/duo_java)](https://github.com/duosecurity/duo_java/issues)
[![Forks](https://img.shields.io/github/forks/duosecurity/duo_java)](https://github.com/duosecurity/duo_java/network/members)
[![Stars](https://img.shields.io/github/stars/duosecurity/duo_java)](https://github.com/duosecurity/duo_java/stargazers)
[![License](https://img.shields.io/badge/License-View%20License-orange)](https://github.com/duosecurity/duo_java/blob/master/LICENSE)

**duo_java** - Duo two-factor authentication for Java web applications

Duo has released a new Java client that will let you integrate the Duo Universal Prompt into your web applications.
Check out https://duo.com/docs/duoweb for more info on the Universal Prompt and [duo_universal_java](https://github.com/duosecurity/duo_universal_java) for the new client.

This package allows a web developer to quickly add Duo's interactive, self-service, two-factor authentication to any web login form - without setting up secondary user accounts, directory synchronization, servers, or hardware.

What's here:

* `js` - Duo Javascript library, to be hosted by your webserver.
* `DuoWeb` - Duo Java SDK to be integrated with your web application, including unit tests

# Usage

This library requires Java 6 or later.

The Java WebSDK project is not currently available from Duo Security on Maven.  Currently this GitHub project is the only supported source for the Duo Java WebSDK.

Developer documentation: <http://www.duosecurity.com/docs/duoweb-v2>

# Support

Report any bugs, feature requests, etc. to us directly:
support@duosecurity.com

Have fun!

<http://www.duosecurity.com>
