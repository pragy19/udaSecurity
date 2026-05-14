module security.service {
    // This tells Java that the security app needs access to the image module you just created
    requires image.service;

    // These are libraries required by the Udacity GUI and Data classes
    requires com.google.common;
    requires com.google.gson;
    requires java.desktop;
    requires java.prefs;

    // Gson needs this 'opens' directive so it can use reflection to serialize your data classes
    opens com.udacity.catpoint.data to com.google.gson;
}