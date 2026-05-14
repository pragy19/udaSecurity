module image.service {
    // This allows the security-service to actually see and use the ImageService interface
    exports com.udacity.image;

    // These are the libraries the image service needs to function
    requires org.slf4j;
    requires java.desktop; // Required for BufferedImage
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.rekognition;
}