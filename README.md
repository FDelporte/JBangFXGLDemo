# JBangFXGLDemo

Demo application to show how JBang can be used to run an [FXGL](https://github.com/AlmasB/FXGL) demo game application written with Java, JavaFX, and FXGL.

Based on the Maven project [github.com/FDelporte/JavaMagazineFXGL](https://github.com/FDelporte/JavaMagazineFXGL), which is fully described in "[Look out, Duke! Part 1: Build a Java game with JavaFX and FXGL](https://blogs.oracle.com/javamagazine/post/look-out-duke-part-1-how-to-build-a-java-game-with-javafx-and-the-fxgl-library)".

## Run the Application

### JBang

Make sure you have JBang installed, see [jbang.dev](https://www.jbang.dev/download/).

### JDK with JavaFX

An SDK with bundled JavaFX is needed, use [SDKMAN](https://sdkman.io/) to install such a version:

```bash
$ curl -s "https://get.sdkman.io" | bash
$ source "$HOME/.sdkman/bin/sdkman-init.sh"
$ sdk install java 21.0.1.fx-zulu
```

### Execute the application

```bash
$ git clone https://github.com/FDelporte/JBangFXGLDemo.git
$ cd JBangFXGLDemo
$ jbang JBangFXGLDemo.java
```
