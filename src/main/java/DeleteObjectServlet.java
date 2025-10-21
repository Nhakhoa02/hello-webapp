import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@WebServlet("/object-delete/*")
public class DeleteObjectServlet extends HttpServlet {
    private static final String BUCKET_NAME = "p-acl";

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        S3Client s3 = S3Client.builder()
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .region(Region.US_EAST_1)
                .build();

        String pathInfo = req.getPathInfo(); 
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("Object key is missing");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        String key = pathParts[1];

        try {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(key)
                    .build());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("Object deleted successfully: " + key);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Delete failed: " + e.getMessage());
        } finally {
            s3.close();
        }
    }
}
