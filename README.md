# salesforce manifest generator
generate the package.xml manifest file including all metadata

## How to execute
### install gradle
    $ brew install gradle

### clone the repository
    $ git clone https://github.com/takahitomiyamoto/salesforce-manifest-generator.git

### execute the gradle command
    $ cd salesforce-manifest-generator
    $ gradle build

### unzip the package
    $ unzip build/distributions/salesforce-manifest-generator.zip

### change your credentials
    $ cp credentials_sample.json salesforce-manifest-generator/bin
    $ cd salesforce-manifest-generator/bin
    $ mv credentials_sample.json credentials.json

    {
        "credentials": {
            "username": "xxxxxxxxxx",
            "password": "xxxxxxxxxx",
            "orgType": "login",
            "apiVersion": 41.0
        }
    }

By the way, "orgType" is "login" or "test".

### execute the shell script and check the generated package.xml
    $ ./salesforce-manifest-generator
    $ ls
