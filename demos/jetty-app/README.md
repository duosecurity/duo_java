Demonstration of a simple Java web server with Duo Web SDK authentication.

# Configuration

To set up, edit duo.properties with the appropriate `ikey`, `skey`, `akey`, and `host` values.

# Requirements

This sample application requires Java 8 or higher.

All required libraries are included.

# Installation

Compile DuoServer with the provided lib directory on the classpath.

For example, on Linux, run:
```
javac -cp "lib/*" DuoServer.java
```

# Run the example

Run the DuoServer java class.

For example, on Linux, run:
```
java -cp "lib/*:." DuoServer
```

# Usage

Visit the root URL with a 'user' argument, e.g.
'http://localhost:8080/?user=myname'.
