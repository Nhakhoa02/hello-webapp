import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

@WebServlet("/object-list")
public class GetObjectListServlet extends HttpServlet {
    private static final String BUCKET_NAME = "p-acl";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        S3Client s3 = S3Client.builder()
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .region(Region.US_EAST_1)
                .build();

        try {
            ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                    .bucket(BUCKET_NAME)
                    .build();  

            ListObjectsV2Response listRes = s3.listObjectsV2(listReq);

            List<String> fileNames = listRes.contents().stream()
                    .map(obj -> obj.key())
                    .collect(Collectors.toList());

        resp.setContentType("application/json");
        String json = fileNames.stream()
            .map(name -> name.replace("\\", "\\\\").replace("\"", "\\\""))
            .map(name -> "\"" + name + "\"")
            .collect(Collectors.joining(",", "[", "]"));
        resp.getWriter().write(json);
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error listing objects: " + e.getMessage());
        } finally {
            s3.close();
        }
    }
}
