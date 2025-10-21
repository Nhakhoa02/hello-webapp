import java.io.IOException;
import java.io.OutputStream;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

@WebServlet("/object-download/*")
public class DownloadObjectServlet extends HttpServlet {
    private static final String BUCKET_NAME = "p-acl";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        S3Client s3 = S3Client.builder()
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .region(Region.US_EAST_1)
                .build();

        String pathInfo = req.getPathInfo(); 
        String[] pathParts = pathInfo.split("/");
        String key = pathParts[1]; 

        try (ResponseInputStream<GetObjectResponse> s3Object = s3.getObject(
                GetObjectRequest.builder()
                        .bucket(BUCKET_NAME)
                        .key(key)
                        .build())) {

            resp.setContentType("application/octet-stream");
            resp.setHeader("Content-Disposition", "attachment; filename=\"" + key + "\"");

            OutputStream out = resp.getOutputStream();
            s3Object.transferTo(out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Download failed: " + e.getMessage());
        } finally {
            s3.close();
        }
    }
}
