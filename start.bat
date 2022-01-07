del /q bootstrap.jar
jar cvf0 bootstrap.jar -C out/production/http4java com/xpcf/http4java/Bootstrap.class -C out/production/http4java com/xpcf/http4java/classloader/CommonClassLoader.class
del /q lib/http4java.jar
cd out
cd production
cd http4java
jar cvf0 ../../../lib/http4java.jar *
cd ..
cd ..
cd ..
java -cp bootstrap.jar com.xpcf.http4java.Bootstrap
pause