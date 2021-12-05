# SOOX Extensions for Saxon

This repository holds source files for Saxon extensions that are needed for SOOX.


## Licence

The project is released under the MIT licence.
See LICENCE file in this repository to review terms and conditions.


## Usage

This project uses the "Integrated extension functions" mechanism.
To use the extensions, you will need to modify Saxon configuration.
Check Saxon documentation to get information o how to do it (see https://www.saxonica.com/documentation10/index.html#!extensibility/integratedfunctions/ext-full-J ).
A simple way to do it is to do it through a configuration file.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration xmlns="http://saxon.sf.net/ns/configuration" edition="HE">
    
    ...

    <resources>
        <extensionFunction>soox.saxon.extensions.Zip</extensionFunction>
        <extensionFunction>soox.saxon.extensions.Unzip</extensionFunction>
    </resources>
    
</configuration>
```

and run Saxon with the '-config <configuration_file>' argument

## Available extensions

### soox.zip - className soox.saxon.extensions.Zip

Compress files defined by a map

### soox.unzip - className soox.saxon.extensions.Unzip

Uncompress a zip file to a map


## How to build

Install Apache Ant and run it on build.xml
