# Ludum Dare 46 

[![BuildStatus](https://jenkins.home.inthelifeofdoug.com/buildStatus/icon?job=LudumDares%2FLudumDare46%2Fmaster)](https://jenkins.home.inthelifeofdoug.com/job/LudumDares/job/LudumDare46/job/master/)

## Build Requirements

* [JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2145151.html)
* [Gradle](https://gradle.org)

## Setup

Gradle can be acquired by direct download: [Gradle](https://gradle.org)

...or through a Windows package manager like [Chocolatey](https://chocolatey.org)

    choco install gradle

...or through a Mac package manager like [Homebrew](https://brew.sh)

    brew install gradle

Once you have gradle installed jump to the 'Run the game' section and run those commands

### Windows (IntelliJ)

Additionally, many IDEs include Gradle by default. [IntelliJ IDEA](https://www.jetbrains.com/idea) is recommended.

    choco install intellijidea-community

To import the project into IntelliJ, follow these steps:

- File -> New -> Project (from version control) -> GitHub
- (or Check out from Version Control -> GitHub)
    - Choose *Password* authorization and enter your GitHub username and password
    - Git Repository URL: *https://github.com/bploeckelman/LudumDare45.git*
    - Parent Directory: *{your choice}*
    - Directory Name: *LudumDare45*
    - *Clone*
- 'Unlinked Gradle project' popup -> Import Gradle Project
    - *Check* _'use auto-import'_
    - *Uncheck* _'Create separate module per source set'_
- Run -> Edit Configurations -> *+* -> Application
    - Name: *desktop*
    - Main class: *lando.systems.ld45.desktop.DesktopLauncher*
    - Working Directory: {project root}*\core\assets*
    - Use classpath of module: *desktop*
    - Before Launch: *+* -> Run Gradle Task
        - Gradle Project: *desktop*
        - Tasks: *sprites*

To setup the project in any other IDE, do something similar. (Feel free to open a pull request with details for other IDEs)

### Mac OS X

The easy way to setup Mac OS X to do LibGDX game dev is to utilize [homebrew](http://brew.sh)

Homebrew requires [xcode](https://developer.apple.com/xcode/downloads/).

Install Homebrew:

    ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

If you need to install `git` and clone the project, do that first:

    brew install git
    mkdir ~/code && cd ~/code
    git clone git@github.com:bploeckelman/LudumDare45.git
    cd LudumDare45

Install Build requirements:

    brew install caskroom/cask/brew-cask
    brew cask install java

If you don't have a java IDE installed, you can easily download one
(IntelliJ in this example) with `brew cask`:

    brew cask install intellij-idea-ce

Eclipse and Netbeans are also available through `brew cask`.

### Run the game!

First, the images in the /sprites folder must be packed into a sprite atlas (re-run this command when images are added or removes from the /sprites folder):

    ./gradlew desktop:sprites

Then you can run the game!

    ./gradlew desktop:run

The game should build and run the desktop version.
