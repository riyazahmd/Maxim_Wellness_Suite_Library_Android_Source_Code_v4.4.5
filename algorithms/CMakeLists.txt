# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

set(LIBS_DIR ${CMAKE_SOURCE_DIR}/libs)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        MaximAlgorithms

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/MaximAlgorithms.cpp
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        libLog
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# shared lib will also be tucked into APK and sent to target
# refer to app/build.gradle, jniLibs section for that purpose.
# ${ANDROID_ABI} is handy for our purpose here. Probably this ${ANDROID_ABI} is
# the most valuable thing of this sample, the rest are pretty much normal cmake
add_library(libMxmWellnessSuite STATIC IMPORTED)
set_target_properties(libMxmWellnessSuite PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/algorithms/lib/${ANDROID_ABI}/libMxmWellnessSuite.a)

target_include_directories(MaximAlgorithms PRIVATE
        ${LIBS_DIR}/algorithms/include)


target_link_libraries( # Specifies the target library.
        MaximAlgorithms
        libMxmWellnessSuite
        # Links the target library to the log library
        # included in the NDK.
        ${libLog})