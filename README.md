# Java 9 Jigsaw Example

## Start by checking out the entire repository.  This is the completed sample

git clone https://github.com/tpalkot/java9jigsaw.git

## Skip to Step 1
*you will be on a detached head - this is okay*

git checkout Step1


##  Start with a Traditional Java class
*Note dependency on java logging*

### Force my local to use java9 (this is the command on mac, your OS is probably different)
export JAVA_HOME=`/usr/libexec/java_home -v 9`

### Compile
javac -d moduleConsumer/target/classes  moduleConsumer/src/com/consumer/Consumer.java 


### Create a Jar
jar cf moduleConsumer/target/consumerModule.jar -C moduleConsumer/target/classes .

### Run with traditional classpath argument
*Note Logging warn and sysout messages*

java -cp moduleConsumer/target/consumerModule.jar com.consumer.Consumer

## Examine the Size

### Look at the size of the JRE, size of the jar

du -sh moduleConsumer/target/consumerModule.jar 


### Look at the size of a traditional JRE
du -sh /Library/Java/JavaVirtualMachines/jdk1.8.0_40.jdk/Contents/Home/jre/

# Convert this into a Java 9 Module
mkdir -p moduleConsumer/src/com.consumer;

### Move the source to reflect the module name
mv  moduleConsumer/src/com moduleConsumer/src/com.consumer/com;

### Create a module-info in the root to describe it
vi moduleConsumer/src/com.consumer/module-info.java
```
module com.consumer {

}
```

### compile the module
*Notice output dir of mods/com.consumer*

javac -d moduleConsumer/target/mods/com.consumer moduleConsumer/src/com.consumer/com/consumer/Consumer.java moduleConsumer/src/com.consumer/module-info.java 

*notice error with loggging*

- comment it out of Consumer.java
- Compile again

*If you are behind, you can skip to this step with*
*git clean -f -d*
*git checkout Step2*

### run the module
java --module-path moduleConsumer/target/mods -m com.consumer/com.consumer.Consumer

### Fix the module-info
- Add dependency on java logging
    java --list-modules
- Export Consumer class so it can be accessed

vi moduleConsumer/src/com.consumer/module-info.java

```
module com.consumer {
    exports com.consumer;
    requires java.logging;
}
```

# Create a Module Distribution
### create a module jar

mkdir moduleConsumer/target/mlib

jar --create --file moduleConsumer/target/mlib/com.consumer@1.0.jar --module-version=1.0 -C moduleConsumer/target/mods/com.consumer/ .

### use jar as a module
- new argument -p is module path
- new argument -m is -m <module>[/<mainclass>]
java -p moduleConsumer/target/mlib/ -m com.consumer/com.consumer.Consumer


### show commands
ls ${JAVA_HOME}/bin

### jlink to produce runtime
${JAVA_HOME}/bin/jlink --module-path $JAVA_HOME/jmods:moduleConsumer/target/mlib/ --add-modules com.consumer --output consumerapp

### view the included modules
consumerapp/bin/java --list-modules

### run in new runtime
consumerapp/bin/java com.consumer.Consumer

### check size of result

- look at size of executable
du -sh consumerapp/
- compared to size of java 8 JRE
du -sh /Library/Java/JavaVirtualMachines/jdk1.8.0_40.jdk/Contents/Home/jre/

*If you are behind, you can skip to this step with*
*git clean -f -d*
*git checkout Step3*

# Introduce a library
- ServiceInterface project
- ServiceImplementation project
- Consumer uses ServiceInterface
    - ServiceLoader
    
## Make the service interface
mkdir -p moduleService/src/com.service/com/service

vi moduleService/src/com.service/com/service/MyService.java

```
package com.service;

public interface MyService {

    void printMessage();
}
```

### Declare it as a module
vi moduleService/src/com.service/module-info.java

```
module com.service {
    exports com.service;
}
```


### Compile and Jar It
javac -d moduleService/target/mods/com.service moduleService/src/com.service/com/service/MyService.java moduleService/src/com.service/module-info.java;

mkdir moduleService/target/mlib;

jar --create --file moduleService/target/mlib/com.service@1.0.jar --module-version=1.0 -C moduleService/target/mods/com.service/ .;

## Create an implementer of the library

mkdir -p moduleImplementer/src/com.implementer/com/implementer

vi moduleImplementer/src/com.implementer/com/implementer/MyServiceImplementer.java

```
package com.implementer;

import com.service.MyService;

public class MyServiceImplementer implements MyService{

    public void printMessage(){
        System.out.println("MyServiceImplementer says Hello World");
    }
    public void secretPublicMethod(){
        System.out.println("You found my secret");
    }
}
```

