import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@WebServlet("/object-upload")
@MultipartConfig
public class UploadObjectsServlet extends HttpServlet {
    private static final String BUCKET_NAME = "p-acl";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Collection<Part> parts = req.getParts(); // All form parts
        S3Client s3 = S3Client.builder()
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .region(Region.US_EAST_1)
                .build();

        StringBuilder result = new StringBuilder();

        try {
            for (Part part : parts) {
                if (part.getName().equals("file")) { // Only process 'file' fields
                    String fileName = part.getSubmittedFileName();
                    if (fileName == null || fileName.isEmpty()) continue;

                    String key = fileName;

                    try (InputStream fileContent = part.getInputStream()) {
                        s3.putObject(PutObjectRequest.builder()
                                        .bucket(BUCKET_NAME)
                                        .key(key)
                                        .build(),
                                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(fileContent, part.getSize()));

                        result.append("Uploaded: ").append(key).append("\n");
                    }
                }
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(result.length() > 0
                    ? "Files uploaded successfully:\n" + result
                    : "No files uploaded.");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Upload failed: " + e.getMessage());
        } finally {
            s3.close();
        }
    }
}
