# ICFP contest 2017 ;)
![image](https://github.com/PaulTaykalo/icfp-2017/blob/master/images/graph.png)

- *Main language*: [Kotlin](https://try.kotlinlang.org/)
- *Build system*: [Gradle](https://guides.gradle.org/creating-new-gradle-builds/)
- *Mocks lib*: [Mockito-Kotlin](https://github.com/nhaarman/mockito-kotlin)
- *Assertions lib*: [Kluent](https://github.com/MarkusAmshove/Kluent)

Info about Kotlin & cool Kotlin libs: [Awesome Kotlin](https://kotlin.link/)

## Run
```
./gradlew run
```

## Run in VM
```
./invm [parameters list]
```

## Test
```
./gradlew test
```

## Distribution
You can build distribution `zip` with all dependencies and start script in this way:
```
./gradlew distZip
```

Output you can find in `./build/distribution/icfp2017.zip`

Inside this `zip`:
```
icfp2017
├── bin
│   ├── icfp2017
│   └── icfp2017.bat
└── lib
    ├── annotations-13.0.jar
    ├── icfp2017.jar
    ├── kotlin-stdlib-1.1.3-2.jar
    ├── kotlin-stdlib-jre7-1.1.3-2.jar
    └── kotlin-stdlib-jre8-1.1.3-2.jar
```

To run just unzip the package and execute:
```
icfp2017/bin/icfp2017 [parameters list]
```

## Submit archive
To build submit archive use `assembleSubmitArchive`

# Recommended IDE
[IntellyJ Idea Community Edition](https://www.jetbrains.com/idea/download/) (Free & OSS)

# Import project to IDEA
Import project as a Gradle project.