### Declare it as a module
vi moduleImplementer/src/com.implementer/module-info.java

```
module com.implementer {
    requires com.service;
    exports com.implementer;
}
```

mkdir -p moduleImplementer/src/META-INF/services/

echo "com.implementer.MyServiceImplementer" > moduleImplementer/src/META-INF/services/com.services.MyService

javac --module-path moduleService/target/mlib/ -d moduleImplementer/target/mods/com.implementer moduleImplementer/src/com.implementer/com/implementer/MyServiceImplementer.java moduleImplementer/src/com.implementer/module-info.java;

mkdir moduleImplementer/target/mlib;

jar --create --file moduleImplementer/target/mlib/com.implementer@1.0.jar --module-version=1.0 -C moduleImplementer/target/mods/com.implementer/ . moduleImplementer/src/META-INF/services/com.services.MyService;

jar --update --file moduleImplementer/target/mlib/com.implementer@1.0.jar --module-version=1.0 -C moduleImplementer/src/ META-INF/services/com.services.MyService;


## Put it all together
vi moduleConsumer/src/com.consumer/com/consumer/Consumer.java 

```
package com.consumer;

import java.util.logging.Logger;
import java.util.ServiceLoader;

import com.service.MyService;

public class Consumer {

    private final static Logger LOGGER = Logger.getLogger(Consumer.class.getName());

    public static void main(String[] args) {
    
        Consumer c = new Consumer();
        c.doSomething();
        LOGGER.warning("In constructor");
    }
    
    public void doSomething(){
        System.out.println("consumer in doSomething()");
        
        ServiceLoader<MyService> myServiceLoader = ServiceLoader.load(MyService.class);
        MyService myService = myServiceLoader.iterator().next();
        myService.printMessage();
        //com.implementer.MySeviceImplementer implementer = (com.implementer.MySeviceImplementer)myService;
    }

    
}
```

vi moduleConsumer/src/com.consumer/module-info.java

```
module com.consumer {
    exports com.consumer;
    requires java.logging;
    requires com.service;
    uses com.service.MyService;
    requires com.implementer;
}
```

rm -rf consumerapp;

javac --module-path moduleConsumer/target/mlib/:moduleService/target/mlib/:moduleImplementer/target/mlib/ -d moduleConsumer/target/mods/com.consumer moduleConsumer/src/com.consumer/com/consumer/Consumer.java moduleConsumer/src/com.consumer/module-info.java;

### Try it out
jar --create --file moduleConsumer/target/mlib/com.consumer@1.0.jar --module-version=1.0 -C moduleConsumer/target/mods/com.consumer/ .;

java --module-path moduleConsumer/target/mlib/:moduleService/target/mlib/:moduleImplementer/target/mlib/ -m com.consumer/com.consumer.Consumer;

${JAVA_HOME}/bin/jlink --module-path $JAVA_HOME/jmods:moduleConsumer/target/mlib/:moduleService/target/mlib/:moduleImplementer/target/mlib/ --add-modules com.consumer,com.service,com.implementer --output consumerapp;

consumerapp/bin/java com.consumer.Consumer

vi moduleService/src/com.service/com/service/SecretService.java

```
package com.service;

public interface SecretService {

    void message();

    public static SecretService getService(){
        return new com.service.impl.SecretServiceImpl();
    }
}
```

mkdir moduleService/src/com.service/com/service/impl/;

vi moduleService/src/com.service/com/service/impl/SecretServiceImpl.java

```
package com.service.impl;

import com.service.SecretService;

public class SecretServiceImpl implements SecretService{

    public void message(){
        System.out.println("Secret message");
    }

}
```


javac $(find moduleService/src/ -name *.java) -d moduleService/target/mods/com.service

jar --create --file moduleService/target/mlib/com.service@1.0.jar --module-version=1.0 -C moduleService/target/mods/com.service .;

change consumer:
vi moduleConsumer/src/com.consumer/com/consumer/Consumer.java
        com.service.SecretService.getService().message();
        
javac --module-path moduleConsumer/target/mlib/:moduleService/target/mlib/:moduleImplementer/target/mlib/ -d moduleConsumer/target/mods/com.consumer moduleConsumer/src/com.consumer/com/consumer/Consumer.java moduleConsumer/src/com.consumer/module-info.java;

### Try it out
jar --create --file moduleConsumer/target/mlib/com.consumer@1.0.jar --module-version=1.0 -C moduleConsumer/target/mods/com.consumer/ .;

java --module-path moduleConsumer/target/mlib/:moduleService/target/mlib/:moduleImplementer/target/mlib/ -m com.consumer/com.consumer.Consumer;


