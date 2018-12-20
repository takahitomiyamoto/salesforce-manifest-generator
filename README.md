# salesforce manifest generator
generate the package.xml manifest file including all metadata

## How to execute
### install gradle
```sh
brew install gradle
```

### clone the repository
```sh
git clone https://github.com/takahitomiyamoto/salesforce-manifest-generator.git
```

### execute the gradle command
```sh
cd salesforce-manifest-generator
gradle build
```

### unzip the package
```sh
unzip build/distributions/salesforce-manifest-generator.zip
```

### change your credentials
```sh
cp credentials_sample.json salesforce-manifest-generator/bin
cd salesforce-manifest-generator/bin
mv credentials_sample.json credentials.json
```

```json
{
    "credentials": {
        "username": "xxxxxxxxxx",
        "password": "xxxxxxxxxx",
        "proxyHost": "xxxxxxxxxx",
        "apiVersion": 44.0,
        "os":"win"
    }
}
```

If you log in to Salesforce via a proxy, set the proxy host and port:

```json
{
    "credentials": {
        "username": "xxxxxxxxxx",
        "password": "xxxxxxxxxx",
        "proxyHost": "samplehost",
        "proxyPort": 9999,
        "orgType": "login",
        "apiVersion": 44.0,
        "os":"win"
    }
}
```

By the way,
- "orgType" : "login" or "test"
    - "login" : production, developer
    - "test" : sandbox
- "os" : "win" or "mac"
    - "win" : use Windows (you should open the generated file with "Shift_JIS".)
    - "mac" : use Mac (you should open the generated file with "UTF-8".)

### execute the shell script and check the generated package.xml
```sh
./salesforce-manifest-generator
ls
```